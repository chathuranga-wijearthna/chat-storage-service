package com.assignment.chatstorage.security;

import com.assignment.chatstorage.constants.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${API_KEY:change-me-please}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/actuator/health")
                || p.startsWith("/v3/api-docs")
                || p.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String provided = request.getHeader(HeaderConstants.API_KEY);

        if (provided == null || !provided.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("Invalid API key");
            return;
        }
        chain.doFilter(request, response);
    }
}
