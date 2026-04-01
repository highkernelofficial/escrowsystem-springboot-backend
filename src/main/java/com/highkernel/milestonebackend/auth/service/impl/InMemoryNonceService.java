package com.highkernel.milestonebackend.auth.service.impl;

import com.highkernel.milestonebackend.auth.service.NonceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InMemoryNonceService implements NonceService {

    private final Map<String, NonceEntry> store = new ConcurrentHashMap<>();

    @Value("${auth.nonce.expiry-seconds:300}")
    private long expirySeconds;

    @Override
    public String createMessage(String walletAddress) {
        String nonce = UUID.randomUUID().toString();
        String message = "Login to MyApp: " + nonce;
        Instant expiresAt = Instant.now().plusSeconds(expirySeconds);

        store.put(walletAddress, new NonceEntry(message, expiresAt));
        log.info("Nonce created for wallet {}", walletAddress);

        return message;
    }

    @Override
    public String getMessage(String walletAddress) {
        NonceEntry entry = store.get(walletAddress);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            store.remove(walletAddress);
            return null;
        }

        return entry.message();
    }

    @Override
    public void deleteMessage(String walletAddress) {
        store.remove(walletAddress);
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredMessages() {
        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private record NonceEntry(String message, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}