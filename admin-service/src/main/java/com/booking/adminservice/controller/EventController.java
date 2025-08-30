package com.booking.adminservice.controller;

import com.booking.adminservice.model.Event;
import com.booking.adminservice.service.AdminLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class EventController {

    private final AdminLogicService adminLogicService;


    public record CreateEventRequest(UUID showId, UUID mapId, Instant startTime, Instant endTime) {}

    @PostMapping
    public Event createEvent(@RequestBody CreateEventRequest request) {


        UUID tenantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // A dummy hardcoded
        return adminLogicService.createEvent(request, tenantId);
    }
}
