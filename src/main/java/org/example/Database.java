package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Database(String databaseType, String name, String host, String port, String[] properties) {

    private static final Map<String, Integer> DEFAULT_PORTS = new HashMap<>();

    static {
        DEFAULT_PORTS.put("mysql", 3306);
        DEFAULT_PORTS.put("postgresql", 5432);
        DEFAULT_PORTS.put("oracle", 1521);
        DEFAULT_PORTS.put("sqlserver", 1433);
        DEFAULT_PORTS.put("mongodb", 27017);
        DEFAULT_PORTS.put("sqlite", 0);
    }

    static Database parseDatabaseURL(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            throw new IllegalArgumentException("Invalid database URL: URL must start with 'jdbc:'");
        }

        String dbType = extractDatabaseType(url);
        if (dbType == null || dbType.isEmpty()) {
            throw new IllegalArgumentException("Invalid database URL: Missing database type");
        }


        if (dbType.equalsIgnoreCase("sqlite")) {
            return parseSQLiteURL(url);
        }

        if (dbType.equalsIgnoreCase("sqlserver")) {
            return parseSQLServerURL(url);
        }

        if (dbType.equalsIgnoreCase("mongodb")) {
            return parseMongoDBURL(url);
        }

        return parseStandardDatabaseURL(url, dbType);
    }

    private static String extractDatabaseType(String url) {
        return url.split(":")[1];  // Extract database type from URL
    }

    private static Database parseStandardDatabaseURL(String url, String dbType) {
        String regex = "^jdbc:(\\w+)://([^:/?]+)(?::(\\d+))?/([^?;]+)(?:[?;](.*))?$";
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            // If the host is missing, use localhost
            regex = "^jdbc:(\\w+)://([^?;]+)(?:[?;](.*))?$";
            matcher = Pattern.compile(regex).matcher(url);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid database URL format: " + url);
            }
            return createDatabaseFromMatcher(matcher, dbType);
        }

        // Ensure group count and valid groups before accessing
        return createDatabaseFromMatcher(matcher, dbType);
    }

    private static Database createDatabaseFromMatcher(Matcher matcher, String dbType) {
        String host = matcher.group(2);  // Extract host from group 2

        String port = matcher.group(3);  // Port is in group 3

        String name = matcher.group(4);  // Database name is in group 4

        // Validate host, default to localhost if missing
        host = validateHost(host);

        // Validate port, default to the database's default port if missing
        port = validatePort(port, dbType);

        // Validate database name
        name = validateDatabaseName(name);

        // Validate and process properties


        // Extracts the properties part from the URL if present (group 5 in the regex match).
// If no properties are found, assigns an empty string.
        String propertiesPart = (matcher.groupCount() >= 5 && matcher.group(5) != null) ? matcher.group(5) : "";

// Splits the properties string using '?', '&', or ';' as delimiters, creating an array.
// If propertiesPart is empty, assigns an empty array instead.
        String[] properties = !propertiesPart.isEmpty() ? propertiesPart.split("[?&;]") : new String[]{};

        return new Database(dbType, name, host, port, properties);
    }

    private static Database parseSQLServerURL(String url) {
        String regex = "^jdbc:sqlserver://([^:]+)(?::(\\d+))?;databaseName=([^?;]+)(?:[?&;](.*))?$";
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL Server URL format: " + url);
        }
        // Extracts database connection properties from the URL if present.
        // Properties are found in group 4 of the regex match and are split by '?', '&', or ';' delimiters.
        // If no properties exist, an empty array is assigned.
        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};

        String host = validateHost(matcher.group(1));
        String port = validatePort(matcher.group(2), "sqlserver");
        String name = validateDatabaseName(matcher.group(3));

        return new Database("sqlserver", name, host, port, properties);
    }

    private static Database parseMongoDBURL(String url) {
        String regex = "^jdbc:mongodb://([^:/]+)(?::(\\d+))?/([^?;]+)(?:[?&;](.*))?$";
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid MongoDB URL format: " + url);
        }

        String host = validateHost(matcher.group(1));
        String port = validatePort(matcher.group(2), "mongodb");
        String name = validateDatabaseName(matcher.group(3));

        String[] properties = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4).split("[?&;]") : new String[]{};

        return new Database("mongodb", name, host, port, properties);
    }

    private static Database parseSQLiteURL(String url) {
        String regex = "^jdbc:sqlite:(.*)$";
        Matcher matcher = Pattern.compile(regex).matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQLite URL format: " + url);
        }

        String path = matcher.group(1);  // SQLite path is captured as the database name
        return new Database("sqlite", path, "", String.valueOf(getDefaultPort("sqlite")), new String[]{"path=" + path});
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
