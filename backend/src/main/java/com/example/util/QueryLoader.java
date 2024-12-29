package com.example.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryLoader {
	
    private static final Properties queries = new Properties();
    
    private static final Logger logger = LogManager.getLogger(QueryLoader.class);
    
    static {
        try {
            String dbType = DatabaseConnection.getDbType();
            String fileName = "queries_" + dbType + ".properties";
            InputStream input = QueryLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (input == null) {
                logger.error("Query file {} not found in classpath", fileName);
                throw new RuntimeException("Query file " + fileName + " not found in classpath");
            }
            queries.load(input);
        } catch (Exception e) {
            logger.error("Failed to load query properties", e);
            throw new RuntimeException("Failed to load query properties", e);
        }
    }

    public static String getQuery(String key) {
        return queries.getProperty(key);
    }
}