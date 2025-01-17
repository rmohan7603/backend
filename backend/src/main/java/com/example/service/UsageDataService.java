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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsageDataService {
    private UsageDataDAO usageDataDAO;
    private UploadMetadataDAO uploadMetadataDAO;

    public UsageDataService() throws SQLException, ClassNotFoundException {
        usageDataDAO = new UsageDataDAO();
        uploadMetadataDAO = new UploadMetadataDAO();
    }
    
    public void processCSV(InputStream fileContent, int adminId, String fileName, String action, HttpSession session) throws Exception {
        try (Stream<UsageData> dataStream = CSVProcessor.parseCSV(fileContent)) {
            List<UsageData> dataList = dataStream.collect(Collectors.toList());

            int previousCount = usageDataDAO.getRowNumber();
            
            usageDataDAO.bulkInsertOrUpdate(dataList, action);
            
            int currentCount = usageDataDAO.getRowNumber();
            
            int inserted = currentCount-previousCount;
            int updated = "update".equals(action)?dataList.size()-inserted:0;
            int skipped = "skip".equals(action)?dataList.size()-inserted:0;
            
            //System.out.println(previousCount+" "+currentCount+" "+inserted+" "+updated+" "+skipped);
            
            uploadMetadataDAO.recordUpload(adminId, fileName, 
            		inserted, updated, skipped);

            session.setAttribute("filesUploaded", (Integer) session.getAttribute("filesUploaded") + 1);
            session.setAttribute("recordsInserted", (Integer) session.getAttribute("recordsInserted") + inserted);
            session.setAttribute("recordsUpdated", (Integer) session.getAttribute("recordsUpdated") + updated);
            session.setAttribute("recordsSkipped", (Integer) session.getAttribute("recordsSkipped") + skipped);
        }
    }
    
    public List<ChartData> getChartData(String filter) throws ClassNotFoundException {
        String baseQuery = QueryLoader.getQuery("getChartDataBase");

        String labelQuery = getLabelQuery(filter);
        String groupQuery = getGroupQuery(filter);
        String fullQuery = baseQuery.replace("{label_query}", labelQuery) + " " + groupQuery;

        long currentEpoch = System.currentTimeMillis() / 1000;
        long[] timeRange = getTimeRange(filter, currentEpoch);

        if (timeRange == null) {
            throw new IllegalArgumentException("Invalid time range for the specified filter.");
        }

        List<ChartData> chartDataList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(fullQuery)) {

            statement.setLong(1, timeRange[0]);
            statement.setLong(2, timeRange[1]);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                	String label = resultSet.getString("label");
                    String userId = resultSet.getString("user_id");
                    int totalUsage = resultSet.getInt("total_usage");
                    chartDataList.add(new ChartData(label, userId, totalUsage));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chartDataList;
    }

    private String getLabelQuery(String filter) {
        switch (filter.toLowerCase()) {
            case "today":
            case "yesterday":
            case "last24hours":
                return "DATE_FORMAT(FROM_UNIXTIME(epoch), '%r')";
            case "thisweek":
            case "lastweek":
            case "thismonth":
            case "lastmonth":
            case "thisyear":
            case "lastyear":
            case "previousquarter":
            case "previoushalf":
            case "total":
                return "DATE_FORMAT(DATE(FROM_UNIXTIME(epoch)),GET_FORMAT(DATE,'EUR'))";
            default:
                throw new IllegalArgumentException("Unsupported filter for label query.");
        }
    }

    private String getGroupQuery(String filter) {
        switch (filter.toLowerCase()) {
            case "today":
            case "yesterday":
            case "last24hours":
                return "group by user_id, DATE_FORMAT(FROM_UNIXTIME(epoch), '%r') order by DATE_FORMAT(FROM_UNIXTIME(epoch), '%r')";
            case "thisweek":
            case "lastweek":
            case "thismonth":
            case "lastmonth":
            case "thisyear":
            case "lastyear":
            case "previousquarter":
            case "previoushalf":
            case "total":
                return "group by user_id, DATE_FORMAT(DATE(FROM_UNIXTIME(epoch)),GET_FORMAT(DATE,'EUR')) order by DATE_FORMAT(DATE(FROM_UNIXTIME(epoch)),GET_FORMAT(DATE,'EUR'))";
            default:
                throw new IllegalArgumentException("Unsupported filter for grouping.");
        }
    }

    private long[] getTimeRange(String filter, long currentEpoch) {
        switch (filter.toLowerCase()) {
            case "today":
                return new long[]{getStartOfTodayEpoch(), currentEpoch};
            case "yesterday":
                return new long[]{getStartOfYesterdayEpoch(), getStartOfTodayEpoch()};
            case "last24hours":
                return new long[]{currentEpoch - 86400, currentEpoch};
            case "thisweek":
                return new long[]{getStartOfThisWeekEpoch(), currentEpoch};
            case "lastweek":
                return new long[]{getStartOfLastWeekEpoch(), getStartOfThisWeekEpoch()};
            case "thismonth":
                return new long[]{getStartOfThisMonthEpoch(), currentEpoch};
            case "lastmonth":
                return new long[]{getStartOfLastMonthEpoch(), getStartOfThisMonthEpoch()};
            case "thisyear":
                return new long[]{getStartOfThisYearEpoch(), currentEpoch};
            case "lastyear":
                return new long[]{getStartOfLastYearEpoch(), getStartOfThisYearEpoch()};
            case "previousquarter":
                return new long[]{getStartOfPreviousQuarterEpoch(), getStartOfCurrentQuarterEpoch()};
            case "previoushalf":
                return new long[]{getStartOfPreviousHalfYearEpoch(), getStartOfCurrentHalfYearEpoch()};
            case "total":
                return new long[]{0, currentEpoch};
            default:
                throw new IllegalArgumentException("Invalid filter specified.");
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

    private long getStartOfPreviousQuarterEpoch() {
        LocalDate today = LocalDate.now();
        int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
        int previousQuarter = currentQuarter == 1 ? 4 : currentQuarter - 1;
        int yearOfPreviousQuarter = currentQuarter == 1 ? today.getYear() - 1 : today.getYear();
        LocalDate startOfPreviousQuarter = LocalDate.of(yearOfPreviousQuarter, (previousQuarter - 1) * 3 + 1, 1);
        return startOfPreviousQuarter.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfCurrentQuarterEpoch() {
        LocalDate today = LocalDate.now();
        int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
        LocalDate startOfQuarter = LocalDate.of(today.getYear(), (currentQuarter - 1) * 3 + 1, 1);
        return startOfQuarter.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfCurrentHalfYearEpoch() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        LocalDate startOfHalf = LocalDate.of(today.getYear(), currentMonth <= 6 ? 1 : 7, 1);
        return startOfHalf.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private long getStartOfPreviousHalfYearEpoch() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int startMonthOfPreviousHalf = currentMonth <= 6 ? 7 : 1;
        int yearOfPreviousHalf = currentMonth <= 6 ? today.getYear() - 1 : today.getYear();
        LocalDate startOfPreviousHalf = LocalDate.of(yearOfPreviousHalf, startMonthOfPreviousHalf, 1);
        return startOfPreviousHalf.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}
