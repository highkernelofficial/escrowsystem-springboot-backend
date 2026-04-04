package com.highkernel.milestonebackend.payment.service.impl;

import com.highkernel.milestonebackend.blockchain.client.BlockchainClient;
import com.highkernel.milestonebackend.blockchain.dto.BlockchainTxnResponse;
import com.highkernel.milestonebackend.blockchain.dto.ReleaseMilestoneTxnRequest;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.payment.dto.PaymentPrepareReleaseResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentReleaseRequest;
import com.highkernel.milestonebackend.payment.dto.PaymentResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentStatusUpdateRequest;
import com.highkernel.milestonebackend.payment.entity.Payment;
import com.highkernel.milestonebackend.payment.repository.PaymentRepository;
import com.highkernel.milestonebackend.payment.service.PaymentService;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import com.highkernel.milestonebackend.user.entity.User;
import com.highkernel.milestonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Set<String> ALLOWED_PAYMENT_STATUSES = Set.of(
            "PENDING",
            "SUCCESS",
            "FAILED",
            "RELEASED"
    );

    private static final Set<String> FINALIZED_PAYMENT_STATUSES = Set.of(
            "SUCCESS",
            "RELEASED"
    );

    private final PaymentRepository paymentRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BlockchainClient blockchainClient;

    @Override
    public PaymentPrepareReleaseResponse prepareReleasePayment(String authUserId, PaymentReleaseRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request == null || request.getMilestoneId() == null) {
            throw new BadRequestException("Milestone ID is required");
        }

        Milestone milestone = getMilestoneOrThrow(request.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        validateClientOwnership(project, userId);
        validateProjectEligibleForRelease(project);
        validateMilestoneReadyForRelease(milestone);

        if (paymentRepository.existsByMilestoneIdAndStatusIn(milestone.getId(), FINALIZED_PAYMENT_STATUSES)) {
            throw new BadRequestException("Payment already released for this milestone");
        }

        User freelancer = getUserOrThrow(milestone.getAssignedFreelancer());

        if (freelancer.getWalletAddress() == null || freelancer.getWalletAddress().isBlank()) {
            throw new BadRequestException("Freelancer wallet address not found");
        }

        BlockchainTxnResponse blockchainResponse = blockchainClient.prepareReleaseMilestoneTxn(
                ReleaseMilestoneTxnRequest.builder()
                        .sender(getWalletAddressOrThrow(userId))
                        .appId(project.getAppId())
                        .milestoneId(milestone.getId().toString())
                        .freelancerAddress(freelancer.getWalletAddress())
                        .amount(milestone.getAmount())
                        .build()
        );

        return PaymentPrepareReleaseResponse.builder()
                .projectId(project.getId())
                .milestoneId(milestone.getId())
                .freelancerId(milestone.getAssignedFreelancer())
                .appId(project.getAppId())
                .amount(milestone.getAmount())
                .unsignedTxn(blockchainResponse.getTxn())
                .message("Unsigned release transaction prepared successfully")
                .build();
    }

    @Override
    public PaymentResponse confirmReleasePayment(String authUserId, PaymentReleaseRequest request) {
        UUID userId = parseUUID(authUserId);

        if (request == null || request.getMilestoneId() == null) {
            throw new BadRequestException("Milestone ID is required");
        }

        if (request.getTxnHash() == null || request.getTxnHash().isBlank()) {
            throw new BadRequestException("txnHash is required after blockchain submission");
        }

        Milestone milestone = getMilestoneOrThrow(request.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        validateClientOwnership(project, userId);
        validateProjectEligibleForRelease(project);
        validateMilestoneReadyForRelease(milestone);

        if (paymentRepository.existsByMilestoneIdAndStatusIn(milestone.getId(), FINALIZED_PAYMENT_STATUSES)) {
            throw new BadRequestException("Payment already released for this milestone");
        }

        Payment payment = Payment.builder()
                .milestoneId(milestone.getId())
                .freelancerId(milestone.getAssignedFreelancer())
                .amount(milestone.getAmount())
                .txnHash(request.getTxnHash().trim())
                .status("RELEASED")
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        milestone.setStatus("PAID");
        milestoneRepository.save(milestone);

        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse updatePaymentStatus(String authUserId, UUID paymentId, PaymentStatusUpdateRequest request) {
        UUID userId = parseUUID(authUserId);

        Payment payment = getPaymentOrThrow(paymentId);
        Milestone milestone = getMilestoneOrThrow(payment.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        if (project.getClientId() == null || !project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can update payment status");
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Payment status is required");
        }

        String normalizedStatus = request.getStatus().trim().toUpperCase();

        if (!ALLOWED_PAYMENT_STATUSES.contains(normalizedStatus)) {
            throw new BadRequestException("Invalid payment status. Allowed values: PENDING, SUCCESS, FAILED, RELEASED");
        }

        payment.setStatus(normalizedStatus);

        if ("RELEASED".equals(normalizedStatus) || "SUCCESS".equals(normalizedStatus)) {
            milestone.setStatus("PAID");
            milestoneRepository.save(milestone);
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponse> getPaymentsByMilestone(String authUserId, UUID milestoneId) {
        UUID userId = parseUUID(authUserId);

        Milestone milestone = getMilestoneOrThrow(milestoneId);
        Project project = getProjectOrThrow(milestone.getProjectId());

        boolean isClient = project.getClientId() != null && project.getClientId().equals(userId);
        boolean isFreelancer = milestone.getAssignedFreelancer() != null
                && milestone.getAssignedFreelancer().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new BadRequestException("Unauthorized to view milestone payments");
        }

        return paymentRepository.findByMilestoneId(milestoneId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByFreelancer(String authUserId, UUID freelancerId) {
        UUID userId = parseUUID(authUserId);

        if (!userId.equals(freelancerId)) {
            throw new BadRequestException("Unauthorized to view freelancer payments");
        }

        return paymentRepository.findByFreelancerId(freelancerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByProject(String authUserId, UUID projectId) {
        UUID userId = parseUUID(authUserId);

        Project project = getProjectOrThrow(projectId);

        if (project.getClientId() == null || !project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can view project payments");
        }

        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        List<UUID> milestoneIds = milestones.stream()
                .map(Milestone::getId)
                .toList();

        if (milestoneIds.isEmpty()) {
            return List.of();
        }

        return paymentRepository.findByMilestoneIdIn(milestoneIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PaymentResponse getPaymentById(String authUserId, UUID paymentId) {
        UUID userId = parseUUID(authUserId);

        Payment payment = getPaymentOrThrow(paymentId);
        Milestone milestone = getMilestoneOrThrow(payment.getMilestoneId());
        Project project = getProjectOrThrow(milestone.getProjectId());

        boolean isClient = project.getClientId() != null && project.getClientId().equals(userId);
        boolean isFreelancer = payment.getFreelancerId() != null && payment.getFreelancerId().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new BadRequestException("Unauthorized to view this payment");
        }

        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getMyPayments(String authUserId) {
        UUID userId = parseUUID(authUserId);

        return paymentRepository.findByFreelancerId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateClientOwnership(Project project, UUID userId) {
        if (project.getClientId() == null || !project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can release payment");
        }
    }

    private void validateProjectEligibleForRelease(Project project) {
        if (project.getAppId() == null || project.getAppId() <= 0) {
            throw new BadRequestException("Project app_id is missing");
        }

        if (project.getStatus() == null || project.getStatus().isBlank()) {
            throw new BadRequestException("Project is not funded yet");
        }

        String normalizedStatus = project.getStatus().trim().toUpperCase();

        if (!"FUNDED".equals(normalizedStatus) && !"ASSIGNED".equals(normalizedStatus)) {
            throw new BadRequestException("Project is not funded yet");
        }
    }

    private void validateMilestoneReadyForRelease(Milestone milestone) {
        if (milestone.getAssignedFreelancer() == null) {
            throw new BadRequestException("Milestone is not assigned to any freelancer");
        }

        if (milestone.getAmount() == null) {
            throw new BadRequestException("Milestone amount is missing");
        }

        if (milestone.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Milestone amount must be greater than zero");
        }

        if (milestone.getStatus() == null || milestone.getStatus().isBlank()) {
            throw new BadRequestException("Only APPROVED or SUBMITTED milestone payment can be released");
        }

        String normalizedStatus = milestone.getStatus().trim().toUpperCase();

        if (!"APPROVED".equals(normalizedStatus) && !"SUBMITTED".equals(normalizedStatus)) {
            throw new BadRequestException("Only APPROVED or SUBMITTED milestone payment can be released");
        }
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
    }

    private Milestone getMilestoneOrThrow(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private String getWalletAddressOrThrow(UUID userId) {
        User user = getUserOrThrow(userId);

        if (user.getWalletAddress() == null || user.getWalletAddress().isBlank()) {
            throw new BadRequestException("Wallet address not found");
        }

        return user.getWalletAddress();
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .milestoneId(payment.getMilestoneId())
                .freelancerId(payment.getFreelancerId())
                .amount(payment.getAmount())
                .txnHash(payment.getTxnHash())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}