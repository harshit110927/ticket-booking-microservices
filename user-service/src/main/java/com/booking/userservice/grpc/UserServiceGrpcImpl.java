package com.booking.userservice.grpc;

import com.booking.user.GetUserDetailsRequest;
import com.booking.user.UserDetailsResponse;
import com.booking.user.UserServiceGrpc;
import com.booking.userservice.service.BusinessLogicService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    private final BusinessLogicService businessLogicService;

    // We override the method defined in our user.proto file.
    @Override
    public void getUserDetails(GetUserDetailsRequest request, StreamObserver<UserDetailsResponse> responseObserver) {
        // Extract the user ID from the gRPC request.
        UUID userId = UUID.fromString(request.getUserId().getValue());

        // Call our business logic service to fetch the data.
        UserDetailsResponse response = businessLogicService.getUserDetails(userId);

        // Send the response back to the client.
        responseObserver.onNext(response);
        // Signal that the RPC is complete.
        responseObserver.onCompleted();
    }
}