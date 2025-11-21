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
    List<Post> findByOwnerId(Integer ownerId);
    List<Post> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.attachments LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner ORDER BY p.createdAt DESC")
    List<Post> findAllWithOwner();
}