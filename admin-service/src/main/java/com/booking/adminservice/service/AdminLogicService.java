package com.booking.adminservice.service;

import com.booking.adminservice.config.RabbitMQConfig;
import com.booking.adminservice.controller.EventController;
import com.booking.adminservice.controller.ShowController;
import com.booking.adminservice.controller.VenueController;
import com.booking.adminservice.events.TicketTransferDTO;
import com.booking.adminservice.model.*;
import com.booking.adminservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminLogicService {

    private final VenueRepository venueRepository;
    private final MapRepository mapRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final RabbitTemplate rabbitTemplate;

    public Venue createVenue(VenueController.CreateVenueRequest request, UUID tenantId) {
        Venue newVenue = new Venue();
        newVenue.setName(request.name());
        newVenue.setLocation(request.location());
        newVenue.setTenantId(tenantId);
        return venueRepository.save(newVenue);
    }

    public Map createMap(VenueController.CreateMapRequest request, UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found for ID: " + venueId));
        Map newMap = new Map();
        newMap.setName(request.name());
        newMap.setVenue(venue);
        return mapRepository.save(newMap);
    }

    public Section createSection(VenueController.CreateSectionRequest request, UUID mapId) {
        Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new RuntimeException("Map not found for ID: " + mapId));
        Section newSection = new Section();
        newSection.setName(request.name());
        newSection.setMap(map);
        return sectionRepository.save(newSection);
    }

    public Seat createSeat(VenueController.CreateSeatRequest request, UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found for ID: " + sectionId));
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

    public Event createEvent(EventController.CreateEventRequest request, UUID tenantId) {
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new RuntimeException("Show not found for ID: " + request.showId()));
        Map map = mapRepository.findById(request.mapId())
                .orElseThrow(() -> new RuntimeException("Map not found for ID: " + request.mapId()));

        Event newEvent = new Event();
        newEvent.setShowId(show.getId());
        newEvent.setMapId(map.getId());
        newEvent.setStartTime(request.startTime());
        newEvent.setEndTime(request.endTime());
        newEvent.setTenantId(tenantId);
        Event savedEvent = eventRepository.save(newEvent);

        generateAndPublishTickets(savedEvent, map);

        return savedEvent;
    }

    private void generateAndPublishTickets(Event event, Map map) {
        List<Ticket> ticketsToSave = new ArrayList<>();
        List<TicketTransferDTO> ticketDTOs = new ArrayList<>();
        BigDecimal defaultPrice = new BigDecimal("25.00");

        List<Section> sections = sectionRepository.findByMapId(map.getId());
        for (Section section : sections) {
            List<Seat> seats = seatRepository.findBySectionId(section.getId());
            for (Seat seat : seats) {
                Ticket ticket = new Ticket();
                ticket.setEvent(event);
                ticket.setSeat(seat);
                ticket.setPrice(defaultPrice);
                ticket.setStatus("AVAILABLE");
                ticketsToSave.add(ticket);
            }
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(ticketsToSave);

        for (Ticket ticket : savedTickets) {
            String seatInfo = String.format("%s-%s%s",
                    ticket.getSeat().getSection().getName(),
                    ticket.getSeat().getRowIdentifier(),
                    ticket.getSeat().getSeatNumber());

            TicketTransferDTO dto = new TicketTransferDTO(
                    ticket.getId(),
                    ticket.getEvent().getId(),
                    seatInfo,
                    ticket.getPrice(),
                    ticket.getStatus()
            );
            ticketDTOs.add(dto);
        }

        System.out.println("Publishing " + ticketDTOs.size() + " tickets to RabbitMQ...");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, ticketDTOs);
        System.out.println("Message published!");
    }
}

