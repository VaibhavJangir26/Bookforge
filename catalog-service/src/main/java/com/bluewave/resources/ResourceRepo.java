package com.bluewave.resources;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepo extends JpaRepository<Resources, String> {
    List<Resources> findBySpaceId(String spaceId);
}