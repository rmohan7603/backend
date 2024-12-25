package com.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("dbconfig.properties")) {
            if (input == null) {
                throw new RuntimeException("dbconfig.properties file not found in classpath");
            }
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load dbconfig.properties", e);
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException, ClassNotFoundException {

    	String dbUrl = properties.getProperty("db.url");
        String dbUsername = properties.getProperty("db.username");
        String dbPassword = properties.getProperty("db.password");
        String dbDriver = properties.getProperty("db.driver");

        Class.forName(dbDriver);

        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}