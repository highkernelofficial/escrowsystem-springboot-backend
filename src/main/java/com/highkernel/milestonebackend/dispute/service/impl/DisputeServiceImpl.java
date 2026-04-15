package com.highkernel.milestonebackend.dispute.service.impl;

import com.highkernel.milestonebackend.dispute.dto.DisputeCreateRequest;
import com.highkernel.milestonebackend.dispute.dto.DisputeResponse;
import com.highkernel.milestonebackend.dispute.dto.DisputeStatusUpdateRequest;
import com.highkernel.milestonebackend.dispute.entity.Dispute;
import com.highkernel.milestonebackend.dispute.repository.DisputeRepository;
import com.highkernel.milestonebackend.dispute.service.DisputeService;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "OPEN",
            "UNDER_REVIEW",
            "RESOLVED",
            "REJECTED"
    );

    private final DisputeRepository disputeRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    @Override
    public DisputeResponse createDispute(String authUserId, DisputeCreateRequest request) {
        UUID clientId = parseUUID(authUserId);

        if (request == null || request.getMilestoneId() == null) {
            throw new BadRequestException("Milestone ID is required");
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("Dispute reason is required");
        }

        Milestone milestone = getMilestoneOrThrow(request.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        if (project.getClientId() == null || !project.getClientId().equals(clientId)) {
            throw new BadRequestException("Only project owner can raise dispute");
        }

        if (milestone.getAssignedFreelancer() == null) {
            throw new BadRequestException("No freelancer assigned to this milestone");
        }

        Dispute dispute = Dispute.builder()
                .milestoneId(request.getMilestoneId())
                .clientId(clientId)
                .freelancerId(milestone.getAssignedFreelancer())
                .reason(request.getReason().trim())
                .status("OPEN")
                .build();

        milestone.setStatus("DISPUTED");
        milestoneRepository.save(milestone);

        return mapToResponse(disputeRepository.save(dispute));
    }

    @Override
    public List<DisputeResponse> getDisputesByMilestone(String authUserId, UUID milestoneId) {
        UUID userId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());

        boolean isClient = project.getClientId() != null && project.getClientId().equals(userId);
        boolean isFreelancer = milestone.getAssignedFreelancer() != null && milestone.getAssignedFreelancer().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new BadRequestException("Unauthorized to view milestone disputes");
        }

        return disputeRepository.findByMilestoneIdOrderByCreatedAtDesc(milestoneId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<DisputeResponse> getMyDisputes(String authUserId) {
        UUID userId = parseUUID(authUserId);

        List<Dispute> clientDisputes = disputeRepository.findByClientIdOrderByCreatedAtDesc(userId);
        List<Dispute> freelancerDisputes = disputeRepository.findByFreelancerIdOrderByCreatedAtDesc(userId);

        return java.util.stream.Stream.concat(clientDisputes.stream(), freelancerDisputes.stream())
                .distinct()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DisputeResponse getDisputeById(String authUserId, UUID disputeId) {
        UUID userId = parseUUID(authUserId);

        Dispute dispute = getDisputeOrThrow(disputeId);

        boolean isClient = dispute.getClientId() != null && dispute.getClientId().equals(userId);
        boolean isFreelancer = dispute.getFreelancerId() != null && dispute.getFreelancerId().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new BadRequestException("Unauthorized to view this dispute");
        }

        return mapToResponse(dispute);
    }

    @Override
    public DisputeResponse updateDisputeStatus(String authUserId, UUID disputeId, DisputeStatusUpdateRequest request) {
        UUID userId = parseUUID(authUserId);

        Dispute dispute = getDisputeOrThrow(disputeId);

        if (dispute.getClientId() == null || !dispute.getClientId().equals(userId)) {
            throw new BadRequestException("Only client can update dispute status");
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Dispute status is required");
        }

        String normalizedStatus = request.getStatus().trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException("Invalid dispute status. Allowed values: OPEN, UNDER_REVIEW, RESOLVED, REJECTED");
        }

        dispute.setStatus(normalizedStatus);

        Milestone milestone = getMilestoneOrThrow(dispute.getMilestoneId());

        if ("RESOLVED".equals(normalizedStatus)) {
            milestone.setStatus("IN_PROGRESS");
        } else if ("REJECTED".equals(normalizedStatus)) {
            milestone.setStatus("REJECTED");
        } else if ("UNDER_REVIEW".equals(normalizedStatus) || "OPEN".equals(normalizedStatus)) {
            milestone.setStatus("DISPUTED");
        }

        milestoneRepository.save(milestone);
        return mapToResponse(disputeRepository.save(dispute));
    }

    @Override
    public List<DisputeResponse> getDisputesByProject(String authUserId, UUID projectId) {
        UUID userId = parseUUID(authUserId);

        Project project = getProjectOrThrow(projectId);
        boolean isClient = project.getClientId() != null && project.getClientId().equals(userId);

        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        boolean isFreelancer = milestones.stream()
                .anyMatch(m -> m.getAssignedFreelancer() != null && m.getAssignedFreelancer().equals(userId));

        if (!isClient && !isFreelancer) {
            throw new BadRequestException("Unauthorized to view project disputes");
        }

        return disputeRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Dispute getDisputeOrThrow(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BadRequestException("Dispute not found"));
    }

    private Milestone getMilestoneOrThrow(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private DisputeResponse mapToResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .milestoneId(dispute.getMilestoneId())
                .clientId(dispute.getClientId())
                .freelancerId(dispute.getFreelancerId())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}