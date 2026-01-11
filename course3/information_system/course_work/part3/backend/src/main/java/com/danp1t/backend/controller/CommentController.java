package com.danp1t.backend.controller;

import com.danp1t.backend.dto.CommentDTO;
import com.danp1t.backend.dto.CommentDetailDTO;
import com.danp1t.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return ResponseEntity.ok(commentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Integer id) {
        return commentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<CommentDetailDTO> getCommentWithDetails(@PathVariable Integer id) {
        return commentService.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByAccount(@PathVariable Integer accountId) {
        return ResponseEntity.ok(commentService.findByAccountId(accountId));
    }

    @PostMapping
    public ResponseEntity<Integer> createComment(@RequestBody CommentDTO commentDTO) {
        Integer commentId = commentService.save(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Integer id, @RequestBody CommentDTO commentDTO) {
        try {
            CommentDTO updated = commentService.update(id, commentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        try {
            commentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}