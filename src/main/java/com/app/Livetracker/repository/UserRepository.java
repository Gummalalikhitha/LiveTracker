package com.app.Livetracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.Livetracker.entity.Role;
import com.app.Livetracker.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // USED IN LOGIN
    Optional<User> findByGmail(String gmail);

    // USED IN SIGNUP
    boolean existsByGmail(String gmail);

    // USED IN ADMIN APIs
    List<User> findByRole(Role role);

    List<User> findByRoleAndIsActive(Role role, boolean isActive);

}
