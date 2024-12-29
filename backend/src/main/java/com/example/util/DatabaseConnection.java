package com.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static DataSource dataSource;

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
        
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/YourDB");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize JNDI DataSource", e);
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
    	System.out.println("DB CONNECTION ESTABLiSHED");
        return dataSource.getConnection();
    }
    
    public static String getDbType() {
        return properties.getProperty("db.type");
    }
}