package com.booking.adminservice.service;

import com.booking.adminservice.controller.EventController;
import com.booking.adminservice.controller.ShowController;
import com.booking.adminservice.controller.VenueController;
import com.booking.adminservice.model.*;
import com.booking.adminservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional // Good practice to make service methods transactional
public class AdminLogicService {

    private final VenueRepository venueRepository;
    private final MapRepository mapRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final EventRepository eventRepository;

    public Venue createVenue(VenueController.CreateVenueRequest request, UUID tenantId) {
        Venue newVenue = new Venue();
        newVenue.setName(request.name());
        newVenue.setLocation(request.location());
        newVenue.setTenantId(tenantId);
        return venueRepository.save(newVenue);
    }

    public Map createMap(VenueController.CreateMapRequest request, UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        Map newMap = new Map();
        newMap.setName(request.name());
        newMap.setVenue(venue);
        return mapRepository.save(newMap);
    }

    public Section createSection(VenueController.CreateSectionRequest request, UUID mapId) {
        Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new RuntimeException("Map not found"));
        Section newSection = new Section();
        newSection.setName(request.name());
        newSection.setMap(map);
        return sectionRepository.save(newSection);
    }

    public Seat createSeat(VenueController.CreateSeatRequest request, UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        Seat newSeat = new Seat();
        newSeat.setRowIdentifier(request.row());
        newSeat.setSeatNumber(request.number());
        newSeat.setSection(section);
        return seatRepository.save(newSeat);
    }

    public Show createShow(ShowController.CreateShowRequest request, UUID tenantId) {
        Show newShow = new Show();
        newShow.setTitle(request.title());
        newShow.setDescription(request.description());
        newShow.setDurationInMinutes(request.durationInMinutes());
        newShow.setTenantId(tenantId);
        return showRepository.save(newShow);
    }

    public Event createEvent(EventController.CreateEventRequest request) {
        // In a real app, we'd validate that the showId and mapId exist
        Event newEvent = new Event();
        newEvent.setShowId(request.showId());
        newEvent.setMapId(request.mapId());
        newEvent.setStartTime(request.startTime());
        newEvent.setEndTime(request.endTime());
        return eventRepository.save(newEvent);
    }
}