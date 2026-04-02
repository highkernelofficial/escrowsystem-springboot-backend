package com.highkernel.milestonebackend.submission.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;
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
    private UUID id;

    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "github_link")
    private String githubLink;

    @Column(name = "demo_link")
    private String demoLink;

    private String description;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "ai_feedback")
    private String aiFeedback;

    // 🔥 FIX: jsonb mapping
    @JdbcTypeCode(Types.OTHER)
    @Column(name = "ai_raw", columnDefinition = "jsonb")
    private JsonNode aiRaw;

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