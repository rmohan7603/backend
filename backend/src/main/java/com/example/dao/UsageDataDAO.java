package com.example.dao;

import com.example.model.UsageData;
import com.example.util.DatabaseConnection;
import com.example.util.QueryLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsageDataDAO {
    private Connection connection;

    public UsageDataDAO() throws SQLException, ClassNotFoundException {
        connection = DatabaseConnection.getConnection();
    }

    public List<UsageData> findDuplicates(List<UsageData> dataList) throws SQLException {
        List<UsageData> duplicates = new ArrayList<>();
        String query = QueryLoader.getQuery("find_duplicates");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (UsageData data : dataList) {
                stmt.setString(1, data.getUserId());
                stmt.setLong(2, data.getEpoch());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        duplicates.add(data);
                    }
                }
            }
        }
        return duplicates;
    }

    public void bulkInsertOrUpdate(List<UsageData> insertList, List<UsageData> updateList) throws SQLException {
        String insertQuery = QueryLoader.getQuery("bulk_insert");
        String updateQuery = QueryLoader.getQuery("bulk_update");

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                for (UsageData data : insertList) {
                    insertStmt.setString(1, data.getUserId());
                    insertStmt.setInt(2, data.getUsageValue());
                    insertStmt.setLong(3, data.getEpoch());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                for (UsageData data : updateList) {
                    updateStmt.setInt(1, data.getUsageValue());
                    updateStmt.setString(2, data.getUserId());
                    updateStmt.setLong(3, data.getEpoch());
                    updateStmt.addBatch();
                }
                updateStmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}