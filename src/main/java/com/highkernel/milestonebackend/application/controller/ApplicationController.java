package com.highkernel.milestonebackend.application.controller;

import com.highkernel.milestonebackend.application.dto.ApplicationCreateRequest;
import com.highkernel.milestonebackend.application.dto.ApplicationResponse;
import com.highkernel.milestonebackend.application.dto.ApplicationStatusUpdateRequest;
import com.highkernel.milestonebackend.application.service.ApplicationService;
import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ApplicationResponse createApplication(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        return applicationService.createApplication(principal.getUserId(), request);
    }

    @GetMapping("/project/{projectId}")
    public List<ApplicationResponse> getApplicationsByProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return applicationService.getApplicationsByProject(principal.getUserId(), projectId);
    }

    @GetMapping("/me")
    public List<ApplicationResponse> getMyApplications(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return applicationService.getMyApplications(principal.getUserId());
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationById(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return applicationService.getApplicationById(principal.getUserId(), id);
    }

    @PutMapping("/{id}/status")
    public ApplicationResponse updateApplicationStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        return applicationService.updateApplicationStatus(principal.getUserId(), id, request);
    }

    @PostMapping("/{id}/assign")
    public ApplicationResponse assignFreelancerToProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return applicationService.assignFreelancerToProject(principal.getUserId(), id);
    }

    @PostMapping("/{id}/unassign")
    public ApplicationResponse unassignFreelancerFromProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return applicationService.unassignFreelancerFromProject(principal.getUserId(), id);
    }
}