package com.example.demoproject;

import java.util.List;
import java.util.function.Function;

import com.example.demoproject.db.tables.pojos.FlywayTest;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/values")
    List<FlywayTest> all() {
        return inRepoTransaction(r -> r.list()).toJavaList();
    }
}
