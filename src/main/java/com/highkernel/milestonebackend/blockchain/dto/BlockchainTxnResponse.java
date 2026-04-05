package com.highkernel.milestonebackend.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTxnResponse {

    @Builder.Default
    private List<String> txns = new ArrayList<>();

    @JsonAlias({"escrow_address", "escrowAddress"})
    private String escrowAddress;

    private String message;

    @JsonSetter("txn")
    public void setTxn(Object value) {
        this.txns = normalize(value);
    }

    @JsonSetter("transactions")
    public void setTransactions(Object value) {
        this.txns = normalize(value);
    }

    @JsonIgnore
    public String getTxn() {
        return (txns == null || txns.isEmpty()) ? null : txns.get(0);
    }

    private List<String> normalize(Object value) {
        List<String> result = new ArrayList<>();

        if (value == null) {
            return result;
        }

        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    String txn = String.valueOf(item).trim();
                    if (!txn.isBlank()) {
                        result.add(txn);
                    }
                }
            }
            return result;
        }

        String txn = String.valueOf(value).trim();
        if (!txn.isBlank()) {
            result.add(txn);
        }

        return result;
    }
}