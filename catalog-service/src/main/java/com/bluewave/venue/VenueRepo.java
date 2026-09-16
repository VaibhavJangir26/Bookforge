package com.bluewave.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepo extends JpaRepository<Venue,String> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug,String id);

}
