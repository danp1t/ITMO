package com.danp1t.backend.repository;

import com.danp1t.backend.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    List<Attachment> findByPostId(Integer postId);
    List<Attachment> findByTypeAttachmentId(Integer typeAttachmentId);

    @Query("SELECT a FROM Attachment a LEFT JOIN FETCH a.typeAttachment WHERE a.id = :id")
    Optional<Attachment> findByIdWithType(@Param("id") Integer id);
}