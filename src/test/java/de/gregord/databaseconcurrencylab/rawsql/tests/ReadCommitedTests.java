package de.gregord.databaseconcurrencylab.rawsql.tests;

import de.gregord.databaseconcurrencylab.rawsql.tests.base.IsolationLevelTests;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ReadCommitedTests extends IsolationLevelTests {

    @Override
    public int getIsolationLevel() {
        return TransactionDefinition.ISOLATION_READ_COMMITTED;
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
