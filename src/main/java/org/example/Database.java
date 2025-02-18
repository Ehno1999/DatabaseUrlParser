package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Database(String databaseType, String name, String host, String port, String[] properties) {
    public static final String SQLITE = "sqlite";
    public static final String SQLSERVER = "sqlserver";
    public static final String MONGODB = "mongodb";
    public static final String MYSQL = "mysql";
    public static final String POSTGRESQL = "postgresql";
    public static final String ORACLE = "oracle";

    // Regular expression for parsing standard database URLs (e.g., jdbc:mysql://localhost:3306/mydb)
    public static final String STANDARD_DATABASE_URL_REGEX =
            "^jdbc:(\\w+)://([^:/?]+)(?::(\\d+))?/([^?;]+)(?:[?;](.*))?$";

    // Regular expression for parsing SQL Server specific URL (e.g., jdbc:sqlserver://localhost:1433;databaseName=mydb)
    public static final String SQLSERVER_URL_REGEX =
            "^jdbc:sqlserver://([^:]+)(?::(\\d+))?;databaseName=([^?;]+)(?:[?&;](.*))?$";

    // Regular expression for parsing MongoDB URL (e.g., jdbc:mongodb://localhost:27017/mydb)
    public static final String MONGODB_URL_REGEX =
            "^jdbc:mongodb://([^:/]+)(?::(\\d+))?/([^?;]+)(?:[?&;](.*))?$";

    // Regular expression for parsing SQLite URL (e.g., jdbc:sqlite:/path/to/database)
    public static final String SQLITE_URL_REGEX =
            "^jdbc:sqlite:(.*)$";
    private static final Map<String, Integer> DEFAULT_PORTS = Map.of(
            MYSQL, 3306,
            POSTGRESQL, 5432,
            ORACLE, 1521,
            SQLSERVER, 1433,
            MONGODB, 27017,
            SQLITE, 0
    );

    private static final Set<String> SUPPORTED_DATABASES = Set.of(SQLITE, SQLSERVER, MONGODB, MYSQL, POSTGRESQL, ORACLE);

    static Database parseDatabaseURL(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            throw new IllegalArgumentException("Invalid database URL: URL must start with 'jdbc:'");
        }

        String dbType = extractDatabaseType(url);
        if (!SUPPORTED_DATABASES.contains(dbType)) {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        return switch (dbType.toLowerCase()) {
            case SQLITE -> parseSQLiteURL(url);
            case SQLSERVER -> parseSQLServerURL(url);
            case MONGODB -> parseMongoDBURL(url);
            default -> parseStandardDatabaseURL(url, dbType);
        };
    }

    private static String extractDatabaseType(String url) {
        return url.split(":")[1];  // Extract database type from URL
    }

    private static Database parseStandardDatabaseURL(String url, String dbType) {
        String regex = STANDARD_DATABASE_URL_REGEX;
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            // If the host is missing, use localhost
            regex = "^jdbc:(\\w+)://([^?;]+)(?:[?;](.*))?$";
            matcher = Pattern.compile(regex).matcher(url);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid database URL format: ");
            }
            return createDatabaseFromMatcher(matcher, dbType);
        }

        // Ensure group count and valid groups before accessing
        return createDatabaseFromMatcher(matcher, dbType);
    }

    private static Database createDatabaseFromMatcher(Matcher matcher, String dbType) {


        // Validate host, default to localhost if missing

        String host = validateHost(matcher.group(2));  // Extract host from group 2

        // Validate port, default to the database's default port if missing
        String port = validatePort(matcher.group(3), dbType);  // Port is in group 3

        // Validate database name
        String name = validateDatabaseName(matcher.group(4));  // Database name is in group 4

        // Extracts the properties part from the URL if present (group 5 in the regex match).
        // If no properties are found, assigns an empty string.
        String propertiesPart = (matcher.groupCount() >= 5 && matcher.group(5) != null) ? matcher.group(5) : "";

        // Splits the properties string using '?', '&', or ';' as delimiters, creating an array.
        // If propertiesPart is empty, assigns an empty array instead.
        String[] properties = !propertiesPart.isEmpty() ? propertiesPart.split("[?&;]") : new String[]{};

        return new Database(dbType, name, host, port, properties);
    }

    private static Database parseSQLServerURL(String url) {
        Matcher matcher = Pattern.compile(SQLSERVER_URL_REGEX).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL Server URL format: " + url);
        }
        // Extracts database connection properties from the URL if present.
        // Properties are found in group 4 of the regex match and are split by '?', '&', or ';' delimiters.
        // If no properties exist, an empty array is assigned.
        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};

        String host = validateHost(matcher.group(1));
        String port = validatePort(matcher.group(2), SQLSERVER);
        String name = validateDatabaseName(matcher.group(3));

        return new Database(SQLSERVER, name, host, port, properties);
    }

    private static Database parseMongoDBURL(String url) {
        Matcher matcher = Pattern.compile(MONGODB_URL_REGEX).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid MongoDB URL format: " + url);
        }

        String host = validateHost(matcher.group(1));
        String port = validatePort(matcher.group(2), "mongodb");
        String name = validateDatabaseName(matcher.group(3));

        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};

        return new Database(MONGODB, name, host, port, properties);
    }

    private static Database parseSQLiteURL(String url) {
        Matcher matcher = Pattern.compile(SQLITE_URL_REGEX).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQLite URL format: " + url);
        }

        String path = matcher.group(1);  // SQLite path is captured as the database name
        return new Database(SQLITE, path, "", String.valueOf(getDefaultPort("sqlite")), new String[]{"path=" + path});
    }

    // Validate Host (non-null, non-empty, default to localhost)
    private static String validateHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return "localhost";  // Default host
        }
        return host;
    }

    // Validate Port (non-null, numeric, use default if invalid)
    private static String validatePort(String port, String dbType) {
        if (port == null || port.trim().isEmpty()) {
            return String.valueOf(getDefaultPort(dbType));  // Use default port if missing
        }

        try {
            int portInt = Integer.parseInt(port);
            if (portInt <= 0 || portInt > 65535) {
                throw new IllegalArgumentException("Invalid port number: " + port);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port number format: " + port);
        }
    }

    // Validate Database Name (non-null, non-empty)
    private static String validateDatabaseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid database name: Name cannot be empty");
        }
        return name;
    }

    // Get Default Port for a given database type
    private static int getDefaultPort(String dbType) {
        return DEFAULT_PORTS.getOrDefault(dbType.toLowerCase(), -1);
    }

    @Override
    public String toString() {
        return "Database{" + "type='" + databaseType + '\'' + ", name='" + name + '\'' + ", host='" + host + '\'' + ", port='" + port + '\'' + ", properties=" + Arrays.toString(properties) + '}';
    }
}
