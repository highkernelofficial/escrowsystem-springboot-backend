package com.highkernel.milestonebackend.project.service;

import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(String authenticatedUserId, ProjectCreateRequest request);
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(UUID projectId);
    List<ProjectResponse> getProjectsByClientId(UUID clientId);
    ProjectResponse updateProject(String authenticatedUserId, UUID projectId, ProjectUpdateRequest request);
    void deleteProject(String authenticatedUserId, UUID projectId);
}