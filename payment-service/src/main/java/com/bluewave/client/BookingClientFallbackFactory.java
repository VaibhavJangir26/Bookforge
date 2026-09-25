package com.bluewave.client;

import com.bluewave.dto.CommonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BookingClientFallbackFactory implements FallbackFactory<BookingClient> {

    @Override
    public BookingClient create(Throwable cause) {
        return bookingId -> {
            log.error("BookingClient fallback triggered for bookingId {}. Reason: {}", bookingId, cause.getMessage());
            return CommonApiResponse.<com.bluewave.dto.BookingResponseDTO>builder()
                    .success(false)
                    .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .message("Booking service unavailable: " + cause.getMessage())
                    .timestamp(LocalDateTime.now())
                    .data(null)
                    .build();
        };
    }
}