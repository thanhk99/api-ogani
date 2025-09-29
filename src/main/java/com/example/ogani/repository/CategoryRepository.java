package com.example.ogani.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("Select c from Category c where c.enable = true")
    List<Category> findALLByEnabled();
    boolean existsByName(String name);

    Optional<Category> findByIdAndEnable(long id, boolean b);
}
