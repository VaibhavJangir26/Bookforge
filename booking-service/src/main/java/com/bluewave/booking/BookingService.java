package com.bluewave.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepo bookingRepo;
    private final BookingResourceRepo bookingResourceRepo;



}
