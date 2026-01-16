//
//package com.app.Livetracker.service;
//
//import com.app.Livetracker.dto.AssignmentRequestDTO;
//import com.app.Livetracker.dto.RiderDecisionDTO;
//import com.app.Livetracker.entity.RiderAssignment;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface RiderAssignmentService {
//
//    void assignOrder(AssignmentRequestDTO dto);
//
//    void riderDecision(RiderDecisionDTO dto);
//
//}

package com.app.Livetracker.service;
import com.app.Livetracker.dto.AssignmentRequestDTO;
import com.app.Livetracker.dto.RiderDecisionDTO;
import com.app.Livetracker.entity.AssignmentStatus;
import com.app.Livetracker.entity.RiderAssignment;

import java.util.List;
import java.util.UUID;

public interface RiderAssignmentService {

    void assignOrder(AssignmentRequestDTO dto);

    void riderDecision(RiderDecisionDTO dto);

    // 🔽 GET METHODS
    List<RiderAssignment> getAllAssignments();

    List<RiderAssignment> getAssignmentsByOrderId(Long orderId);

    List<RiderAssignment> getAssignmentsByRiderId(UUID riderId);

    List<RiderAssignment> getAssignmentsByStatus(AssignmentStatus status);

    List<RiderAssignment> getRiderAssignmentsByStatus(UUID riderId, AssignmentStatus status);
}
