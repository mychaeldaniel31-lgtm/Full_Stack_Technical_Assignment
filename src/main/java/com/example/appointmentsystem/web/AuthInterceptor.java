package com.example.appointmentsystem.web;

import com.example.appointmentsystem.service.SessionService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessions;
    public AuthInterceptor(SessionService sessions) { this.sessions = sessions; }

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicEndpoint(request)) return true;
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) { unauthorized(response); return false; }
        try {
            request.setAttribute("user", sessions.requireUser(authorization.substring("Bearer ".length())));
            return true;
        } catch (org.springframework.web.server.ResponseStatusException exception) { unauthorized(response); return false; }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return (request.getRequestURI().equals("/api/auth/login") && "POST".equals(request.getMethod()))
                || (request.getRequestURI().equals("/api/users") && "POST".equals(request.getMethod()));
    }
    private void unauthorized(HttpServletResponse response) { response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); }
}
