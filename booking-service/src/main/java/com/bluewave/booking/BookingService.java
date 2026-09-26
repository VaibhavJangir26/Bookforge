package com.bluewave.booking;

import com.bluewave.booking.client.CatalogClient;
import com.bluewave.booking.dto.*;
import com.bluewave.booking.dto.UpdateBookingStatusRequestDTO;
import com.bluewave.booking.model.Booking;
import com.bluewave.booking.model.BookingResource;
import com.bluewave.constants.BookingStatus;
import com.bluewave.dto.*;
import com.bluewave.exception.ResourceConflictException;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.kafka.BookingEventPublisher;
import com.bluewave.kafka_common_event.BookingCancelledEvent;
import com.bluewave.kafka_common_event.BookingCreatedEvent;
import com.bluewave.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepo bookingRepo;
    private final RedissonClient redissonClient;
    private final CatalogClient catalogClient;
    private final BookingEventPublisher bookingEventPublisher;

    @Transactional
    public CommonApiResponse<BookingResponseDTO> createBooking(CreateBookingRequestDTO requestDTO) {

        // step 1 checking that idempotent exists or not to prevent double booking
        Optional<Booking> existing = bookingRepo.findByIdempotencyKey(requestDTO.getIdempotencyKey());
        if (existing.isPresent()) {
            return CommonApiResponse.<BookingResponseDTO>builder()
                    .data(bookingResponseMapToDTO(existing.get()))
                    .message("booking already processed")
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // the booking must in future past booking before current time is not allowed
        if (requestDTO.getSlotStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("booking can't be in past time");
        }
        // helper variable
        String spaceId = requestDTO.getSpaceId();
        String customerId = UserContext.getUserId();
        LocalDateTime startTime = requestDTO.getSlotStartTime();
        LocalDateTime endTime = requestDTO.getSlotEndTime();

        // now we create the redisson spin lock
        // to prevent 100s of concurrent request to access the db
        String lockKey = String.format("lock:space:%s:time:%s_%s", spaceId, startTime, endTime);
        RLock lock = redissonClient.getLock(lockKey);
        boolean isAcquired = false;
        try {
            // Wait up to 0 seconds (fail immediately if someone else is trying to book this exact slot).
            // Lease time 3 seconds (auto-releases if this server crashes).
            isAcquired = lock.tryLock(0, 3, TimeUnit.SECONDS);
            if (!isAcquired) {
                throw new ResourceConflictException("this slot is currently being held or booked by someone else, please try again");
            }

            // 3. Fast-Path Redis Hold Verification
            // Checks if a 5-minute checkout hold already exists for this exact time slot.
            // it will prevent new user to book with the time window fot that 5 min
            String holdKey = String.format("hold:space:%s:time:%s_%s", spaceId, startTime, endTime);
            RBucket<String> holdBucket = redissonClient.getBucket(holdKey);
            if (holdBucket.isExists()) {
                throw new ResourceConflictException("this slot is currently locked for checkout by another user");
            }

            // 4. PostgreSQL Overlap & Blackout Validation
            // Ensures no overlapping bookings slipped through during a previous checkout.
            List<Booking> overlappingBooking = bookingRepo.findOverlappingActiveBookings(spaceId, startTime, endTime);
            if (!overlappingBooking.isEmpty()) {
                throw new ResourceConflictException("slot is already booked, slot is no longer available");
            }

            // 5. Synchronous Feign Call (Validation & Dynamic Pricing)
            // Relies on Catalog Service as the source of truth for pricing and blackouts.
            // FIX: Multiply resourceId by quantity so full inventory cost is calculated!
            List<String> resourceId = requestDTO.getBookingResourceItem() != null ?
                    requestDTO.getBookingResourceItem().stream()
                            .filter(dto -> dto.getResourceId() != null)
                            .flatMap(dto -> Collections.nCopies(Math.max(1, dto.getQuantity() != null ? dto.getQuantity() : 1), dto.getResourceId()).stream())
                            .toList()
                    : List.of();

            CalculatePriceRequestDTO priceReq = CalculatePriceRequestDTO.builder()
                    .spaceId(spaceId)
                    .slotStartTime(startTime)
                    .slotEndTime(endTime)
                    .resourceIds(resourceId)
                    .build();

            CommonApiResponse<CalculatePriceResponseDTO> priceResp = catalogClient.calculatePrice(priceReq);
            if (priceResp == null || !priceResp.isSuccess() || priceResp.getData() == null) {
                throw new IllegalArgumentException("failed to validate slot availability and calculate pricing");
            }

            // We use the backend-calculated price, ignoring the customer's submitted price to prevent tampering.
            CalculatePriceResponseDTO pricingData = priceResp.getData();

            // 6. Establish the 5-Minute Inventory Hold
            // Sets a Redis key that locks this slot globally for 5 minutes.
            holdBucket.set(customerId, 5, TimeUnit.MINUTES);

            // 7. ACID Database Commit
            Booking booking = new Booking();
            booking.setIdempotencyKey(requestDTO.getIdempotencyKey());
            booking.setCustomerId(customerId);
            booking.setVenueId(requestDTO.getVenueId());
            booking.setSpaceId(spaceId);
            booking.setSlotStartTime(startTime);
            booking.setSlotEndTime(endTime);
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
            booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));
            booking.setBasePriceAmount(pricingData.getCalculatedSpacePrice());
            booking.setResourceTotalAmount(pricingData.getTotalResourcePrice());
            booking.setTaxAmount(BigDecimal.ZERO); // Add tax logic here if applicable
            booking.setDiscountAmount(BigDecimal.ZERO); // Add coupon/discount logic here if applicable
            booking.setTotalAmount(pricingData.getFinalTotalPrice());

            // Map Resources cleanly from DTO to Entity
            if (requestDTO.getBookingResourceItem() != null && !requestDTO.getBookingResourceItem().isEmpty()) {
                List<BookingResource> resources = requestDTO.getBookingResourceItem().stream().map(dto -> {
                    BookingResource res = new BookingResource();
                    res.setResourceId(dto.getResourceId());
                    res.setName(dto.getName());
                    res.setQuantity(dto.getQuantity());
                    res.setPricePerUnit(dto.getPricePerUnit());
                    res.setBooking(booking); // Link child to parent
                    return res;
                }).toList();
                booking.setBookingResourceList(resources);
            }

            Booking savedBooking = bookingRepo.save(booking);

            // 8. Publish Kafka Event
            // Triggers Payment Service, Notifications, and the 5-minute expiry timeout listener
            BookingCreatedEvent event = new BookingCreatedEvent(
                    savedBooking.getId(),
                    savedBooking.getCustomerId(),
                    savedBooking.getSpaceId(),
                    savedBooking.getVenueId(),
                    savedBooking.getSlotStartTime(),
                    savedBooking.getSlotEndTime(),
                    savedBooking.getTotalAmount(),
                    savedBooking.getHoldExpiresAt(),
                    savedBooking.getCreatedAt()
            );
            bookingEventPublisher.publishBookingCreated(event);

            return CommonApiResponse.<BookingResponseDTO>builder()
                    .data(bookingResponseMapToDTO(savedBooking))
                    .message("Booking slot held successfully. Please complete payment within 5 minutes.")
                    .status(HttpStatus.CREATED.value())
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Process interrupted while acquiring booking lock");
        } finally {
            // 9. Release the Redisson Spin-Lock
            // The slot remains protected by the 5-minute Redis holdBucket, but the Mutex is freed for other threads.
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public String cancelBooking(CancelBookingRequestDTO requestDTO) {

        // 1. Fetch Booking
        Booking booking = bookingRepo.findById(requestDTO.getBookingId()).orElseThrow(() -> new ResourceNotFoundException("booking not found with id" + requestDTO.getBookingId()));

        // 2. Security Tenant Isolation Check
        // Ensures that only the customer who booked it, the provider who owns the space, or an admin can cancel it.
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin()) {
            if (UserContext.isCustomer() && !booking.getCustomerId().equals(currentUserId)) {
                throw new AccessDeniedException("You are not authorized to cancel this booking.");
            } else if (UserContext.isProvider()) {
                validateProviderSpaceAccess(booking.getSpaceId());
            }
        }

        // 3. State Validation
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new IllegalStateException("Booking is already " + booking.getStatus());
        }
        if (booking.getSlotStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel a booking that has already started or passed.");
        }

        // 4. Determine Refund Eligibility (The 24-Hour Rule)
        boolean isEligibleForRefund = false;
        String refundStatusIndicator = "";

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            // Check if current time is strictly before 24 hours of the slot start time
            if (LocalDateTime.now().isBefore(booking.getSlotStartTime().minusHours(24))) {
                isEligibleForRefund = true;
                refundStatusIndicator = "[FULL REFUND APPROVED] ";
            } else {
                refundStatusIndicator = "[NO REFUND - LATE CANCELLATION] ";
            }
        } else if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            // If they cancel while on the checkout screen
            refundStatusIndicator = "[NO CHARGE - CANCELLED BEFORE PAYMENT] ";
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED && booking.getBookingResourceList() != null) {
            for (var resItem : booking.getBookingResourceList()) {
                try {
                    catalogClient.restoreResourceStock(resItem.getResourceId(), resItem.getQuantity());
                    log.info("Restored {} units of resource {} on cancellation", resItem.getQuantity(), resItem.getResourceId());
                } catch (Exception ex) {
                    log.error("Failed to restore stock for resource {}: {}", resItem.getResourceId(), ex.getMessage());
                }
            }
        }

        // 5. ACID Database Commit
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(refundStatusIndicator + requestDTO.getCancellationReason());

        // 6. Release Redis Inventory Hold
        // If the booking was in PENDING_PAYMENT, we must immediately delete the 5-minute hold key
        // so other users can book this slot right away without waiting for the timeout.
        String holdKey = String.format("hold:space:%s:time:%s_%s", booking.getSpaceId(), booking.getSlotStartTime(), booking.getSlotEndTime());
        redissonClient.getBucket(holdKey).delete();

        // 7. Publish Kafka Event
        BookingCancelledEvent event = new BookingCancelledEvent(
                booking.getId(),
                booking.getCustomerId(),
                booking.getSpaceId(),
                booking.getSlotStartTime(),
                booking.getSlotEndTime(),
                booking.getCancellationReason(),
                LocalDateTime.now()
        );
        bookingEventPublisher.publishBookingCancelled(event);

        // 8. Return appropriate user feedback
        if (isEligibleForRefund) {
            return "Booking cancelled successfully. A full refund has been initiated to your original payment method.";
        } else if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            return "Checkout session cancelled successfully.";
        } else {
            return "Booking cancelled successfully. Please note: as the cancellation occurred within 24 hours of the start time, no refund will be issued.";
        }
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<BookingResponseDTO>> getAllMyBookingHistory() {
        String currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("authentication required to access booking history");
        }

        List<Booking> bookings = bookingRepo.findAllByCustomerId(currentUserId);
        List<BookingResponseDTO> dtos = bookings.stream()
                .map(this::bookingResponseMapToDTO)
                .toList();

        return CommonApiResponse.<List<BookingResponseDTO>>builder()
                .data(dtos)
                .message("booking history fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(String bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("booking not found with id" + bookingId));
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin()) {
            boolean isOwnerCustomer = booking.getCustomerId().equals(currentUserId);
            if (isOwnerCustomer) {
                // Customer authorized
            } else if (UserContext.isProvider()) {
                // Verify provider owns the space where the booking took place
                validateProviderSpaceAccess(booking.getSpaceId());
            } else {
                throw new AccessDeniedException("You are not authorized to view this booking record");
            }
        }

        return CommonApiResponse.<BookingResponseDTO>builder()
                .data(bookingResponseMapToDTO(booking))
                .message("Booking details fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<BookingResponseDTO>> getBookingsBySpace(String spaceId) {
        if (!UserContext.isAdmin()) {
            validateProviderSpaceAccess(spaceId);
        }
        List<Booking> bookings = bookingRepo.findBySpaceId(spaceId);
        List<BookingResponseDTO> dtos = bookings.stream()
                .map(this::bookingResponseMapToDTO)
                .toList();
        return CommonApiResponse.<List<BookingResponseDTO>>builder()
                .data(dtos)
                .message("space bookings retrieved successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional
    public String updateBookingStatus(String bookingId, UpdateBookingStatusRequestDTO requestDTO) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        booking.setStatus(requestDTO.getBookingStatus());
        log.info("booking {} status transitioned to {}", bookingId, requestDTO.getBookingStatus());

        return "booking status updated successfully";
    }

    private void validateProviderSpaceAccess(String spaceId) {
        String currentUserId = UserContext.getUserId();
        try {
            CommonApiResponse<ResponseSpacesDTO> response = catalogClient.getSpaceById(spaceId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("space not found with id" + spaceId);
            }
            String providerId = response.getData().getProviderId();
            if (!currentUserId.equals(providerId)) {
                throw new AccessDeniedException("you do not own the venue for space" + spaceId);
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("failed to verify space ownership for spaceId {} and userId {} Error{}", spaceId, currentUserId, e.getMessage());
            throw new AccessDeniedException("could not verify space ownership credentials");
        }
    }

    private BookingResponseDTO bookingResponseMapToDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .venueId(booking.getVenueId())
                .spaceId(booking.getSpaceId())
                .status(booking.getStatus())
                .basePriceAmount(booking.getBasePriceAmount())
                .resourceTotalAmount(booking.getResourceTotalAmount())
                .taxAmount(booking.getTaxAmount())
                .discountAmount(booking.getDiscountAmount())
                .totalAmount(booking.getTotalAmount())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .resourceItems(booking.getBookingResourceList() != null ?
                        booking.getBookingResourceList().stream().map(r -> BookingResourceResponseDTO.builder()
                                .id(r.getId())
                                .resourceId(r.getResourceId())
                                .name(r.getName())
                                .quantity(r.getQuantity())
                                .pricePerUnit(r.getPricePerUnit())
                                .build()
                        ).toList()
                        : List.of())
                .build();
    }
}


