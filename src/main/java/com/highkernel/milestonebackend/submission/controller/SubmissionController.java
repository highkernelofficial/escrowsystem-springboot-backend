package com.highkernel.milestonebackend.submission.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.submission.dto.ClientProjectMilestoneSubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionAiEvaluationResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionCreateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionStatusUpdateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionUpdateRequest;
import com.highkernel.milestonebackend.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public SubmissionResponse createSubmission(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody SubmissionCreateRequest request
    ) {
        return submissionService.createSubmission(principal.getUserId(), request);
    }

    @GetMapping("/client/project/{projectId}")
    public List<ClientProjectMilestoneSubmissionResponse> getClientProjectMilestoneSubmissions(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return submissionService.getClientProjectMilestoneSubmissions(principal.getUserId(), projectId);
    }

    @GetMapping("/milestone/{milestoneId}")
    public List<SubmissionResponse> getSubmissionsByMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID milestoneId
    ) {
        return submissionService.getSubmissionsByMilestone(principal.getUserId(), milestoneId);
    }

    @GetMapping("/me")
    public List<SubmissionResponse> getMySubmissions(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return submissionService.getMySubmissions(principal.getUserId());
    }

    @GetMapping("/{id}")
    public SubmissionResponse getSubmissionById(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return submissionService.getSubmissionById(principal.getUserId(), id);
    }

    @PutMapping("/{id}")
    public SubmissionResponse updateSubmission(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @RequestBody SubmissionUpdateRequest request
    ) {
        return submissionService.updateSubmission(principal.getUserId(), id, request);
    }

    @PutMapping("/{id}/status")
    public SubmissionResponse updateSubmissionStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SubmissionStatusUpdateRequest request
    ) {
        return submissionService.updateSubmissionStatus(principal.getUserId(), id, request);
    }

    @PostMapping("/{submissionId}/evaluate-ai")
    public SubmissionAiEvaluationResponse evaluateSubmissionWithAi(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID submissionId
    ) {
        return submissionService.evaluateSubmissionWithAi(principal.getUserId(), submissionId);
    }
}