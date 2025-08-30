Multi-Tenant Ticket Booking Service (Microservices)
This project is the result of re-architecting a monolithic Java Spring Boot application into a modern, event-driven microservices system. The architecture is designed to be scalable, resilient, and independently deployable, mirroring the practices of enterprise-grade backend systems.

Architecture Overview
The system is a distributed application composed of independent, containerized services. Each service has a single, well-defined responsibility and owns its own dedicated database, ensuring true loose coupling.

Services (3):

user-service: Manages identity, tenants, users, and authentication.

admin-service: Manages business logistics like venues, maps, shows, and event creation.

booking-service: Handles all high-traffic, customer-facing booking transactions.

Communication Patterns:

External (REST API): A single API Gateway exposes a REST API for all client traffic.

Internal Synchronous (gRPC): Services use gRPC for high-performance, strongly-typed, request/response communication.

Internal Asynchronous (RabbitMQ): An event-driven approach using a message queue decouples services for background tasks and data replication, enhancing resilience.

![](C:\Users\harsh\Downloads\arch.png)

Technology Stack
Backend: Java 21, Spring Boot 3.3.3

Database: PostgreSQL

Data Access: Spring Data JPA, Hibernate

Communication: gRPC (internal sync), RabbitMQ (internal async)

Containerization: Docker, Docker Compose

Build Tool: Maven

Getting Started
Prerequisites
Java 21 (or newer)

Apache Maven

Docker and Docker Compose

Running the Entire System
The entire microservices ecosystem is orchestrated with Docker Compose. This single command will build the Docker images for all three services, start their dedicated databases, and launch the RabbitMQ message broker.

From the root directory of the project (ticket-booking-microservices), run the following command:

docker-compose up --build

The system is now running. The services will be available at:

User Service: http://localhost:8080

Admin Service: http://localhost:8081

Booking Service: http://localhost:8082

RabbitMQ Management: http://localhost:15672 (user: guest, pass: guest)

End-to-End Testing Workflow (Postman)
This flow demonstrates the complete, decoupled workflow from event creation to ticket booking.

Part A: Admin Setup (Targeting admin-service)
Create a Venue

Method: POST

URL: http://localhost:8081/api/v1/admin/venues

Body (JSON): { "name": "Grand Cinema Hall", "location": "City Center" }

Action: Send and copy the id from the response (e.g., venue-id-1).

Create a Map

Method: POST

URL: http://localhost:8081/api/v1/admin/venues/{venue-id-1}/maps

Body (JSON): { "name": "Auditorium 1" }

Action: Send and copy the id (e.g., map-id-1).

Create a Section and Seat

Method: POST

URL: http://localhost:8081/api/v1/admin/venues/maps/{map-id-1}/sections

Body (JSON): { "name": "Front Row" }

Action: Send and copy the id (e.g., section-id-1).

Method: POST

URL: http://localhost:8081/api/v1/admin/venues/sections/{section-id-1}/seats

Body (JSON): { "row": "A", "number": "1" }

Action: Send.

Create a Show

Method: POST

URL: http://localhost:8081/api/v1/admin/shows

Body (JSON): { "title": "The Final Test", "description": "A thrilling conclusion.", "durationInMinutes": 120 }

Action: Send and copy the id (e.g., show-id-1).

Create an Event (Triggers Asynchronous Flow)

Method: POST

URL: http://localhost:8081/api/v1/admin/events

Body (JSON):

{
"showId": "show-id-1",
"mapId": "map-id-1",
"startTime": "2025-12-01T20:00:00Z",
"endTime": "2025-12-01T22:00:00Z"
}

Action: Send.

Part B: Verification
Check the console logs for the admin-service container. You should see a message like "Message published!".

Check the console logs for the booking-service container. You should see "Received message... Successfully replicated...". This confirms the message queue is working.

Part C: Customer Booking (Targeting booking-service)
Find a Ticket ID

Connect to the booking-db on localhost:5434 with a database tool.

Run the query SELECT * FROM tickets;

Copy the id of the ticket that was just replicated.

Create a Booking

Method: POST

URL: http://localhost:8082/api/v1/bookings

Body (JSON):

{
"ticketIds": ["<the-ticket-id-you-copied>"]
}
