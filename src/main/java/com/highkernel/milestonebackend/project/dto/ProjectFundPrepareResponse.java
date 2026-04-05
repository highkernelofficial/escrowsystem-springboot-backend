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
    private UUID milestoneId;
    private Long appId;
    private BigDecimal amount;

    // backward compatibility for old frontend
    private String unsignedTxn;

    // new grouped transaction support
    private List<String> unsignedTxns;

    private String message;
}