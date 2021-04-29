package com.example.demoproject;

import static com.example.demoproject.db.tables.FlywayTest.FLYWAY_TEST;

import com.example.demoproject.db.Tables;
import com.example.demoproject.db.tables.pojos.FlywayTest;

import com.example.demoproject.db.tables.records.FlywayTestRecord;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.UpdateConditionStep;
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
    public FlywayTest findById(Integer id);
    public FlywayTest create(FlywayTest flywaytest);
    public FlywayTest update(Integer id, FlywayTest flywaytest);

    public static class Live {
        public static DemoRepository apply(Configuration cfg) {
            final var ctx = DSL.using(cfg);

            return new DemoRepository() {
                public Seq<FlywayTest> list() {
                    return Vector.ofAll(ctx.selectFrom(FLYWAY_TEST).fetchInto(FlywayTest.class));
                }

                @Override
                public FlywayTest findById(Integer id) {
                    return ctx.selectFrom(FLYWAY_TEST).where(FLYWAY_TEST.ID.eq(id)).fetchOneInto(FlywayTest.class);
                }

                public FlywayTest create(FlywayTest flywaytest) {
                    //TODO refactor to functional style?
                    FlywayTestRecord rec = ctx.newRecord(Tables.FLYWAY_TEST);
                    rec.setKey(flywaytest.getKey());
                    rec.setValue(flywaytest.getValue());
                    rec.store();
                    return new FlywayTest(rec.getKey(), rec.getValue(), rec.getId());
                }

                @Override
                public FlywayTest update(Integer id, FlywayTest flywaytest) {
                    int execute = ctx.update(Tables.FLYWAY_TEST)
                            .set(FLYWAY_TEST.KEY, flywaytest.getKey())
                            .set(FLYWAY_TEST.VALUE, flywaytest.getValue())
                            .where(FLYWAY_TEST.ID.eq(id)).execute();
                    return findById(id);
                }
            };
        }
    }
}
