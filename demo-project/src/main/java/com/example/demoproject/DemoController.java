package com.example.demoproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.example.demoproject.db.tables.pojos.FlywayTest;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class DemoController {
    private final DSLContext ctx;
    private final Function<Configuration,DemoRepository> repository;
    private final RabbitTemplate template;
    private final DistributionSummary incomingSize;
    private final Counter updated;

    public DemoController(Configuration cfg, Function<Configuration,DemoRepository> repository, RabbitTemplate template, MeterRegistry metrics) {
        this.ctx = DSL.using(cfg);
        this.repository = repository;
        this.template = template;
        this.incomingSize = DistributionSummary.builder("demo.controller.incoming.size")
            .description("Size of created or updated items in characters")
            .register(metrics);
        this.updated = Counter.builder("demo.controller.updated")
            .description("Number if received updates")
            .register(metrics);
    }

    /**
     * Starts a transaction, configures a DemoRepository to  use that transaction, and then
     * invokes [f] to execute some business logic with it.
     * After [f] returns successfully, the transaction is co mmitted before returning the result.
     */
    private <T> T inRepoTransaction(Function<DemoRepository,T> f) {
        return ctx.transactionResult(c ->
            repository.andThen(f).apply(c)
        );
    }
    private void inRepoTransactionDo(Consumer<DemoRepository> f) {
        ctx.transaction(c ->
            f.accept(repository.apply(c))
        );
    }

    private void sendToQueue(String message) {
        template.invoke(cb -> {
            cb.convertAndSend("demoQueue", message);
            // Potentially we can do other things in between sending the message and awaiting
            // its conformation.
            cb.waitForConfirmsOrDie(1000);

            // RabbitTemplate.invoke method is declared to take a closure which must return a value,
            // even though that closure is intended to yield side effects (publishing on a queue).
            // Hence, we unfortunately need a dummy return statement here.
            return true;
        });
    }

    @GetMapping("/values")
    List<FlywayTest> all() {
        return inRepoTransaction(r -> r.list()).toJavaList();
    }

    private static Map<String, List<String>> rooms = new HashMap<String, List<String>>();

    @PostMapping("/chat/{room}/messages")
    public ResponseEntity<?> create(@PathVariable("room") String room, @RequestBody String message) {

        if (rooms.get(room)==null) {
            rooms.put(room, new ArrayList<String>());
        }
        var messages = rooms.get(room);
        messages.add(message);
        sendToQueue("Got message: " + message);
        System.out.println("Got message!");
        // We return the body back in this example, but it's more common to respond
        // with an empty body for a POST-to-create. In that case, just say
        // ResponseEntity.created(uri).build().
        return ResponseEntity.ok("posted message");
    }

    @GetMapping("/chat/{room}/messages")
    List<String> allMessages(@PathVariable("room") String room) {
        return rooms.get(room);
    }

    @PostMapping("/values")
    public ResponseEntity<?> create(@RequestBody FlywayTest body) {
        incomingSize.record(body.getValue().length());

        var created = inRepoTransaction(r -> r.create(body));
        sendToQueue("Created: " + body);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .pathSegment(created.getId().toString())
            .build()
            .toUri();

        // We return the body back in this example, but it's more common to respond
        // with an empty body for a POST-to-create. In that case, just say
        // ResponseEntity.created(uri).build().
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/values/{id}")
    public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody FlywayTest body) {
        incomingSize.record(body.getValue().length());
        updated.increment();

        // Unfortunately, we have to verbosely copy here, since JOOQ hasn't implemented "with" methods
        // See https://github.com/jOOQ/jOOQ/issues/5257 for details.
        var obj = new FlywayTest(body.getKey(), body.getValue(), id);
        inRepoTransactionDo(r -> r.update(obj));
        sendToQueue("Updated: " + body);
        return ResponseEntity.noContent().build();
    }
}
