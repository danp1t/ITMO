package com.danp1t.backend.repository;

import com.danp1t.backend.model.TypeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TypeAttachmentRepository extends JpaRepository<TypeAttachment, Integer> {
    Optional<TypeAttachment> findByName(String name);
}