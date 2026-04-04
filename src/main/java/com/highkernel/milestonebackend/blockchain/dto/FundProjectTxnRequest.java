package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundProjectTxnRequest {
    private String sender;

    @JsonProperty("escrow_address")
    private String escrowAddress;

    private BigDecimal amount;
}