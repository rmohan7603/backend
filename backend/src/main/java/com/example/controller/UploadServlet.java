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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@WebServlet(urlPatterns = "/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private UsageDataService usageDataService;
    
    private static final Logger logger = LogManager.getLogger(UploadServlet.class);
    
    @Override
    public void init() {
        try {
            usageDataService = new UsageDataService();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error initializing UsageDataService", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer adminId = (Integer) session.getAttribute("adminId");
        logger.info("Received upload request. Admin ID: {}", adminId);
        
//        System.out.println(adminId);
        if (adminId == null) {
            logger.warn("Admin not logged in. Redirecting to login page.");
            response.sendRedirect("login.jsp");
            return;
        }

        String duplicateAction = request.getParameter("duplicateAction");
//        System.out.println(duplicateAction);
        
        Collection<Part> fileParts = request.getParts();
        if (fileParts.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("No files uploaded!");
            return;
        }

        session.setAttribute("filesUploaded", 0);
        session.setAttribute("recordsInserted", 0);
        session.setAttribute("recordsUpdated", 0);
        session.setAttribute("recordsSkipped", 0);

        for (Part filePart : fileParts) {
            if (filePart.getName().equals("files") && filePart.getSize() > 0) {
                try (InputStream fileContent = filePart.getInputStream()) {
                	usageDataService.processCSV(fileContent, adminId, filePart.getSubmittedFileName(),duplicateAction, session);
                    session.setAttribute("message", "File Upload Success");
                    session.setAttribute("messageType", "1");
                    logger.info("File upload success for file: {}", filePart.getSubmittedFileName());
                } catch (Exception e) {
                	session.setAttribute("message", "File Upload Failed");
                	session.setAttribute("messageType", "-1");
                    response.setStatus(500);
                    response.getWriter().write("Error processing file: " + e.getMessage());
                    logger.error("Error processing file: {}", filePart.getSubmittedFileName(), e);
                    return;
                }
            }
        }
        response.sendRedirect("index.jsp");
    }
}