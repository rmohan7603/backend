package com.example.service;

import com.example.dao.UploadMetadataDAO;
import com.example.dao.UsageDataDAO;
import com.example.model.ChartData;
import com.example.model.UsageData;
import com.example.util.CSVProcessor;
import com.example.util.DatabaseConnection;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class UsageDataService {
	private UsageDataDAO usageDataDAO;
    private UploadMetadataDAO uploadMetadataDAO;

    public UsageDataService() throws SQLException, ClassNotFoundException {
        usageDataDAO = new UsageDataDAO();
        uploadMetadataDAO = new UploadMetadataDAO();
    }

    public void processCSV(InputStream fileContent, int adminId, String fileName) throws Exception {
        List<UsageData> dataList = CSVProcessor.parseCSV(fileContent);
        List<UsageData> duplicates = usageDataDAO.findDuplicates(dataList);

        // Simulate admin decision logic
        List<UsageData> insertList = new ArrayList<>();
        List<UsageData> updateList = new ArrayList<>();
        for (UsageData data : dataList) {
            if (duplicates.contains(data)) {
                // Simulate admin choice: Update duplicates
                updateList.add(data);
            } else {
                insertList.add(data);
            }
        }

        usageDataDAO.bulkInsertOrUpdate(insertList, updateList);

        // Record metadata without file size
        uploadMetadataDAO.recordUpload(adminId, fileName, insertList.size(), updateList.size(), duplicates.size() - updateList.size());
    }

    public List<ChartData> getChartData(String filter) throws ClassNotFoundException {
        System.out.println(filter);
    	String query = "SELECT user_id, SUM(usage_value) AS total_usage FROM usage_table WHERE 1=1 ";
        long currentEpoch = System.currentTimeMillis() / 1000; // Current time in epoch seconds

        switch (filter.toLowerCase()) {
            case "today":
                query += "AND epoch >= " + getStartOfTodayEpoch();
                break;
            case "yesterday":
                query += "AND epoch >= " + getStartOfYesterdayEpoch() + " AND epoch < " + getStartOfTodayEpoch();
                break;
            case "last24hours":
                query += "AND epoch >= " + (currentEpoch - 86400); // 24 * 60 * 60
                break;
            case "thisweek":
                query += "AND epoch >= " + getStartOfThisWeekEpoch();
                break;
            case "lastweek":
                query += "AND epoch >= " + getStartOfLastWeekEpoch() + " AND epoch < " + getStartOfThisWeekEpoch();
                break;
            case "thismonth":
                query += "AND epoch >= " + getStartOfThisMonthEpoch();
                break;
            case "lastmonth":
                query += "AND epoch >= " + getStartOfLastMonthEpoch() + " AND epoch < " + getStartOfThisMonthEpoch();
                break;
            case "thisyear":
                query += "AND epoch >= " + getStartOfThisYearEpoch();
                break;
            case "lastyear":
                query += "AND epoch >= " + getStartOfLastYearEpoch() + " AND epoch < " + getStartOfThisYearEpoch();
                break;
            case "total":
            default:
                // No additional conditions
                break;
        }

        query += " GROUP BY user_id";

        List<ChartData> chartDataList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String totalUsageStr = resultSet.getString("total_usage");

                // Check if total_usage can be parsed to an integer or handle it as needed
                try {
                    if (totalUsageStr != null && !totalUsageStr.isEmpty()) {
                        int totalUsage = Integer.parseInt(totalUsageStr);
                        chartDataList.add(new ChartData(userId, totalUsage));
                    } else {
                        System.out.println("Invalid usage value for user: " + userId);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse usage value for user " + userId + ": " + totalUsageStr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chartDataList;
    }

    private long getStartOfTodayEpoch() {
        LocalDate today = LocalDate.now();
        return today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfYesterdayEpoch() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return yesterday.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfThisWeekEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        return startOfWeek.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfLastWeekEpoch() {
        LocalDate startOfLastWeek = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        return startOfLastWeek.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfThisMonthEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return startOfMonth.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfLastMonthEpoch() {
        LocalDate startOfLastMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        return startOfLastMonth.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfThisYearEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);
        return startOfYear.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfLastYearEpoch() {
        LocalDate startOfLastYear = LocalDate.now().withDayOfYear(1).minusYears(1);
        return startOfLastYear.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}