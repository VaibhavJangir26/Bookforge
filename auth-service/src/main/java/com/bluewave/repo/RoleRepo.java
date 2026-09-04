package com.bluewave.repo;

import com.bluewave.constants.AppRole;
import com.bluewave.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Roles,String> {

    Optional<Roles> findByAppRole(AppRole appRole);

}
