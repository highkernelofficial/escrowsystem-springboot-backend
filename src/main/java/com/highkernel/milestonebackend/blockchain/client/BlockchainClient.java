package com.highkernel.milestonebackend.blockchain.client;

import com.highkernel.milestonebackend.blockchain.dto.BlockchainTxnResponse;
import com.highkernel.milestonebackend.blockchain.dto.DeployContractRequest;
import com.highkernel.milestonebackend.blockchain.dto.FundProjectTxnRequest;
import com.highkernel.milestonebackend.blockchain.dto.GetAppIdRequest;
import com.highkernel.milestonebackend.blockchain.dto.GetAppIdResponse;
import com.highkernel.milestonebackend.blockchain.dto.ReleaseMilestoneTxnRequest;
import com.highkernel.milestonebackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BlockchainClient {

    private final RestTemplate restTemplate;

    @Value("${blockchain.base-url}")
    private String blockchainBaseUrl;

    @Value("${blockchain.deploy-contract-path:/api/v1/blockchain/deploy-contract}")
    private String deployContractPath;

    @Value("${blockchain.get-app-id-path:/api/v1/blockchain/get-app-id}")
    private String getAppIdPath;

    @Value("${blockchain.fund-project-path:/api/v1/blockchain/fund-project}")
    private String fundProjectPath;

    @Value("${blockchain.release-milestone-path:/api/v1/blockchain/release-milestone}")
    private String releaseMilestonePath;

    public BlockchainTxnResponse prepareDeployContractTxn(DeployContractRequest request) {
        return postForTxn(deployContractPath, request, "deploy contract");
    }

    public GetAppIdResponse getAppId(GetAppIdRequest request) {
        String url = blockchainBaseUrl + getAppIdPath;
        try {
            GetAppIdResponse response = restTemplate.postForObject(url, request, GetAppIdResponse.class);
            if (response == null || response.getAppId() == null) {
                throw new BadRequestException("FastAPI returned empty app_id");
            }
            return response;
        } catch (Exception e) {
            throw new BadRequestException("Failed to fetch app_id from FastAPI: " + e.getMessage());
        }
    }

    public BlockchainTxnResponse prepareFundProjectTxn(FundProjectTxnRequest request) {
        return postForTxn(fundProjectPath, request, "fund project");
    }

    public BlockchainTxnResponse prepareReleaseMilestoneTxn(ReleaseMilestoneTxnRequest request) {
        return postForTxn(releaseMilestonePath, request, "release milestone");
    }

    private BlockchainTxnResponse postForTxn(String path, Object request, String action) {
        String url = blockchainBaseUrl + path;
        try {
            BlockchainTxnResponse response = restTemplate.postForObject(url, request, BlockchainTxnResponse.class);

            if (response == null || response.getTxns() == null || response.getTxns().isEmpty()) {
                throw new BadRequestException("FastAPI returned empty txn payload for " + action);
            }

            boolean hasAnyBlankTxn = response.getTxns().stream()
                    .anyMatch(txn -> txn == null || txn.isBlank());

            if (hasAnyBlankTxn) {
                throw new BadRequestException("FastAPI returned invalid txn payload for " + action);
            }

            return response;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to " + action + " via FastAPI: " + e.getMessage());
        }
    }
}