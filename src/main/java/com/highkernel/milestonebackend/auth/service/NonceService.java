package com.highkernel.milestonebackend.auth.service;

public interface NonceService {
    String createMessage(String walletAddress);
    String getMessage(String walletAddress);
    void deleteMessage(String walletAddress);
}