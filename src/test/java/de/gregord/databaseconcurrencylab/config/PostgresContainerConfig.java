package de.gregord.databaseconcurrencylab.config;

import de.gregord.databaseconcurrencylab.rawsql.DialectSpecific;
import de.gregord.databaseconcurrencylab.rawsql.MySqlDialectSpecific;
import de.gregord.databaseconcurrencylab.rawsql.PostgresDialectSpecific;
import org.slf4j.Logger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.slf4j.LoggerFactory.getLogger;

@TestConfiguration
@Profile("postgres")
// @Testcontainers, disabled see comment below
public class PostgresContainerConfig {
    private static final Logger LOG = getLogger(PostgresContainerConfig.class);
    /* @Container
     * The @Container Annotation doesn't work correctly when using this class in multiple Test Classes.
     * A new container gets created for each Class that extends this class, but the connection pool and
     * other stuff is still configured for the old mysql container, which is kinda strange, because
     * the @Testcontainers documentation states that it should work.
     * But I guess it doesn't really work great together with the @SerivceConnection Annotation or Spring Boot.
     * I think its because by default Spring keeps reusing the same Context across the tests and then the new
     * Container configuration isn't injected into the Autoconfigurations that configure the jdbc connection pools.
     */
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("password")
//                .withLogConsumer(frame -> { // Capture logs
//                    LOG.info("SQL LOG: {}", frame.getUtf8String());
//                })
                ;
    }

    @Bean
    DialectSpecific dialectSpecific() {
        return new PostgresDialectSpecific();
    }
}

