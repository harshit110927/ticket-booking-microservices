package com.booking.adminservice.repository;

import com.booking.adminservice.model.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MapRepository extends JpaRepository<Map, UUID> {
}