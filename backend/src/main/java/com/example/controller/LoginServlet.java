package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import com.example.service.AdminService;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private AdminService adminService;

    @Override
    public void init() {
        try {
            adminService = new AdminService();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            // Authenticate user and retrieve adminId
            int adminId = adminService.authenticateAndGetAdminId(username, password);
            if (adminId > 0) { // Successful authentication
                HttpSession session = request.getSession();
                session.setAttribute("adminId", adminId);
                session.setAttribute("username", username);
                response.sendRedirect("index.jsp");
            } else {
                request.setAttribute("error", "Invalid username or password. Please try again.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            throw new ServletException("Error during login", e);
        }
    }
}
