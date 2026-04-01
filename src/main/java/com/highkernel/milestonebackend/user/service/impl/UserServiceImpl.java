package com.highkernel.milestonebackend.user.service.impl;

import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.user.dto.UserResponse;
import com.highkernel.milestonebackend.user.entity.User;
import com.highkernel.milestonebackend.user.repository.UserRepository;
import com.highkernel.milestonebackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser(String userId) {
        UUID uuid = parseUuid(userId, "Invalid authenticated user id");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return mapToResponse(user);
    }

    private UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException(errorMessage);
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .walletAddress(user.getWalletAddress())
                .createdAt(user.getCreatedAt())
                .build();
    }
}