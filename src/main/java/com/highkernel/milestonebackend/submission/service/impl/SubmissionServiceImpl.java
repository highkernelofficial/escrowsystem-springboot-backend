package com.highkernel.milestonebackend.submission.service.impl;

import com.highkernel.milestonebackend.ai.client.AiEvaluationClient;
import com.highkernel.milestonebackend.ai.dto.AiEvaluationRequest;
import com.highkernel.milestonebackend.ai.dto.AiEvaluationResponse;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import com.highkernel.milestonebackend.submission.dto.ClientProjectMilestoneSubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.ClientSubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionAiEvaluationResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionCreateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionStatusUpdateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionUpdateRequest;
import com.highkernel.milestonebackend.submission.entity.Submission;
import com.highkernel.milestonebackend.submission.repository.SubmissionRepository;
import com.highkernel.milestonebackend.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED");

    private final SubmissionRepository submissionRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final AiEvaluationClient aiEvaluationClient;

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
                .aiApproval(null)
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
        submission.setAiApproval(null);

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

    @Override
    public List<ClientProjectMilestoneSubmissionResponse> getClientProjectMilestoneSubmissions(
            String authUserId,
            UUID projectId
    ) {
        UUID clientId = parseUUID(authUserId);

        Project project = getProjectOrThrow(projectId);

        if (!project.getClientId().equals(clientId)) {
            throw new BadRequestException("Only project owner can view submissions for this project");
        }

        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        if (milestones.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> milestoneIds = milestones.stream()
                .map(Milestone::getId)
                .toList();

        Map<UUID, List<ClientSubmissionResponse>> submissionsByMilestoneId =
                submissionRepository.findByMilestoneIdInOrderByCreatedAtDesc(milestoneIds)
                        .stream()
                        .map(this::mapToClientSubmissionResponse)
                        .collect(Collectors.groupingBy(ClientSubmissionResponse::getMilestoneId));

        return milestones.stream()
                .map(milestone -> ClientProjectMilestoneSubmissionResponse.builder()
                        .milestoneId(milestone.getId())
                        .projectId(milestone.getProjectId())
                        .milestoneTitle(milestone.getTitle())
                        .milestoneDescription(milestone.getDescription())
                        .milestoneAmount(milestone.getAmount())
                        .assignedFreelancer(milestone.getAssignedFreelancer())
                        .milestoneStatus(milestone.getStatus())
                        .attemptCount(milestone.getAttemptCount())
                        .reassignmentReason(milestone.getReassignmentReason())
                        .percentage(milestone.getPercentage())
                        .milestoneCreatedAt(milestone.getCreatedAt())
                        .submissions(submissionsByMilestoneId.getOrDefault(
                                milestone.getId(),
                                Collections.emptyList()
                        ))
                        .build())
                .toList();
    }

    @Override
    public SubmissionAiEvaluationResponse evaluateSubmissionWithAi(String authUserId, UUID submissionId) {
        UUID clientId = parseUUID(authUserId);

        Submission submission = getSubmissionOrThrow(submissionId);
        Milestone currentMilestone = getMilestoneOrThrow(submission.getMilestoneId());
        Project project = getProjectOrThrow(currentMilestone.getProjectId());

        if (!project.getClientId().equals(clientId)) {
            throw new BadRequestException("Only project owner can evaluate submission with AI");
        }

        String requirementText = buildProjectContextOneLine(project, currentMilestone);
        String submissionText = buildSubmissionContextOneLine(submission);

        AiEvaluationRequest aiRequest = AiEvaluationRequest.builder()
                .requirement(requirementText)
                .submission(submissionText)
                .build();

        AiEvaluationResponse aiResponse = aiEvaluationClient.evaluateSubmission(aiRequest);

        String approvalText = aiResponse.getApproved() == null
                ? null
                : String.valueOf(aiResponse.getApproved());

        submission.setAiScore(aiResponse.getScore());
        submission.setAiFeedback(aiResponse.getFeedback());
        submission.setAiApproval(approvalText);

        Submission savedSubmission = submissionRepository.save(submission);

        return SubmissionAiEvaluationResponse.builder()
                .submissionId(savedSubmission.getId())
                .milestoneId(currentMilestone.getId())
                .projectId(project.getId())
                .aiScore(savedSubmission.getAiScore())
                .approved(aiResponse.getApproved())
                .aiFeedback(savedSubmission.getAiFeedback())
                .aiApproval(savedSubmission.getAiApproval())
                .submissionStatus(savedSubmission.getStatus())
                .build();
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
        return SubmissionResponse.builder()
                .id(submission.getId())
                .milestoneId(submission.getMilestoneId())
                .freelancerId(submission.getFreelancerId())
                .githubLink(submission.getGithubLink())
                .demoLink(submission.getDemoLink())
                .description(submission.getDescription())
                .aiScore(submission.getAiScore())
                .aiFeedback(submission.getAiFeedback())
                .aiApproval(submission.getAiApproval())
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .build();
    }

    private ClientSubmissionResponse mapToClientSubmissionResponse(Submission submission) {
        return ClientSubmissionResponse.builder()
                .id(submission.getId())
                .milestoneId(submission.getMilestoneId())
                .freelancerId(submission.getFreelancerId())
                .githubLink(submission.getGithubLink())
                .demoLink(submission.getDemoLink())
                .description(submission.getDescription())
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .build();
    }

    private String buildProjectContextOneLine(Project project, Milestone currentMilestone) {
        String context = String.format(
                "project_title=%s ; expected_outcome=%s ; milestone_title=%s ; milestone_description=%s ; milestone_amount=%s",
                safe(project.getTitle()),
                safe(project.getExpectedOutcome()),
                safe(currentMilestone.getTitle()),
                safe(currentMilestone.getDescription()),
                safe(currentMilestone.getAmount())
        );

        return normalizeToOneLine(context);
    }

    private String buildSubmissionContextOneLine(Submission submission) {
        String githubLink = safe(submission.getGithubLink());
        String demoLink = safe(submission.getDemoLink());

        String context;
        if ("N/A".equals(demoLink)) {
            context = String.format(
                    "github_link=%s",
                    githubLink
            );
        } else {
            context = String.format(
                    "github_link=%s ; demo_link=%s",
                    githubLink,
                    demoLink
            );
        }

        return normalizeToOneLine(context);
    }

    private String normalizeToOneLine(Object value) {
        if (value == null) {
            return "N/A";
        }
        return value.toString()
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safe(Object value) {
        return value == null ? "N/A" : normalizeToOneLine(value);
    }
}