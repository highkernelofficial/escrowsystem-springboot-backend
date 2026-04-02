package com.highkernel.milestonebackend.milestone.dto;

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
public class MilestoneResponse {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private Long amount;
    private UUID assignedFreelancer;
    private String status;
    private Integer attemptCount;
    private String reassignmentReason;
    private BigDecimal percentage;
    private OffsetDateTime createdAt;
}