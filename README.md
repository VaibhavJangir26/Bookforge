# 🚀 BookForge Backend — Distributed Microservices Architecture

BookForge is a high-concurrency, enterprise-grade venue, space, and resource reservation platform built using a distributed **Spring Cloud Microservices** architecture. It is designed to handle high-traffic booking rushes without race conditions, utilizing event-driven choreography via Apache Kafka, distributed locking via Redisson, Redis-backed caching, and perimeter rate limiting.

---

## 🏗️ Tech Stack & Architecture

* **Core Framework:** Spring Boot 3.x, Spring Cloud
* **Service Discovery:** Netflix Eureka Server
* **API Gateway:** Spring Cloud Gateway (Reactive WebFlux, Token Bucket Rate Limiting)
* **Database & ORM:** PostgreSQL, Spring Data JPA / Hibernate
* **Caching & Distributed Locks:** Redis, Redisson (`RLock`, `RBucket`)
* **Event-Driven Messaging:** Apache Kafka (Choreography-Based Saga Pattern)
* **File Storage:** Cloudinary API
* **Security:** Stateless JWT Authentication & Role-Based Access Control (RBAC)
* **Build Tool:** Apache Maven (Multi-Module Workspace)

---

## 📂 Microservices Overview

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`discovery-server`** | `8761` | Eureka server for dynamic microservice registration and discovery. |
| **`api-gateway`** | `8080` | Edge router, JWT validator, and perimeter Token Bucket rate limiter. |
| **`auth-service`** | `8081` | Handles user registration, authentication, and JWT issuance. |
| **`catalog-service`** | `8082` | Manages venues, spaces, and resources with Cloudinary image uploads & Redis caching. |
| **`booking-service`** | `8700` | Manages reservations, operational rules, blackout slots, and 5-minute Redis inventory holds. |
| **`payment-service`** | `8084` | Handles payment intents, transaction verification, and secure webhooks. |
| **`common`** | *N/A* | Shared module containing DTOs, global exception handlers, and utility classes. |

---

## ⚡ High-Concurrency & Production Capabilities

### 1. Zero Double-Booking Guarantee (Redisson Distributed Locking)
* **The Problem:** During high-demand flash sales or rush hours, simultaneous requests for the exact same space and time slot can cause race conditions and database inconsistencies.
* **The Solution:** Implemented **Redisson Distributed Locks (`RLock`)** and **5-minute Inventory Holds (`RBucket`)** inside `booking-service`. Before a booking is written to PostgreSQL, a distributed lock is acquired on the specific slot key, ensuring mutual exclusion across horizontally scaled microservice instances and automatically releasing expired holds if payments stall.

### 2. Event-Driven Choreography & Saga Pattern (Kafka)
To maintain consistency across isolated microservice databases without heavy distributed two-phase commits (2PC), BookForge uses an **event-driven choreography saga pattern** via Apache Kafka:
1. **`BOOKING_INITIATED`**: When a user books a space, the booking is marked as `PENDING`, a 5-minute inventory hold is applied in Redis, and an event is published to Kafka.
2. **`PAYMENT_COMPLETED` / `PAYMENT_FAILED`**: The `payment-service` consumes the event to process payment. 
   * On **Success**: Emits a completion event; booking status transitions to `CONFIRMED`, and resource quantities are permanently deducted.
   * On **Failure / Timeout**: Emits a failure event, triggering automatic **compensating transactions** (releasing the Redis slot lock immediately and marking the booking as `CANCELLED`).

### 3. Perimeter Rate Limiting
* Implemented at the API Gateway level using a **Token Bucket algorithm** backed by Redis. Protects downstream services from traffic spikes, abuse, and DDoS attacks by enforcing strict rate rules per client IP/user.

---

## ⚙️ Getting Started & Local Setup

### Prerequisites
* **Java SDK:** JDK 21+
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Maven 3.9+

### 1. Clone the Repository
```bash
git clone [https://github.com/VaibhavJangir26/Bookforge.git](https://github.com/VaibhavJangir26/Bookforge.git)
cd bookforge
