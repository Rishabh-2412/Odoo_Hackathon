package com.transitops.backend.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", "UP");
        response.put("application", "TransitOps Backend");
        response.put(
            "message",
            "TransitOps backend is running successfully"
        );
        response.put("timestamp", Instant.now());

        return ResponseEntity.ok(response);
    }
}