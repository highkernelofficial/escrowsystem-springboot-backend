package com.highkernel.milestonebackend.milestone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMilestoneStatusRequest {
    private String status;
    private String reassignmentReason;
    private BigDecimal percentage;
}