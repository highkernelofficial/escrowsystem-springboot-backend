package com.highkernel.milestonebackend.submission.service;

import com.highkernel.milestonebackend.submission.dto.ClientProjectMilestoneSubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionAiEvaluationResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionCreateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionResponse;
import com.highkernel.milestonebackend.submission.dto.SubmissionStatusUpdateRequest;
import com.highkernel.milestonebackend.submission.dto.SubmissionUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {

    SubmissionResponse createSubmission(String authUserId, SubmissionCreateRequest request);

    List<SubmissionResponse> getSubmissionsByMilestone(String authUserId, UUID milestoneId);

    List<SubmissionResponse> getMySubmissions(String authUserId);

    SubmissionResponse getSubmissionById(String authUserId, UUID submissionId);

    SubmissionResponse updateSubmission(String authUserId, UUID submissionId, SubmissionUpdateRequest request);

    SubmissionResponse updateSubmissionStatus(String authUserId, UUID submissionId, SubmissionStatusUpdateRequest request);

    List<ClientProjectMilestoneSubmissionResponse> getClientProjectMilestoneSubmissions(String authUserId, UUID projectId);

    SubmissionAiEvaluationResponse evaluateSubmissionWithAi(String authUserId, UUID submissionId);
}