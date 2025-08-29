package com.booking.adminservice.controller;

import com.booking.adminservice.model.Show;
import com.booking.adminservice.service.AdminLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/shows")
@RequiredArgsConstructor
public class ShowController {

    private final AdminLogicService adminLogicService;

    public record CreateShowRequest(String title, String description, int durationInMinutes) {}

    @PostMapping
    public ResponseEntity<Show> createShow(@RequestBody CreateShowRequest request) {
        UUID dummyTenantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Show createdShow = adminLogicService.createShow(request, dummyTenantId);
        return ResponseEntity.ok(createdShow);
    }
}
