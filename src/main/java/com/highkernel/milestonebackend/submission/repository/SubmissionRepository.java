package com.highkernel.milestonebackend.submission.repository;

import com.highkernel.milestonebackend.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByMilestoneIdOrderByCreatedAtDesc(UUID milestoneId);

    List<Submission> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);

    Optional<Submission> findTopByMilestoneIdAndFreelancerIdOrderByCreatedAtDesc(UUID milestoneId, UUID freelancerId);
}