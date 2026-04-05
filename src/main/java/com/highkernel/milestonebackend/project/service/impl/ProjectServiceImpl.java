package com.highkernel.milestonebackend.project.service.impl;

import com.highkernel.milestonebackend.ai.client.AiValidatorClient;
import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationItem;
import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationRequest;
import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationResponse;
import com.highkernel.milestonebackend.blockchain.client.BlockchainClient;
import com.highkernel.milestonebackend.blockchain.dto.BlockchainTxnResponse;
import com.highkernel.milestonebackend.blockchain.dto.DeployContractRequest;
import com.highkernel.milestonebackend.blockchain.dto.FundProjectTxnRequest;
import com.highkernel.milestonebackend.blockchain.dto.GetAppIdRequest;
import com.highkernel.milestonebackend.blockchain.dto.GetAppIdResponse;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.milestone.dto.MilestoneItemRequest;
import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import com.highkernel.milestonebackend.milestone.entity.Milestone;
import com.highkernel.milestonebackend.milestone.repository.MilestoneRepository;
import com.highkernel.milestonebackend.project.dto.ConfirmProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.GeneratedMilestonesPreviewResponse;
import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectDeployConfirmRequest;
import com.highkernel.milestonebackend.project.dto.ProjectDeployPrepareResponse;
import com.highkernel.milestonebackend.project.dto.ProjectFundConfirmRequest;
import com.highkernel.milestonebackend.project.dto.ProjectFundPrepareRequest;
import com.highkernel.milestonebackend.project.dto.ProjectFundPrepareResponse;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectWithMilestonesResponse;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import com.highkernel.milestonebackend.project.service.ProjectService;
import com.highkernel.milestonebackend.user.entity.User;
import com.highkernel.milestonebackend.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final AiValidatorClient aiValidatorClient;
    private final BlockchainClient blockchainClient;
    private final UserRepository userRepository;

    @Value("${spring.datasource.url:NOT_FOUND}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:NOT_FOUND}")
    private String datasourceUsername;

    @PostConstruct
    public void debugDatasourceConfig() {
        System.out.println("\n================ DATASOURCE DEBUG ================");
        System.out.println("spring.datasource.url = " + datasourceUrl);
        System.out.println("spring.datasource.username = " + datasourceUsername);
        System.out.println("==================================================\n");
    }

    @Override
    public ProjectResponse createProject(String authenticatedUserId, ProjectCreateRequest request) {
        UUID clientId = parseUUID(authenticatedUserId);

        Project project = Project.builder()
                .clientId(clientId)
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .expectedOutcome(request.getExpectedOutcome())
                .status(request.getStatus() != null ? request.getStatus() : "OPEN")
                .appId(request.getAppId())
                .totalAmount(request.getTotalAmount())
                .build();

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Override
    public GeneratedMilestonesPreviewResponse generateMilestonesPreview(String authenticatedUserId, ProjectCreateRequest request) {
        parseUUID(authenticatedUserId);

        MilestoneGenerationRequest aiRequest = MilestoneGenerationRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .expectedOutcome(request.getExpectedOutcome())
                .totalBudget(request.getTotalAmount())
                .build();

        MilestoneGenerationResponse aiResponse = aiValidatorClient.generateMilestones(aiRequest);

        if (aiResponse == null || aiResponse.getMilestones() == null || aiResponse.getMilestones().isEmpty()) {
            throw new BadRequestException("AI validator returned no milestones");
        }

        List<MilestoneResponse> previewMilestones = aiResponse.getMilestones()
                .stream()
                .map(this::mapGeneratedMilestonePreview)
                .toList();

        return GeneratedMilestonesPreviewResponse.builder()
                .milestones(previewMilestones)
                .build();
    }

    @Override
    @Transactional
    public ProjectWithMilestonesResponse confirmCreateProject(String authenticatedUserId, ConfirmProjectCreateRequest request) {
        UUID clientId = parseUUID(authenticatedUserId);

        Project project = Project.builder()
                .clientId(clientId)
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .expectedOutcome(request.getExpectedOutcome())
                .status(request.getStatus() != null ? request.getStatus() : "OPEN")
                .appId(request.getAppId())
                .totalAmount(request.getTotalAmount())
                .build();

        Project savedProject = projectRepository.save(project);

        List<Milestone> milestonesToSave = request.getMilestones()
                .stream()
                .map(item -> buildMilestoneFromConfirmedItem(savedProject.getId(), item))
                .toList();

        List<MilestoneResponse> savedMilestones = milestoneRepository.saveAll(milestonesToSave)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();

        return ProjectWithMilestonesResponse.builder()
                .project(mapToResponse(savedProject))
                .milestones(savedMilestones)
                .build();
    }

    @Override
    @Transactional
    public ProjectWithMilestonesResponse createProjectWithGeneratedMilestones(String authenticatedUserId, ProjectCreateRequest request) {
        UUID clientId = parseUUID(authenticatedUserId);

        MilestoneGenerationRequest aiRequest = MilestoneGenerationRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .expectedOutcome(request.getExpectedOutcome())
                .totalBudget(request.getTotalAmount())
                .build();

        MilestoneGenerationResponse aiResponse = aiValidatorClient.generateMilestones(aiRequest);

        if (aiResponse == null || aiResponse.getMilestones() == null || aiResponse.getMilestones().isEmpty()) {
            throw new BadRequestException("AI validator returned no milestones");
        }

        Project project = Project.builder()
                .clientId(clientId)
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .expectedOutcome(request.getExpectedOutcome())
                .status(request.getStatus() != null ? request.getStatus() : "OPEN")
                .appId(request.getAppId())
                .totalAmount(request.getTotalAmount())
                .build();

        Project savedProject = projectRepository.save(project);

        List<Milestone> milestonesToSave = aiResponse.getMilestones()
                .stream()
                .map(item -> buildGeneratedMilestone(savedProject.getId(), item))
                .toList();

        List<MilestoneResponse> savedMilestones = milestoneRepository.saveAll(milestonesToSave)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();

        return ProjectWithMilestonesResponse.builder()
                .project(mapToResponse(savedProject))
                .milestones(savedMilestones)
                .build();
    }

    @Override
    @Transactional
    public ProjectDeployPrepareResponse prepareDeployContract(String authenticatedUserId, UUID projectId) {
        UUID userId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(userId, project);

        if (project.getAppId() != null && project.getAppId() > 0) {
            throw new BadRequestException("Project already has app_id. Contract is already deployed.");
        }

        User client = getUserOrThrow(userId);

        if (client.getWalletAddress() == null || client.getWalletAddress().isBlank()) {
            throw new BadRequestException("Client wallet address not found");
        }

        BlockchainTxnResponse response = blockchainClient.prepareDeployContractTxn(
                DeployContractRequest.builder()
                        .sender(client.getWalletAddress())
                        .build()
        );

        return ProjectDeployPrepareResponse.builder()
                .projectId(project.getId())
                .unsignedTxn(response.getTxn())
                .message("Unsigned deploy-contract transaction prepared successfully")
                .build();
    }

    @Override
    @Transactional
    public ProjectResponse confirmDeployContract(String authenticatedUserId, ProjectDeployConfirmRequest request) {
        UUID userId = parseUUID(authenticatedUserId);

        if (request == null || request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }

        if (request.getTxnId() == null || request.getTxnId().isBlank()) {
            throw new BadRequestException("txnId is required");
        }

        Project project = getProjectOrThrow(request.getProjectId());
        validateProjectOwnership(userId, project);

        if (project.getAppId() != null && project.getAppId() > 0) {
            throw new BadRequestException("Project already has app_id. Contract is already deployed.");
        }

        GetAppIdResponse appIdResponse = blockchainClient.getAppId(
                GetAppIdRequest.builder()
                        .txnId(request.getTxnId().trim())
                        .build()
        );

        if (appIdResponse == null || appIdResponse.getAppId() == null || appIdResponse.getAppId() <= 0) {
            throw new BadRequestException("Failed to fetch valid appId from blockchain transaction");
        }

        project.setAppId(appIdResponse.getAppId());

        if (project.getStatus() == null || project.getStatus().isBlank() || "OPEN".equalsIgnoreCase(project.getStatus())) {
            project.setStatus("CREATED");
        }

        return mapToResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectFundPrepareResponse prepareFundProject(String authenticatedUserId, UUID projectId, ProjectFundPrepareRequest request) {
        UUID userId = parseUUID(authenticatedUserId);

        if (request == null || request.getMilestoneId() == null) {
            throw new BadRequestException("Milestone ID is required");
        }

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(userId, project);

        if (project.getAppId() == null || project.getAppId() <= 0) {
            throw new BadRequestException("Project app_id is missing. Deploy contract first.");
        }

        if ("FUNDED".equalsIgnoreCase(project.getStatus())) {
            throw new BadRequestException("Project is already funded");
        }

        Milestone milestone = getMilestoneOrThrow(request.getMilestoneId());

        if (!projectId.equals(milestone.getProjectId())) {
            throw new BadRequestException("Milestone does not belong to this project");
        }

        if (milestone.getAmount() == null) {
            throw new BadRequestException("Milestone amount is missing");
        }

        if (milestone.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Milestone amount must be greater than zero");
        }

        User client = getUserOrThrow(userId);

        if (client.getWalletAddress() == null || client.getWalletAddress().isBlank()) {
            throw new BadRequestException("Client wallet address not found");
        }

        BlockchainTxnResponse response = blockchainClient.prepareFundProjectTxn(
                FundProjectTxnRequest.builder()
                        .sender(client.getWalletAddress())
                        .appId(project.getAppId())
                        .milestoneId(milestone.getId().toString())
                        .amount(milestone.getAmount())
                        .build()
        );

        if (response.getTxns() == null || response.getTxns().isEmpty()) {
            throw new BadRequestException("FastAPI returned empty funding transaction payload");
        }

        return ProjectFundPrepareResponse.builder()
                .projectId(project.getId())
                .milestoneId(milestone.getId())
                .appId(project.getAppId())
                .amount(milestone.getAmount())
                .unsignedTxn(response.getTxn())
                .unsignedTxns(response.getTxns())
                .message("Unsigned funding transaction(s) prepared successfully")
                .build();
    }

    @Override
    @Transactional
    public ProjectResponse confirmFundProject(String authenticatedUserId, ProjectFundConfirmRequest request) {
        UUID userId = parseUUID(authenticatedUserId);

        if (request == null || request.getProjectId() == null) {
            throw new BadRequestException("Project ID is required");
        }

        if (request.getTxnHash() == null || request.getTxnHash().isBlank()) {
            throw new BadRequestException("txnHash is required");
        }

        Project project = getProjectOrThrow(request.getProjectId());
        validateProjectOwnership(userId, project);

        if (project.getAppId() == null || project.getAppId() <= 0) {
            throw new BadRequestException("Project app_id is missing. Deploy contract first.");
        }

        if ("FUNDED".equalsIgnoreCase(project.getStatus())) {
            throw new BadRequestException("Project already funded");
        }

        if (request.getTxnHash().startsWith("simulated_hash_")) {
            throw new BadRequestException("Simulated txnHash is not allowed. Please complete real wallet transaction.");
        }

        project.setFundingTxnHash(request.getTxnHash().trim());
        project.setStatus("FUNDED");

        return mapToResponse(projectRepository.save(project));
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProjectWithMilestonesResponse> getAllProjectsWithMilestones() {
        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDesc();

        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> projectIds = projects.stream()
                .map(Project::getId)
                .toList();

        List<Milestone> allMilestones = milestoneRepository.findByProjectIdInOrderByCreatedAtAsc(projectIds);

        Map<UUID, List<Milestone>> milestonesByProjectId = allMilestones.stream()
                .collect(Collectors.groupingBy(Milestone::getProjectId));

        return projects.stream()
                .map(project -> ProjectWithMilestonesResponse.builder()
                        .project(mapToResponse(project))
                        .milestones(
                                milestonesByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                        .stream()
                                        .map(this::mapMilestoneToResponse)
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

    @Override
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = getProjectOrThrow(projectId);
        return mapToResponse(project);
    }

    @Override
    public ProjectWithMilestonesResponse getProjectWithMilestones(UUID projectId) {
        Project project = getProjectOrThrow(projectId);

        List<MilestoneResponse> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();

        return ProjectWithMilestonesResponse.builder()
                .project(mapToResponse(project))
                .milestones(milestones)
                .build();
    }

    @Override
    public List<ProjectResponse> getProjectsByClientId(UUID clientId) {
        return projectRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProjectResponse> getMyOwnedProjects(String authenticatedUserId) {
        UUID clientId = parseUUID(authenticatedUserId);

        return projectRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProjectWithMilestonesResponse> getMyOwnedProjectsWithMilestones(String authenticatedUserId) {
        UUID clientId = parseUUID(authenticatedUserId);

        List<Project> projects = projectRepository.findByClientId(clientId)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> projectIds = projects.stream()
                .map(Project::getId)
                .toList();

        List<Milestone> allMilestones = milestoneRepository.findByProjectIdInOrderByCreatedAtAsc(projectIds);

        Map<UUID, List<Milestone>> milestonesByProjectId = allMilestones.stream()
                .collect(Collectors.groupingBy(Milestone::getProjectId));

        return projects.stream()
                .map(project -> ProjectWithMilestonesResponse.builder()
                        .project(mapToResponse(project))
                        .milestones(
                                milestonesByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                        .stream()
                                        .map(this::mapMilestoneToResponse)
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

    @Override
    public ProjectWithMilestonesResponse getMyOwnedProjectWithMilestones(String authenticatedUserId, UUID projectId) {
        UUID clientId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(clientId, project);

        List<MilestoneResponse> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();

        return ProjectWithMilestonesResponse.builder()
                .project(mapToResponse(project))
                .milestones(milestones)
                .build();
    }

    @Override
    public List<ProjectResponse> getMyWorkspaceProjects(String authenticatedUserId) {
        UUID freelancerId = parseUUID(authenticatedUserId);

        return projectRepository.findWorkspaceProjectsByFreelancerId(freelancerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProjectWithMilestonesResponse> getMyWorkspaceProjectsWithMilestones(String authenticatedUserId) {
        UUID freelancerId = parseUUID(authenticatedUserId);

        List<Project> projects = projectRepository.findWorkspaceProjectsByFreelancerId(freelancerId);

        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> projectIds = projects.stream()
                .map(Project::getId)
                .toList();

        List<Milestone> allMilestones = milestoneRepository.findByProjectIdInOrderByCreatedAtAsc(projectIds);

        Map<UUID, List<Milestone>> milestonesByProjectId = allMilestones.stream()
                .collect(Collectors.groupingBy(Milestone::getProjectId));

        return projects.stream()
                .map(project -> ProjectWithMilestonesResponse.builder()
                        .project(mapToResponse(project))
                        .milestones(
                                milestonesByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                        .stream()
                                        .map(this::mapMilestoneToResponse)
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

    @Override
    public ProjectWithMilestonesResponse getMyWorkspaceProjectWithMilestones(String authenticatedUserId, UUID projectId) {
        UUID freelancerId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);

        boolean isPartOfProject = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .anyMatch(milestone -> freelancerId.equals(milestone.getAssignedFreelancer()));

        if (!isPartOfProject) {
            throw new BadRequestException("You are not assigned to this project");
        }

        List<MilestoneResponse> milestones = milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();

        return ProjectWithMilestonesResponse.builder()
                .project(mapToResponse(project))
                .milestones(milestones)
                .build();
    }

    @Override
    public ProjectResponse updateProject(String authenticatedUserId, UUID projectId, ProjectUpdateRequest request) {
        UUID userId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(userId, project);

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getTechStack() != null) {
            project.setTechStack(request.getTechStack());
        }
        if (request.getExpectedOutcome() != null) {
            project.setExpectedOutcome(request.getExpectedOutcome());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getAppId() != null) {
            project.setAppId(request.getAppId());
        }
        if (request.getTotalAmount() != null) {
            project.setTotalAmount(request.getTotalAmount());
        }

        return mapToResponse(projectRepository.save(project));
    }

    @Override
    public void deleteProject(String authenticatedUserId, UUID projectId) {
        UUID userId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(userId, project);

        projectRepository.delete(project);
    }

    @Override
    public List<MilestoneResponse> generateMilestones(String authenticatedUserId, UUID projectId, Boolean overwriteExisting) {
        UUID userId = parseUUID(authenticatedUserId);

        Project project = getProjectOrThrow(projectId);
        validateProjectOwnership(userId, project);

        long existingMilestoneCount = milestoneRepository.countByProjectId(projectId);
        if (existingMilestoneCount > 0 && !Boolean.TRUE.equals(overwriteExisting)) {
            throw new BadRequestException("Milestones already exist for this project. Pass overwriteExisting=true to regenerate.");
        }

        if (existingMilestoneCount > 0) {
            milestoneRepository.deleteByProjectId(projectId);
        }

        MilestoneGenerationRequest aiRequest = MilestoneGenerationRequest.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .expectedOutcome(project.getExpectedOutcome())
                .totalBudget(project.getTotalAmount())
                .build();

        MilestoneGenerationResponse aiResponse = aiValidatorClient.generateMilestones(aiRequest);

        if (aiResponse == null || aiResponse.getMilestones() == null || aiResponse.getMilestones().isEmpty()) {
            throw new BadRequestException("AI validator returned no milestones");
        }

        List<Milestone> milestones = aiResponse.getMilestones()
                .stream()
                .map(item -> buildGeneratedMilestone(projectId, item))
                .toList();

        return milestoneRepository.saveAll(milestones)
                .stream()
                .map(this::mapMilestoneToResponse)
                .toList();
    }

    private Milestone buildGeneratedMilestone(UUID projectId, MilestoneGenerationItem item) {
        return Milestone.builder()
                .projectId(projectId)
                .title(item.getTitle())
                .description(item.getDescription())
                .amount(item.getAmount())
                .percentage(item.getPercentage())
                .status("PENDING")
                .attemptCount(0)
                .assignedFreelancer(null)
                .reassignmentReason(null)
                .build();
    }

    private Milestone buildMilestoneFromConfirmedItem(UUID projectId, MilestoneItemRequest item) {
        return Milestone.builder()
                .projectId(projectId)
                .title(item.getTitle())
                .description(item.getDescription())
                .amount(item.getAmount())
                .percentage(item.getPercentage())
                .status("PENDING")
                .attemptCount(0)
                .assignedFreelancer(null)
                .reassignmentReason(null)
                .build();
    }

    private MilestoneResponse mapGeneratedMilestonePreview(MilestoneGenerationItem item) {
        return MilestoneResponse.builder()
                .id(null)
                .projectId(null)
                .title(item.getTitle())
                .description(item.getDescription())
                .amount(item.getAmount())
                .assignedFreelancer(null)
                .status("PENDING")
                .attemptCount(0)
                .reassignmentReason(null)
                .percentage(item.getPercentage())
                .createdAt(null)
                .build();
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private Milestone getMilestoneOrThrow(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private void validateProjectOwnership(UUID userId, Project project) {
        if (project.getClientId() == null || !project.getClientId().equals(userId)) {
            throw new BadRequestException("Only project owner can perform this action");
        }
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID");
        }
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .expectedOutcome(project.getExpectedOutcome())
                .status(project.getStatus())
                .appId(project.getAppId())
                .totalAmount(project.getTotalAmount())
                .fundingTxnHash(project.getFundingTxnHash())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private MilestoneResponse mapMilestoneToResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProjectId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .amount(milestone.getAmount())
                .assignedFreelancer(milestone.getAssignedFreelancer())
                .status(milestone.getStatus())
                .attemptCount(milestone.getAttemptCount())
                .reassignmentReason(milestone.getReassignmentReason())
                .percentage(milestone.getPercentage())
                .createdAt(milestone.getCreatedAt())
                .build();
    }
}