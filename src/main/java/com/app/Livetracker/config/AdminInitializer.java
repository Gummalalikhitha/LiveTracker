package com.app.Livetracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.Livetracker.entity.Role;
import com.app.Livetracker.entity.User;
import com.app.Livetracker.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        boolean adminExists =
                userRepository.existsByGmail(adminEmail);

        if (adminExists) {
            return;
        }

        User admin = new User();
        admin.setName("Admin");
        admin.setGmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setIsActive(null); // Admin does not need active status
        userRepository.save(admin);

    }
}


