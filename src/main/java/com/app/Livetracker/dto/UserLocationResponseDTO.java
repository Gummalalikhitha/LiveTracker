package com.app.Livetracker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLocationResponseDTO {
    private double lat;
    private double lng;
}
