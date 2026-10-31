package com.example.demoproject;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

@RestController
public class ChatRoomController {
    record Room(Seq<String> messages) {
        static final Room empty = new Room(Vector.empty());

        public Room post(String message) {
            return new Room(messages.append(message));
        }
    }

    record Rooms(Map<String, Room> rooms) {
        static final Rooms empty = new Rooms(HashMap.empty());

        public Rooms post(String room, String message) {
            return new Rooms(rooms.put(room, rooms.getOrElse(room, Room.empty).post(message)));
        }
    }

    private final AtomicReference<Rooms> rooms = new AtomicReference<>(Rooms.empty);

    @PostMapping("/rooms/{room}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void postMessage(@PathVariable("room") String room, @RequestBody String message) {
        rooms.updateAndGet(all ->
            all.post(room, message)
        );
    }

    @GetMapping(value = "/rooms/{room}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMessagesText(@PathVariable("room") String roomName) {
        var room = rooms.get().rooms.get(roomName);
        return room.map(r ->
            ResponseEntity.ok(r.messages.mkString("\n"))
        ).getOrElse(() ->
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room " + roomName + " not found")
        );
    }

    @GetMapping(value = "/rooms/{room}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Room> getMessagesJson(@PathVariable("room") String roomName) {
        var room = rooms.get().rooms.get(roomName);
        return room.map(r ->
            ResponseEntity.ok(r)
        ).getOrElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Room " + roomName + " not found"
            )
        );
    }

}
