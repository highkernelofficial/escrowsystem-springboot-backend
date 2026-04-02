package com.highkernel.milestonebackend.application.repository;

import com.highkernel.milestonebackend.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByProjectIdAndFreelancerId(UUID projectId, UUID freelancerId);

    List<Application> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Application> findByFreelancerIdOrderByCreatedAtDesc(UUID freelancerId);
}