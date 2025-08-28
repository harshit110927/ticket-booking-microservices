package com.booking.userservice.repository;
// just a quick reminder for you that repository if the abstract class of CRUD operations we get for free

import com.booking.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<User,UUID> {
}
