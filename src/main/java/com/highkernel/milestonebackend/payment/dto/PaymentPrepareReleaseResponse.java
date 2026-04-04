package com.highkernel.milestonebackend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareReleaseResponse {
    private UUID projectId;
    private UUID milestoneId;
    private UUID freelancerId;
    private Long appId;
    private BigDecimal amount;
    private String unsignedTxn;
    private String message;
}