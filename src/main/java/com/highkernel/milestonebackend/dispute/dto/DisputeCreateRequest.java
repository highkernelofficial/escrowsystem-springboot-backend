package com.highkernel.milestonebackend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeCreateRequest {

    @NotNull
    private UUID milestoneId;

    @NotBlank
    private String reason;
}