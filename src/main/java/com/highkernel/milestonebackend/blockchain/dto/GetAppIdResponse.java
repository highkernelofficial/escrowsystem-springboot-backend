package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAppIdResponse {

    @JsonProperty("app_id")
    @JsonAlias({"appId"})
    private Long appId;

    private String message;
}