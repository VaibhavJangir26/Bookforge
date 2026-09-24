package com.bluewave.booking;

import com.bluewave.booking.dto.BookingResponseDTO;
import com.bluewave.booking.dto.CancelBookingRequestDTO;
import com.bluewave.booking.dto.CreateBookingRequestDTO;
import com.bluewave.booking.dto.UpdateBookingStatusRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create-booking")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<BookingResponseDTO>> createBooking(
            @Valid @RequestBody CreateBookingRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(requestDTO));
    }

    @PostMapping("/cancel-booking")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @Valid @RequestBody CancelBookingRequestDTO requestDTO) {
        String message = bookingService.cancelBooking(requestDTO);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<List<BookingResponseDTO>>> getAllMyBookingHistory() {
        return ResponseEntity.ok(bookingService.getAllMyBookingHistory());
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<BookingResponseDTO>> getSingleBookingDetails(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.getSingleBookingDetails(bookingId));
    }

    @GetMapping("/space/{spaceId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<List<BookingResponseDTO>>> getBookingsBySpace(
            @PathVariable String spaceId) {
        return ResponseEntity.ok(bookingService.getBookingsBySpace(spaceId));
    }

    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateBookingStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody UpdateBookingStatusRequestDTO requestDTO) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, requestDTO));
    }
}