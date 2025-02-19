package org.example;

import java.util.regex.Matcher;

class DatabaseParser {

    public static DatabaseConnection parseDatabaseURL(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            throw new IllegalArgumentException("Invalid database URL: URL must start with 'jdbc:'");
        }

        String dbType = extractDatabaseType(url);
        DatabaseType databaseType = DatabaseType.fromString(dbType);

        return switch (databaseType) {
            case SQLITE -> parseSQLiteURL(url, databaseType);
            case SQLSERVER -> parseSQLServerURL(url, databaseType);
            case MONGODB -> parseMongoDBURL(url, databaseType);
            default -> parseStandardDatabaseURL(url, dbType, databaseType);
        };
    }

    private static String extractDatabaseType(String url) {
        return url.split(":")[1];
    }

    private static DatabaseConnection parseStandardDatabaseURL(String url, String dbType, DatabaseType databaseType) {
        Matcher matcher = DatabasePattern.STANDARD.getMatcher(url);

        if (!matcher.matches()) {
            matcher = DatabasePattern.STANDARD_ALT.getMatcher(url);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid standard database URL format");
            }
        }

        return createDatabaseFromMatcher(matcher, dbType, databaseType);
    }

    private static DatabaseConnection parseSQLServerURL(String url, DatabaseType databaseType) {
        Matcher matcher = DatabasePattern.SQLSERVER.getMatcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL Server URL format");
        }

        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};
        String host = DatabaseValidator.validateHost(matcher.group(1));
        String port = DatabaseValidator.validatePort(matcher.group(2), databaseType);
        String name = DatabaseValidator.validateDatabaseName(matcher.group(3));

        return new DatabaseConnection("sqlserver", name, host, port, properties);
    }

    private static DatabaseConnection parseMongoDBURL(String url, DatabaseType databaseType) {
        Matcher matcher = DatabasePattern.MONGODB.getMatcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid MongoDB URL format");
        }

        String host = DatabaseValidator.validateHost(matcher.group(1));
        String port = DatabaseValidator.validatePort(matcher.group(2), databaseType);
        String name = DatabaseValidator.validateDatabaseName(matcher.group(3));

        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};

        return new DatabaseConnection("mongodb", name, host, port, properties);
    }

    private static DatabaseConnection parseSQLiteURL(String url, DatabaseType databaseType) {
        Matcher matcher = DatabasePattern.SQLITE.getMatcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQLite URL format");
        }

        String path = matcher.group(1);
        return new DatabaseConnection("sqlite", path, "", String.valueOf(databaseType.getDefaultPort()), new String[]{"path=" + path});
    }

    private static DatabaseConnection createDatabaseFromMatcher(Matcher matcher, String dbType, DatabaseType databaseType) {
        String host = DatabaseValidator.validateHost(matcher.group(2));
        String port = DatabaseValidator.validatePort(matcher.group(3), databaseType);
        String name = DatabaseValidator.validateDatabaseName(matcher.group(4));

        String propertiesPart = (matcher.groupCount() >= 5 && matcher.group(5) != null) ? matcher.group(5) : "";
        String[] properties = !propertiesPart.isEmpty() ? propertiesPart.split("[?&;]") : new String[]{};

        return new DatabaseConnection(dbType, name, host, port, properties);
    }
}
