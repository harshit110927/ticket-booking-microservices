package com.booking.bookingservice.events;

import java.math.BigDecimal;
import java.util.UUID;

// This is a simple, plain Java object (POJO) used for transferring data.
// It's not a database entity.
// We are using a record for a concise, immutable data carrier.
public record TicketTransferDTO(
        UUID ticketId,
        UUID eventId,
        String seatInfo,
        BigDecimal price,
        String status
) {}