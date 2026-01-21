package com.app.Livetracker.service;
import com.app.Livetracker.dto.AssignmentRequestDTO;
import com.app.Livetracker.dto.RiderDecisionDTO;
import com.app.Livetracker.entity.*;
import com.app.Livetracker.exception.AlreadyExistsException;
import com.app.Livetracker.exception.BadRequestException;
import com.app.Livetracker.exception.NotFoundException;
import com.app.Livetracker.notification.NotificationService;
import com.app.Livetracker.repository.OrderRepository;
import com.app.Livetracker.repository.RiderAssignmentRepository;
import com.app.Livetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiderAssignmentServiceImpl implements RiderAssignmentService {

    private final RiderAssignmentRepository assignmentRepo;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    private void scheduleAssignmentExpiry(Long assignmentId) {

        new Thread(() -> {
            try {
                Thread.sleep(360_000);

                RiderAssignment assignment =
                        assignmentRepo.findById(assignmentId).orElse(null);
                // SAFETY CHECK
                if (assignment == null) return;
                // Only expire if still pending
                if (assignment.getStatus() == AssignmentStatus.PENDING) {
                    assignment.setStatus(AssignmentStatus.EXPIRED);
                    assignment.setRespondedAt(LocalDateTime.now());
                    assignmentRepo.save(assignment);
                    // 🔔 NOTIFY ADMIN (SYSTEM → ADMIN)
                    userRepository.findByRole(Role.ADMIN)
                            .forEach(admin ->
                                    notificationService.send(
                                            admin.getId(),
                                            assignment.getRider().getId(),
                                            NotificationType.RIDER_EXPIRED,
                                            "Rider did not respond. Assignment expired.",
                                            assignment.getOrder().getId(),
                                            frontendBaseUrl + "/assignRider?orderId=" + assignment.getOrder().getId()
                                    )
                            );
                }

            } catch (InterruptedException ignored) {
            }
        }).start();
    }


    @Override
    @Transactional
    public void assignOrder(AssignmentRequestDTO dto) {

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        User rider = userRepository.findById(dto.getRiderId())
                .orElseThrow(() -> new NotFoundException("Rider not found"));
        if (rider.getRole() != Role.RIDER) {
            throw new BadRequestException("Provided user is not a rider");
        }
        RiderAssignment assignment = new RiderAssignment();
        assignment.setOrder(order);
        assignment.setRider(rider);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepo.save(assignment);

        // 🔔 NOTIFICATION → RIDER (ADMIN ASSIGNED ORDER)
        userRepository.findByRole(Role.ADMIN)
                .forEach(admin ->
                        notificationService.send(
                                rider.getId(),
                                admin.getId(),
                                NotificationType.RIDER_REQUEST,
                                "New delivery assigned. Please accept or reject.",
                                order.getId(),
                                frontendBaseUrl + "/riderDashboard"
                        )
                );
        scheduleAssignmentExpiry(assignment.getId());
    }

    @Override
    @Transactional
    public void riderDecision(RiderDecisionDTO dto) {

        RiderAssignment assignment =
                assignmentRepo.findByOrderIdAndRiderId(
                        dto.getOrderId(),
                        dto.getRiderId()
                ).orElseThrow(() ->
                        new NotFoundException("Assignment not found"));

        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new AlreadyExistsException("Decision already taken");
        }
        if (assignment.getRider().getRole() != Role.RIDER) {
            throw new BadRequestException("Only riders can take decisions on assignments");
        }

        assignment.setStatus(dto.getDecision());
        assignment.setRespondedAt(LocalDateTime.now());

        assignmentRepo.save(assignment);

        if (dto.getDecision() == AssignmentStatus.ACCEPTED) {

            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            order.setRiderId(dto.getRiderId());
            orderRepository.save(order);
            userRepository.findByRole(Role.ADMIN)
                    .forEach(admin ->
                            notificationService.send(
                                    admin.getId(),
                                    order.getRiderId(),
                                    NotificationType.RIDER_ASSIGNED,
                                    "Rider accepted your order",
                                    order.getId(),
                                    frontendBaseUrl + "/assignRider?orderId=" + order.getId()
                            )
                    );

            // 🔔 NOTIFICATION → USER (RIDER ACCEPTED)
            notificationService.send(
                    order.getUserId(),
                    order.getRiderId(),
                    NotificationType.RIDER_ASSIGNED,
                    "Rider accepted your order",
                    order.getId(),
                    frontendBaseUrl
                            + "/CurrentOrder?orderId=" + order.getId()
                            + "&userId=" + order.getUserId()
            );
        }

        if (dto.getDecision() == AssignmentStatus.REJECTED) {

            // 🔔 NOTIFICATION → USER (RIDER REJECTED)
            userRepository.findByRole(Role.ADMIN)
                    .forEach(admin ->
                            notificationService.send(
                                    admin.getId(),
                                    assignment.getRider().getId(),
                                    NotificationType.RIDER_ASSIGNED,
                                    "Rider rejected the order. Admin will assign another rider.",
                                    assignment.getOrder().getId(),
                                    frontendBaseUrl + "/orderDetail"
                            )
                    );
        }
    }

    public List<RiderAssignment> getAllAssignments() {
        return assignmentRepo.findAll();
    }

    public List<RiderAssignment> getAssignmentsByOrderId(Long orderId) {
        return assignmentRepo.findByOrderId(orderId);
    }

    public List<RiderAssignment> getAssignmentsByRiderId(UUID riderId) {
        return assignmentRepo.findByRiderId(riderId);
    }

    public List<RiderAssignment> getAssignmentsByStatus(AssignmentStatus status) {
        return assignmentRepo.findByStatus(status);
    }

    public List<RiderAssignment> getRiderAssignmentsByStatus(UUID riderId, AssignmentStatus status) {
        return assignmentRepo.findByRiderIdAndStatus(riderId, status);
    }

}
