package com.danp1t.backend.service;

import com.danp1t.backend.dto.AttachmentDTO;
import com.danp1t.backend.model.Attachment;
import com.danp1t.backend.model.Post;
import com.danp1t.backend.model.TypeAttachment;
import com.danp1t.backend.repository.AttachmentRepository;
import com.danp1t.backend.repository.PostRepository;
import com.danp1t.backend.repository.TypeAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TypeAttachmentRepository typeAttachmentRepository;

    private AttachmentDTO toDTO(Attachment attachment) {
        return new AttachmentDTO(
                attachment.getId(),
                attachment.getPost().getId(),
                attachment.getName(),
                attachment.getPath(),
                attachment.getTypeAttachment().getId(),
                attachment.getTypeAttachment().getName()
        );
    }

    private Attachment toEntity(AttachmentDTO dto) {
        Attachment attachment = new Attachment();
        attachment.setId(dto.getId());
        attachment.setName(dto.getName());
        attachment.setPath(dto.getPath());

        // Устанавливаем связь с Post
        if (dto.getPostId() != null) {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found with id: " + dto.getPostId()));
            attachment.setPost(post);
        } else {
            throw new RuntimeException("PostId cannot be null for Attachment");
        }

        // Устанавливаем связь с TypeAttachment
        if (dto.getTypeAttachmentId() != null) {
            TypeAttachment typeAttachment = typeAttachmentRepository.findById(dto.getTypeAttachmentId())
                    .orElseThrow(() -> new RuntimeException("TypeAttachment not found with id: " + dto.getTypeAttachmentId()));
            attachment.setTypeAttachment(typeAttachment);
        } else {
            throw new RuntimeException("TypeAttachmentId cannot be null for Attachment");
        }

        return attachment;
    }

    public List<AttachmentDTO> findAll() {
        return attachmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<AttachmentDTO> findById(Integer id) {
        return attachmentRepository.findById(id).map(this::toDTO);
    }

    public List<AttachmentDTO> findByPostId(Integer postId) {
        return attachmentRepository.findByPostId(postId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AttachmentDTO> findByTypeAttachmentId(Integer typeAttachmentId) {
        return attachmentRepository.findByTypeAttachmentId(typeAttachmentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AttachmentDTO save(AttachmentDTO attachmentDTO) {
        Attachment attachment = toEntity(attachmentDTO);
        Attachment saved = attachmentRepository.save(attachment);
        return toDTO(saved);
    }

    public AttachmentDTO update(Integer id, AttachmentDTO attachmentDTO) {
        if (!attachmentRepository.existsById(id)) {
            throw new RuntimeException("Attachment not found with id: " + id);
        }
        Attachment attachment = toEntity(attachmentDTO);
        attachment.setId(id);
        Attachment updated = attachmentRepository.save(attachment);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!attachmentRepository.existsById(id)) {
            throw new RuntimeException("Attachment not found with id: " + id);
        }
        attachmentRepository.deleteById(id);
    }
}