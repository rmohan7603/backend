package com.example.util;

import java.io.InputStream;
import java.util.Properties;

public class QueryLoader {
    private static final Properties queries = new Properties();

    static {
        try {
            String dbType = DatabaseConnection.getDbType();
            String fileName = "queries_" + dbType + ".properties";
            InputStream input = QueryLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (input == null) {
                throw new RuntimeException("Query file " + fileName + " not found in classpath");
            }
            queries.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load query properties", e);
        }
    }

    public static String getQuery(String key) {
        return queries.getProperty(key);
    }
}