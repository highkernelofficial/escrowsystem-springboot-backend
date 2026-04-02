package com.highkernel.milestonebackend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID milestoneId;
    private UUID freelancerId;
    private BigDecimal amount;
    private String txnHash;
    private String status;
    private OffsetDateTime createdAt;
}