package com.app.Livetracker.controller;

import com.app.Livetracker.dto.LocationUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserLocationController {

    private final RedisTemplate<String, String> redisTemplate;
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/location")
    public void saveUserLocation(
            @AuthenticationPrincipal UUID userId,
            @RequestBody LocationUpdateDTO location) throws Exception {

        Map<String, Object> payload = new HashMap<>();
        payload.put("lat",location.getLat());
        payload.put("lng",location.getLng());
        payload.put("role", "USER");
        payload.put("timestamp", Instant.now().getEpochSecond());

        String redisKey = "user:location:" + userId;
        String redisValue = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(payload);

        redisTemplate.opsForValue().set(redisKey, redisValue);
    }
}
