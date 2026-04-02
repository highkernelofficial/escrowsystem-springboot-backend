package com.highkernel.milestonebackend.ai.client;

import com.highkernel.milestonebackend.ai.dto.AiEvaluationRequest;
import com.highkernel.milestonebackend.ai.dto.AiEvaluationResponse;
import com.highkernel.milestonebackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiEvaluationClient {

    private final RestTemplate restTemplate;

    @Value("${ai.validator.base-url}")
    private String aiValidatorBaseUrl;

    @Value("${ai.validator.evaluate-path:/api/v1/ai/evaluate}")
    private String evaluatePath;

    public AiEvaluationResponse evaluateSubmission(AiEvaluationRequest request) {
        String url = aiValidatorBaseUrl + evaluatePath;

        try {
            AiEvaluationResponse response =
                    restTemplate.postForObject(url, request, AiEvaluationResponse.class);

            if (response == null) {
                throw new BadRequestException("AI evaluation returned empty response");
            }

            return response;
        } catch (Exception e) {
            throw new BadRequestException("Failed to evaluate submission from AI validator: " + e.getMessage());
        }
    }
}