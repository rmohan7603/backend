package com.example.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.example.model.ChartData;
import com.example.service.UsageDataService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/chart-data")
public class ChartDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest("total", response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Parse JSON request body to get filter
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
            filter = "total"; // Default filter
        }

        try {
            UsageDataService usageDataService = new UsageDataService();
            List<ChartData> chartData = usageDataService.getChartData(filter);

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(chartData));
            System.out.println("Chart data sent: " + new Gson().toJson(chartData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An error occurred while fetching chart data.\"}");
            e.printStackTrace();
        }
    }
}