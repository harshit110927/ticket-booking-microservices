package com.booking.bookingservice.controller;

import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.service.BookingLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingLogicService bookingLogicService;

    // A simple record for the JSON request body.
    public record CreateBookingRequest(List<UUID> ticketIds) {}

    @PostMapping
    public Booking createBooking(@RequestBody CreateBookingRequest request) {
        // In a real application, the userId would be extracted from a validated JWT.
        // For this test, we'll hardcode a dummy userId.
        UUID dummyUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        return bookingLogicService.createBooking(request, dummyUserId);
    }
}
