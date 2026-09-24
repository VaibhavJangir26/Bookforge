package com.bluewave.booking;

import com.bluewave.booking.dto.BookingResponseDTO;
import com.bluewave.booking.dto.CancelBookingRequestDTO;
import com.bluewave.booking.dto.CreateBookingRequestDTO;
import com.bluewave.booking.dto.UpdateBookingStatusRequestDTO;
import com.bluewave.booking.model.Booking;
import com.bluewave.dto.CommonApiResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepo bookingRepo;
    private final BookingResourceRepo bookingResourceRepo;
    private final RedissonClient redissonClient;


    @Transactional
    public CommonApiResponse<BookingResponseDTO> createBooking(CreateBookingRequestDTO requestDTO) {
    return null;
    }

    @Transactional
    public String cancelBooking(CancelBookingRequestDTO requestDTO) {

        return "booking cancel successfully";
    }

    public CommonApiResponse<List<BookingResponseDTO>> getAllMyBookingHistory() {
        return null;}

    @Transactional
    public String updateBookingStatus(String bookingId, UpdateBookingStatusRequestDTO requestDTO) {


        return "booking status updated successfully";
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<BookingResponseDTO> getSingleBookingDetails(String bookingId) {

        return null;
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<BookingResponseDTO>> getBookingsBySpace(String spaceId) {
        return null;
    }


    private BookingResponseDTO bookingResponseMapToDTO(Booking booking){
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .venueId(booking.getVenueId())
                .spaceId(booking.getSpaceId())
                .status(booking.getStatus())
                .basePriceAmount(booking.getBasePriceAmount())
                .taxAmount(booking.getTaxAmount())
                .discountAmount(booking.getDiscountAmount())
                .totalAmount(booking.getTotalAmount())
                .cancellationReason(booking.getCancellationReason())
                .build();
    }



}
