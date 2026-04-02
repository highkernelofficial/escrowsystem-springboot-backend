package com.highkernel.milestonebackend.application.service;

import com.highkernel.milestonebackend.application.dto.ApplicationCreateRequest;
import com.highkernel.milestonebackend.application.dto.ApplicationResponse;
import com.highkernel.milestonebackend.application.dto.ApplicationStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    ApplicationResponse createApplication(String authUserId, ApplicationCreateRequest request);

    List<ApplicationResponse> getApplicationsByProject(String authUserId, UUID projectId);

    List<ApplicationResponse> getMyApplications(String authUserId);

    ApplicationResponse getApplicationById(String authUserId, UUID applicationId);

    ApplicationResponse updateApplicationStatus(String authUserId, UUID applicationId, ApplicationStatusUpdateRequest request);

    ApplicationResponse assignFreelancerToProject(String authUserId, UUID applicationId);

    ApplicationResponse unassignFreelancerFromProject(String authUserId, UUID applicationId);
}