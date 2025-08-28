package com.booking.userservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {


    public record OnboardTenantRequest(String tenantName, String adminUserName, String adminEmail, String adminPassword) {}

    @PostMapping("/tenant")
    public String onboardTenant(@RequestBody OnboardTenantRequest request) {
        return "Tenant onboarding request received for: " + request.tenantName();
    }
}

