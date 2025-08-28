package com.booking.userservice.service;

import com.booking.user.UserDetailsResponse;
import com.booking.userservice.model.User;
import com.booking.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessLogicService {
    private final UserRepository userRepository;

    public UserDetailsResponse getUserDetails(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User Not Found"));

        return UserDetailsResponse.newBuilder()
                .setUserId(com.booking.common.UUID.newBuilder().setValue(user.getId().toString()).build())
                .setTenantId(com.booking.common.UUID.newBuilder().setValue(user.getTenant().getId().toString()).build())
                .setUserName(user.getName())
                .setEmail(user.getEmail())
                .setRole(user.getRole())
                .build();
    }
}
