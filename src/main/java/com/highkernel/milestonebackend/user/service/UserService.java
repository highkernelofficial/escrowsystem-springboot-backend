package com.highkernel.milestonebackend.user.service;

import com.highkernel.milestonebackend.user.dto.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse getCurrentUser(String userId);
    UserResponse getUserById(UUID userId);
}