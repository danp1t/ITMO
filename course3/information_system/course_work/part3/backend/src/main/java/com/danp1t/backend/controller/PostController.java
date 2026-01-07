package com.danp1t.backend.controller;

import com.danp1t.backend.dto.PostDTO;
import com.danp1t.backend.dto.PostDetailDTO;
import com.danp1t.backend.dto.TagDTO;
import com.danp1t.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Integer id) {
        return postService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<PostDetailDTO> getPostWithDetails(@PathVariable Integer id) {
        return postService.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PostDTO>> getPostsByOwner(@PathVariable Integer ownerId) {
        return ResponseEntity.ok(postService.findByOwnerId(ownerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam String title) {
        return ResponseEntity.ok(postService.findByTitleContaining(title));
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        PostDTO created = postService.save(postDTO, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Integer id,
                                              @RequestBody PostDTO postDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PostDTO updated = postService.update(id, postDTO, userDetails.getUsername());
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            postService.deleteById(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Integer id) {
        try {
            postService.likePost(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<Void> unlikePost(@PathVariable Integer id) {
        try {
            postService.unlikePost(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<PostDTO>> getPostsByTag(@PathVariable Integer tagId) {
        return ResponseEntity.ok(postService.findByTagId(tagId));
    }

    @PostMapping("/{postId}/tags/{tagId}")
    public ResponseEntity<Void> addTagToPost(@PathVariable Integer postId,
                                             @PathVariable Integer tagId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            postService.addTagToPost(postId, tagId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tag/name/{tagName}")
    public ResponseEntity<List<PostDTO>> getPostsByTagName(@PathVariable String tagName) {
        return ResponseEntity.ok(postService.findByTagName(tagName));
    }

    @DeleteMapping("/{postId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromPost(@PathVariable Integer postId,
                                                  @PathVariable Integer tagId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            postService.removeTagFromPost(postId, tagId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Получить теги поста
    @GetMapping("/{postId}/tags")
    public ResponseEntity<List<TagDTO>> getPostTags(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.getPostTags(postId));
    }


}