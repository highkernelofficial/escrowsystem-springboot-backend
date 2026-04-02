package com.highkernel.milestonebackend.project.dto;

import com.highkernel.milestonebackend.milestone.dto.MilestoneResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedMilestonesPreviewResponse {

    private List<MilestoneResponse> milestones;
}