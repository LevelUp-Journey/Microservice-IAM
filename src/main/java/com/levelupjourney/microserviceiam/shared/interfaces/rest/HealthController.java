package com.levelupjourney.microserviceiam.shared.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/iam/health")
public class HealthController {

    @GetMapping("/check")
    public String check() {
        return "IAM Service is up and running!";
    }
}
