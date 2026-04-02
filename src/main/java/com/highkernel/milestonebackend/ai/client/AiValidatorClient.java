package com.highkernel.milestonebackend.ai.client;

import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationRequest;
import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationResponse;
import com.highkernel.milestonebackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiValidatorClient {

    private final RestTemplate restTemplate;

    @Value("${ai.validator.base-url}")
    private String aiValidatorBaseUrl;

    @Value("${ai.validator.generate-milestones-path:/api/v1/ai/generate-milestones}")
    private String generateMilestonesPath;

    public MilestoneGenerationResponse generateMilestones(MilestoneGenerationRequest request) {
        String url = aiValidatorBaseUrl + generateMilestonesPath;

        try {
            MilestoneGenerationResponse response =
                    restTemplate.postForObject(url, request, MilestoneGenerationResponse.class);

            if (response == null || response.getMilestones() == null || response.getMilestones().isEmpty()) {
                throw new BadRequestException("AI validator returned empty milestone list");
            }

            return response;
        } catch (Exception e) {
            throw new BadRequestException("Failed to generate milestones from AI validator: " + e.getMessage());
        }
    }
}