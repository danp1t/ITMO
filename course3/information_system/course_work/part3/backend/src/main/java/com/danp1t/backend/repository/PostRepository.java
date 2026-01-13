package com.danp1t.backend.repository;

import com.danp1t.backend.model.Account;
import com.danp1t.backend.model.Post;
import com.danp1t.backend.model.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByTitleContainingIgnoreCase(String title, Sort sort);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.tags ORDER BY p.createdAt DESC")
    List<Post> findAllWithOwnerAndTags(Sort sort);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Post> findByIdWithOwner(@Param("id") Integer id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.attachments LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.account WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Integer id);

    @Query("SELECT p FROM Post p JOIN p.owner o WHERE o.id = :ownerId")
    List<Post> findByOwnerId(@Param("ownerId") Integer ownerId, Sort sort);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    List<Post> findByTagId(@Param("tagId") Integer tagId, Sort sort);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    List<Post> findByTagName(@Param("tagName") String tagName, Sort sort);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.email = :email")
    Optional<Account> findAccountByEmailWithRoles(@Param("email") String email);

    @Query("SELECT t FROM Tag t WHERE t.id IN :ids")
    List<Tag> findTagsByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT t FROM Tag t WHERE t.id = :id")
    Optional<Tag> findTagById(@Param("id") Integer id);
}