package com.example.service;

import java.sql.SQLException;

import com.example.dao.AdminDAO;

public class AdminService {
    private AdminDAO adminDAO;

    public AdminService() throws SQLException, ClassNotFoundException {
        adminDAO = new AdminDAO();
    }

    public int authenticateAndGetAdminId(String username, String password) throws SQLException {
        String storedPassword = adminDAO.getPasswordByUsername(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            return adminDAO.getAdminIdByUsername(username);
        }
        return -1;
    }
}