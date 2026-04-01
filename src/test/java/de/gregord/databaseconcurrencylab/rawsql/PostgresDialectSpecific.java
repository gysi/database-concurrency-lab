package de.gregord.databaseconcurrencylab.rawsql;

public class PostgresDialectSpecific implements DialectSpecific {
    @Override
    public String getTruncateStatement(String tableName) {
        return "TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE";
    }

    @Override
    public String getAllTablesForCurrentSchemaStatement() {
        return "SELECT tablename FROM pg_tables WHERE schemaname = current_schema()";
    }

    @Override
    public String getCurrentIsolationLevelQuery() {
        return "SELECT current_setting('transaction_isolation');";
    }

    @Override
    public boolean doesNeedDeactivationOfForeignKeysForTruncation() {
        return false;
    }

    @Override
    public String getDisableForeignKeysStatement() {
        throw new UnsupportedOperationException("Not needed for Postgres");
    }

    @Override
    public String getEnableForeignKeysStatement() {
        throw new UnsupportedOperationException("Not needed for Postgres");
    }


}
