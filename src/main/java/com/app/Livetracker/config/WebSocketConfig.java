package com.app.Livetracker.config;

import com.app.Livetracker.websocket.LocationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LocationWebSocketHandler locationWebSocketHandler;
    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;

    public WebSocketConfig(
            LocationWebSocketHandler handler,
            JwtWebSocketInterceptor interceptor) {
        this.locationWebSocketHandler = handler;
        this.jwtWebSocketInterceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(locationWebSocketHandler, "/ws/location")
                .addInterceptors(jwtWebSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
