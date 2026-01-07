package com.danp1t.backend.repository;

import com.danp1t.backend.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    // Основные методы
    List<Post> findAll();

    Optional<Post> findById(Integer id);

    // Найти по владельцу
    List<Post> findByOwnerId(Integer ownerId);

    // Поиск по заголовку
    List<Post> findByTitleContainingIgnoreCase(String title);

    // Найти все посты с владельцем (жадная загрузка)
    @Query("SELECT p FROM Post p JOIN FETCH p.owner")
    List<Post> findAllWithOwner();

    // Найти пост по ID с владельцем
    @Query("SELECT p FROM Post p JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Post> findByIdWithOwner(@Param("id") Integer id);

    // Найти пост по ID со всеми деталями
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.attachments " +
            "LEFT JOIN FETCH p.tags " +
            "LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.account " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Integer id);

    // Найти посты по тегу
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id = :tagId " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByTagId(@Param("tagId") Integer tagId);

    // Найти посты по имени тега
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.name = :tagName " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByTagName(@Param("tagName") String tagName);

    // Найти посты по нескольким тегам (AND - все теги должны быть)
    @Query("SELECT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id IN :tagIds " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByAllTags(@Param("tagIds") List<Integer> tagIds, @Param("tagCount") long tagCount);

    // Найти посты по нескольким тегам (OR - любой из тегов)
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id IN :tagIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByAnyTag(@Param("tagIds") List<Integer> tagIds);


    // Найти пост по ID с тегами
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.tags")
    List<Post> findAllWithOwnerAndTags();
}