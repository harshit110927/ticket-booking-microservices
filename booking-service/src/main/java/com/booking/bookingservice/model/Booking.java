package com.booking.bookingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private Instant bookingTime;
    @Column(nullable = false)
    private BigDecimal totalAmount;
    @Column(nullable = false)
    private String status = "PENDING"; // e.g., PENDING, CONFIRMED, CANCELLED
}