package com.booking.adminservice.controller;

import com.booking.adminservice.model.Map;
import com.booking.adminservice.model.Section;
import com.booking.adminservice.model.Seat;
import com.booking.adminservice.model.Venue;
import com.booking.adminservice.service.AdminLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/venues")
@RequiredArgsConstructor
public class VenueController {

    private final AdminLogicService adminLogicService;

    // DTOs (Data Transfer Objects) for requests
    public record CreateVenueRequest(String name, String location) {}
    public record CreateMapRequest(String name) {}
    public record CreateSectionRequest(String name) {}
    public record CreateSeatRequest(String row, String number) {}

    @PostMapping
    public ResponseEntity<Venue> createVenue(@RequestBody CreateVenueRequest request) {
        UUID dummyTenantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Venue createdVenue = adminLogicService.createVenue(request, dummyTenantId);
        return ResponseEntity.ok(createdVenue);
    }

    @PostMapping("/{venueId}/maps")
    public ResponseEntity<Map> createMap(@PathVariable UUID venueId, @RequestBody CreateMapRequest request) {
        Map createdMap = adminLogicService.createMap(request, venueId);
        return ResponseEntity.ok(createdMap);
    }

    @PostMapping("/maps/{mapId}/sections")
    public ResponseEntity<Section> createSection(@PathVariable UUID mapId, @RequestBody CreateSectionRequest request) {
        Section createdSection = adminLogicService.createSection(request, mapId);
        return ResponseEntity.ok(createdSection);
    }

    @PostMapping("/sections/{sectionId}/seats")
    public ResponseEntity<Seat> createSeat(@PathVariable UUID sectionId, @RequestBody CreateSeatRequest request) {
        Seat createdSeat = adminLogicService.createSeat(request, sectionId);
        return ResponseEntity.ok(createdSeat);
    }
}
