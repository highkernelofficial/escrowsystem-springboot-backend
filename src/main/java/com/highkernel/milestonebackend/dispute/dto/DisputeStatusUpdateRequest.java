package com.highkernel.milestonebackend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeStatusUpdateRequest {

    @NotBlank
    private String status;
}