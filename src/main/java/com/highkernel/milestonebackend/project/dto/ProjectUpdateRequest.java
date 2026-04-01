package com.highkernel.milestonebackend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUpdateRequest {
    private String title;
    private String description;
    private List<String> techStack;
    private String expectedOutcome;
    private String status;
    private Long appId;
    private Long totalAmount;
}