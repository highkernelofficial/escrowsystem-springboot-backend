package com.highkernel.milestonebackend.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionAiEvaluationResponse {

    private UUID submissionId;
    private UUID milestoneId;
    private UUID projectId;

    private Integer aiScore;
    private Boolean approved;
    private String aiFeedback;
    private String aiApproval;

    private String submissionStatus;
}