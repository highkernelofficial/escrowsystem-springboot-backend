package com.highkernel.milestonebackend.project.dto;

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
public class ProjectFundPrepareResponse {

    private UUID projectId;
    private Long appId;
    private BigDecimal totalAmount;

    /** Ordered list of base64-encoded msgpack transactions (1 payment + N app calls) */
    private List<String> unsignedTxns;

    private String message;
}