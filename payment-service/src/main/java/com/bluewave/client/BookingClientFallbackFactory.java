package com.bluewave.client;

import com.bluewave.dto.BookingResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.UpdateBookingStatusRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BookingClientFallbackFactory implements FallbackFactory<BookingClient> {

    @Override
    public BookingClient create(Throwable cause) {
        return new BookingClient() {
            @Override
            public CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(String bookingId) {
                log.error("BookingClient fallback triggered for getSingleBookingDetails {}. Reason: {}", bookingId, cause.getMessage());
                return CommonApiResponse.<BookingResponseDTO>builder()
                        .success(false)
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .message("Booking service unavailable: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }

            @Override
            public ResponseEntity<String> updateBookingStatus(String bookingId, UpdateBookingStatusRequestDTO requestDTO) {
                log.error("BookingClient fallback triggered for updateBookingStatus {}. Reason: {}", bookingId, cause.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Booking service unavailable for status update: " + cause.getMessage());
            }
        };
    }
}
