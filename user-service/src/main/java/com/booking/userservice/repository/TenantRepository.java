package com.booking.userservice.repository;

import com.booking.userservice.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant,UUID> {
}
