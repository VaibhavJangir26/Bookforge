package com.bluewave.space;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SpaceRepo extends JpaRepository<Space,String> {

    List<Space> findByVenueId(String venueId);

    List<Space> findByVenueIdAndActiveTrue(String venueId);

}
