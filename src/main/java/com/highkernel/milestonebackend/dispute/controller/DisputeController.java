package com.highkernel.milestonebackend.dispute.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.dispute.dto.*;
import com.highkernel.milestonebackend.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public DisputeResponse create(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody DisputeCreateRequest request
    ) {
        return disputeService.createDispute(principal.getUserId(), request);
    }

    @GetMapping("/milestone/{milestoneId}")
    public List<DisputeResponse> getByMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID milestoneId
    ) {
        return disputeService.getDisputesByMilestone(principal.getUserId(), milestoneId);
    }

    @GetMapping("/project/{projectId}")
    public List<DisputeResponse> getByProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return disputeService.getDisputesByProject(principal.getUserId(), projectId);
    }

    @GetMapping("/me")
    public List<DisputeResponse> getMy(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return disputeService.getMyDisputes(principal.getUserId());
    }

    @GetMapping("/{id}")
    public DisputeResponse getById(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return disputeService.getDisputeById(principal.getUserId(), id);
    }

    @PutMapping("/{id}/status")
    public DisputeResponse updateStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody DisputeStatusUpdateRequest request
    ) {
        return disputeService.updateDisputeStatus(principal.getUserId(), id, request);
    }
}