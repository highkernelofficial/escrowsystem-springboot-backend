package com.highkernel.milestonebackend.milestone.repository;

import com.highkernel.milestonebackend.milestone.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    List<Milestone> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    List<Milestone> findByProjectIdInOrderByCreatedAtAsc(List<UUID> projectIds);

    long countByProjectId(UUID projectId);

    void deleteByProjectId(UUID projectId);
}