package com.app.Livetracker.service;

import com.app.Livetracker.exception.AlreadyExistsException;
import com.app.Livetracker.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Livetracker.dto.CreateRiderRequestDTO;
import com.app.Livetracker.entity.Role;
import com.app.Livetracker.entity.User;
import com.app.Livetracker.exception.CustomException;
import com.app.Livetracker.repository.UserRepository;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createRider(CreateRiderRequestDTO dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByGmail(dto.getGmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        User rider = new User();
        rider.setName(dto.getName());
        rider.setPhone(dto.getPhone());
        rider.setGmail(dto.getGmail());
        rider.setPassword(passwordEncoder.encode(dto.getPassword()));
        rider.setRole(Role.RIDER);
        rider.setVehicleNumber(dto.getVehicleNumber());
        rider.setVehicleType(dto.getVehicleType());
        rider.setIsActive(true);

        userRepository.save(rider);
    }
}

