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

    public void bulkInsertOrUpdate(List<UsageData> dataList, String action) throws SQLException {
        String query = QueryLoader.getQuery("bulk_insert_or_update").replace("{}", 
            action.equals("update") ? "values(usage_value)" : "usage_value");

        try {
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                for (UsageData data : dataList) {
                    stmt.setString(1, data.getUserId());
                    stmt.setInt(2, data.getUsageValue());
                    stmt.setLong(3, data.getEpoch());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

    }
    
    public int getRowNumber() throws SQLException {
        String query = QueryLoader.getQuery("max_row_id");
        
        int rowNumber=0;
        try (PreparedStatement stmt2 = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt2.executeQuery()) {
                if (rs.next()) {
                    rowNumber = rs.getInt("count(*)");
                }
            }
        }
        return rowNumber;
    }
}