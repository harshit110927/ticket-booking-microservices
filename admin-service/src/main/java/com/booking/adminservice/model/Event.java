package com.booking.adminservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    // An event is a specific instance of a show
    @Column(nullable = false)
    private UUID showId;

    // An event happens at a specific map within a venue
    @Column(nullable = false)
    private UUID mapId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private String status = "SCHEDULED"; // e.g., SCHEDULED, ON_SALE, CANCELLED
}
