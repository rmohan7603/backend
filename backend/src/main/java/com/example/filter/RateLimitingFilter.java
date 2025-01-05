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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebFilter("/*")
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(RateLimitingFilter.class);

    private static final long TIME_WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS = 60;

    private final Map<String, ConcurrentLinkedQueue<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String username = (String) request.getSession().getAttribute("username");
        if (username != null) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();

        boolean allowed = isAllowed(clientIp, currentTime);

        if (!allowed) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return;
        }

        logger.info("Request allowed for IP: {}", clientIp);
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String clientIp, long currentTime) {
        requestTimestamps.putIfAbsent(clientIp, new ConcurrentLinkedQueue<>());

        ConcurrentLinkedQueue<Long> timestamps = requestTimestamps.get(clientIp);

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peek() < currentTime - TIME_WINDOW_MS) {
                timestamps.poll();
            }

            if (timestamps.size() < MAX_REQUESTS) {
                timestamps.offer(currentTime);
                return true;
            }

            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}