package com.bluewave.client;

import com.bluewave.dto.BookingResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOKING-SERVICE",fallback = BookingClientFallbackFactory.class)
public interface BookingClient {

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(@PathVariable String bookingId);

}
