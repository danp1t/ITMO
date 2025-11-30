package com.danp1t.backend.repository;

import com.danp1t.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.attachments LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner ORDER BY p.createdAt DESC")
    List<Post> findAllWithOwner();

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id = :tagId " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByTagId(@Param("tagId") Integer tagId);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.name = :tagName " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner WHERE p.owner.id = :ownerId ORDER BY p.createdAt DESC")
    List<Post> findByOwnerId(@Param("ownerId") Integer ownerId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY p.createdAt DESC")
    List<Post> findByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Post> findByIdWithOwner(@Param("id") Integer id);
}