<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Usage Visualization</title>
    <script src="https://cdn.jsdelivr.net/npm/echarts/dist/echarts.min.js"></script>
    <style>
        p {
            font-size: 14px;
        }
        
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f9f9f9;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px;
            background-color: #0078D4;
            color: white;
        }

        header h1 {
            font-size: 24px;
            margin: 0;
        }

        header .admin-section {
            display: flex;
            align-items: center;
        }

        header .admin-section button, 
        header .admin-section a {
            margin-left: 15px;
            background-color: #fff;
            border: none;
            padding: 8px 12px;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            text-decoration: none;
            color: #0078D4;
        }

        header .admin-section button:hover,
        header .admin-section a:hover {
            background-color: #e0e0e0;
        }

        #container {
            width: 90%;
            height: 600px;
            margin: 30px auto;
            border: 1px solid #ddd;
            background-color: white;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            border-radius: 8px;
        }

        form {
            width: 90%;
            margin: 20px auto;
            text-align: center;
        }

        form label {
            font-size: 18px;
            margin-right: 10px;
        }

        form select {
            font-size: 16px;
            padding: 5px 10px;
            margin-right: 10px;
        }

        form button {
            font-size: 16px;
            padding: 5px 15px;
            background-color: #0078D4;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        form button:hover {
            background-color: #005ea1;
        }
        
        .message-popup {
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            font-size: 16px;
            display: flex;
            align-items: center;
            z-index: 1000;
        }

        .message-popup .icon {
            margin-right: 10px;
            font-size: 24px;
        }

        .message-popup .text {
            flex-grow: 1;
        }

        .message-popup .close-btn {
            background: none;
            border: none;
            color: #fff;
            font-size: 20px;
            cursor: pointer;
        }

        .success {
            background-color: #28a745;
            color: white;
        }

        .error {
            background-color: #dc3545;
            color: white;
        }

        .fade-out {
            animation: fadeOut 2s forwards;
        }

        @keyframes fadeOut {
            0% {
                opacity: 1;
            }
            100% {
                opacity: 0;
            }
        }
    </style>
