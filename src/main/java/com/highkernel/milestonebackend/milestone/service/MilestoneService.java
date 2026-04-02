package com.highkernel.milestonebackend.milestone.service;

import com.highkernel.milestonebackend.milestone.dto.AssignFreelancerRequest;
import com.highkernel.milestonebackend.milestone.dto.BulkMilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.milestone.dto.MilestoneUpdateRequest;
import com.highkernel.milestonebackend.milestone.dto.UpdateMilestoneStatusRequest;

import java.util.List;
import java.util.UUID;

public interface MilestoneService {

    MilestoneResponse createMilestone(String authUserId, MilestoneCreateRequest request);

    List<MilestoneResponse> createMilestonesBulk(String authUserId, BulkMilestoneCreateRequest request);

    List<MilestoneResponse> getMilestonesByProject(UUID projectId);

    List<MilestoneResponse> getMilestonesByProjectId(UUID projectId);

    MilestoneResponse getMilestoneById(UUID milestoneId);

    MilestoneResponse updateMilestone(String authUserId, UUID milestoneId, MilestoneUpdateRequest request);

    MilestoneResponse assignFreelancer(String authUserId, UUID milestoneId, AssignFreelancerRequest request);

    MilestoneResponse updateStatus(String authUserId, UUID milestoneId, UpdateMilestoneStatusRequest request);

    void deleteMilestone(String authUserId, UUID milestoneId);
}