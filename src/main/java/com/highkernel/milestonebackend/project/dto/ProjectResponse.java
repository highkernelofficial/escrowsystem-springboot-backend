package com.highkernel.milestonebackend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private UUID id;
    private UUID clientId;
    private String title;
    private String description;
    private List<String> techStack;
    private String expectedOutcome;
    private String status;
    private Long appId;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
}