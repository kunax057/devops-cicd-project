package com.devops.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DevOpsController {

    @GetMapping("/")
    public Map<String, String> home() {

        Map<String, String> response = new HashMap<>();

        response.put("application", "DevOps CI/CD Demo");
        response.put("status", "Running");
        response.put("environment", "Development");
        response.put("message", "Application deployed successfully");

        return response;
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy";
    }
}
