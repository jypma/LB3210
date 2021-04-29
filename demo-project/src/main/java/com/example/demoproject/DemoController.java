package com.example.demoproject;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import com.example.demoproject.db.tables.pojos.FlywayTest;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class DemoController {
    private final DSLContext ctx;
    private final Function<Configuration, DemoRepository> repository;

    public DemoController(Configuration cfg, Function<Configuration, DemoRepository> repository) {
        this.ctx = DSL.using(cfg);
        this.repository = repository;
    }

    /**
     * Starts a transaction, configures a DemoRepository to  use that transaction, and then
     * invokes [f] to execute some business logic with it.
     * After [f] returns successfully, the transaction is co mmitted before returning the result.
     */
    private <T> T inRepoTransaction(Function<DemoRepository, T> f) {
        return ctx.transactionResult(c ->
                repository.andThen(f).apply(c)
        );
    }

    @GetMapping(value = "/values", produces = MediaType.APPLICATION_JSON_VALUE)
    List<FlywayTest> all() {
        return inRepoTransaction(r -> r.list()).toJavaList();
    }

    @GetMapping(value = "/values/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FlywayTest> findById(@PathVariable("id") Integer id) {
        FlywayTest ft = inRepoTransaction(r -> r.findById(id));
        if (ft == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(ft);
        }
    }

    @PostMapping(value = "/values", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FlywayTest> create(@RequestBody FlywayTest body) {
        FlywayTest created = inRepoTransaction(r -> r.create(body));

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping(value = "/values/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FlywayTest> update(@PathVariable("id") Integer id, @RequestBody FlywayTest body) {
        FlywayTest updated = inRepoTransaction(r -> r.update(id, body));
        if (updated == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(updated);
        }
    }
}
