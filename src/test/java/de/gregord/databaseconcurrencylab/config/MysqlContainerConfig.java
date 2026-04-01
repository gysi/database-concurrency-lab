package de.gregord.databaseconcurrencylab.config;

import de.gregord.databaseconcurrencylab.rawsql.DialectSpecific;
import de.gregord.databaseconcurrencylab.rawsql.MySqlDialectSpecific;
import org.slf4j.Logger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.slf4j.LoggerFactory.getLogger;

@TestConfiguration
@Profile("mysql")
// @Testcontainers, disabled see comment below
public class MysqlContainerConfig {
    private static final Logger LOG = getLogger(MysqlContainerConfig.class);
    /* @Container
     * The @Container Annotation doesn't work correctly when using this class in multiple Test Classes.
     * A new container gets created for each Class that extends this class, but the connection pool and
     * other stuff is still configured for the old mysql container, which is kinda strange, because
     * the @Testcontainers documentation states that it should work.
     * But I guess it doesn't really work great together with the @ServiceConnection Annotation or Spring Boot.
     * I think its because by default Spring keeps reusing the same Context across the tests and then the new
     * Container configuration isn't injected into the AutoConfigurations that configure the jdbc connection pools.
     */
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8"))
                .withEnv("MYSQL_INITDB_SKIP_TZINFO", "true")
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
        return new MySqlDialectSpecific();
    }
}
