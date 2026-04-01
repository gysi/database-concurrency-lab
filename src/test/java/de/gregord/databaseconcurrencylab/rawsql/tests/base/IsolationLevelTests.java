package de.gregord.databaseconcurrencylab.rawsql.tests.base;

import de.gregord.databaseconcurrencylab.rawsql.Util;
import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class IsolationLevelTests extends BaseJdbcTests {
    static final Logger LOG = LoggerFactory.getLogger(IsolationLevelTests.class);

    public abstract int getIsolationLevel();

    /**
     * A transaction reads data that has been modified by another transaction but not yet committed.
     * If the other transaction rolls back, the data read by the first transaction is now invalid
     * (it never actually existed in the database).
     */
    public void test_dirty_reads() {
        insertUserAndProduct();

        AtomicReference<String> firstRead = new AtomicReference<>();
        AtomicReference<String> secondRead = new AtomicReference<>();
        Thread thread1 = Thread.ofVirtual().name("1").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                LOG.info("Reading description");
                firstRead.set(jdbcTemplate.queryForObject("SELECT description FROM products where id = 1", String.class));
                sleep(1000);
                LOG.info("Reading description again");
                secondRead.set(jdbcTemplate.queryForObject("SELECT description FROM products where id = 1", String.class));
                status.setRollbackOnly();
            });
        });

        Thread thread2 = Thread.ofVirtual().name("2").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                sleep(500);
                LOG.info("Change description");
                jdbcTemplate.update("UPDATE products SET description = 'changed' WHERE id = 1");
                sleep(1000);
                status.setRollbackOnly();
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(firstRead.get()).isEqualTo(secondRead.get());
    }

    public void test_repeatable_reads() {
        insertUserAndProduct();

        AtomicReference<String> firstRead = new AtomicReference<>();
        AtomicReference<String> secondRead = new AtomicReference<>();
        Thread thread1 = Thread.ofVirtual().name("1").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                LOG.info("Reading description");
                firstRead.set(jdbcTemplate.queryForObject("SELECT description FROM products where id = 1", String.class));
                sleep(1000);
                LOG.info("Reading description again");
                secondRead.set(jdbcTemplate.queryForObject("SELECT description FROM products where id = 1", String.class));
                status.setRollbackOnly();
            });
        });

        Thread thread2 = Thread.ofVirtual().name("2").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                sleep(500);
                LOG.info("Change description");
                jdbcTemplate.update("UPDATE products SET description = 'changed' WHERE id = 1");
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(firstRead.get()).isEqualTo(secondRead.get());
    }

    public void test_phantom_reads_after_single_insert() {
        insertUserAndProduct();

        AtomicInteger productCountPhantomRead = new AtomicInteger();
        Thread thread1 = Thread.ofVirtual().name("1").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                LOG.info("Reading all products");
                Integer firstCount = jdbcTemplate.queryForObject("SELECT count(*) FROM products WHERE id >= 1", Integer.class);
                LOG.info("Read {} products on the first time", firstCount);
                sleep(1000);
                LOG.info("Reading all products again");
                Integer secondCount = jdbcTemplate.queryForObject("SELECT count(*) FROM products WHERE id >= 1", Integer.class);
                LOG.info("Read {} products on the second time", secondCount);
                status.setRollbackOnly();
                productCountPhantomRead.set(secondCount);
            });
        });

        Thread thread2 = Thread.ofVirtual().name("2").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                sleep(500);
                LOG.info("Inserting product2");
                jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product2", "desc2", 10, 1);
                LOG.info("Inserted product2");
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(productCountPhantomRead.get()).isEqualTo(1);
    }

    public void test_phantom_reads_after_concurrent_inserts() {
        insertUserAndProduct();

        AtomicInteger productCountPhantomRead = new AtomicInteger();
        Thread thread1 = Thread.ofVirtual().name("1").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                LOG.info("Reading all products");
                Integer firstCount = jdbcTemplate.queryForObject("SELECT count(*) FROM products WHERE id >= 1", Integer.class);
                LOG.info("Read {} products on the first time", firstCount);
                sleep(1000);
                LOG.info("Inserting product3");
                jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product3", "desc3", 10, 1);
                LOG.info("Inserted product3");
                LOG.info("Reading all products again");
                Integer secondCount = jdbcTemplate.queryForObject("SELECT count(*) FROM products WHERE id >= 1", Integer.class);
                LOG.info("Read {} products on the second time", secondCount);
                status.setRollbackOnly();
                productCountPhantomRead.set(secondCount);
            });
        });

        Thread thread2 = Thread.ofVirtual().name("2").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                sleep(500);
                LOG.info("Inserting product2");
                jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product2", "desc2", 10, 1);
                LOG.info("Inserted product2");
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(productCountPhantomRead.get()).isEqualTo(2);
    }

    public void test_phantom_reads_without_initial_select() {
        insertUserAndProduct();

        AtomicInteger productCountPhantomRead = new AtomicInteger();
        Thread thread1 = Thread.ofVirtual().name("1").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                LOG.info("Reading all users");
                Integer firstCount = jdbcTemplate.queryForObject("SELECT count(*) FROM users WHERE id >= 1", Integer.class);
                LOG.info("Read {} users", firstCount);
                sleep(1000);
                LOG.info("Inserting product3");
                jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product3", "desc3", 10, 1);
                LOG.info("Inserted product3");
                LOG.info("Reading all products again");
                Integer secondCount = jdbcTemplate.queryForObject("SELECT count(*) FROM products WHERE id >= 1", Integer.class);
                LOG.info("Read {} products on the second time", secondCount);
                status.setRollbackOnly();
                productCountPhantomRead.set(secondCount);
            });
        });

        Thread thread2 = Thread.ofVirtual().name("2").start(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                sleep(500);
                LOG.info("Inserting product2");
                jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product2", "desc2", 10, 1);
                LOG.info("Inserted product2");
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(productCountPhantomRead.get()).isEqualTo(2);
    }

    public void test_lost_updates_single_step() {
        final int initialBalance = 10;
        final int reducedByAmount = 2;
        insertUserAndProduct();
        jdbcTemplate.update("UPDATE users set balance = ? where id = 1", initialBalance);
        Function<Integer, Runnable> reduceBalance = (reduceBy) -> () ->
                Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                    LOG.info("Reduce balance by 1");
                    jdbcTemplate.update("UPDATE users set balance = balance - ? where id = 1", reduceBy);
                    LOG.info("Balance reduced by 1");
                    sleep(500);
                });

        Thread thread1 = Thread.ofVirtual().name("1").start(reduceBalance.apply(reducedByAmount/2));
        Thread thread2 = Thread.ofVirtual().name("2").start(reduceBalance.apply(reducedByAmount/2));

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int newBalance = jdbcTemplate.queryForObject("SELECT balance FROM users where id = 1", Integer.class);
        assertThat(newBalance).isEqualTo(initialBalance - reducedByAmount);
    }

    public void test_lost_updates_with_initial_select() {
        final int initialBalance = 10;
        final int reducedByAmount = 2;
        insertUserAndProduct();
        jdbcTemplate.update("UPDATE users set balance = ? where id = 1", initialBalance);
        Function<Integer, Runnable> reduceBalance = (reduceBy) -> () ->
                Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                    LOG.info("Selecting balance");
                    jdbcTemplate.queryForObject("SELECT balance FROM users where id = 1", Integer.class);
                    sleep(500);
                    LOG.info("Reduce balance by 1");
                    jdbcTemplate.update("UPDATE users set balance = balance - ? where id = 1", reduceBy);
                    LOG.info("Balance reduced by 1");
                    sleep(500);
                });

        Thread thread1 = Thread.ofVirtual().name("1").start(reduceBalance.apply(reducedByAmount/2));
        Thread thread2 = Thread.ofVirtual().name("2").start(reduceBalance.apply(reducedByAmount/2));

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int newBalance = jdbcTemplate.queryForObject("SELECT balance FROM users where id = 1", Integer.class);
        assertThat(newBalance).isEqualTo(initialBalance - reducedByAmount);
    }

    public void test_lost_updates_in_two_steps() {
        final int initialBalance = 10;
        final int reducedByAmount = 2;
        insertUserAndProduct();
        jdbcTemplate.update("UPDATE users set balance = ? where id = 1", initialBalance);
        Function<Integer, Runnable> reduceBalance = (reduceBy) -> () ->
                Util.createTransactionTemplate(transactionManager, getIsolationLevel()).executeWithoutResult(status -> {
                    LOG.info("Query balance");
                    Integer balance = jdbcTemplate.queryForObject("SELECT balance FROM users where id = 1", Integer.class);
                    LOG.info("Read balance: {}", balance);
                    sleep(500);
                    LOG.info("Reduce balance by 1");
                    jdbcTemplate.update("UPDATE users set balance = ? where id = 1", balance - reduceBy);
                    LOG.info("Balance reduced");
                });
        Thread thread1 = Thread.ofVirtual().name("1").start(reduceBalance.apply(reducedByAmount / 2));
        Thread thread2 = Thread.ofVirtual().name("2").start(reduceBalance.apply(reducedByAmount / 2));

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int newBalance = jdbcTemplate.queryForObject("SELECT balance FROM users where id = 1", Integer.class);
        assertThat(newBalance).isEqualTo(initialBalance - reducedByAmount);
    }

    public void test_deadlock_detection() {
        insertUserAndProduct(); // ensure at least two rows to lock

        CompletableFuture<Void> t1 = CompletableFuture.runAsync(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel())
                    .executeWithoutResult(status -> {
                        jdbcTemplate.update("UPDATE users SET balance = balance WHERE id = 1");
                        sleep(500);
                        jdbcTemplate.update("UPDATE products SET stock = stock WHERE id = 1");
                    });
        }, new VirtualThreadExecutor("t1"));
        CompletableFuture<Void> t2 = CompletableFuture.runAsync(() -> {
            Util.createTransactionTemplate(transactionManager, getIsolationLevel())
                    .executeWithoutResult(status -> {
                        jdbcTemplate.update("UPDATE products SET stock = stock WHERE id = 1");
                        sleep(500);
                        jdbcTemplate.update("UPDATE users SET balance = balance WHERE id = 1");
                    });
        }, new VirtualThreadExecutor("t2"));

        assertThatThrownBy(() -> CompletableFuture.allOf(t1, t2).join())
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(PessimisticLockingFailureException.class);

    }

    void insertUserAndProduct() {
        LOG.info("Inserting user and product");
        jdbcTemplate.update("INSERT INTO users (username, email, password_hash, balance) VALUES (?, ?, ?, ?)", "user1", "user1@example.com", "hash1", 10);
        jdbcTemplate.update("INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)", "product1", "desc1", 10, 1);
    }

    @BeforeEach
    void cleanUpDatabase() {
        LOG.info("Cleaning up database");
        {
            if(dialectSpecific.doesNeedDeactivationOfForeignKeysForTruncation()){
                jdbcTemplate.execute(dialectSpecific.getDisableForeignKeysStatement());
            }
            List<String> tableNames = jdbcTemplate.queryForList(
                    dialectSpecific.getAllTablesForCurrentSchemaStatement(),
                    String.class
            );
            tableNames.forEach(tableName -> jdbcTemplate.execute(dialectSpecific.getTruncateStatement(tableName)));
            if(dialectSpecific.doesNeedDeactivationOfForeignKeysForTruncation()){
                jdbcTemplate.execute(dialectSpecific.getEnableForeignKeysStatement());
            }
        }
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
