package com.highkernel.milestonebackend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPrepareReleaseResponse {
    private UUID projectId;
    private UUID milestoneId;
    private UUID freelancerId;
    private Long appId;
    private BigDecimal amount;
    private List<String> unsignedTxns;
    private String message;
}