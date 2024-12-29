package com.example.controller;

import com.example.service.UsageDataService;
import com.example.util.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;

@WebServlet(urlPatterns = "/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private UsageDataService usageDataService;

    @Override
    public void init() {
        try {
            usageDataService = new UsageDataService();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer adminId = (Integer) session.getAttribute("adminId");
        System.out.println(adminId);
        if (adminId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String duplicateAction = request.getParameter("duplicateAction");
        System.out.println(duplicateAction);
        
        Collection<Part> fileParts = request.getParts();
        if (fileParts.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("No files uploaded!");
            return;
        }

        for (Part filePart : fileParts) {
            if (filePart.getName().equals("files") && filePart.getSize() > 0) {
                try (InputStream fileContent = filePart.getInputStream()) {
                    usageDataService.processCSV(fileContent, adminId, filePart.getSubmittedFileName(),duplicateAction);
                    session.setAttribute("message", "File Upload Success");
                    session.setAttribute("messageType", "1");
                } catch (Exception e) {
                	session.setAttribute("message", "File Upload Failed");
                	session.setAttribute("messageType", "-1");
                    response.setStatus(500);
                    response.getWriter().write("Error processing file: " + e.getMessage());
                    return;
                }
            }
        }
        response.sendRedirect("index.jsp");
    }
}