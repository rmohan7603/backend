package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import com.example.service.AdminService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private AdminService adminService;
    
    private static final Logger logger = LogManager.getLogger(LoginServlet.class);

    @Override
    public void init() {
        try {
            adminService = new AdminService();
        } catch (SQLException | ClassNotFoundException e) {
        	logger.error("Error initializing AdminService", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            int adminId = adminService.authenticateAndGetAdminId(username, password);
            if (adminId > 0) {
                HttpSession session = request.getSession();
                session.setAttribute("adminId", adminId);
                session.setAttribute("username", username);
                logger.info("Login successful for user: {}", username);
                response.sendRedirect("index.jsp");
            } else {
                logger.warn("Login failed for user: {}", username);
                request.setAttribute("error", "Invalid username or password. Please try again.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.error("Error during login", e);
            throw new ServletException("Error during login", e);
        }
    }
}
