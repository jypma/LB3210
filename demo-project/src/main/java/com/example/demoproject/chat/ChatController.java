package com.example.demoproject.chat;

import com.example.demoproject.db.tables.pojos.Chatmessage;
import com.example.demoproject.db.tables.pojos.Chatroom;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.vavr.collection.Seq;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@RestController
public class ChatController {
    private final DSLContext ctx;
    private final Function<Configuration, ChatRepository> repository;
    private final DistributionSummary messageSize;
    private final Counter messageCounter;

    public ChatController(Configuration cfg, Function<Configuration,ChatRepository> repository, MeterRegistry metrics) {
        this.ctx = DSL.using(cfg);
        this.repository = repository;
        this.messageSize = DistributionSummary.builder("chat.controller.incoming.message.size")
            .description("Size of messages in characters")
            .register(metrics);
        this.messageCounter = Counter.builder("chat.controller.incoming.message.number")
            .description("Number of messages")
            .register(metrics);
    }

    /**
     * Starts a transaction, configures a DemoRepository to  use that transaction, and then
     * invokes [f] to execute some business logic with it.
     * After [f] returns successfully, the transaction is committed before returning the result.
     */
    private <T> T inRepoTransaction(Function<ChatRepository,T> f) {
        return ctx.transactionResult(c ->
            repository.andThen(f).apply(c)
        );
    }

    private void inRepoTransactionDo(Consumer<ChatRepository> f) {
        ctx.transaction(c ->
            f.accept(repository.apply(c))
        );
    }

    @GetMapping("/rooms")
    List<Chatroom> allRooms() {
        return inRepoTransaction(r -> r.getChatrooms()).toJavaList();
    }

    /**
     * Posts a chat message to a room.
     *
     * @param room the message will be posted in this room. If the room does not exist it will be created.
     *
     */
    @PostMapping("/rooms/{room}/messages")
    public ResponseEntity<String> postMessage(@PathVariable("room") String room, @RequestBody String message) {

        Optional<Chatroom> opt = inRepoTransaction(r -> r.findChatroomByName(room));

        Chatroom chatroom;
        if (opt.isEmpty()) { //if chatroom does not exist, create it
            chatroom = inRepoTransaction(r -> r.createChatroom(new Chatroom(null, room)));
        } else {
            chatroom = opt.get();
        }
        Chatmessage chatmessage = inRepoTransaction(r -> r.postMessageToChatroom(chatroom, message));
        System.out.println("Posted message: " + chatmessage.toString());
        messageCounter.increment();
        messageSize.record(message.length());
        return ResponseEntity.ok("Posted message");
    }

    @GetMapping("/rooms/{room}/messages")
    ResponseEntity<List<String>> allMessages(@PathVariable("room") String room) {
        Optional<Chatroom> opt = inRepoTransaction(r -> r.findChatroomByName(room));

        if (opt.isEmpty()) { //return 404 if room not found
            return ResponseEntity.notFound().build();
        }
        Chatroom chatroom = opt.get();
        Seq<Chatmessage> chatmessages = inRepoTransaction(r -> r.getMessagesByChatroom(chatroom));
        List<String> list = chatmessages.map(m -> m.getMessage()).toJavaList();
        return ResponseEntity.ok(list);
    }

}
