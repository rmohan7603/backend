package com.example.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.example.model.ChartData;
import com.example.service.UsageDataService;
import com.example.util.DatabaseConnection;
import com.example.util.QueryLoader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet("/chart-data")
public class ChartDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger(ChartDataServlet.class);
    
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("Initializing ChartDataServlet...");

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            String[] createTableQueries = {
                QueryLoader.getQuery("create_table_admin"),
                QueryLoader.getQuery("create_table_upload_metadata"),
                QueryLoader.getQuery("create_table_usage_table")
            };

            for (String query : createTableQueries) {
                if (query != null) {
                    statement.execute(query);
                    logger.info("Executed table creation query: {}", query);
                } else {
                    logger.warn("Query not found for table creation.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error during table creation", e);
            throw new ServletException("Failed to initialize database tables", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	logger.info("Handling GET request for chart data.");
        processRequest("total", response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	logger.info("Handling POST request for chart data.");
    	
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }
        String json = jsonBuffer.toString();
        String filter = new Gson().fromJson(json, JsonObject.class).get("filter").getAsString();

        processRequest(filter, response);
    }

    private void processRequest(String filter, HttpServletResponse response) throws IOException {
        if (filter == null || filter.isEmpty()) {
            filter = "total";
        }

        try {
            UsageDataService usageDataService = new UsageDataService();
            List<ChartData> chartData = usageDataService.getChartData(filter);

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(chartData));
            System.out.println("Chart data sent: " + new Gson().toJson(chartData));
        } catch (Exception e) {
        	logger.error("Error processing request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An error occurred while fetching chart data.\"}");
        }
    }
}