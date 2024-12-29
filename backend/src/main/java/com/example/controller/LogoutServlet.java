package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@WebServlet(urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {
	
    private static final Logger logger = LogManager.getLogger(LogoutServlet.class);
    
    @Override
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
}