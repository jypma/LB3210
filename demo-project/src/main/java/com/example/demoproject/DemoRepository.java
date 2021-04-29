package com.example.demoproject;

import static com.example.demoproject.db.tables.FlywayTest.FLYWAY_TEST;

import com.example.demoproject.db.tables.pojos.FlywayTest;

import org.jooq.Configuration;
import org.jooq.impl.DSL;

import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

public interface DemoRepository {
    /**
     * Lists all objects currently stored in the repository.
     *
     * Note that the return type is an (immutable) vavr list, of (immutable) FlywayTest data objects, so the result is guaranteed
     * to be complete and in memory.
     */
    public Seq<FlywayTest> list();

    public static class Live {
        public static DemoRepository apply(Configuration cfg) {
            final var ctx = DSL.using(cfg);

            return new DemoRepository() {
                public Seq<FlywayTest> list() {
                    return Vector.ofAll(ctx.selectFrom(FLYWAY_TEST).fetchInto(FlywayTest.class));
                }
            };
        }
    }
}
