package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.util.DatabaseConnection;
import com.example.util.QueryLoader;

public class AdminDAO {
    private Connection connection;

    public AdminDAO() throws SQLException, ClassNotFoundException {
        connection = DatabaseConnection.getConnection();
    }

    public String getPasswordByUsername(String username) throws SQLException {
        String query = QueryLoader.getQuery("getPasswordByUsername");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }
        return null;
    }

    public int getAdminIdByUsername(String username) throws SQLException {
        String query = QueryLoader.getQuery("getAdminIdByUsername");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("admin_id");
                }
            }
        }
        return -1;
    }
}