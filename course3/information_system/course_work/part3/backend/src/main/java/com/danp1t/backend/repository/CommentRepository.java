package com.danp1t.backend.repository;

import com.danp1t.backend.dto.CommentDTO;
import com.danp1t.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByAccountId(Integer accountId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.account WHERE c.id = :id")
    Optional<Comment> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.account WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdWithAccount(@Param("postId") Integer postId);

    @Query(value = "SELECT * FROM create_comment(:userComment, :postId, :accountId)",
            nativeQuery = true)
    Integer createComment(
            @Param("userComment") String userComment,
            @Param("postId") Integer postId,
            @Param("accountId") Integer accountId
    );
}