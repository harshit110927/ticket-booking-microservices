package com.booking.adminservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Data
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Each venue belongs to a specific tenant.
    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    private String location;
}