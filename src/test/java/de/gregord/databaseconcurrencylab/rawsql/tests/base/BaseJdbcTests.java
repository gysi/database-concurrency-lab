package de.gregord.databaseconcurrencylab.rawsql.tests.base;

import de.gregord.databaseconcurrencylab.config.MysqlContainerConfig;
import de.gregord.databaseconcurrencylab.config.PostgresContainerConfig;
import de.gregord.databaseconcurrencylab.rawsql.DialectSpecific;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
@Import(value = { MysqlContainerConfig.class, PostgresContainerConfig.class })
public abstract class BaseJdbcTests {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DialectSpecific dialectSpecific;

    @Test
    void contextLoads() {
    }

    public DialectSpecific getDialectSpecific() {
        return dialectSpecific;
    }
}
