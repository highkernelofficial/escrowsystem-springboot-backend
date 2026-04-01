package com.highkernel.milestonebackend.auth.controller;

import com.highkernel.milestonebackend.auth.security.WalletPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthTestController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal WalletPrincipal principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Authenticated successfully");
        response.put("userId", principal.getUserId());
        response.put("wallet", principal.getWalletAddress());
        return response;
    }
}