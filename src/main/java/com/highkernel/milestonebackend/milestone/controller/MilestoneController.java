package com.highkernel.milestonebackend.milestone.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.milestone.dto.AssignFreelancerRequest;
import com.highkernel.milestonebackend.milestone.dto.BulkMilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneCreateRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.milestone.dto.MilestoneUpdateRequest;
import com.highkernel.milestonebackend.milestone.dto.UpdateMilestoneStatusRequest;
import com.highkernel.milestonebackend.milestone.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public MilestoneResponse createMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody MilestoneCreateRequest request
    ) {
        return milestoneService.createMilestone(principal.getUserId(), request);
    }

    @PostMapping("/bulk")
    public List<MilestoneResponse> createMilestonesBulk(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody BulkMilestoneCreateRequest request
    ) {
        return milestoneService.createMilestonesBulk(principal.getUserId(), request);
    }

    @GetMapping("/project/{projectId}")
    public List<MilestoneResponse> getMilestonesByProject(@PathVariable UUID projectId) {
        return milestoneService.getMilestonesByProject(projectId);
    }

    @PutMapping("/{id}")
    public MilestoneResponse updateMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody MilestoneUpdateRequest request
    ) {
        return milestoneService.updateMilestone(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        milestoneService.deleteMilestone(principal.getUserId(), id);
    }

    @PutMapping("/{id}/assign")
    public MilestoneResponse assignFreelancer(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody AssignFreelancerRequest request
    ) {
        return milestoneService.assignFreelancer(principal.getUserId(), id, request);
    }

    @PutMapping("/{id}/status")
    public MilestoneResponse updateStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMilestoneStatusRequest request
    ) {
        return milestoneService.updateStatus(principal.getUserId(), id, request);
    }
}