package com.app.Livetracker.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NearestRiderResponseDTO {

    private UUID riderId;
    private double lat;
    private double lng;
}
