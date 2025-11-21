package com.danp1t.backend.service;

import com.danp1t.backend.dto.PostDTO;
import com.danp1t.backend.dto.PostDetailDTO;
import com.danp1t.backend.dto.AttachmentSimpleDTO;
import com.danp1t.backend.dto.TagDTO;
import com.danp1t.backend.dto.CommentSimpleDTO;
import com.danp1t.backend.dto.AccountSimpleDTO;
import com.danp1t.backend.model.Post;
import com.danp1t.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    private PostDTO toDTO(Post post) {
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getCreatedAt(),
                post.getCountLike(),
                post.getOwner().getId(),
                post.getOwner().getName()
        );
    }

    private PostDetailDTO toDetailDTO(Post post) {
        AccountSimpleDTO owner = new AccountSimpleDTO(
                post.getOwner().getId(),
                post.getOwner().getName(),
                post.getOwner().getEmail()
        );

        List<AttachmentSimpleDTO> attachments = post.getAttachments().stream()
                .map(attachment -> new AttachmentSimpleDTO(
                        attachment.getId(),
                        attachment.getName(),
                        attachment.getPath(),
                        attachment.getTypeAttachment().getName()
                ))
                .collect(Collectors.toList());

        List<TagDTO> tags = post.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getDescription()))
                .collect(Collectors.toList());

        List<CommentSimpleDTO> comments = post.getComments().stream()
                .map(comment -> new CommentSimpleDTO(
                        comment.getId(),
                        comment.getCreatedAt(),
                        comment.getUserComment(),
                        comment.getAccount().getName()
                ))
                .collect(Collectors.toList());

        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getCreatedAt(),
                post.getCountLike(),
                owner,
                attachments,
                tags,
                comments
        );
    }

    private Post toEntity(PostDTO dto) {
        Post post = new Post();
        post.setId(dto.getId());
        post.setTitle(dto.getTitle());
        post.setText(dto.getText());
        post.setCreatedAt(dto.getCreatedAt());
        post.setCountLike(dto.getCountLike());
        // Owner устанавливается отдельно через ID
        return post;
    }

    public List<PostDTO> findAll() {
        return postRepository.findAllWithOwner().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<PostDTO> findById(Integer id) {
        return postRepository.findById(id).map(this::toDTO);
    }

    public Optional<PostDetailDTO> findByIdWithDetails(Integer id) {
        return postRepository.findByIdWithDetails(id).map(this::toDetailDTO);
    }

    public List<PostDTO> findByOwnerId(Integer ownerId) {
        return postRepository.findByOwnerId(ownerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> findByTitleContaining(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PostDTO save(PostDTO postDTO) {
        Post post = toEntity(postDTO);
        Post saved = postRepository.save(post);
        return toDTO(saved);
    }

    public PostDTO update(Integer id, PostDTO postDTO) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        Post post = toEntity(postDTO);
        post.setId(id);
        Post updated = postRepository.save(post);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }
}