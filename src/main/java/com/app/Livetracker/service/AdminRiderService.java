package com.app.Livetracker.service;

import com.app.Livetracker.dto.NearestRiderResponseDTO;
import com.app.Livetracker.entity.Order;
import com.app.Livetracker.entity.OrderStatus;
import com.app.Livetracker.entity.Role;
import com.app.Livetracker.entity.User;
import com.app.Livetracker.repository.OrderRepository;
import com.app.Livetracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRiderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // =====================================================
    // Restaurant fixed location
    // =====================================================
    private static final double REST_LAT = 12.9716;
    private static final double REST_LNG = 77.5946;

    // =====================================================
    // ADMIN: FETCH NEAREST AVAILABLE RIDERS
    // =====================================================
    public List<NearestRiderResponseDTO> getNearestAvailableRiders() {

        // Fetch all active riders
        List<User> activeRiders =
                userRepository.findByRoleAndIsActive(Role.RIDER, true);

        if (activeRiders.isEmpty()) {
            return Collections.emptyList();
        }

        // Find busy rider IDs
        Set<UUID> busyRiderIds =
                orderRepository.findByStatusIn(
                                List.of(
                                        OrderStatus.RIDER_ACCEPTED,
                                        OrderStatus.PICKED_UP
                                )
                        )
                        .stream()
                        .map(Order::getRiderId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        // 3️⃣ Filter free riders + fetch Redis location
        return activeRiders.stream()
                .filter(rider -> !busyRiderIds.contains(rider.getId()))
                .map(this::mapToNearestRider)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(r ->
                        distanceKm(
                                REST_LAT,
                                REST_LNG,
                                r.getLat(),
                                r.getLng()
                        )
                ))
                .limit(10)
                .collect(Collectors.toList());
    }

    // =====================================================
    // MAP REDIS LOCATION → DTO
    // =====================================================
    private NearestRiderResponseDTO mapToNearestRider(User rider) {

        try {
            String key = "user:location:" + rider.getId();
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                return null; // rider not online / no location
            }

            Map<String, Object> location =
                    objectMapper.readValue(value, Map.class);

            double lat = ((Number) location.get("lat")).doubleValue();
            double lng = ((Number) location.get("lng")).doubleValue();

            return NearestRiderResponseDTO.builder()
                    .riderId(rider.getId())
                    .lat(lat)
                    .lng(lng)
                    .build();

        } catch (Exception e) {
            return null; // malformed redis data
        }
    }

    // =====================================================
    // HAVERSINE DISTANCE (KM)
    // =====================================================
    private double distanceKm(
            double lat1, double lon1,
            double lat2, double lon2) {

        double R = 6371; // Earth radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
