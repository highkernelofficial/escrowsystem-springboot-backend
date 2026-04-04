package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTxnResponse {

    @JsonAlias({"txn", "transaction", "unsignedTxn"})
    private String txn;

    @JsonAlias({"escrow_address", "escrowAddress"})
    private String escrowAddress;

    private String message;
}