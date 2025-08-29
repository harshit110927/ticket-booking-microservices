package com.booking.adminservice.controller;

import com.booking.adminservice.model.Event;
import com.booking.adminservice.service.AdminLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class EventController {

    private final AdminLogicService adminLogicService;

    public record CreateEventRequest(UUID showId, UUID mapId, Instant startTime, Instant endTime) {}

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody CreateEventRequest request) {
        Event createdEvent = adminLogicService.createEvent(request);
        return ResponseEntity.ok(createdEvent);
    }
}
