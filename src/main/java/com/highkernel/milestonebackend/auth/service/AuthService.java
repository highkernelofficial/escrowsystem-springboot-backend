package com.highkernel.milestonebackend.auth.service;

import com.highkernel.milestonebackend.auth.dto.AuthRequest;
import com.highkernel.milestonebackend.auth.dto.AuthResponse;
import com.highkernel.milestonebackend.auth.dto.NonceResponse;

public interface AuthService {
    NonceResponse getNonce(String wallet);
    AuthResponse verify(AuthRequest request);
}