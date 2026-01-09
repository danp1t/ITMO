package com.danp1t.backend.repository;

import com.danp1t.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    List<Tag> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);

    @Query("SELECT t FROM Tag t JOIN t.posts p WHERE p.id = :postId ORDER BY t.name")
    List<Tag> findByPostId(@Param("postId") Integer postId);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.posts WHERE t.id = :id")
    Optional<Tag> findByIdWithPosts(@Param("id") Integer id);

}