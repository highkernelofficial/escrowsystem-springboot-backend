package com.highkernel.milestonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MilestoneGenerationRequest {

    private String title;

    private String description;

    @JsonProperty("tech_stack")
    private List<String> techStack;

    @JsonProperty("expected_outcome")
    private String expectedOutcome;

    @JsonProperty("total_budget")
    private Long totalBudget;
}