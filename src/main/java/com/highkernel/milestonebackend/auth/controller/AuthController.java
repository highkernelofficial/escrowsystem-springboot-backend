package com.highkernel.milestonebackend.auth.controller;

import com.highkernel.milestonebackend.auth.dto.AuthRequest;
import com.highkernel.milestonebackend.auth.dto.AuthResponse;
import com.highkernel.milestonebackend.auth.dto.NonceResponse;
import com.highkernel.milestonebackend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/nonce")
    public NonceResponse getNonce(@RequestParam("wallet") String wallet) {
        return authService.getNonce(wallet);
    }

    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody AuthRequest request) {
        return authService.verify(request);
    }
}