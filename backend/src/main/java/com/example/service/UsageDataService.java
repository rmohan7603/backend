package com.example.service;

import com.example.dao.UploadMetadataDAO;
import com.example.dao.UsageDataDAO;
import com.example.model.ChartData;
import com.example.model.UsageData;
import com.example.util.CSVProcessor;
import com.example.util.DatabaseConnection;
import com.example.util.QueryLoader;

import jakarta.servlet.http.HttpSession;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;

public class UsageDataService {
    private UsageDataDAO usageDataDAO;
    private UploadMetadataDAO uploadMetadataDAO;

    public UsageDataService() throws SQLException, ClassNotFoundException {
        usageDataDAO = new UsageDataDAO();
        uploadMetadataDAO = new UploadMetadataDAO();
    }
    
    public void processCSV(InputStream fileContent, int adminId, String fileName, String action, HttpSession session) throws Exception {
        List<UsageData> dataList = CSVProcessor.parseCSV(fileContent);
        List<UsageData> duplicates = usageDataDAO.findDuplicates(dataList);

        List<UsageData> insertList = new ArrayList<>();
        List<UsageData> updateList = new ArrayList<>();
        for (UsageData data : dataList) {
            if (duplicates.contains(data)) {
                updateList.add(data);
            } else {
                insertList.add(data);
            }
        }
        if(action.equals("skip"))
        	updateList.clear();
        
        usageDataDAO.bulkInsertOrUpdate(insertList, updateList);
        uploadMetadataDAO.recordUpload(adminId, fileName, insertList.size(), updateList.size(), duplicates.size() - updateList.size());
        
        int filesUploaded=(Integer)session.getAttribute("filesUploaded")+1;
        int recordsInserted=(Integer)session.getAttribute("recordsInserted")+insertList.size();
        int recordsUpdated=(Integer)session.getAttribute("recordsUpdated")+updateList.size();
        int recordsSkipped=(Integer)session.getAttribute("recordsSkipped")+duplicates.size() - updateList.size();
        
        session.setAttribute("filesUploaded", filesUploaded);
        session.setAttribute("recordsInserted", recordsInserted);
        session.setAttribute("recordsUpdated", recordsUpdated);
        session.setAttribute("recordsSkipped", recordsSkipped);
    }

    public List<ChartData> getChartData(String filter) throws ClassNotFoundException {
        //System.out.println("Filter: " + filter);
        
        String query = QueryLoader.getQuery("getChartDataBase");
        long currentEpoch = System.currentTimeMillis() / 1000;
        
        if (filter == null || filter.trim().isEmpty()) {
            filter = "previousquarter";
        }
        
        long[] timeRange = getTimeRange(filter, currentEpoch);
        
        if (timeRange == null) {
            throw new IllegalArgumentException("Invalid time filter specified.");
        }
        
        query += " ";
        query += QueryLoader.getQuery("getChartDataGroupBy");

        //System.out.println("Executing query: " + query);

        List<ChartData> chartDataList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, timeRange[0]); // start time
            statement.setLong(2, timeRange[1]); // end time

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    String totalUsageStr = resultSet.getString("total_usage");

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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return chartDataList;
    }

    private long[] getTimeRange(String filter, long currentEpoch) {
        switch (filter.toLowerCase()) {
            case "today":
                return new long[] {getStartOfTodayEpoch(), currentEpoch};
            case "yesterday":
                return new long[] {getStartOfYesterdayEpoch(), getStartOfTodayEpoch()};
            case "last24hours":
                return new long[] {currentEpoch - 86400, currentEpoch};
            case "thisweek":
                return new long[] {getStartOfThisWeekEpoch(), currentEpoch};
            case "lastweek":
                return new long[] {getStartOfLastWeekEpoch(), getStartOfThisWeekEpoch()};
            case "thismonth":
                return new long[] {getStartOfThisMonthEpoch(), currentEpoch};
            case "lastmonth":
                return new long[] {getStartOfLastMonthEpoch(), getStartOfThisMonthEpoch()};
            case "thisyear":
                return new long[] {getStartOfThisYearEpoch(), currentEpoch};
            case "lastyear":
                return new long[] {getStartOfLastYearEpoch(), getStartOfThisYearEpoch()};
            case "previoushalf":
                return new long[] {getStartOfPreviousHalfYearEpoch(), getStartOfCurrentHalfYearEpoch()};
            case "total":
                return new long[] {0, currentEpoch};
            case "previousquarter":
            default:
                return new long[] {getStartOfPreviousQuarterEpoch(), getStartOfCurrentQuarterEpoch()};
        }
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
    
    private long getStartOfCurrentQuarterEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate startOfQuarter = today.withDayOfMonth(1)
            .with(IsoFields.DAY_OF_QUARTER, 1);
        return startOfQuarter.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfPreviousQuarterEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate startOfPreviousQuarter = today.withDayOfMonth(1)
            .with(IsoFields.DAY_OF_QUARTER, 1)
            .minusMonths(3);
        return startOfPreviousQuarter.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfCurrentHalfYearEpoch() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        LocalDate startOfHalf = today.withDayOfMonth(1)
            .withMonth(currentMonth <= 6 ? 1 : 7);
        return startOfHalf.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfPreviousHalfYearEpoch() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        LocalDate startOfPreviousHalf = today.withDayOfMonth(1)
            .withMonth(currentMonth <= 6 ? 7 : 1)
            .minusMonths(6);
        return startOfPreviousHalf.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}