package com.highkernel.milestonebackend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {

    private UUID id;
    private UUID projectId;
    private UUID freelancerId;
    private String name;
    private String email;
    private String mobileNumber;
    private String linkedin;
    private String github;
    private String proposal;
    private String status;
    private OffsetDateTime createdAt;
}