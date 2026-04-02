package com.highkernel.milestonebackend.project.repository;

import com.highkernel.milestonebackend.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByClientId(UUID clientId);

    List<Project> findAllByOrderByCreatedAtDesc();

    @Query("""
            select distinct p
            from Project p, Milestone m
            where m.projectId = p.id
              and m.assignedFreelancer = :freelancerId
            order by p.createdAt desc
            """)
    List<Project> findWorkspaceProjectsByFreelancerId(@Param("freelancerId") UUID freelancerId);
}