package com.highkernel.milestonebackend.payment.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.payment.dto.PaymentPrepareReleaseResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentReleaseRequest;
import com.highkernel.milestonebackend.payment.dto.PaymentResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentStatusUpdateRequest;
import com.highkernel.milestonebackend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/release/prepare")
    public PaymentPrepareReleaseResponse prepareReleasePayment(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody PaymentReleaseRequest request
    ) {
        return paymentService.prepareReleasePayment(principal.getUserId(), request);
    }

    @PostMapping("/release/confirm")
    public PaymentResponse confirmReleasePayment(
            @AuthenticationPrincipal WalletPrincipal principal,
            @RequestBody(required = false) PaymentReleaseRequest bodyRequest,
            @RequestParam(value = "milestoneId", required = false) UUID milestoneId,
            @RequestParam(value = "txnHash", required = false) String txnHash
    ) {
        PaymentReleaseRequest request = bodyRequest;

        if (request == null) {
            if (milestoneId == null || txnHash == null || txnHash.isBlank()) {
                throw new BadRequestException("milestoneId and txnHash are required");
            }

            request = PaymentReleaseRequest.builder()
                    .milestoneId(milestoneId)
                    .txnHash(txnHash)
                    .build();
        }

        return paymentService.confirmReleasePayment(principal.getUserId(), request);
    }

    @PatchMapping("/{paymentId}/status")
    public PaymentResponse updatePaymentStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request
    ) {
        return paymentService.updatePaymentStatus(principal.getUserId(), paymentId, request);
    }

    @GetMapping("/milestone/{milestoneId}")
    public List<PaymentResponse> getPaymentsByMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID milestoneId
    ) {
        return paymentService.getPaymentsByMilestone(principal.getUserId(), milestoneId);
    }

    @GetMapping("/freelancer/{freelancerId}")
    public List<PaymentResponse> getPaymentsByFreelancer(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID freelancerId
    ) {
        return paymentService.getPaymentsByFreelancer(principal.getUserId(), freelancerId);
    }

    @GetMapping("/project/{projectId}")
    public List<PaymentResponse> getPaymentsByProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return paymentService.getPaymentsByProject(principal.getUserId(), projectId);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPaymentById(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID paymentId
    ) {
        return paymentService.getPaymentById(principal.getUserId(), paymentId);
    }

    @GetMapping("/me")
    public List<PaymentResponse> getMyPayments(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return paymentService.getMyPayments(principal.getUserId());
    }
}