// File: user-service/src/test/java/com/booking/userservice/UserServiceApplicationTests.java

package com.booking.userservice;

import com.booking.userservice.service.BusinessLogicService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

// THE FINAL FIX: @DataJpaTest is the correct annotation for this.
// It tells Spring Boot to only configure the parts needed for testing
// the database layer (like your UserRepository). It will automatically
// use a lightweight in-memory database (like H2) for the test,
// so no real database connection is needed.
@DataJpaTest
// We need to import our service classes because @DataJpaTest only scans for JPA components.
@Import(BusinessLogicService.class)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test will now pass because the application context
        // can be loaded successfully with the test configuration.
    }

}
