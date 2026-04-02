package com.highkernel.milestonebackend.dispute.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeResponse {

    private UUID id;
    private UUID milestoneId;
    private UUID clientId;
    private UUID freelancerId;
    private String reason;
    private String status;
    private OffsetDateTime createdAt;
}