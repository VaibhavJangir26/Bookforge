package com.bluewave.client;

import com.bluewave.dto.BookingResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.UpdateBookingStatusRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "BOOKING-SERVICE", fallbackFactory = BookingClientFallbackFactory.class)
public interface BookingClient {

    @GetMapping("/api/v1/booking/{bookingId}")
    CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(@PathVariable("bookingId") String bookingId);

    @PatchMapping("/api/v1/booking/{bookingId}/status")
    ResponseEntity<String> updateBookingStatus(@PathVariable("bookingId") String bookingId, @RequestBody UpdateBookingStatusRequestDTO requestDTO);
}
