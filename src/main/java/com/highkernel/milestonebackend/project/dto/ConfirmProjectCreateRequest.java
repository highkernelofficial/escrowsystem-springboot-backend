package com.highkernel.milestonebackend.project.dto;

import com.highkernel.milestonebackend.milestone.dto.MilestoneItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmProjectCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotEmpty(message = "Tech stack is required")
    private List<String> techStack;

    @NotBlank(message = "Expected outcome is required")
    private String expectedOutcome;

    private String status;

    private Long appId;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    @Valid
    @NotEmpty(message = "Milestones are required")
    private List<MilestoneItemRequest> milestones;
}