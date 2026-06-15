package gt.report;

import gtapp.jooq.DefaultSchema;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseJooqTest {

    @Autowired
    protected DSLContext dsl;

    private static final AtomicBoolean schemaCreated = new AtomicBoolean(false);

    @BeforeEach
    void ensureSchema() {
        if (schemaCreated.compareAndSet(false, true)) {

            // 1. Disable FK checks so we can drop in any order
            dsl.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 2. Drop all existing tables
            DefaultSchema.DEFAULT_SCHEMA.getTables()
                .forEach(table ->
                    dsl.execute("DROP TABLE IF EXISTS `" + table.getName() + "`")
                );

            // 3. Re-enable FK checks
            dsl.execute("SET FOREIGN_KEY_CHECKS = 1");

            // 4. Recreate schema cleanly
            dsl.meta(DefaultSchema.DEFAULT_SCHEMA)
                .ddl()
                .executeBatch();
        }
    }
}
