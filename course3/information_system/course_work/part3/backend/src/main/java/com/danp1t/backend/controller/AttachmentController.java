package com.danp1t.backend.controller;

import com.danp1t.backend.dto.AttachmentDTO;
import com.danp1t.backend.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
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
            attachmentService.deleteAttachmentFile(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("postId") Integer postId,
            @RequestParam("typeAttachmentId") Integer typeAttachmentId) {
        try {
            AttachmentDTO attachmentDTO = attachmentService.uploadFile(file, postId, typeAttachmentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachmentDTO);
        } catch (RuntimeException e) {
            System.out.printf("Error: %s\n", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer id) {
        AttachmentDTO attachment = attachmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        try {
            Path filePath = Paths.get("uploads", attachment.getPath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getName() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}