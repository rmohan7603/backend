package com.example.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@WebFilter("/*")
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(RateLimitingFilter.class);
    private static final long REQUEST_LIMIT_TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_REQUESTS_PER_WINDOW = 25;
    private static final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String isAdmin = (String) request.getSession().getAttribute("username");
        //System.out.println(isAdmin);
        
        if (isAdmin!=null) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        RequestInfo info = requestCounts.compute(clientIp, (key, oldInfo) -> {
            if (oldInfo == null || currentTime - oldInfo.windowStartTime > REQUEST_LIMIT_TIME_WINDOW_MS) {
                return new RequestInfo(1, currentTime);
            }
            oldInfo.requestCount++;
            System.out.println(oldInfo.requestCount+" "+oldInfo.windowStartTime);
            return oldInfo;
        });

        if (info.requestCount > MAX_REQUESTS_PER_WINDOW) {
        	logger.warn("Rate limit exceeded for IP: {} - Request count: {}", clientIp, info.requestCount);
        	
        	response.setStatus(429);
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return;
        }
        
        logger.info("Request allowed for IP: {} - Request count: {}", clientIp, info.requestCount);
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    private static class RequestInfo {
        int requestCount;
        long windowStartTime;

        RequestInfo(int requestCount, long windowStartTime) {
            this.requestCount = requestCount;
            this.windowStartTime = windowStartTime;
        }
    }
}