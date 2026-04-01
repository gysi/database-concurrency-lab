package de.gregord.databaseconcurrencylab.rawsql.tests;

import de.gregord.databaseconcurrencylab.rawsql.tests.base.IsolationLevelTests;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SerializableTests extends IsolationLevelTests {

    @Override
    public int getIsolationLevel() {
        return TransactionDefinition.ISOLATION_SERIALIZABLE;
    }

    @Test
    @Order(10)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_dirty_reads() {
        super.test_dirty_reads();
    }

    @Test
    @Order(20)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_repeatable_reads() {
        super.test_repeatable_reads();
    }

    @Test
    @Order(30)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_phantom_reads_after_single_insert() {
        super.test_phantom_reads_after_single_insert();
    }

    @Test
    @Order(31)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_phantom_reads_after_concurrent_inserts() {
        super.test_phantom_reads_after_concurrent_inserts();
    }

    @Test
    @Order(32)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_phantom_reads_without_initial_select() {
        /**
         * Why this fails for mysql:
         * see: https://dev.mysql.com/doc/refman/8.0/en/commit.html
         * "The only isolation level that permits a consistent read is REPEATABLE READ.
         * For all other isolation levels, the WITH CONSISTENT SNAPSHOT clause is ignored.
         * A warning is generated when the WITH CONSISTENT SNAPSHOT clause is ignored."
         *
         * So it seems that serializable only offers consistent read by issuing locks on tables previously selected
         * because it automatically sets SELECT ... FOR SHARE if a SELECT statement is issued.
         *
         * See also:
         * https://dba.stackexchange.com/questions/256110/why-does-start-transaction-with-consistent-snapshot-not-work-with-serializable-i
         */
        super.test_phantom_reads_without_initial_select();
    }

    @Test
    @Order(40)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_lost_updates_single_step() {
        super.test_lost_updates_single_step();
    }

    @Test
    @Order(41)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_lost_updates_with_initial_select() {
        super.test_lost_updates_with_initial_select();
    }

    @Test
    @Order(42)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void test_lost_updates_in_two_steps() {
        super.test_lost_updates_in_two_steps();
    }

}
