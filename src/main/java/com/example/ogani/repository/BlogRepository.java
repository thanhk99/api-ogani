package com.example.ogani.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

}
