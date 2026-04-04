package com.highkernel.milestonebackend.ai.client;

import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationRequest;
import com.highkernel.milestonebackend.ai.dto.MilestoneGenerationResponse;
import com.highkernel.milestonebackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
            System.out.println("AI BASE URL = " + aiValidatorBaseUrl);
            System.out.println("AI FULL URL = " + url);
            System.out.println("AI REQUEST BODY = " + request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            HttpEntity<MilestoneGenerationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<MilestoneGenerationResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    MilestoneGenerationResponse.class
            );

            MilestoneGenerationResponse body = response.getBody();

            if (body == null || body.getMilestones() == null || body.getMilestones().isEmpty()) {
                throw new BadRequestException("AI validator returned empty milestone list");
            }

            return body;

        } catch (ResourceAccessException e) {
            e.printStackTrace();
            throw new BadRequestException(
                    "Failed to generate milestones from AI validator: " + e.getMessage()
            );
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new BadRequestException(
                    "Failed to generate milestones from AI validator: " + e.getMessage()
            );
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(
                    "Failed to generate milestones from AI validator: " + e.getMessage()
            );
        }
    }
}