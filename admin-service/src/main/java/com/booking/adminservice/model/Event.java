package com.booking.adminservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // An event is a specific show happening in a specific map
    @Column(nullable = false)
    private UUID showId;

    @Column(nullable = false)
    private UUID mapId;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private String status = "SCHEDULED"; // e.g., SCHEDULED, ON_SALE, CANCELLED
}