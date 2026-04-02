package com.highkernel.milestonebackend.milestone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkMilestoneCreateRequest {
    private UUID projectId;
    private List<MilestoneCreateRequest> milestones;
}