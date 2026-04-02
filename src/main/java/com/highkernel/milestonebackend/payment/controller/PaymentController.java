package com.highkernel.milestonebackend.payment.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
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

    // 🔹 1. Release payment (Client → Freelancer)
    @PostMapping("/release")
    public PaymentResponse releasePayment(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody PaymentReleaseRequest request
    ) {
        return paymentService.releasePayment(principal.getUserId(), request);
    }

    // 🔹 2. Update payment status
    @PatchMapping("/{paymentId}/status")
    public PaymentResponse updatePaymentStatus(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request
    ) {
        return paymentService.updatePaymentStatus(principal.getUserId(), paymentId, request);
    }

    // 🔹 3. Get payments by milestone
    @GetMapping("/milestone/{milestoneId}")
    public List<PaymentResponse> getPaymentsByMilestone(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID milestoneId
    ) {
        return paymentService.getPaymentsByMilestone(principal.getUserId(), milestoneId);
    }

    // 🔹 4. Get payments by freelancer
    @GetMapping("/freelancer/{freelancerId}")
    public List<PaymentResponse> getPaymentsByFreelancer(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID freelancerId
    ) {
        return paymentService.getPaymentsByFreelancer(principal.getUserId(), freelancerId);
    }

    // 🔹 5. Get payments by project
    @GetMapping("/project/{projectId}")
    public List<PaymentResponse> getPaymentsByProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return paymentService.getPaymentsByProject(principal.getUserId(), projectId);
    }

    // 🔹 6. Get single payment
    @GetMapping("/{paymentId}")
    public PaymentResponse getPaymentById(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID paymentId
    ) {
        return paymentService.getPaymentById(principal.getUserId(), paymentId);
    }

    // 🔹 7. Get my payments (freelancer dashboard)
    @GetMapping("/me")
    public List<PaymentResponse> getMyPayments(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return paymentService.getMyPayments(principal.getUserId());
    }
}