package de.gregord.databaseconcurrencylab;

import org.springframework.boot.SpringApplication;

public class TestDatabaseConcurrencyLabApplication {

    public static void main(String[] args) {
        SpringApplication.from(DatabaseConcurrencyLabApplication::main).run(args);
    }

}
