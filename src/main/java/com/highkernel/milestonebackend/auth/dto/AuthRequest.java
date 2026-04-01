package com.highkernel.milestonebackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "wallet is required")
    private String wallet;

    @NotBlank(message = "message is required")
    private String message;

    @NotBlank(message = "signature is required")
    private String signature;
}