package com.danp1t.backend.service;

import com.danp1t.backend.dto.*;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.model.Post;
import com.danp1t.backend.model.Tag;
import com.danp1t.backend.repository.AccountRepository;
import com.danp1t.backend.repository.PostRepository;
import com.danp1t.backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TagRepository tagRepository;

    private PostDTO toDTO(Post post) {
        List<TagDTO> tagDTOs = null;
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            tagDTOs = post.getTags().stream()
                    .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getDescription()))
                    .collect(Collectors.toList());
        }

        Integer commentsCount = post.getComments() != null ? post.getComments().size() : 0;

        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getCreatedAt(),
                post.getCountLike(),
                post.getOwner().getId(),
                post.getOwner().getName(),
                tagDTOs,
                commentsCount
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
        post.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        post.setCountLike(dto.getCountLike() != null ? dto.getCountLike() : 0);

        if (dto.getOwnerId() != null) {
            Account owner = new Account();
            owner.setId(dto.getOwnerId());
            post.setOwner(owner);
        }

        return post;
    }

    public List<PostDTO> findAll() {
        return postRepository.findAllWithOwnerAndTags().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    @Transactional
    public PostDTO save(PostDTO postDTO, String currentUserEmail) {
        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean canPublish = currentUser.getRoles().stream()
                .anyMatch(role -> "OAPI:ROLE:PublishPost".equals(role.getName()));

        if (!canPublish) {
            throw new SecurityException("User doesn't have permission to publish posts");
        }

        Post post = toEntity(postDTO);
        post.setOwner(currentUser);

        // Сохраняем теги если есть
        if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(
                    postDTO.getTags().stream()
                            .map(TagDTO::getId)
                            .collect(Collectors.toList())
            );
            post.setTags(tags);
        }

        Post saved = postRepository.save(post);
        return toDTO(saved);
    }

    @Transactional
    public PostDTO update(Integer id, PostDTO postDTO, String currentUserEmail) {
        Post existingPost = postRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, является ли пользователь владельцем или имеет роль редактирования
        boolean isOwner = existingPost.getOwner().getEmail().equals(currentUserEmail);
        boolean canEdit = currentUser.getRoles().stream()
                .anyMatch(role -> "OAPI:ROLE:EditPost".equals(role.getName()));

        if (!isOwner && !canEdit) {
            throw new SecurityException("Not authorized to edit this post");
        }

        // Обновляем пост
        existingPost.setTitle(postDTO.getTitle());
        existingPost.setText(postDTO.getText());

        // Обновляем теги если есть
        if (postDTO.getTags() != null) {
            List<Tag> tags = tagRepository.findAllById(
                    postDTO.getTags().stream()
                            .map(TagDTO::getId)
                            .collect(Collectors.toList())
            );
            existingPost.setTags(tags);
        }

        Post updated = postRepository.save(existingPost);
        return toDTO(updated);
    }

    @Transactional
    public void deleteById(Integer id, String currentUserEmail) {
        Post existingPost = postRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, является ли пользователь владельцем или имеет роль удаления
        boolean isOwner = existingPost.getOwner().getEmail().equals(currentUserEmail);
        boolean canDelete = currentUser.getRoles().stream()
                .anyMatch(role -> "OAPI:ROLE:DeletePost".equals(role.getName()));

        if (!isOwner && !canDelete) {
            throw new SecurityException("Not authorized to delete this post");
        }

        postRepository.deleteById(id);
    }

    @Transactional
    public void likePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setCountLike(post.getCountLike() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        if (post.getCountLike() > 0) {
            post.setCountLike(post.getCountLike() - 1);
            postRepository.save(post);
        }
    }

    public List<PostDTO> findByTagId(Integer tagId) {
        return postRepository.findByTagId(tagId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> findByTagName(String tagName) {
        return postRepository.findByTagName(tagName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addTagToPost(Integer postId, Integer tagId, String currentUserEmail) {
        Post post = postRepository.findByIdWithOwner(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка прав
        boolean isOwner = post.getOwner().getEmail().equals(currentUserEmail);
        boolean canEdit = currentUser.getRoles().stream()
                .anyMatch(role -> "OAPI:ROLE:EditPost".equals(role.getName()));

        if (!isOwner && !canEdit) {
            throw new SecurityException("Not authorized to edit this post");
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (!post.getTags().contains(tag)) {
            post.getTags().add(tag);
            postRepository.save(post);
        }
    }

    @Transactional
    public void removeTagFromPost(Integer postId, Integer tagId, String currentUserEmail) {
        Post post = postRepository.findByIdWithOwner(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка прав
        boolean isOwner = post.getOwner().getEmail().equals(currentUserEmail);
        boolean canEdit = currentUser.getRoles().stream()
                .anyMatch(role -> "OAPI:ROLE:EditPost".equals(role.getName()));

        if (!isOwner && !canEdit) {
            throw new SecurityException("Not authorized to edit this post");
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        post.getTags().remove(tag);
        postRepository.save(post);
    }

    public List<TagDTO> getPostTags(Integer postId) {
        Post post = postRepository.findByIdWithTags(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return post.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getDescription()))
                .collect(Collectors.toList());
    }
}