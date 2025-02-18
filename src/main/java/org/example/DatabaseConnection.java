package org.example;

import java.util.Arrays;


public record DatabaseConnection(String databaseType, String name, String host, String port, String[] properties) {

    @Override
    public String toString() {
        return "DatabaseConnection{" +
                "databaseType='" + databaseType + '\'' +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}