package com.bluewave.booking;

import com.bluewave.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking,String> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b FROM Booking b WHERE b.spaceId = :spaceId " +
            "AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED') " +
            "AND b.slotStartTime < :endTime AND b.slotEndTime > :startTime")
    List<Booking> findOverlappingActiveBookings(
            @Param("spaceId") String spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}
