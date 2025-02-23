package org.example;

public class Main {
    public static void main(String[] args) {
        String[] urls = {
                // MySQL Test URL
                "jdbc:mysql://localhost:9999/mydatabase?user=root&password=secret",
                "jdbc:mysql://localhost/mydatabase?user=root&password=secret",

                // PostgreSQL Test URL
                "jdbc:postgresql://db.example.com:512/testdb?ssl=true",
                "jdbc:postgresql://db.example.com/testdb?ssl=true",
                // Oracle Test URL
                "jdbc:oracle://192.168.1.100/testdb",

                // SQL Server Test URL
                "jdbc:sqlserver://server.company.com:1433;databaseName=employees",

                // MongoDB Test URL
                "jdbc:mongodb://127.0.0.1/mydb?replicaSet=mySet",

                // SQLite Test URL
                "jdbc:sqlite:///home/user/database.db"
        };

        for (String url : urls) {
            try {
                DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
                System.out.println(db);
            } catch (Exception e) {
                System.err.println("Error parsing URL: " + url + " - " + e.getMessage());
            }
        }
    }
}