/*
* 1. The createBooking Flow (The Checkout Gate)
bookingRepo.findByIdempotencyKey(...)
The Scenario: A user clicks "Book Now" while on a laggy train Wi-Fi. The app thinks the request failed and automatically retries, sending a second identical request.
The Protection: The idempotencyKey is a unique ID generated by the phone for that specific button press. If we already saved it, we just return the existing booking instead of charging their credit card twice.

requestDTO.getSlotStartTime().isBefore(LocalDateTime.now())
The Scenario: A malicious user alters the API request to book a venue for yesterday so they don't have to pay.
The Protection: Unless they have a time machine, the system immediately rejects any past dates.

RLock lock = redissonClient.getLock(lockKey); lock.tryLock(0, 3, TimeUnit.SECONDS);
The Scenario: (The Black Friday Rush). Ten users click "Book" on the exact same studio space for 2:00 PM at the exact same millisecond. If all ten talk to your PostgreSQL database at once, the database will say "Sure, it's empty!" to all ten. You end up with 10 people fighting over one room.
The Protection: This is the Spin-Lock. It’s a narrow door where only one person can pass at a time. The first thread grabs the lock. The other 9 threads hit a closed door (tryLock(0) means "wait 0 seconds"), fail instantly, and the users see: "Someone else is looking at this seat!"

RBucket<String> holdBucket = redissonClient.getBucket(holdKey); if(holdBucket.isExists())
The Scenario: Thread 1 gets through the door, but someone else is already on the payment screen typing in their credit card.
The Protection: We check Redis for a "Reserved" sign (the hold). If the sign is there, we reject the new user immediately.

bookingRepo.findOverlappingActiveBookings(...)
The Scenario: A phantom booking somehow bypassed the cache, or the system crashed yesterday and left a permanent confirmed booking in the database.
The Protection: The ultimate safety net. We query the hard disk (Postgres) to guarantee the slot is 100% empty.

catalogClient.calculatePrice(priceReq);
The Scenario: A hacker intercepts the mobile app traffic and changes basePriceAmount: 5000 to basePriceAmount: 1.
The Protection: We never trust the client's math. We ask the central catalog-service to calculate the real price based on active dynamic rules (like weekend surges) and ensure the provider didn't just add a blackout for maintenance 5 seconds ago.

holdBucket.set(customerId, 5, TimeUnit.MINUTES);
The Scenario: We verified the slot is empty and the price is correct. Now we need to let the user pay.
The Protection: We slap a "Reserved" sign on the slot in Redis that automatically evaporates in exactly 5 minutes. If they abandon their phone, the seat frees itself.

Booking savedBooking = bookingRepo.save(booking);
The Scenario: The order needs to be recorded officially.
The Protection: We save it to Postgres with a status of PENDING_PAYMENT. It’s in the ledger, but not confirmed.

bookingEventPublisher.publishBookingCreated(event);
The Scenario: Other departments in your company need to know this happened.
The Protection: You yell into the Kafka megaphone. The Notification Service hears it and texts the user: "Complete your payment!". The Timeout Service hears it and starts a 5-minute countdown clock to cancel it if no payment arrives.

finally { if (isAcquired) lock.unlock(); }
The Scenario: The narrow door is still locked from step 3.
The Protection: No matter if the code succeeds, fails, or crashes, the finally block guarantees we unlock the door so the next person in line can step up.

2. The cancelBooking Flow (The Refund & Release)
bookingRepo.findById(...)
The Scenario: We need the receipt to process the return.
The Protection: Throws a 404 if the booking doesn't exist.

if (!UserContext.isAdmin() && ... !booking.getCustomerId().equals(currentUserId))
The Scenario: Malicious user Alice figures out Bob's booking ID and sends an API request to cancel Bob's wedding venue.
The Protection: The system checks the ID on the token against the ID on the receipt. If you didn't buy it, and you don't own the venue, you can't cancel it. Access Denied.

if (booking.getStatus() == BookingStatus.CANCELLED ...)
The Scenario: A user spam-clicks the cancel button, trying to trigger multiple refunds.
The Protection: If it’s already cancelled, we stop immediately.

if (LocalDateTime.now().isBefore(booking.getSlotStartTime().minusHours(24)))
The Scenario: A user books a music studio for tomorrow at 5 PM. They try to cancel it today at 6 PM (23 hours before). The studio owner loses money because it's too late to find a new renter.
The Protection: The 24-hour rule. If you cancel early, isEligibleForRefund becomes true. If you cancel late, the system marks it as [NO REFUND] and the provider keeps the money.

booking.setStatus(BookingStatus.CANCELLED);
The Scenario: The ledger must reflect the new reality.
The Protection: Because this method has @Transactional, Spring detects the change and automatically updates the database row when the method finishes.

redissonClient.getBucket(holdKey).delete();
The Scenario: A user gets to the Razorpay checkout screen, realizes they forgot their wallet, and clicks "Cancel Order".
The Protection: Normally, that 5-minute hold from createBooking would keep the seat locked. By deleting it manually right now, the seat instantly becomes available for the next customer without waiting for the timer to run out.

bookingEventPublisher.publishBookingCancelled(event);
The Scenario: The database says cancelled, but the customer's bank account hasn't been refunded yet.
The Protection: You broadcast the cancellation to Kafka. The payment-service catches it, looks at the event to see if [FULL REFUND APPROVED] is attached, and if so, talks to Stripe/Razorpay to automatically send the money back to the user's card.
* */