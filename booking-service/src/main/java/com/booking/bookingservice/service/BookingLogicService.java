package com.booking.bookingservice.service;

import com.booking.bookingservice.controller.BookingController;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.model.Ticket;
import com.booking.bookingservice.repository.BookingRepository;
import com.booking.bookingservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingLogicService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Booking createBooking(BookingController.CreateBookingRequest request, UUID userId) {
        // 1. Find the tickets the user wants to book.
        List<Ticket> ticketsToBook = ticketRepository.findAllById(request.ticketIds());

        // 2. Validate the tickets (e.g., ensure they are all available).
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Ticket ticket : ticketsToBook) {
            if (!"AVAILABLE".equals(ticket.getStatus())) {
                throw new IllegalStateException("Ticket " + ticket.getId() + " is not available for booking.");
            }
            totalAmount = totalAmount.add(ticket.getPrice());
        }

        // 3. Create the main booking record.
        Booking newBooking = new Booking();
        newBooking.setUserId(userId);
        newBooking.setTotalAmount(totalAmount);
        newBooking.setStatus("CONFIRMED"); // For simplicity, we confirm immediately.
        Booking savedBooking = bookingRepository.save(newBooking);

        // 4. Update the status of each ticket and link it to the booking.
        for (Ticket ticket : ticketsToBook) {
            ticket.setStatus("BOOKED");
            ticket.setBooking(savedBooking);
        }
        ticketRepository.saveAll(ticketsToBook);

        return savedBooking;
    }
}
