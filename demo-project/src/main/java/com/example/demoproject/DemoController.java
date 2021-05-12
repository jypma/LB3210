package com.example.demoproject;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.example.demoproject.db.tables.pojos.FlywayTest;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class DemoController {
    private final DSLContext ctx;
    private final Function<Configuration,DemoRepository> repository;

    public DemoController(Configuration cfg, Function<Configuration,DemoRepository> repository) {
        this.ctx = DSL.using(cfg);
        this.repository = repository;
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


    @GetMapping("/values")
    List<FlywayTest> all() {
        return inRepoTransaction(r -> r.list()).toJavaList();
    }

    @PostMapping("/values")
    public ResponseEntity<?> create(@RequestBody FlywayTest body) {
        var created = inRepoTransaction(r -> r.create(body));

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
        // Unfortunately, we have to verbosely copy here, since JOOQ hasn't implemented "with" methods
        // See https://github.com/jOOQ/jOOQ/issues/5257 for details.
        var obj = new FlywayTest(body.getKey(), body.getValue(), id);

        inRepoTransactionDo(r -> r.update(obj));

        return ResponseEntity.noContent().build();
    }
}
