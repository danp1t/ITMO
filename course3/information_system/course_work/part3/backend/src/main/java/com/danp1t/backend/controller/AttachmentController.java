package com.danp1t.backend.controller;

import com.danp1t.backend.dto.AttachmentDTO;
import com.danp1t.backend.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "*")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping
    public ResponseEntity<List<AttachmentDTO>> getAllAttachments() {
        return ResponseEntity.ok(attachmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDTO> getAttachmentById(@PathVariable Integer id) {
        return attachmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(attachmentService.findByPostId(postId));
    }

    @GetMapping("/type/{typeAttachmentId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByType(@PathVariable Integer typeAttachmentId) {
        return ResponseEntity.ok(attachmentService.findByTypeAttachmentId(typeAttachmentId));
    }

    @PostMapping
    public ResponseEntity<AttachmentDTO> createAttachment(@RequestBody AttachmentDTO attachmentDTO) {
        AttachmentDTO created = attachmentService.save(attachmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttachmentDTO> updateAttachment(@PathVariable Integer id, @RequestBody AttachmentDTO attachmentDTO) {
        try {
            AttachmentDTO updated = attachmentService.update(id, attachmentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Integer id) {
        try {
            attachmentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}