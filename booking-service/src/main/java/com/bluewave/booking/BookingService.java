package com.bluewave.booking;

import com.bluewave.booking.client.CatalogClient;
import com.bluewave.booking.dto.BookingResourceResponseDTO;
import com.bluewave.booking.dto.BookingResponseDTO;
import com.bluewave.booking.dto.CancelBookingRequestDTO;
import com.bluewave.booking.dto.CreateBookingRequestDTO;
import com.bluewave.booking.dto.UpdateBookingStatusRequestDTO;
import com.bluewave.booking.model.Booking;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ResponseSpacesDTO;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.kafka.BookingEventPublisher;
import com.bluewave.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepo bookingRepo;
    private final BookingResourceRepo bookingResourceRepo;
    private final RedissonClient redissonClient;
    private final CatalogClient catalogClient;
    private final BookingEventPublisher bookingEventPublisher;

    @Transactional
    public CommonApiResponse<BookingResponseDTO> createBooking(CreateBookingRequestDTO requestDTO) {
        // Reserved for your custom implementation
        return null;
    }

    @Transactional
    public String cancelBooking(CancelBookingRequestDTO requestDTO) {
        // Reserved for your custom implementation
        return "booking cancelled successfully";
    }

    /**
     * Retrieves the booking history for the authenticated user.
     * Customers get bookings where they are the buyer.
     * If an Admin invokes this, it returns bookings tied to their user account.
     */
    @Transactional(readOnly = true)
    public CommonApiResponse<List<BookingResponseDTO>> getAllMyBookingHistory() {
        String currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("Authentication required to access booking history");
        }

        List<Booking> bookings = bookingRepo.findAllByCustomerId(currentUserId);
        List<BookingResponseDTO> dtos = bookings.stream()
                .map(this::bookingResponseMapToDTO)
                .toList();

        return CommonApiResponse.<List<BookingResponseDTO>>builder()
                .data(dtos)
                .message("Booking history fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Secure retrieval of a single booking.
     * Accessible by:
     * 1. The customer who booked it.
     * 2. The provider who owns the space.
     * 3. An Admin.
     */
    @Transactional(readOnly = true)
    public CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(String bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

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

    /**
     * Retrieves all bookings for a given space.
     * Strictly reserved for the Provider who owns the space, or an Admin.
     */
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
                .message("Space bookings retrieved successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Admin override for lifecycle status transition.
     */
    @Transactional
    public String updateBookingStatus(String bookingId, UpdateBookingStatusRequestDTO requestDTO) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        booking.setStatus(requestDTO.getBookingStatus());
        log.info("Booking {} status transitioned to {} by admin", bookingId, requestDTO.getBookingStatus());

        return "Booking status updated successfully";
    }

    /**
     * Enforces strict multi-tenancy.
     * Confirms the calling provider is the legitimate owner of the space via catalog-service.
     */
    private void validateProviderSpaceAccess(String spaceId) {
        String currentUserId = UserContext.getUserId();

        try {
            CommonApiResponse<ResponseSpacesDTO> response = catalogClient.getSpaceById(spaceId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Space not found with id: " + spaceId);
            }

            String providerId = response.getData().getProviderId();
            if (!currentUserId.equals(providerId)) {
                throw new AccessDeniedException("Access denied: You do not own the venue for space " + spaceId);
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify space ownership for spaceId: {} and userId: {}. Error: {}", spaceId, currentUserId, e.getMessage());
            throw new AccessDeniedException("Could not verify space ownership credentials");
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