package com.app.Livetracker.config;

import com.app.Livetracker.service.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class JwtWebSocketInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public JwtWebSocketInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String query = request.getURI().getQuery();

        if (query == null) {
            return false;
        }

        String token = null;

        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }

        if (token == null || !jwtService.isTokenValid(token)) {
            return false;
        }

        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        attributes.put("userId", userId);
        attributes.put("role", role);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}
