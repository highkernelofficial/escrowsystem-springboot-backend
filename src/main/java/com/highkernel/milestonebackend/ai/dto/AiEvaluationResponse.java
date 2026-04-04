package com.highkernel.milestonebackend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEvaluationResponse {

    private Integer score;
    private Boolean approved;
    private String feedback;
}