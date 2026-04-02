package com.highkernel.milestonebackend.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private UUID id;
    private UUID milestoneId;
    private UUID freelancerId;
    private String githubLink;
    private String demoLink;
    private String description;
    private Integer aiScore;
    private String aiFeedback;
    private String aiRaw;
    private String status;
    private OffsetDateTime createdAt;
}