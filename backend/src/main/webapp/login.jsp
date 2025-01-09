<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .login-container {
            background-color: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 350px;
            text-align: center;
        }

        .login-container h2 {
            margin: 0 0 20px;
            font-size: 24px;
            color: #333;
        }

        .login-container .error-message {
            color: red;
            font-size: 14px;
            margin-bottom: 15px;
        }

        .login-container form {
            display: flex;
            flex-direction: column;
        }

        .login-container input {
            margin-bottom: 15px;
            padding: 10px;
            font-size: 14px;
            border: 1px solid #ddd;
            border-radius: 20px;
            width: 93%;
        }

        .login-container button {
            padding: 10px;
            font-size: 16px;
            background-color: #0078D4;
            color: white;
            border: none;
            border-radius: 20px;
            cursor: pointer;
        }

        .login-container button:hover {
            background-color: #005ea1;
        }

        .login-container p {
            margin-top: 20px;
            font-size: 14px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>Admin Login</h2>
        <% if (request.getAttribute("error") != null) { %>
            <div class="error-message"><%= request.getAttribute("error") %></div>
        <% } %>
        <form method="post" action="auth">
            <input type="text" name="username" placeholder="Enter Username" required>
            <input type="password" name="password" placeholder="Enter Password" required>
            <button type="submit">Login</button>
        </form>
        <p>Only authorized users can log in</p>
    </div>
</body>
</html>