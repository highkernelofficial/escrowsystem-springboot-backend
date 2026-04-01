package com.highkernel.milestonebackend.auth.service.impl;

import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.Signature;
import com.highkernel.milestonebackend.auth.service.CryptoService;
import com.highkernel.milestonebackend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class AlgorandCryptoService implements CryptoService {

    @Override
    public boolean isValidWallet(String walletAddress) {
        try {
            new Address(walletAddress);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean verifySignature(String walletAddress, String message, String base64Signature) {
        try {
            Address address = new Address(walletAddress);
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
            Signature signature = new Signature(signatureBytes);

            return address.verifyBytes(message.getBytes(StandardCharsets.UTF_8), signature);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Signature must be a valid base64 string");
        } catch (Exception ex) {
            log.warn("Signature verification failed for wallet {}", walletAddress, ex);
            return false;
        }
    }
}