package com.highkernel.milestonebackend.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProjectMilestoneSubmissionResponse {

    private UUID milestoneId;
    private UUID projectId;
    private String milestoneTitle;
    private String milestoneDescription;
    private BigDecimal milestoneAmount;
    private UUID assignedFreelancer;
    private String milestoneStatus;
    private Integer attemptCount;
    private String reassignmentReason;
    private BigDecimal percentage;
    private OffsetDateTime milestoneCreatedAt;
    private List<ClientSubmissionResponse> submissions;
}