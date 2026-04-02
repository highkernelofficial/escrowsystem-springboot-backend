package com.highkernel.milestonebackend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneGenerationItem {

    private String title;
    private String description;
    private Long amount;
    private BigDecimal percentage;
}