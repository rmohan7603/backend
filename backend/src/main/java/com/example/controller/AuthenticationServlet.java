package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.service.AdminService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(urlPatterns = "/auth")
public class AuthenticationServlet extends HttpServlet {
    private AdminService adminService;

    private static final Logger logger = LogManager.getLogger(AuthenticationServlet.class);

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long COOLDOWN_PERIOD_MS = 3 * 60 * 1000;
    private static final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    @Override
    public void init() {
        try {
            adminService = new AdminService();
        } catch (Exception e) {
            logger.error("Error initializing AdminService", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String clientIp = request.getRemoteAddr();

        long currentTime = System.currentTimeMillis();
        LoginAttemptInfo attemptInfo = loginAttempts.compute(clientIp, (key, oldInfo) -> {
            if (oldInfo == null || currentTime - oldInfo.lastFailedTime > COOLDOWN_PERIOD_MS) {
                return new LoginAttemptInfo(0, 0);
            }
            return oldInfo;
        });

        if (attemptInfo.failedAttempts >= MAX_FAILED_ATTEMPTS && currentTime - attemptInfo.lastFailedTime < COOLDOWN_PERIOD_MS) {
            long waitTime = COOLDOWN_PERIOD_MS - (currentTime - attemptInfo.lastFailedTime);
            request.setAttribute("error", "Too many failed login attempts. Please try again in " + (waitTime / 1000) + " seconds.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            int adminId = adminService.authenticateAndGetAdminId(username, password);
            if (adminId > 0) {
                loginAttempts.remove(clientIp);
                HttpSession session = request.getSession();
                session.setAttribute("adminId", adminId);
                session.setAttribute("username", username);
                logger.info("Login successful for user: {}", username);
                response.sendRedirect("index.jsp");
            } else {
                attemptInfo.failedAttempts++;
                attemptInfo.lastFailedTime = currentTime;
                logger.warn("Login failed for user: {}", username);
                request.setAttribute("error", "Invalid username or password. Please try again.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.error("Error during login", e);
            throw new ServletException("Error during login", e);
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("User is logging out. Invalidating session: {}", session.getId());
            session.invalidate();
        } else {
            logger.warn("No session found for user. Logout attempt failed.");
        }
        response.sendRedirect("index.jsp");
    }

    private static class LoginAttemptInfo {
        int failedAttempts;
        long lastFailedTime;

        LoginAttemptInfo(int failedAttempts, long lastFailedTime) {
            this.failedAttempts = failedAttempts;
            this.lastFailedTime = lastFailedTime;
        }
    }
}