package com.highkernel.milestonebackend.milestone.service.impl;

import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.dto.AssignFreelancerRequest;
import com.highkernel.milestonebackend.milestone.dto.BulkMilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.milestone.dto.MilestoneUpdateRequest;
import com.highkernel.milestonebackend.milestone.dto.UpdateMilestoneStatusRequest;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.milestone.service.MilestoneService;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING",
            "ASSIGNED",
            "IN_PROGRESS",
            "SUBMITTED",
            "UNDER_REVIEW",
            "APPROVED",
            "REJECTED",
            "DISPUTED",
            "PAID"
    );

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    @Override
    public MilestoneResponse createMilestone(String authUserId, MilestoneCreateRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }

        Project project = getProjectOrThrow(request.getProjectId());
        validateProjectOwnership(userId, project);

        Milestone milestone = Milestone.builder()
                .projectId(request.getProjectId())
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .assignedFreelancer(request.getAssignedFreelancer())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .attemptCount(request.getAttemptCount() != null ? request.getAttemptCount() : 0)
                .reassignmentReason(request.getReassignmentReason())
                .percentage(request.getPercentage())
                .build();

        return mapToResponse(milestoneRepository.save(milestone));
    }

    @Override
    public List<MilestoneResponse> createMilestonesBulk(String authUserId, BulkMilestoneCreateRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request == null || request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }

        if (request.getMilestones() == null || request.getMilestones().isEmpty()) {
            throw new BadRequestException("At least one milestone is required");
        }

        Project project = getProjectOrThrow(request.getProjectId());
        validateProjectOwnership(userId, project);

        List<Milestone> milestones = request.getMilestones()
                .stream()
                .map(item -> Milestone.builder()
                        .projectId(request.getProjectId())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .amount(item.getAmount())
                        .assignedFreelancer(item.getAssignedFreelancer())
                        .status(item.getStatus() != null ? item.getStatus() : "PENDING")
                        .attemptCount(item.getAttemptCount() != null ? item.getAttemptCount() : 0)
                        .reassignmentReason(item.getReassignmentReason())
                        .percentage(item.getPercentage())
                        .build())
                .toList();

        return milestoneRepository.saveAll(milestones)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MilestoneResponse> getMilestonesByProject(UUID projectId) {
        return getMilestonesByProjectId(projectId);
    }

    @Override
    public List<MilestoneResponse> getMilestonesByProjectId(UUID projectId) {
        getProjectOrThrow(projectId);

        return milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MilestoneResponse getMilestoneById(UUID milestoneId) {
        Milestone milestone = getMilestoneOrThrow(milestoneId);
        return mapToResponse(milestone);
    }

    @Override
    public MilestoneResponse updateMilestone(String authUserId, UUID milestoneId, MilestoneUpdateRequest request) {
        UUID userId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());
        validateProjectOwnership(userId, project);

        if (request.getTitle() != null) {
            milestone.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            milestone.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            milestone.setAmount(request.getAmount());
        }
        if (request.getAssignedFreelancer() != null) {
            milestone.setAssignedFreelancer(request.getAssignedFreelancer());
        }
        if (request.getStatus() != null) {
            milestone.setStatus(request.getStatus());
        }
        if (request.getAttemptCount() != null) {
            milestone.setAttemptCount(request.getAttemptCount());
        }
        if (request.getReassignmentReason() != null) {
            milestone.setReassignmentReason(request.getReassignmentReason());
        }
        if (request.getPercentage() != null) {
            milestone.setPercentage(request.getPercentage());
        }

        return mapToResponse(milestoneRepository.save(milestone));
    }

    @Override
    public MilestoneResponse assignFreelancer(String authUserId, UUID milestoneId, AssignFreelancerRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request == null || request.getFreelancerId() == null) {
            throw new BadRequestException("Freelancer ID is required");
        }

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());
        validateProjectOwnership(userId, project);

        milestone.setAssignedFreelancer(request.getFreelancerId());

        if (request.getReassignmentReason() != null && !request.getReassignmentReason().isBlank()) {
            milestone.setReassignmentReason(request.getReassignmentReason());
        }

        if (milestone.getStatus() == null || milestone.getStatus().isBlank() || "PENDING".equalsIgnoreCase(milestone.getStatus())) {
            milestone.setStatus("IN_PROGRESS");
        }

        return mapToResponse(milestoneRepository.save(milestone));
    }

    @Override
    public MilestoneResponse updateStatus(String authUserId, UUID milestoneId, UpdateMilestoneStatusRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Milestone status is required");
        }

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());
        validateProjectOwnership(userId, project);

        String normalizedStatus = request.getStatus().trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException(
                    "Invalid milestone status. Allowed values: PENDING, IN_PROGRESS, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISPUTED, PAID"
            );
        }

        milestone.setStatus(normalizedStatus);

        if (request.getReassignmentReason() != null) {
            milestone.setReassignmentReason(request.getReassignmentReason());
        }

        if (request.getPercentage() != null) {
            milestone.setPercentage(request.getPercentage());
        }

        return mapToResponse(milestoneRepository.save(milestone));
    }

    @Override
    public void deleteMilestone(String authUserId, UUID milestoneId) {
        UUID userId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());
        validateProjectOwnership(userId, project);

        milestoneRepository.delete(milestone);
    }

    private Milestone getMilestoneOrThrow(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private void validateProjectOwnership(UUID userId, Project project) {
        if (project.getClientId() == null || !project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can perform this action");
        }
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private MilestoneResponse mapToResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProjectId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .amount(milestone.getAmount())
                .assignedFreelancer(milestone.getAssignedFreelancer())
                .status(milestone.getStatus())
                .attemptCount(milestone.getAttemptCount())
                .reassignmentReason(milestone.getReassignmentReason())
                .percentage(milestone.getPercentage())
                .createdAt(milestone.getCreatedAt())
                .build();
    }
}