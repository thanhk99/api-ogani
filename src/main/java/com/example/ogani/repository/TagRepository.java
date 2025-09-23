package com.example.ogani.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Tag findByName(String name);

}
