package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DatabasePattern {
    STANDARD("^jdbc:(\\w+)://([^:/?]+)(?::(\\d+))?/([^?;]+)(?:[?;](.*))?$"), // Standard JDBC URL with optional port and parameters
    STANDARD_ALT("^jdbc:(\\w+)://([^?;]+)(?:[?;](.*))?$"), // Alternative for standard DBs without a port
    SQLSERVER("^jdbc:sqlserver://([^:]+)(?::(\\d+))?;databaseName=([^?;]+)(?:[?&;](.*))?$"), // SQL Server JDBC URL with optional port and parameters
    MONGODB("^jdbc:mongodb://([^:/]+)(?::(\\d+))?/([^?;]+)(?:[?&;](.*))?$"), // MongoDB JDBC URL with optional port and parameters
    SQLITE("^jdbc:sqlite:(.*)$"); // SQLite JDBC URL (file-based, no host/port)

    private final Pattern pattern;

    DatabasePattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher getMatcher(String url) {
        return pattern.matcher(url);
    }
}
