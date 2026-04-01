package com.highkernel.milestonebackend.auth.service;

public interface CryptoService {
    boolean isValidWallet(String walletAddress);
    boolean verifySignature(String walletAddress, String message, String base64Signature);
}