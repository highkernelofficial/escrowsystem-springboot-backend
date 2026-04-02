package com.highkernel.milestonebackend.dispute.service;

import com.highkernel.milestonebackend.dispute.dto.DisputeCreateRequest;
import com.highkernel.milestonebackend.dispute.dto.DisputeResponse;
import com.highkernel.milestonebackend.dispute.dto.DisputeStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface DisputeService {

    DisputeResponse createDispute(String authUserId, DisputeCreateRequest request);

    List<DisputeResponse> getDisputesByMilestone(String authUserId, UUID milestoneId);

    List<DisputeResponse> getMyDisputes(String authUserId);

    DisputeResponse getDisputeById(String authUserId, UUID disputeId);

    DisputeResponse updateDisputeStatus(String authUserId, UUID disputeId, DisputeStatusUpdateRequest request);
}