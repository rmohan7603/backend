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
            width: 95%;
            height: 850px;
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
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="error-message"><%= request.getAttribute("error") %></div>
        <% } %>
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
            <option value="line">Line Chart</option>
            <!-- <option value="bar">Bar Chart</option> -->
            <option value="area">Area Chart</option>
            <!-- <option value="scatter">Scatter Chart</option> -->
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
    	    preserveFilters();
        	loadDefaultChart();
    	});

	    window.addEventListener('resize', function () {
    	    if (chartInstance) {
        	    chartInstance.resize();
        	}
    	});
	    
	    window.addEventListener('load', () => {
            const statusMessage = document.getElementById('statusMessage');
            if (statusMessage) {
                setTimeout(() => {
                    closeMessage();
                }, 3000);
            }
        });
	    
	    function closeMessage() {
            const statusMessage = document.getElementById('statusMessage');
            if (statusMessage) {
                statusMessage.classList.add('fade-out');
                setTimeout(() => {
                    statusMessage.style.display = 'none';
                }, 2000);
            }
        }

    	function preserveFilters() {
        	const savedGraphType = sessionStorage.getItem('graphType') || 'line';
        	const savedFilter = sessionStorage.getItem('filter') || 'previousquarter';
        	document.getElementById('graphType').value = savedGraphType;
        	document.getElementById('filter').value = savedFilter;
        	currentGraphType = savedGraphType;
        	currentFilter = savedFilter;
    	}

	    function saveFilters() {
    	    sessionStorage.setItem('graphType', currentGraphType);
        	sessionStorage.setItem('filter', currentFilter);
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
    	    saveFilters();

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
    	            if (data.status !== 'success' || !data.data || !Array.isArray(data.data)) {
    	                throw new Error('Invalid data format received');
    	            }
    	            cachedData = data.data;
    	            renderChart(cachedData);
    	        })
    	        .catch(error => {
    	            console.error('Error loading filtered chart data:', error);
    	        });
    	}

    	function updateGraphType() {
        	currentGraphType = document.getElementById('graphType').value;
        	saveFilters();

	        if (cachedData) {
	            renderChart(cachedData);
        	}
    	}

    	function disposeChart() {
        	if (chartInstance) {
            	chartInstance.dispose();
        	}
    	}
    	
    	const utils = {
    			stringToColor: function (str) {
    			    let hash = 0;
    			    for (let i = 0; i < str.length; i++) {
    			        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    			    }

    			    const hue = Math.abs(hash) % 360;

    			    const saturation = 70;
    			    const lightness = 50;

    			    return "hsl("+hue+","+saturation+"%,"+lightness+"%)";
    			},

        	processChartData: function (data) {
            	const labels = [...new Set(data.map((item) => item.label))];
            	const userIds = [...new Set(data.map((item) => item.userId))];

            	const series = userIds.map((userId) => {
                	const userData = labels.map((label) => {
                    	const point = data.find((d) => d.userId === userId && d.label === label);
                    	return point ? point.totalUsage : 0;
                	});
                	return {
                    	name: userId,
                    	smooth: true,
                    	data: userData,
                    	color: this.stringToColor(userId),
                    	label: {
                        	show: true,
                        	position: 'top'
                      	},
                    	emphasis: {
                        	focus: 'series'
                    	}
                	};
            	});

            	return { labels, series };
        	},
    	};

    	function renderChartBase(labels, series, options = {}) {
        	return {
            	title: {
                	text: `TREND ANALYSIS`,
                	left: "center",
                	top: 10,
            	},
            	toolbox: {
                	feature: {
                  		saveAsImage: {}
                	}
              	},
            	tooltip: {
                	trigger: "axis",
                	axisPointer: { type: "cross", label: { backgroundColor: "#6a7985" } },
            	},
            	legend: {
                	data: series.map((s) => s.name),
                	top: "bottom",
                	selectedMode: "multiple",
            	},
            	grid: { left: "3%", right: "4%", bottom: "10%", top: "15%", containLabel: true },
            	dataZoom: [
                	{ type: "inside", start: 0, end: 100 },
            	],
            	xAxis: {
                	type: "category",
                	name: "Time Frame",
                	nameLocation: "middle",
                	nameGap: 50,
                	boundaryGap: false,
                	data: labels,
            	},
            	yAxis: {
                	type: "value",
                	name: "Usage Value",
                	nameLocation: "middle",
                	nameGap: 50,
            	},
            	series: series.map((s) => ({
                	...s,
                	type: "line",
                	areaStyle: options.areaStyle ? {} : null,
            	})),
        	};
    	}

    	function renderChart(data) {
        	disposeChart();

        	if (!data || data.length === 0) {
            	document.getElementById("container").innerHTML = `
                	<div style="display: flex; align-items: center; justify-content: center; height: 100px; width: 250px; border: 2px solid #ccc; border-radius: 10px; background-color: #f9f9f9; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: auto;">
                    	<p style="font-size: 18px; color: #666; margin: 0;">NO DATA AVAILABLE</p>
                	</div>`;
            	return;
        	}

        	const { labels, series } = utils.processChartData(data);

        	const options = {
            	chartType: currentGraphType === "area" ? "Stacked Area Chart" : "Stacked Line Chart",
            	areaStyle: currentGraphType === "area",
        	};

        	const chartOptions = renderChartBase(labels, series, options);

        	chartInstance = echarts.init(document.getElementById("container"));
        	chartInstance.setOption(chartOptions);
    	}

    	document.getElementById('filter').addEventListener('change', loadFilteredChart);
    	document.getElementById('graphType').addEventListener('change', updateGraphType);

    </script>

</body>
</html>