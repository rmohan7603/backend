package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.util.DatabaseConnection;

public class AdminDAO {
    private Connection connection;

    public AdminDAO() throws SQLException, ClassNotFoundException {
        connection = DatabaseConnection.getConnection();
    }

    public String getPasswordByUsername(String username) throws SQLException {
        String query = "SELECT password FROM admin WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }
        return null; // Return null if no password found for the given username
    }

    public int getAdminIdByUsername(String username) throws SQLException {
        String query = "SELECT admin_id FROM admin WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("admin_id");
                }
            }
        }
        return -1; // Return -1 if admin_id not found for the given username
    }
}