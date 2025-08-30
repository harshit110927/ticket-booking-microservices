package com.booking.bookingservice.listener;

import com.booking.bookingservice.model.Ticket;
import com.booking.bookingservice.repository.TicketRepository;
import com.booking.bookingservice.events.TicketTransferDTO; // Import the shared DTO
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMessageListener {

    private final TicketRepository ticketRepository;

    // This method will be automatically invoked when a message arrives on the queue.
    @RabbitListener(queues = "ticket-queue")
    public void receiveTicketData(List<TicketTransferDTO> ticketDTOs) {
        System.out.println("Received message with " + ticketDTOs.size() + " tickets from RabbitMQ.");

        List<Ticket> ticketsToReplicate = new ArrayList<>();
        for (TicketTransferDTO dto : ticketDTOs) {
            Ticket ticket = new Ticket();
            ticket.setId(dto.ticketId());
            ticket.setEventId(dto.eventId());
            ticket.setSeatInfo(dto.seatInfo());
            ticket.setPrice(dto.price());
            ticket.setStatus(dto.status());
//
            ticketsToReplicate.add(ticket);
        }

        // Save the replicated tickets into the booking-service's own database.
        ticketRepository.saveAll(ticketsToReplicate);
        System.out.println("Successfully replicated and saved " + ticketsToReplicate.size() + " tickets.");
    }
}
