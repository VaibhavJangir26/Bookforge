package com.bluewave.availablity.repo;

import com.bluewave.availablity.model.AvailableRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailableRepo extends JpaRepository<AvailableRule,String> {

    List<AvailableRule> findAllByAvailable_spaceId(String spaceId);


}
