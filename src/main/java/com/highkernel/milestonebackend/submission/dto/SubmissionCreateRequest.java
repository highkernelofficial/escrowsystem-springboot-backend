package com.highkernel.milestonebackend.submission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionCreateRequest {

    @NotNull(message = "Milestone ID is required")
    private UUID milestoneId;

    private String githubLink;

    private String demoLink;

    private String description;
}