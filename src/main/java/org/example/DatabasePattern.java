package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DatabasePattern {
    STANDARD("^jdbc:(\\w+)://([^:/?]+)(?::(\\d+))?/([^?;]+)(?:[?;](.*))?$"),
    STANDARD_ALT("^jdbc:(\\w+)://([^?;]+)(?:[?;](.*))?$"), // Alternative for standard DBs without port
    SQLSERVER("^jdbc:sqlserver://([^:]+)(?::(\\d+))?;databaseName=([^?;]+)(?:[?&;](.*))?$"),
    MONGODB("^jdbc:mongodb://([^:/]+)(?::(\\d+))?/([^?;]+)(?:[?&;](.*))?$"),
    SQLITE("^jdbc:sqlite:(.*)$");

    private final Pattern pattern;

    DatabasePattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher getMatcher(String url) {
        return pattern.matcher(url);
    }
}
