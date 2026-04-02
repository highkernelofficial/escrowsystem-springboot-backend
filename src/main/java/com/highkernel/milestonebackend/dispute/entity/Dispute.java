package com.highkernel.milestonebackend.dispute.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    private String reason;

    private String status;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}