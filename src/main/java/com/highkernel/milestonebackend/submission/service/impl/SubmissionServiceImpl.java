package com.highkernel.milestonebackend.submission.service.impl;

import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import com.highkernel.milestonebackend.submission.dto.SubmissionCreateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionStatusUpdateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionUpdateRequest;
import com.highkernel.milestonebackend.submission.entity.Submission;
import com.highkernel.milestonebackend.submission.repository.SubmissionRepository;
import com.highkernel.milestonebackend.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED");

    private final SubmissionRepository submissionRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    @Override
    public SubmissionResponse createSubmission(String authUserId, SubmissionCreateRequest request) {
        UUID freelancerId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(request.getMilestoneId());
        validateAssignedFreelancer(milestone, freelancerId);

        Submission submission = Submission.builder()
                .milestoneId(request.getMilestoneId())
                .freelancerId(freelancerId)
                .githubLink(request.getGithubLink())
                .demoLink(request.getDemoLink())
                .description(request.getDescription())
                .aiScore(null)
                .aiFeedback(null)
                .aiRaw(null)
                .status("SUBMITTED")
                .build();

        Submission savedSubmission = submissionRepository.save(submission);

        milestone.setStatus("SUBMITTED");
        milestoneRepository.save(milestone);

        return mapToResponse(savedSubmission);
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByMilestone(String authUserId, UUID milestoneId) {
        UUID userId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());

        boolean isOwner = project.getClientId().equals(userId);
        boolean isAssignedFreelancer = milestone.getAssignedFreelancer() != null
                && milestone.getAssignedFreelancer().equals(userId);

        if (!isOwner && !isAssignedFreelancer) {
            throw new BadRequestException("Unauthorized to view milestone submissions");
        }

        return submissionRepository.findByMilestoneIdOrderByCreatedAtDesc(milestoneId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SubmissionResponse> getMySubmissions(String authUserId) {
        UUID freelancerId = parseUUID(authUserId);

        return submissionRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SubmissionResponse getSubmissionById(String authUserId, UUID submissionId) {
        UUID userId = parseUUID(authUserId);

        Submission submission = getSubmissionOrThrow(submissionId);
        Milestone milestone = getMilestoneOrThrow(submission.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        boolean isOwner = project.getClientId().equals(userId);
        boolean isSubmitter = submission.getFreelancerId().equals(userId);

        if (!isOwner && !isSubmitter) {
            throw new BadRequestException("Unauthorized to view this submission");
        }

        return mapToResponse(submission);
    }

    @Override
    public SubmissionResponse updateSubmission(String authUserId, UUID submissionId, SubmissionUpdateRequest request) {
        UUID freelancerId = parseUUID(authUserId);

        Submission submission = getSubmissionOrThrow(submissionId);

        if (!submission.getFreelancerId().equals(freelancerId)) {
            throw new BadRequestException("Only submission owner can update submission");
        }

        Milestone milestone = getMilestoneOrThrow(submission.getMilestoneId());
        validateAssignedFreelancer(milestone, freelancerId);

        if (request.getGithubLink() != null) {
            submission.setGithubLink(request.getGithubLink());
        }
        if (request.getDemoLink() != null) {
            submission.setDemoLink(request.getDemoLink());
        }
        if (request.getDescription() != null) {
            submission.setDescription(request.getDescription());
        }

        submission.setStatus("SUBMITTED");
        submission.setAiScore(null);
        submission.setAiFeedback(null);
        submission.setAiRaw(null);

        milestone.setStatus("SUBMITTED");
        milestoneRepository.save(milestone);

        return mapToResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse updateSubmissionStatus(String authUserId, UUID submissionId, SubmissionStatusUpdateRequest request) {
        UUID userId = parseUUID(authUserId);

        Submission submission = getSubmissionOrThrow(submissionId);
        Milestone milestone = getMilestoneOrThrow(submission.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        if (!project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can update submission status");
        }

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Submission status is required");
        }

        String normalizedStatus = request.getStatus().trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException(
                    "Invalid submission status. Allowed values: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED"
            );
        }

        submission.setStatus(normalizedStatus);

        if ("APPROVED".equals(normalizedStatus)) {
            milestone.setStatus("APPROVED");
        } else if ("REJECTED".equals(normalizedStatus)) {
            milestone.setStatus("REJECTED");
            milestone.setAttemptCount(
                    milestone.getAttemptCount() == null ? 1 : milestone.getAttemptCount() + 1
            );
        } else if ("UNDER_REVIEW".equals(normalizedStatus)) {
            milestone.setStatus("UNDER_REVIEW");
        } else {
            milestone.setStatus("SUBMITTED");
        }

        milestoneRepository.save(milestone);
        return mapToResponse(submissionRepository.save(submission));
    }

    private void validateAssignedFreelancer(Milestone milestone, UUID freelancerId) {
        if (milestone.getAssignedFreelancer() == null) {
            throw new BadRequestException("No freelancer assigned to this milestone");
        }

        if (!milestone.getAssignedFreelancer().equals(freelancerId)) {
            throw new BadRequestException("Only assigned freelancer can submit work for this milestone");
        }
    }

    private Milestone getMilestoneOrThrow(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private Submission getSubmissionOrThrow(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BadRequestException("Submission not found"));
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        String aiRawString = submission.getAiRaw() != null
                ? submission.getAiRaw().toString()
                : null;

        return SubmissionResponse.builder()
                .id(submission.getId())
                .milestoneId(submission.getMilestoneId())
                .freelancerId(submission.getFreelancerId())
                .githubLink(submission.getGithubLink())
                .demoLink(submission.getDemoLink())
                .description(submission.getDescription())
                .aiScore(submission.getAiScore())
                .aiFeedback(submission.getAiFeedback())
                .aiRaw(aiRawString)
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}