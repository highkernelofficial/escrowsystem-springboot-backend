package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundProjectTxnRequest {

    private String sender;

    @JsonProperty("app_id")
    private Long appId;

    /** All milestones for this project — each gets its own on-chain state entry */
    private List<MilestoneFundItem> milestones;

    /** Sum of all milestone amounts (in ALGOs) sent as single PaymentTxn */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneFundItem {

        @JsonProperty("milestone_id")
        private String milestoneId;

        private BigDecimal amount;
    }
}