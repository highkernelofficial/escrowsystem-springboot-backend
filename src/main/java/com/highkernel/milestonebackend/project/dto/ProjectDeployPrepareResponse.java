package com.highkernel.milestonebackend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDeployPrepareResponse {
    private UUID projectId;
    private String unsignedTxn;
    private String message;
}