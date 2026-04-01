package com.highkernel.milestonebackend.auth.service.impl;

import com.highkernel.milestonebackend.auth.dto.AuthRequest;
import com.highkernel.milestonebackend.auth.dto.AuthResponse;
import com.highkernel.milestonebackend.auth.dto.NonceResponse;
import com.highkernel.milestonebackend.auth.security.JwtService;
import com.highkernel.milestonebackend.auth.service.AuthService;
import com.highkernel.milestonebackend.auth.service.CryptoService;
import com.highkernel.milestonebackend.auth.service.NonceService;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.exception.UnauthorizedException;
import com.highkernel.milestonebackend.user.entity.User;
import com.highkernel.milestonebackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final NonceService nonceService;
    private final CryptoService cryptoService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${auth.nonce.expiry-seconds:300}")
    private long expirySeconds;

    @Override
    public NonceResponse getNonce(String wallet) {
        String normalizedWallet = normalizeWallet(wallet);

        if (normalizedWallet == null || normalizedWallet.isBlank()) {
            throw new BadRequestException("wallet is required");
        }

        if (!cryptoService.isValidWallet(normalizedWallet)) {
            throw new BadRequestException("Invalid Algorand wallet address");
        }

        String message = nonceService.createMessage(normalizedWallet);

        return NonceResponse.builder()
                .message(message)
                .expiresInSeconds(expirySeconds)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verify(AuthRequest request) {
        String wallet = normalizeWallet(request.getWallet());

        if (wallet == null || wallet.isBlank()) {
            throw new BadRequestException("wallet is required");
        }

        if (!cryptoService.isValidWallet(wallet)) {
            throw new BadRequestException("Invalid Algorand wallet address");
        }

        String storedMessage = nonceService.getMessage(wallet);

        if (storedMessage == null) {
            throw new UnauthorizedException("Nonce missing or expired");
        }

        if (!storedMessage.equals(request.getMessage())) {
            throw new UnauthorizedException("Message mismatch");
        }

        boolean valid = cryptoService.verifySignature(wallet, storedMessage, request.getSignature());

        if (!valid) {
            throw new UnauthorizedException("Invalid signature");
        }

        nonceService.deleteMessage(wallet);

        User user = userRepository.findByWalletAddress(wallet)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .walletAddress(wallet)
                                .build()
                ));

        String token = jwtService.generateToken(user.getId().toString(), wallet);

        log.info("Wallet authenticated successfully: {}", wallet);

        return AuthResponse.builder()
                .token(token)
                .wallet(wallet)
                .userId(user.getId().toString())
                .build();
    }

    private String normalizeWallet(String wallet) {
        return wallet == null ? null : wallet.trim();
    }
}