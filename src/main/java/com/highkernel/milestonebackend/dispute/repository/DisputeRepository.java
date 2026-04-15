package com.highkernel.milestonebackend.dispute.repository;

import com.highkernel.milestonebackend.dispute.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    List<Dispute> findByMilestoneIdOrderByCreatedAtDesc(UUID milestoneId);

    List<Dispute> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);

    List<Dispute> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @Query("SELECT d FROM Dispute d WHERE d.milestoneId IN " +
           "(SELECT m.id FROM Milestone m WHERE m.projectId = :projectId) " +
           "ORDER BY d.createdAt DESC")
    List<Dispute> findByProjectIdOrderByCreatedAtDesc(@Param("projectId") UUID projectId);

    List<Dispute> findByMilestoneIdIn(List<UUID> milestoneIds);
}