package com.highkernel.milestonebackend.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReleaseRequest {

    @NotNull(message = "Milestone ID is required")
    private UUID milestoneId;

    private String txnHash;
}