package org.example;

import java.util.Map;

public enum DatabaseType {
    SQLITE("sqlite", 0),
    SQLSERVER("sqlserver", 1433),
    MONGODB("mongodb", 27017),
    MYSQL("mysql", 3306),
    POSTGRESQL("postgresql", 5432),
    ORACLE("oracle", 1521);

    private final String type;
    private final int defaultPort;

    DatabaseType(String type, int defaultPort) {
        this.type = type;
        this.defaultPort = defaultPort;
    }

    public String getType() {
        return type;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    // Method to get the DatabaseType from a string (case-insensitive)
    public static DatabaseType fromString(String type) {
        for (DatabaseType dbType : DatabaseType.values()) {
            if (dbType.getType().equalsIgnoreCase(type)) {
                return dbType;
            }
        }
        throw new IllegalArgumentException("Unsupported database type: " + type);
    }
}
