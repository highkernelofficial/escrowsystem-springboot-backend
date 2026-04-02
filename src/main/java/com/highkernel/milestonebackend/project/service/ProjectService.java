package com.highkernel.milestonebackend.project.service;

import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.project.dto.ConfirmProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.GeneratedMilestonesPreviewResponse;
import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectWithMilestonesResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(String authenticatedUserId, ProjectCreateRequest request);

    GeneratedMilestonesPreviewResponse generateMilestonesPreview(String authenticatedUserId, ProjectCreateRequest request);

    ProjectWithMilestonesResponse confirmCreateProject(String authenticatedUserId, ConfirmProjectCreateRequest request);

    ProjectWithMilestonesResponse createProjectWithGeneratedMilestones(String authenticatedUserId, ProjectCreateRequest request);

    List<ProjectResponse> getAllProjects();

    List<ProjectWithMilestonesResponse> getAllProjectsWithMilestones();

    ProjectResponse getProjectById(UUID projectId);

    ProjectWithMilestonesResponse getProjectWithMilestones(UUID projectId);

    List<ProjectResponse> getProjectsByClientId(UUID clientId);

    List<ProjectResponse> getMyOwnedProjects(String authenticatedUserId);

    List<ProjectWithMilestonesResponse> getMyOwnedProjectsWithMilestones(String authenticatedUserId);

    ProjectWithMilestonesResponse getMyOwnedProjectWithMilestones(String authenticatedUserId, UUID projectId);

    List<ProjectResponse> getMyWorkspaceProjects(String authenticatedUserId);

    List<ProjectWithMilestonesResponse> getMyWorkspaceProjectsWithMilestones(String authenticatedUserId);

    ProjectWithMilestonesResponse getMyWorkspaceProjectWithMilestones(String authenticatedUserId, UUID projectId);

    ProjectResponse updateProject(String authenticatedUserId, UUID projectId, ProjectUpdateRequest request);

    void deleteProject(String authenticatedUserId, UUID projectId);

    List<MilestoneResponse> generateMilestones(String authenticatedUserId, UUID projectId, Boolean overwriteExisting);
}