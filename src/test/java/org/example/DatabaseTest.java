package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    // Test for MySQL
    @Test
    void testMySQLURL() {
        String url = "jdbc:mysql://localhost:3306/mydatabase?user=root&password=secret";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.MYSQL.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("localhost", db.host());
        assertEquals("3306", db.port());
        assertArrayEquals(new String[]{"user=root", "password=secret"}, db.properties());
    }

    // Test for PostgreSQL
    @Test
    void testPostgreSQLURL() {
        String url = "jdbc:postgresql://db.example.com/testdb?ssl=true";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.POSTGRESQL.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("db.example.com", db.host());
        assertEquals("5432", db.port());
        assertArrayEquals(new String[]{"ssl=true"}, db.properties());
    }

    // Test for Oracle
    @Test
    void testOracleURL() {
        String url = "jdbc:oracle://192.168.1.100/testdb";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.ORACLE.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("192.168.1.100", db.host());
        assertEquals("1521", db.port());
        assertArrayEquals(new String[]{}, db.properties());
    }

    // Test for SQL Server
    @Test
    void testSQLServerURL() {
        String url = "jdbc:sqlserver://server.company.com:1433;databaseName=employees";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.SQLSERVER.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("server.company.com", db.host());
        assertEquals("1433", db.port());
        assertArrayEquals(new String[]{}, db.properties());
    }

    // Test for MongoDB
    @Test
    void testMongoDBURL() {
        String url = "jdbc:mongodb://127.0.0.1/mydb?replicaSet=mySet";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.MONGODB.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("127.0.0.1", db.host());
        assertEquals("27017", db.port());
        assertArrayEquals(new String[]{"replicaSet=mySet"}, db.properties());
    }

    // Test for SQLite
    @Test
    void testSQLiteURL() {
        String url = "jdbc:sqlite:///home/user/database.db";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        assertEquals(DatabaseType.SQLITE.toString(), db.databaseType().toUpperCase()); // Convert to uppercase for comparison
        assertEquals("", db.host());
        assertEquals("0", db.port());
        assertArrayEquals(new String[]{"path=///home/user/database.db"}, db.properties());
    }

    // Test for the toString method of DatabaseConnection
    @Test
    void testToString() {
        String url = "jdbc:mysql://localhost:3306/mydatabase?user=root&password=secret";
        DatabaseConnection db = DatabaseParser.parseDatabaseURL(url);
        String expected = "DatabaseConnection{databaseType='mysql', name='mydatabase', host='localhost', port='3306', properties=[user=root, password=secret]}";
        assertEquals(expected, db.toString());
    }
}
