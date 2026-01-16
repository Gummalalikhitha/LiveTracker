package com.app.Livetracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRiderRequestDTO {

    private String name;
    private String phone;
    private String gmail;
    private String password;
    private String confirmPassword;
    private String vehicleNumber;
    private String vehicleType;
}
