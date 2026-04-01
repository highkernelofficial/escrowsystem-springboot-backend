package com.highkernel.milestonebackend.common.controller;

import com.highkernel.milestonebackend.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "milestonebackend");
        data.put("timestamp", OffsetDateTime.now());

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Service is running")
                .data(data)
                .build();
    }
}