package com.highkernel.milestonebackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonceRequest {

    @NotBlank(message = "wallet is required")
    private String wallet;
}