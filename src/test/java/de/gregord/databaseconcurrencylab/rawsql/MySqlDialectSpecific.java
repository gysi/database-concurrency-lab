package de.gregord.databaseconcurrencylab.rawsql;

public class MySqlDialectSpecific implements DialectSpecific {

    @Override
    public String getCurrentIsolationLevelQuery() {
        return "SELECT @@transaction_isolation";
    }

    @Override
    public boolean doesNeedDeactivationOfForeignKeysForTruncation() {
        return true;
    }

    @Override
    public String getEnableForeignKeysStatement() {
        return "SET FOREIGN_KEY_CHECKS = 1;";
    }

    @Override
    public String getTruncateStatement(String tableName) {
        return "TRUNCATE TABLE " + tableName;
    }

    @Override
    public String getAllTablesForCurrentSchemaStatement() {
        return "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE();";
    }

    @Override
    public String getDisableForeignKeysStatement() {
        return "SET FOREIGN_KEY_CHECKS = 0;";
    }
}
