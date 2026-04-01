package de.gregord.databaseconcurrencylab.rawsql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class Util {
    static String getTransactionLevelLog(DialectSpecific dialectSpecific, JdbcTemplate jdbcTemplate, String appIsolationLevel) {
        String dbIsolationLevel = jdbcTemplate.queryForObject(dialectSpecific.getCurrentIsolationLevelQuery(), String.class);
        return String.format("""
                Comparing APP and DB isolation Levels
                Isolation level (queried from DB):
                App isolation level: %s
                DB isolation level: %s
                """, appIsolationLevel, dbIsolationLevel);
    }

    public static TransactionTemplate createTransactionTemplate(PlatformTransactionManager transactionManager, int isolationLevel) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName(Thread.currentThread().toString());
        transactionTemplate.setIsolationLevel(isolationLevel);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }
}
