package com.danp1t.backend.repository;

import com.danp1t.backend.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findAll(Sort sort);

    Optional<Post> findById(Integer id);

    List<Post> findByOwnerId(Integer ownerId, Sort sort);

    List<Post> findByTitleContainingIgnoreCase(String title, Sort sort);

    @Query("SELECT p FROM Post p JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Post> findByIdWithOwner(@Param("id") Integer id);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.attachments " +
            "LEFT JOIN FETCH p.tags " +
            "LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.account " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id = :tagId")
    List<Post> findByTagId(@Param("tagId") Integer tagId, Sort sort);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.name = :tagName")
    List<Post> findByTagName(@Param("tagName") String tagName, Sort sort);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.tags")
    List<Post> findAllWithOwnerAndTags(Sort sort);
}