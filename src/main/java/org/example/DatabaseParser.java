package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DatabaseParser {

    // Regular expressions for parsing various database URLs
    private static final String STANDARD_DATABASE_URL_REGEX =
            "^jdbc:(\\w+)://([^:/?]+)(?::(\\d+))?/([^?;]+)(?:[?;](.*))?$";

    private static final String SQLSERVER_URL_REGEX =
            "^jdbc:sqlserver://([^:]+)(?::(\\d+))?;databaseName=([^?;]+)(?:[?&;](.*))?$";

    private static final String MONGODB_URL_REGEX =
            "^jdbc:mongodb://([^:/]+)(?::(\\d+))?/([^?;]+)(?:[?&;](.*))?$";

    private static final String SQLITE_URL_REGEX =
            "^jdbc:sqlite:(.*)$";

    // Method to parse the database URL and return a DatabaseConnection object
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

    // Helper method to extract the database type from the URL
    private static String extractDatabaseType(String url) {
        return url.split(":")[1];
    }

    // Parse standard database URL format
    private static DatabaseConnection parseStandardDatabaseURL(String url, String dbType, DatabaseType databaseType) {
        String regex = STANDARD_DATABASE_URL_REGEX;
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            regex = "^jdbc:(\\w+)://([^?;]+)(?:[?;](.*))?$";
            matcher = Pattern.compile(regex).matcher(url);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid database URL format");
            }
            return createDatabaseFromMatcher(matcher, dbType, databaseType);
        }

        return createDatabaseFromMatcher(matcher, dbType, databaseType);
    }

    // Create a DatabaseConnection object from the regex match
    private static DatabaseConnection createDatabaseFromMatcher(Matcher matcher, String dbType, DatabaseType databaseType) {
        String host = DatabaseValidator.validateHost(matcher.group(2));
        String port = DatabaseValidator.validatePort(matcher.group(3), databaseType);
        String name = DatabaseValidator.validateDatabaseName(matcher.group(4));

        String propertiesPart = (matcher.groupCount() >= 5 && matcher.group(5) != null) ? matcher.group(5) : "";
        String[] properties = !propertiesPart.isEmpty() ? propertiesPart.split("[?&;]") : new String[]{};

        return new DatabaseConnection(dbType, name, host, port, properties);
    }

    private static DatabaseConnection parseSQLServerURL(String url, DatabaseType databaseType) {
        Matcher matcher = Pattern.compile(SQLSERVER_URL_REGEX).matcher(url);

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
        Matcher matcher = Pattern.compile(MONGODB_URL_REGEX).matcher(url);

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
        Matcher matcher = Pattern.compile(SQLITE_URL_REGEX).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQLite URL format");
        }

        String path = matcher.group(1);
        return new DatabaseConnection("sqlite", path, "", String.valueOf(databaseType.getDefaultPort()), new String[]{"path=" + path});
    }
}
