package com.highkernel.milestonebackend.submission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "github_link")
    private String githubLink;

    @Column(name = "demo_link")
    private String demoLink;

    @Column(name = "description")
    private String description;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "ai_feedback")
    private String aiFeedback;

    @Column(name = "ai_raw", columnDefinition = "jsonb")
    private String aiRaw;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}