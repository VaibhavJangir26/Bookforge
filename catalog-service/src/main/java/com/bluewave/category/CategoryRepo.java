package com.bluewave.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category,String> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

}
