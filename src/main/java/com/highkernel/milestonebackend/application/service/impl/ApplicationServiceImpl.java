package com.highkernel.milestonebackend.application.service.impl;

import com.highkernel.milestonebackend.application.dto.ApplicationCreateRequest;
import com.highkernel.milestonebackend.application.dto.ApplicationResponse;
import com.highkernel.milestonebackend.application.dto.ApplicationStatusUpdateRequest;
import com.highkernel.milestonebackend.application.entity.Application;
import com.highkernel.milestonebackend.application.repository.ApplicationRepository;
import com.highkernel.milestonebackend.application.service.ApplicationService;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "ACCEPTED", "REJECTED", "ASSIGNED");

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    public ApplicationResponse createApplication(String authUserId, ApplicationCreateRequest request) {
        UUID freelancerId = parseUUID(authUserId);

        Project project = getProjectOrThrow(request.getProjectId());

        if (project.getClientId().equals(freelancerId)) {
            throw new BadRequestException("Project owner cannot apply to own project");
        }

        boolean alreadyApplied = applicationRepository.existsByProjectIdAndFreelancerId(
                request.getProjectId(),
                freelancerId
        );

        if (alreadyApplied) {
            throw new BadRequestException("You have already applied to this project");
        }

        Application application = Application.builder()
                .projectId(request.getProjectId())
                .freelancerId(freelancerId)
                .name(request.getName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .linkedin(request.getLinkedin())
                .github(request.getGithub())
                .proposal(request.getProposal())
                .status("PENDING")
                .build();

        return mapToResponse(applicationRepository.save(application));
    }

    @Override
    public List<ApplicationResponse> getApplicationsByProject(String authUserId, UUID projectId) {
        UUID userId = parseUUID(authUserId);

        Project project = getProjectOrThrow(projectId);

        if (!project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can view project applications");
        }

        return applicationRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getMyApplications(String authUserId) {
        UUID freelancerId = parseUUID(authUserId);

        return applicationRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ApplicationResponse getApplicationById(String authUserId, UUID applicationId) {
        UUID userId = parseUUID(authUserId);

        Application application = getApplicationOrThrow(applicationId);
        Project project = getProjectOrThrow(application.getProjectId());

        boolean isOwner = project.getClientId().equals(userId);
        boolean isApplicant = application.getFreelancerId().equals(userId);

        if (!isOwner && !isApplicant) {
            throw new BadRequestException("Unauthorized to view this application");
        }

        return mapToResponse(application);
    }

    @Override
    public ApplicationResponse updateApplicationStatus(String authUserId, UUID applicationId, ApplicationStatusUpdateRequest request) {
        UUID userId = parseUUID(authUserId);

        Application application = getApplicationOrThrow(applicationId);
        Project project = getProjectOrThrow(application.getProjectId());

        if (!project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can update application status");
        }

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Application status is required");
        }

        String normalizedStatus = request.getStatus().trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException("Invalid application status. Allowed values: PENDING, ACCEPTED, REJECTED, ASSIGNED");
        }

        application.setStatus(normalizedStatus);

        return mapToResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public ApplicationResponse assignFreelancerToProject(String authUserId, UUID applicationId) {
        UUID userId = parseUUID(authUserId);

        Application application = getApplicationOrThrow(applicationId);
        Project project = getProjectOrThrow(application.getProjectId());

        if (!project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can assign freelancer");
        }

        if ("REJECTED".equalsIgnoreCase(application.getStatus())) {
            throw new BadRequestException("Rejected application cannot be assigned");
        }

        UUID freelancerId = application.getFreelancerId();

        if (freelancerId == null) {
            throw new BadRequestException("Application freelancer not found");
        }

        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());

        if (milestones.isEmpty()) {
            throw new BadRequestException("No milestones found for this project");
        }

        for (Milestone milestone : milestones) {
            milestone.setAssignedFreelancer(freelancerId);

            if (milestone.getStatus() == null
                    || milestone.getStatus().isBlank()
                    || "PENDING".equalsIgnoreCase(milestone.getStatus())) {
                milestone.setStatus("ASSIGNED");
            }
        }

        milestoneRepository.saveAll(milestones);

        application.setStatus("ASSIGNED");
        applicationRepository.save(application);

        project.setStatus("ASSIGNED");
        projectRepository.save(project);

        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse unassignFreelancerFromProject(String authUserId, UUID applicationId) {
        UUID userId = parseUUID(authUserId);

        Application application = getApplicationOrThrow(applicationId);
        Project project = getProjectOrThrow(application.getProjectId());

        if (!project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can unassign freelancer");
        }

        if (!"ASSIGNED".equalsIgnoreCase(application.getStatus())) {
            throw new BadRequestException("Only assigned application can be unassigned");
        }

        UUID freelancerId = application.getFreelancerId();

        if (freelancerId == null) {
            throw new BadRequestException("Application freelancer not found");
        }

        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());

        if (milestones.isEmpty()) {
            throw new BadRequestException("No milestones found for this project");
        }

        for (Milestone milestone : milestones) {
            if (freelancerId.equals(milestone.getAssignedFreelancer())) {
                milestone.setAssignedFreelancer(null);
                milestone.setStatus("PENDING");
                milestone.setReassignmentReason("Freelancer unassigned by client");
            }
        }

        milestoneRepository.saveAll(milestones);

        application.setStatus("PENDING");
        applicationRepository.save(application);

        project.setStatus("OPEN");
        projectRepository.save(project);

        return mapToResponse(application);
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private Application getApplicationOrThrow(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BadRequestException("Application not found"));
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .projectId(application.getProjectId())
                .freelancerId(application.getFreelancerId())
                .name(application.getName())
                .email(application.getEmail())
                .mobileNumber(application.getMobileNumber())
                .linkedin(application.getLinkedin())
                .github(application.getGithub())
                .proposal(application.getProposal())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}