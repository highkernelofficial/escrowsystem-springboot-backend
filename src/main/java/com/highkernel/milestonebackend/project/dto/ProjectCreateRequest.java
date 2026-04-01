package com.highkernel.milestonebackend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProjectCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Tech stack is required")
    private List<String> techStack;

    @NotBlank(message = "Expected outcome is required")
    private String expectedOutcome;

    private String status;

    private Long appId;

    @NotNull(message = "Total amount is required")
    private Long totalAmount;
}