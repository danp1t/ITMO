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

    // Основные методы с сортировкой
    List<Post> findAll(Sort sort);

    Optional<Post> findById(Integer id);

    // Найти по владельцу с сортировкой
    List<Post> findByOwnerId(Integer ownerId, Sort sort);

    // Поиск по заголовку с сортировкой
    List<Post> findByTitleContainingIgnoreCase(String title, Sort sort);

    // Найти все посты с владельцем (жадная загрузка) с сортировкой
    @Query("SELECT p FROM Post p JOIN FETCH p.owner")
    List<Post> findAllWithOwner(Sort sort);

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

    // Найти посты по тегу с сортировкой
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id = :tagId")
    List<Post> findByTagId(@Param("tagId") Integer tagId, Sort sort);

    // Найти посты по имени тега с сортировкой
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.name = :tagName")
    List<Post> findByTagName(@Param("tagName") String tagName, Sort sort);

    // Найти посты по нескольким тегам (AND - все теги должны быть) с сортировкой
    @Query("SELECT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id IN :tagIds " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount")
    List<Post> findByAllTags(@Param("tagIds") List<Integer> tagIds, @Param("tagCount") long tagCount, Sort sort);

    // Найти посты по нескольким тегам (OR - любой из тегов) с сортировкой
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE t.id IN :tagIds")
    List<Post> findByAnyTag(@Param("tagIds") List<Integer> tagIds, Sort sort);

    // Найти пост по ID с тегами
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Integer id);

    // Найти все посты с владельцем и тегами с сортировкой
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.tags")
    List<Post> findAllWithOwnerAndTags(Sort sort);
}