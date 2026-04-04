package com.highkernel.milestonebackend.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSubmissionResponse {

    private UUID id;
    private UUID milestoneId;
    private UUID freelancerId;
    private String githubLink;
    private String demoLink;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
}