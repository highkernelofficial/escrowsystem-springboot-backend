package com.highkernel.milestonebackend.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionUpdateRequest {

    private String githubLink;

    private String demoLink;

    private String description;
}