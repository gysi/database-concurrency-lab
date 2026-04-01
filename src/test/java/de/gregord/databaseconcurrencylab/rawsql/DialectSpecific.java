package de.gregord.databaseconcurrencylab.rawsql;

public interface DialectSpecific {
    String getCurrentIsolationLevelQuery();
    boolean doesNeedDeactivationOfForeignKeysForTruncation();
    String getDisableForeignKeysStatement();
    String getEnableForeignKeysStatement();
    String getTruncateStatement(String tableName);
    String getAllTablesForCurrentSchemaStatement();
}
