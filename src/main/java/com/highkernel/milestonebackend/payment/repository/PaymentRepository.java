package com.highkernel.milestonebackend.payment.repository;

import com.highkernel.milestonebackend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByMilestoneId(UUID milestoneId);

    List<Payment> findByFreelancerId(UUID freelancerId);

    List<Payment> findByMilestoneIdIn(List<UUID> milestoneIds);

    boolean existsByMilestoneIdAndStatusIn(UUID milestoneId, Collection<String> statuses);
}