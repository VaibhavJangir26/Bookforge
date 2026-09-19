package com.bluewave.availablity.repo;

import com.bluewave.availablity.DayOfWeek;
import com.bluewave.availablity.model.AvailableRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailableRepo extends JpaRepository<AvailableRule, String> {

    List<AvailableRule> findBySpaceId(String spaceId);

    Optional<AvailableRule> findBySpaceIdAndDayOfWeek(String spaceId, DayOfWeek dayOfWeek);

    boolean existsBySpaceIdAndDayOfWeek(String spaceId, DayOfWeek dayOfWeek);
}