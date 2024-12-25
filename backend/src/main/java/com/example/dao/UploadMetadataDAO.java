package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.example.util.DatabaseConnection;

public class UploadMetadataDAO {
    private Connection connection;

    public UploadMetadataDAO() throws SQLException, ClassNotFoundException {
        connection = DatabaseConnection.getConnection();
    }

    public void recordUpload(int adminId, String fileName, int recordsInserted, int recordsUpdated, int recordsDiscarded) throws SQLException {
        String query = "INSERT INTO upload_metadata (admin_id, file_name, records_inserted, records_updated, records_discarded, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            stmt.setString(2, fileName);
            stmt.setInt(3, recordsInserted);
            stmt.setInt(4, recordsUpdated);
            stmt.setInt(5, recordsDiscarded);
            stmt.setString(6, "SUCCESS");
            stmt.executeUpdate();
        }
    }
}