package com.bluewave.availablity.repo;

import com.bluewave.availablity.model.BlackoutSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlackoutSlotRepo extends JpaRepository<BlackoutSlot, String> {
    List<BlackoutSlot> findBySpaceId(String spaceId);

    @Query("SELECT b FROM BlackoutSlot b WHERE b.space.id = :spaceId " +
            "AND b.startDateTime < :endDateTime AND b.endDateTime > :startDateTime")
    List<BlackoutSlot> findOverlappingBlackouts(
            @Param("spaceId") String spaceId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}