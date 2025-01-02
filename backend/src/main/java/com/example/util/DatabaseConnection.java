package com.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static DataSource dataSource;

    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("dbconfig.properties")) {
            if (input == null) {
                logger.error("dbconfig.properties file not found in classpath");
                throw new RuntimeException("dbconfig.properties file not found in classpath");
            }
            properties.load(input);
            logger.info("Successfully loaded dbconfig.properties");
        } catch (Exception e) {
            logger.error("Failed to load dbconfig.properties", e);
            throw new RuntimeException("Failed to load dbconfig.properties", e);
        }

        String dbType = properties.getProperty("db.type");
        String host = System.getenv().getOrDefault("DB_HOST", properties.getProperty("db.host", "localhost"));
        String port = System.getenv().getOrDefault("DB_PORT", properties.getProperty("db.port", "3306"));
        String database = properties.getProperty("db.database");
        String username = System.getenv().getOrDefault("DB_USERNAME", properties.getProperty("db.username"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", properties.getProperty("db.password"));

        String url = "jdbc:" + dbType + "://" + host + ":" + port + "/" + database;

        //logger.info("Constructed database URL: {}", url);

        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/myDBSource");
            logger.info("Successfully initialized JNDI DataSource");
        } catch (NamingException e) {
            logger.error("Failed to initialize JNDI DataSource", e);
            throw new RuntimeException("Failed to initialize JNDI DataSource", e);
        }

    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        logger.info("Establishing database connection...");
        try {
            Connection connection = dataSource.getConnection();
            logger.info("Database connection established successfully.");
            return connection;
        } catch (SQLException e) {
            logger.error("Error establishing database connection", e);
            throw e;
        }
    }

    public static String getDbType() {
        String dbType = properties.getProperty("db.type");
        return dbType;
    }
}