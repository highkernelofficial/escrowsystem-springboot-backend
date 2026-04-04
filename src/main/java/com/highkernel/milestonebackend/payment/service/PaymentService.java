package com.highkernel.milestonebackend.payment.service;

import com.highkernel.milestonebackend.payment.dto.PaymentPrepareReleaseResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentReleaseRequest;
import com.highkernel.milestonebackend.payment.dto.PaymentResponse;
import com.highkernel.milestonebackend.payment.dto.PaymentStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentPrepareReleaseResponse prepareReleasePayment(String authUserId, PaymentReleaseRequest request);

    PaymentResponse confirmReleasePayment(String authUserId, PaymentReleaseRequest request);

    PaymentResponse updatePaymentStatus(String authUserId, UUID paymentId, PaymentStatusUpdateRequest request);

    List<PaymentResponse> getPaymentsByMilestone(String authUserId, UUID milestoneId);

    List<PaymentResponse> getPaymentsByFreelancer(String authUserId, UUID freelancerId);

    List<PaymentResponse> getPaymentsByProject(String authUserId, UUID projectId);

    PaymentResponse getPaymentById(String authUserId, UUID paymentId);

    List<PaymentResponse> getMyPayments(String authUserId);
}