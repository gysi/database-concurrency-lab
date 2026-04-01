package de.gregord.databaseconcurrencylab.rawsql;

import de.gregord.databaseconcurrencylab.rawsql.tests.base.BaseJdbcTests;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;

import java.util.Map;

public class AppAndDbIsolationLevelsTest extends BaseJdbcTests {
    private static final Logger LOG = LoggerFactory.getLogger(AppAndDbIsolationLevelsTest.class);

    /**
     * Evaluates how Spring transaction isolation levels are interpreted and enforced by PostgreSQL and MySQL databases.
     * <p>
     * The test iterates through {@code TransactionDefinition} isolation levels, setting up a new transaction for each,
     * logging the target level, querying the DB for the active level, comparing it with the intended level, and committing the transaction.
     * <p>
     * Key observation: Discrepancies between the application's intended isolation level and the database's actual implementation.
     * <p>
     * <b>Database-Specific Isolation Level Mapping:</b>
     * <table border="1">
     *     <tr>
     *         <th rowspan="2">Spring Isolation Level</th>
     *         <th rowspan="2">Numerical Value</th>
     *         <th colspan="2">DB Isolation Level</th>
     *         <th rowspan="2">Notes</th>
     *     </tr>
     *     <tr>
     *         <th>PostgreSQL</th>
     *         <th>MySQL</th>
     *     </tr>
     *     <tr>
     *         <td>{@code ISOLATION_DEFAULT}</td>
     *         <td>-1</td>
     *         <td>{@code read committed}</td>
     *         <td>{@code REPEATABLE-READ}</td>
     *         <td></td>
     *     </tr>
     *     <tr>
     *         <td>{@code ISOLATION_READ_UNCOMMITTED}</td>
     *         <td>1</td>
     *         <td>{@code read uncommitted}</td>
     *         <td>{@code READ-UNCOMMITTED}</td>
     *         <td>PostgreSQL effectively treats this as {@code READ COMMITTED}.
     *         See <a href="https://www.postgresql.org/docs/current/transaction-iso.html">Posgres documentation</a></td>
     *     </tr>
     *     <tr>
     *         <td>{@code ISOLATION_READ_COMMITTED}</td>
     *         <td>2</td>
     *         <td>{@code read committed}</td>
     *         <td>{@code READ-COMMITTED}</td>
     *         <td>Direct mapping.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ISOLATION_REPEATABLE_READ}</td>
     *         <td>4</td>
     *         <td>{@code repeatable read}</td>
     *         <td>{@code REPEATABLE-READ}</td>
     *         <td>Direct mapping.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ISOLATION_SERIALIZABLE}</td>
     *         <td>8</td>
     *         <td>{@code serializable}</td>
     *         <td>{@code SERIALIZABLE}</td>
     *         <td>Direct mapping.</td>
     *     </tr>
     * </table>
     * <p>
     * <b>Key Takeaways:</b>
     * <ul>
     *     <li><b>Default Behavior:</b> PostgreSQL uses {@code READ COMMITTED}; MySQL uses {@code REPEATABLE READ} as default.</li>
     *     <li><b>PostgreSQL & {@code READ_UNCOMMITTED}:</b> PostgreSQL effectively treats it as {@code READ COMMITTED}.</li>
     *     <li><b>Direct Mappings:</b> Both DBs generally map Spring's levels to their equivalents (except for PostgreSQL & {@code READ_UNCOMMITTED}).</li>
     * </ul>
     */
    @Test
    void test_how_different_isolation_levels_are_reflected_in_the_database() {
        Map<Integer, String> appIsolationLevels = Map.of(
                TransactionDefinition.ISOLATION_DEFAULT, "ISOLATION_DEFAULT",
                TransactionDefinition.ISOLATION_READ_UNCOMMITTED, "ISOLATION_READ_UNCOMMITTED",
                TransactionDefinition.ISOLATION_READ_COMMITTED, "ISOLATION_READ_COMMITTED",
                TransactionDefinition.ISOLATION_REPEATABLE_READ, "ISOLATION_REPEATABLE_READ",
                TransactionDefinition.ISOLATION_SERIALIZABLE, "ISOLATION_SERIALIZABLE"
        );

        for (int isolationLevel : appIsolationLevels.keySet()) {
            Util.createTransactionTemplate(transactionManager, isolationLevel).executeWithoutResult(status -> {
                String isolationName = appIsolationLevels.get(isolationLevel);
                LOG.info("Testing with Isolation Level: {} ({})", isolationLevel, isolationName);
                LOG.info(Util.getTransactionLevelLog(this.getDialectSpecific(), jdbcTemplate, isolationName));
            });
        }
    }
}
