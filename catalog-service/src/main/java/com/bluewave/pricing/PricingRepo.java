package com.bluewave.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRepo extends JpaRepository<PricingRule, String> {
    List<PricingRule> findBySpaceId(String spaceId);
    List<PricingRule> findBySpaceIdAndActiveTrueOrderByPriorityDesc(String spaceId);
}