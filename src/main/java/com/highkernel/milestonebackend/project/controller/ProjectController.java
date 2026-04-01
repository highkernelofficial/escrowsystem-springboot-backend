package com.highkernel.milestonebackend.project.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;
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

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/client/{clientId}")
    public List<ProjectResponse> getProjectsByClientId(@PathVariable UUID clientId) {
        return projectService.getProjectsByClientId(clientId);
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
}