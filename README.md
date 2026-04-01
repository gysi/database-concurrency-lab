# database-concurrency-lab

An experiment-driven Spring Boot project for exploring how JDBC transaction isolation and locking behavior differ between MySQL and PostgreSQL.

Instead of modeling a full business application, this repository focuses on small, reproducible concurrency scenarios implemented with raw SQL, Spring transactions, Testcontainers, and JUnit tests.

## What this project is about

This repository is a sandbox for answering practical questions such as:

- Which anomalies are possible at each transaction isolation level?
- How do Spring isolation settings map to the actual database isolation level?
- Where do MySQL and PostgreSQL behave differently?
- Which scenarios lead to assertion failures versus locking exceptions?

The tests spin up real databases in containers and exercise concurrent transactions against a small schema containing `users`, `products`, and `orders` tables.

## Tech stack

- Java 21
- Gradle
- Spring Boot 3.4
- Spring JDBC / transaction management
- Flyway migrations
- JUnit 5
- Testcontainers
- MySQL 8
- PostgreSQL 17
- Virtual threads for concurrent test execution

## How the tests are organized

### Concurrency/anomaly scenarios

`IsolationLevelTests` defines the reusable scenarios, and concrete subclasses execute them for specific isolation levels:

- `ReadUncommitedTests`
- `ReadCommitedTests`
- `RepeatableReadTests`
- `SerializableTests`

Covered scenarios include:

- dirty reads
- repeatable reads
- phantom reads after a single insert
- phantom reads after concurrent inserts
- phantom reads without an initial select
- lost updates in several variants
- deadlock detection

The shared test logic uses explicit SQL statements and concurrent transactions so behavior stays easy to inspect.

## Database setup used by the tests

The project uses Spring Boot test configuration plus Testcontainers:

- `mysql` profile starts a `mysql:8` container
- `postgres` profile starts a `postgres:17` container
- Flyway migrations are switched per profile
- The default test profile configuration currently activates `test,mysql`

Schema differences are handled explicitly where needed:

- separate Flyway migration scripts for MySQL and PostgreSQL
- dialect-specific SQL for querying isolation level
- dialect-specific table truncation and foreign-key handling

## Running the tests

### Run the default test setup

```bash
./gradlew test
```

By default, test configuration activates the `test,mysql` profiles.

### Run against PostgreSQL

Override the active Spring profiles for the test run, for example:

```bash
SPRING_PROFILES_ACTIVE=test,postgres ./gradlew test
```

### Run a focused test class

```bash
./gradlew test --tests de.gregord.databaseconcurrencylab.rawsql.AppAndDbIsolationLevelsTest
```

## Interpreting the results

The repository already contains a compact result table in:

[src/test/java/de/gregord/databaseconcurrencylab/rawsql/tests/README.md](src/test/java/de/gregord/databaseconcurrencylab/rawsql/tests/README.md)

That table records which scenarios currently:

- pass as asserted
- fail assertions
- fail with locking-related exceptions

One notable documented edge case is that `SerializableTests.test_phantom_reads_without_initial_select()` behaves differently on MySQL because consistent snapshots there depend on prior reads in ways that differ from PostgreSQL.

## Why this project is useful

This repository is valuable if you want to:

- learn database isolation levels through runnable examples
- compare MySQL and PostgreSQL behavior instead of relying on generic definitions
- understand what Spring's transaction isolation settings really do in practice
- experiment with locking anomalies using a minimal, reproducible setup

## Further reading
- https://vladmihalcea.com/optimistic-vs-pessimistic-locking/
