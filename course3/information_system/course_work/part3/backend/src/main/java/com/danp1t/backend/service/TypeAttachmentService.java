package com.danp1t.backend.service;

import com.danp1t.backend.model.TypeAttachment;
import com.danp1t.backend.repository.TypeAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;

@Service
public class TypeAttachmentService {

    @Autowired
    private TypeAttachmentRepository typeAttachmentRepository;

    @Transactional(readOnly = true)
    public List<TypeAttachment> findAll() {
        return typeAttachmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TypeAttachment> findById(Integer id) {
        return typeAttachmentRepository.findById(id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TypeAttachment save(TypeAttachment typeAttachment) {
        return typeAttachmentRepository.save(typeAttachment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(Integer id) {
        if (!typeAttachmentRepository.existsById(id)) {
            throw new RuntimeException("TypeAttachment not found with id: " + id);
        }
        typeAttachmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return typeAttachmentRepository.existsById(id);
    }
}