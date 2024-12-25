<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Upload Data</title>
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

        .upload-container {
            background-color: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 400px;
            text-align: center;
        }

        .upload-container h2 {
            margin: 0 0 20px;
            font-size: 24px;
            color: #333;
        }

        .upload-container form {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .upload-container input[type="file"] {
            margin-bottom: 15px;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 0px;
            font-size: 14px;
            width: 100%;
        }

        .upload-container button {
            padding: 10px;
            font-size: 16px;
            background-color: #0078D4;
            color: white;
            border: none;
            border-radius: 20px;
            cursor: pointer;
            width: 100%;
        }

        .upload-container button:hover {
            background-color: #005ea1;
        }

        .upload-container p {
            margin-top: 20px;
            font-size: 14px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="upload-container">
        <h2>Upload CSV File</h2>
        <form method="post" action="upload" enctype="multipart/form-data">
            <input type="file" name="files" accept=".csv" multiple required>
            <button type="submit">Upload</button>
        </form>
        <p>Upload CSV files containing usage data</p>
    <br><a href="index.jsp" style="font-size:20px">Go Back to Dashboard</a>
    </div>
</body>
</html>