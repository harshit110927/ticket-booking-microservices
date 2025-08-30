package com.booking.adminservice.events;

import java.math.BigDecimal;
import java.util.UUID;


// It's not a database entity.
public record TicketTransferDTO(
        UUID ticketId,
        UUID eventId,
        String seatInfo,
        BigDecimal price,
        String status
) {}