</head>
<body>
    <header>
        <h1>USAGE-DASHBOARD</h1>
        <div class="admin-section">
            <%
                String username = (session != null) ? (String) session.getAttribute("username") : null;
                if (username != null) {
            %>
                <p>Welcome, <%= username %>!</p>
                <a href="upload.jsp">Upload New Data</a>
                <button onclick="window.location.href='logout'">Logout</button>
            <% } else { %>
                <button onclick="window.location.href='login.jsp'">Admin Login</button>
            <% } %>
        </div>
    </header>

    <div id="container"></div>

    <form id="filterForm" method="post" onsubmit="loadFilteredChart(event)">
        <label for="filter">Select Time Range:</label>
        <select id="filter" name="filter">
            <option value="previousquarter" selected>Previous Quarter</option>
            <option value="previoushalf">Previous Half Year</option>
            <option value="today">Today</option>
            <option value="yesterday">Yesterday</option>
            <option value="last24hours">Last 24 Hours</option>
            <option value="thisweek">This Week</option>
            <option value="lastweek">Last Week</option>
            <option value="thismonth">This Month</option>
            <option value="lastmonth">Last Month</option>
            <option value="thisyear">This Year</option>
            <option value="lastyear">Last Year</option>
            <option value="total">Total</option>
        </select>

        <label for="graphType">Select Graph Type:</label>
        <select id="graphType" onchange="updateGraphType()">
            <option value="line">Line</option>
            <option value="bar">Bar</option>
        </select>
    </form>

    <%
        Integer filesUploaded = (Integer) session.getAttribute("filesUploaded");
        Integer recordsInserted = (Integer) session.getAttribute("recordsInserted");
        Integer recordsUpdated = (Integer) session.getAttribute("recordsUpdated");
        Integer recordsSkipped = (Integer) session.getAttribute("recordsSkipped");
        String message = (String) session.getAttribute("message");
        String messageType = (String) session.getAttribute("messageType");
        if (message != null) {
            String messageClass = "success";
            String icon = "✔️";
            if ("-1".equals(messageType)) {
                messageClass = "error";
                icon = "❌";
            }
    %>
    <div id="statusMessage" class="message-popup <%= messageClass %>">
        <span class="icon"><%= icon %></span>
        <span class="text">
            <b><%= message %></b><br>
            Files Uploaded: <%= filesUploaded != null ? filesUploaded : 0 %>
            , Records Inserted: <%= recordsInserted != null ? recordsInserted : 0 %>
            , Records Updated: <%= recordsUpdated != null ? recordsUpdated : 0 %>
            , Records Skipped: <%= recordsSkipped != null ? recordsSkipped : 0 %>
        </span>
        <button class="close-btn" onclick="closeMessage()">×</button>
    </div>
    <%
            session.removeAttribute("message");
            session.removeAttribute("messageType");
            session.removeAttribute("filesUploaded");
            session.removeAttribute("recordsInserted");
            session.removeAttribute("recordsUpdated");
            session.removeAttribute("recordsSkipped");
        }
    %>

    <script>
        let currentGraphType = 'line';
        let cachedData = null;
        let currentFilter = 'previousquarter';
        let chartInstance = null;

        window.addEventListener('DOMContentLoaded', (event) => {
            const messageElement = document.getElementById('statusMessage');
            if (messageElement) {
                setTimeout(() => {
                    messageElement.classList.add('fade-out');
                }, 3000);
            }
            loadDefaultChart();
        });

        window.addEventListener('resize', function() {
            if (chartInstance) {
                chartInstance.resize();
            }
        });

        function closeMessage() {
            const messageElement = document.getElementById('statusMessage');
            if (messageElement) {
                messageElement.classList.add('fade-out');
            }
        }

        function loadDefaultChart() {
            loadFilteredChart(currentFilter);
        }

        function loadFilteredChart(event) {
            if (event && event.preventDefault) {
                event.preventDefault();
            }

            const filter = typeof event === 'string' ? event : document.getElementById('filter').value;

            if (filter === currentFilter && cachedData) {
                renderChart(cachedData);
                return;
            }

            currentFilter = filter;

            fetch('chart-data', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ filter: filter })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    if (!data || !Array.isArray(data)) {
                        throw new Error('Invalid data format received');
                    }
                    cachedData = data;
                    renderChart(data);
                })
                .catch(error => {
                    console.error('Error loading filtered chart data:', error);
                    alert('Failed to load filtered chart data. Please try again later.');
                });
        }

        function updateGraphType() {
            const graphTypeSelect = document.getElementById('graphType');
            currentGraphType = graphTypeSelect.value;

            if (cachedData) {
                renderChart(cachedData);
            }
        }

        function handleTimeFrameChange() {
            const filterValue = document.getElementById('filter').value;
            loadFilteredChart(filterValue);
        }

        function disposeChart() {
            if (chartInstance) {
                chartInstance.dispose();
            }
        }

        function renderChart(data) {
            disposeChart();
            
            const xValues = data.map(item => item.userId);
            const yValues = data.map(item => item.usageValue);

            chartInstance = echarts.init(document.getElementById('container'));

            const options = {
                title: {
                    text: 'Displaying Usage of Each User',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'shadow' }
                },
                xAxis: {
                    type: 'category',
                    data: xValues,
                    name: 'User ID',
                    nameLocation: 'middle',
                    nameGap: 50,
                    axisLabel: {
                        interval: 0,
                        rotate: 45,
                        fontSize: 10
                    }
                },
                yAxis: {
                    type: 'value',
                    name: 'Usage Value',
                    nameLocation: 'middle',
                    nameGap: 80
                },
                series: [{
                    name: 'Usage Value',
                    type: currentGraphType,
                    data: yValues,
                    smooth: currentGraphType === 'line',
                    itemStyle: { color: '#82EEFD' }
                }]
            };

            chartInstance.setOption(options);
        }

        document.getElementById('filter').addEventListener('change', handleTimeFrameChange);
        document.getElementById('graphType').addEventListener('change', updateGraphType);
    </script>
</body>
</html>