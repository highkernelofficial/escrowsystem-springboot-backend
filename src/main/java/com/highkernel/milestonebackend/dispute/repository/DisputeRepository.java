package com.highkernel.milestonebackend.dispute.repository;

import com.highkernel.milestonebackend.dispute.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    List<Dispute> findByMilestoneIdOrderByCreatedAtDesc(UUID milestoneId);

    List<Dispute> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);

    List<Dispute> findByClientIdOrderByCreatedAtDesc(UUID clientId);
}