package com.chatapp.pingchat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LocalOnlyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Protect the admin/server-control page and its endpoints
        boolean isProtectedPath =
                uri.equals("/")
                        || uri.equals("/index.html")
                        || uri.startsWith("/server/");

        if (isProtectedPath) {
            String remoteAddr = request.getRemoteAddr();

            boolean isLocal = "127.0.0.1".equals(remoteAddr)
                    || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                    || "::1".equals(remoteAddr);

            if (!isLocal) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: server UI is local-only");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}