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
        log.info("===== GET NONCE START =====");
        log.info("Raw wallet received: {}", wallet);

        String normalizedWallet = normalizeWallet(wallet);
        log.info("Normalized wallet: {}", normalizedWallet);

        if (normalizedWallet == null || normalizedWallet.isBlank()) {
            log.error("Wallet is null or blank");
            throw new BadRequestException("wallet is required");
        }

        try {
            log.info("Checking if wallet is valid...");
            boolean isValidWallet = cryptoService.isValidWallet(normalizedWallet);
            log.info("Wallet validity result: {}", isValidWallet);

            if (!isValidWallet) {
                log.error("Invalid Algorand wallet address: {}", normalizedWallet);
                throw new BadRequestException("Invalid Algorand wallet address");
            }

            log.info("Creating nonce message for wallet: {}", normalizedWallet);
            String message = nonceService.createMessage(normalizedWallet);
            log.info("Nonce message created successfully: {}", message);

            NonceResponse response = NonceResponse.builder()
                    .message(message)
                    .expiresInSeconds(expirySeconds)
                    .build();

            log.info("NonceResponse prepared successfully for wallet: {}", normalizedWallet);
            log.info("===== GET NONCE END =====");

            return response;

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while generating nonce for wallet: {}", normalizedWallet, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public AuthResponse verify(AuthRequest request) {
        log.info("===== VERIFY START =====");
        log.info("Verify called for wallet: {}", request.getWallet());

        String wallet = normalizeWallet(request.getWallet());
        log.info("Normalized wallet for verify: {}", wallet);

        if (wallet == null || wallet.isBlank()) {
            log.error("wallet is required during verify");
            throw new BadRequestException("wallet is required");
        }

        if (!cryptoService.isValidWallet(wallet)) {
            log.error("Invalid Algorand wallet address during verify: {}", wallet);
            throw new BadRequestException("Invalid Algorand wallet address");
        }

        log.info("Fetching stored nonce message for wallet: {}", wallet);
        String storedMessage = nonceService.getMessage(wallet);
        log.info("Stored message found: {}", storedMessage);

        if (storedMessage == null) {
            log.error("Nonce missing or expired for wallet: {}", wallet);
            throw new UnauthorizedException("Nonce missing or expired");
        }

        log.info("Comparing stored message with request message...");
        if (!storedMessage.equals(request.getMessage())) {
            log.error("Message mismatch for wallet: {}", wallet);
            log.error("Stored message: {}", storedMessage);
            log.error("Request message: {}", request.getMessage());
            throw new UnauthorizedException("Message mismatch");
        }

        log.info("Verifying signature for wallet: {}", wallet);
        boolean valid = cryptoService.verifySignature(wallet, storedMessage, request.getSignature());
        log.info("Signature verification result: {}", valid);

        if (!valid) {
            log.error("Invalid signature for wallet: {}", wallet);
            throw new UnauthorizedException("Invalid signature");
        }

        log.info("Deleting nonce after successful verification for wallet: {}", wallet);
        nonceService.deleteMessage(wallet);

        log.info("Finding user by wallet address: {}", wallet);
        User user = userRepository.findByWalletAddress(wallet)
                .orElseGet(() -> {
                    log.info("User not found, creating new user for wallet: {}", wallet);
                    return userRepository.save(
                            User.builder()
                                    .walletAddress(wallet)
                                    .build()
                    );
                });

        log.info("User resolved successfully. User ID: {}", user.getId());

        String token = jwtService.generateToken(user.getId().toString(), wallet);
        log.info("JWT generated successfully for wallet: {}", wallet);

        log.info("Wallet authenticated successfully: {}", wallet);
        log.info("===== VERIFY END =====");

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