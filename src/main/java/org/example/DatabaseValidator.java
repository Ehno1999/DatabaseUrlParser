package org.example;

class DatabaseValidator {

    // Validate host (non-null, non-empty, default to localhost)
    public static String validateHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return "localhost";
        }
        return host;
    }

    // Validate port (non-null, numeric, use default if invalid)
    public static String validatePort(String port, DatabaseType dbType) {
        if (port == null || port.trim().isEmpty()) {
            // Return the default port only if the port is not provided in the URL
            return String.valueOf(dbType.getDefaultPort());
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

    // Validate database name (non-null, non-empty)
    public static String validateDatabaseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid database name: Name cannot be empty");
        }
        return name;
    }

}
