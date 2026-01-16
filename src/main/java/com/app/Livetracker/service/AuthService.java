package com.app.Livetracker.service;

import com.app.Livetracker.dto.RiderStatusUpdateDTO;
import com.app.Livetracker.exception.AlreadyExistsException;
import com.app.Livetracker.exception.BadRequestException;
import com.app.Livetracker.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Livetracker.dto.JwtResponseDTO;
import com.app.Livetracker.dto.LoginRequestDTO;
import com.app.Livetracker.dto.SignupRequestDTO;
import com.app.Livetracker.entity.Role;
import com.app.Livetracker.entity.User;
import com.app.Livetracker.exception.CustomException;
import com.app.Livetracker.repository.UserRepository;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // ===============================
    // USER SIGNUP
    // ===============================
    public void signup(SignupRequestDTO dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByGmail(dto.getGmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setGmail(dto.getGmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        // USER & ADMIN → isActive can be null
        user.setIsActive(null);

        userRepository.save(user);
    }

    // ===============================
    // LOGIN (JWT CONTAINS UUID + ROLE)
    // ===============================
    public JwtResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByGmail(dto.getGmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        // 🔐 JWT subject = UUID (not gmail)
        String token = jwtService.generateToken(user);

        return new JwtResponseDTO(token);
    }
    // ===============================
// RIDER UPDATE OWN STATUS
// ===============================
    public void updateRiderStatus(RiderStatusUpdateDTO dto) {

        // 🔐 Get UUID from JWT (set in JwtAuthenticationFilter)
        UUID riderId = (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new NotFoundException("Rider not found"));

        if (rider.getRole() != Role.RIDER) {
            throw new BadRequestException("Only riders can update status");
        }

        rider.setIsActive(dto.getIsActive());
        userRepository.save(rider);
    }


}
