package com.bluewave.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepo extends JpaRepository<Venue,String> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug,String id);

    List<Venue> findByProviderId(String providerId);

    List<Venue> findByCategoryId(String categoryId);

}
