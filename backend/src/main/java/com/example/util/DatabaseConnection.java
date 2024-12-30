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
            
//            System.out.println("DB Connection EStablished");
            logger.info("Database connection established successfully.");
            return connection;
        } catch (SQLException e) {
            logger.error("Error establishing database connection", e);
            throw e;
        }
    }

    public static String getDbType() {
        String dbType = properties.getProperty("db.type");
        logger.info("Database type: {}", dbType);
        return dbType;
    }
}