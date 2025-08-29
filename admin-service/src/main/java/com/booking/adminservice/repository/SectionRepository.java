package com.booking.adminservice.repository;

import com.booking.adminservice.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
}