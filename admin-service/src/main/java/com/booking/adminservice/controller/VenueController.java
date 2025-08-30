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


    public record CreateVenueRequest(String name, String location) {}
    public record CreateMapRequest(String name) {}
    public record CreateSectionRequest(String name) {}
    public record CreateSeatRequest(String row, String number) {}


    public record VenueResponse(UUID id, String name, String location, UUID tenantId) {}
    public record MapResponse(UUID id, String name, UUID venueId) {}
    public record SectionResponse(UUID id, String name, UUID mapId) {}
    public record SeatResponse(UUID id, String row, String number, UUID sectionId) {}


    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@RequestBody CreateVenueRequest request) {
        UUID dummyTenantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Venue createdVenue = adminLogicService.createVenue(request, dummyTenantId);
        VenueResponse response = new VenueResponse(createdVenue.getId(), createdVenue.getName(), createdVenue.getLocation(), createdVenue.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{venueId}/maps")
    public ResponseEntity<MapResponse> createMap(@PathVariable UUID venueId, @RequestBody CreateMapRequest request) {
        Map createdMap = adminLogicService.createMap(request, venueId);
        MapResponse response = new MapResponse(createdMap.getId(), createdMap.getName(), createdMap.getVenue().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/maps/{mapId}/sections")
    public ResponseEntity<SectionResponse> createSection(@PathVariable UUID mapId, @RequestBody CreateSectionRequest request) {
        Section createdSection = adminLogicService.createSection(request, mapId);
        SectionResponse response = new SectionResponse(createdSection.getId(), createdSection.getName(), createdSection.getMap().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sections/{sectionId}/seats")
    public ResponseEntity<SeatResponse> createSeat(@PathVariable UUID sectionId, @RequestBody CreateSeatRequest request) {
        Seat createdSeat = adminLogicService.createSeat(request, sectionId);
        SeatResponse response = new SeatResponse(createdSeat.getId(), createdSeat.getRowIdentifier(), createdSeat.getSeatNumber(), createdSeat.getSection().getId());
        return ResponseEntity.ok(response);
    }
}

