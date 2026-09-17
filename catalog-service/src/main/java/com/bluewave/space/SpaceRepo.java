package com.bluewave.space;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepo extends JpaRepository<Space,String> {

}
