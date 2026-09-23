package com.bluewave.booking;

import com.bluewave.booking.model.BookingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingResourceRepo extends JpaRepository<BookingResource,String> {

}
