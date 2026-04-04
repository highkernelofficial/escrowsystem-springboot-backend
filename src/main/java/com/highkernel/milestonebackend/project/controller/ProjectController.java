package com.highkernel.milestonebackend.project.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.project.dto.ConfirmProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.GenerateMilestonesRequest;
import com.highkernel.milestonebackend.project.dto.GeneratedMilestonesPreviewResponse;
import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectDeployConfirmRequest;
import com.highkernel.milestonebackend.project.dto.ProjectDeployPrepareResponse;
import com.highkernel.milestonebackend.project.dto.ProjectFundConfirmRequest;
import com.highkernel.milestonebackend.project.dto.ProjectFundPrepareResponse;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectWithMilestonesResponse;
import com.highkernel.milestonebackend.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectResponse createProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        return projectService.createProject(principal.getUserId(), request);
    }

    @PostMapping("/{projectId}/deploy-contract/prepare")
    public ProjectDeployPrepareResponse prepareDeployContract(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return projectService.prepareDeployContract(principal.getUserId(), projectId);
    }

    @PostMapping("/deploy-contract/confirm")
    public ProjectResponse confirmDeployContract(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ProjectDeployConfirmRequest request
    ) {
        return projectService.confirmDeployContract(principal.getUserId(), request);
    }

    @PostMapping("/{projectId}/fund/prepare")
    public ProjectFundPrepareResponse prepareFundProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return projectService.prepareFundProject(principal.getUserId(), projectId);
    }

    @PostMapping("/fund/confirm")
    public ProjectResponse confirmFundProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ProjectFundConfirmRequest request
    ) {
        return projectService.confirmFundProject(principal.getUserId(), request);
    }

    @PostMapping("/generate-milestones-preview")
    public GeneratedMilestonesPreviewResponse generateMilestonesPreview(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        return projectService.generateMilestonesPreview(principal.getUserId(), request);
    }

    @PostMapping("/confirm-create")
    public ProjectWithMilestonesResponse confirmCreateProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ConfirmProjectCreateRequest request
    ) {
        return projectService.confirmCreateProject(principal.getUserId(), request);
    }

    @PostMapping("/with-generated-milestones")
    public ProjectWithMilestonesResponse createProjectWithGeneratedMilestones(
            @AuthenticationPrincipal WalletPrincipal principal,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        return projectService.createProjectWithGeneratedMilestones(principal.getUserId(), request);
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/with-milestones")
    public List<ProjectWithMilestonesResponse> getAllProjectsWithMilestones() {
        return projectService.getAllProjectsWithMilestones();
    }

    @GetMapping("/{id}/with-milestones")
    public ProjectWithMilestonesResponse getProjectWithMilestones(@PathVariable UUID id) {
        return projectService.getProjectWithMilestones(id);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/client/{clientId}")
    public List<ProjectResponse> getProjectsByClientId(@PathVariable UUID clientId) {
        return projectService.getProjectsByClientId(clientId);
    }

    @GetMapping("/me/owned")
    public List<ProjectResponse> getMyOwnedProjects(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return projectService.getMyOwnedProjects(principal.getUserId());
    }

    @GetMapping("/me/owned/with-milestones")
    public List<ProjectWithMilestonesResponse> getMyOwnedProjectsWithMilestones(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return projectService.getMyOwnedProjectsWithMilestones(principal.getUserId());
    }

    @GetMapping("/me/owned/{id}/with-milestones")
    public ProjectWithMilestonesResponse getMyOwnedProjectWithMilestones(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return projectService.getMyOwnedProjectWithMilestones(principal.getUserId(), id);
    }

    @GetMapping("/me/workspace")
    public List<ProjectResponse> getMyWorkspaceProjects(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return projectService.getMyWorkspaceProjects(principal.getUserId());
    }

    @GetMapping("/me/workspace/with-milestones")
    public List<ProjectWithMilestonesResponse> getMyWorkspaceProjectsWithMilestones(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {
        return projectService.getMyWorkspaceProjectsWithMilestones(principal.getUserId());
    }

    @GetMapping("/me/workspace/{id}/with-milestones")
    public ProjectWithMilestonesResponse getMyWorkspaceProjectWithMilestones(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        return projectService.getMyWorkspaceProjectWithMilestones(principal.getUserId(), id);
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id,
            @RequestBody ProjectUpdateRequest request
    ) {
        return projectService.updateProject(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID id
    ) {
        projectService.deleteProject(principal.getUserId(), id);
    }

    @PostMapping("/{projectId}/generate-milestones")
    public List<MilestoneResponse> generateMilestones(
            @AuthenticationPrincipal WalletPrincipal principal,
            @PathVariable UUID projectId,
            @RequestBody(required = false) GenerateMilestonesRequest request
    ) {
        boolean overwriteExisting = request != null && Boolean.TRUE.equals(request.getOverwriteExisting());
        return projectService.generateMilestones(principal.getUserId(), projectId, overwriteExisting);
    }
}