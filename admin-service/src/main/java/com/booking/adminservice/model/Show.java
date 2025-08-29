package com.booking.adminservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "shows")
@Data
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // A show can be created by a tenant.
    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String title;

    @Lob // For longer text descriptions
    private String description;

    private int durationInMinutes;
}