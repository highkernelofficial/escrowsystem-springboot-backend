package com.highkernel.milestonebackend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NonceResponse {
    private String message;
    private long expiresInSeconds;
}