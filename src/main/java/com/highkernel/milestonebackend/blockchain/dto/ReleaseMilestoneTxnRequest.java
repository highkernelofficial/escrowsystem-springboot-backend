package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseMilestoneTxnRequest {

    private String sender;

    @JsonProperty("app_id")
    private Long appId;

    @JsonProperty("milestone_id")
    private String milestoneId;

    @JsonProperty("freelancer_address")
    private String freelancerAddress;
}