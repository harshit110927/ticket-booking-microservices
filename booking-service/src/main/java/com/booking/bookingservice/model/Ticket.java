package com.booking.bookingservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// This entity is a replicated copy of the ticket data from the admin-service.
@Entity
@Table(name = "tickets")
@Data
public class Ticket {

    @Id
    private UUID id; // The ID is the same as the original ticket in the admin-db

    private UUID eventId;

    private String seatInfo;

    private BigDecimal price;

    private String status;

    // THIS IS THE NEW, CORRECTED RELATIONSHIP
    // Many tickets can belong to one booking.
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
