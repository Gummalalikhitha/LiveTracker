//package com.app.Livetracker.repository;
//import com.app.Livetracker.entity.RiderAssignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import com.app.Livetracker.entity.AssignmentStatus;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public interface RiderAssignmentRepository
//        extends JpaRepository<RiderAssignment, Long> {
//
//    Optional<RiderAssignment> findByOrderIdAndRiderId(Long orderId,UUID riderId);
//
//    List<RiderAssignment> findByStatusAndAssignedAtBefore(
//            AssignmentStatus status, LocalDateTime time);
//    Optional<RiderAssignment> findByOrderId(Long orderId);
//    List<RiderAssignment> findByRiderId(UUID riderId);
//}
//
//



package com.app.Livetracker.repository;

import com.app.Livetracker.entity.AssignmentStatus;
import com.app.Livetracker.entity.RiderAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiderAssignmentRepository extends JpaRepository<RiderAssignment, Long> {

    List<RiderAssignment> findByRiderId(UUID riderId);

    List<RiderAssignment> findByOrderId(Long orderId);

    List<RiderAssignment> findByStatus(AssignmentStatus status);

    List<RiderAssignment> findByRiderIdAndStatus(UUID riderId, AssignmentStatus status);




    Optional<RiderAssignment> findByOrderIdAndRiderId(Long orderId, UUID riderId);
}
