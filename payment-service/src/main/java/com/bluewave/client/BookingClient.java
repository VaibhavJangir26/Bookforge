package com.bluewave.client;

import com.bluewave.dto.BookingResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOKING-SERVICE", fallbackFactory = BookingClientFallbackFactory.class)
public interface BookingClient {

    @GetMapping("/api/v1/booking/{bookingId}")
    CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(@PathVariable("bookingId") String bookingId);
}