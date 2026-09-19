package com.bluewave.availablity.repo;

import com.bluewave.availablity.model.BlackoutSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackoutSlotRepo extends JpaRepository<BlackoutSlot,String > {
    List<BlackoutSlot> findAllByBlackout_spaceId(String spaceId);
}
