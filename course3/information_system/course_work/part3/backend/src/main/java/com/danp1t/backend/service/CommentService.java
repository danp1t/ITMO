package com.danp1t.backend.service;

import com.danp1t.backend.dto.CommentDTO;
import com.danp1t.backend.dto.CommentDetailDTO;
import com.danp1t.backend.dto.PostSimpleDTO;
import com.danp1t.backend.dto.AccountSimpleDTO;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.model.Comment;
import com.danp1t.backend.model.Post;
import com.danp1t.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    private CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getCreatedAt(),
                comment.getUserComment(),
                comment.getPost().getId(),
                comment.getAccount().getId(),
                comment.getAccount().getName()
        );
    }

    private CommentDetailDTO toDetailDTO(Comment comment) {
        PostSimpleDTO post = new PostSimpleDTO(
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getPost().getCreatedAt(),
                comment.getPost().getCountLike()
        );

        AccountSimpleDTO account = new AccountSimpleDTO(
                comment.getAccount().getId(),
                comment.getAccount().getName(),
                comment.getAccount().getEmail()
        );

        return new CommentDetailDTO(
                comment.getId(),
                comment.getCreatedAt(),
                comment.getUserComment(),
                post,
                account
        );
    }

    private Comment toEntity(CommentDTO dto) {
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        comment.setUserComment(dto.getUserComment());

        // Устанавливаем связь с постом
        if (dto.getPostId() != null) {
            Post post = new Post();
            post.setId(dto.getPostId());
            comment.setPost(post);
        }

        // Устанавливаем связь с аккаунтом
        if (dto.getAccountId() != null) {
            Account account = new Account();
            account.setId(dto.getAccountId());
            comment.setAccount(account);
        }

        return comment;
    }

    public List<CommentDTO> findAll() {
        return commentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CommentDTO> findById(Integer id) {
        return commentRepository.findById(id).map(this::toDTO);
    }

    public Optional<CommentDetailDTO> findByIdWithDetails(Integer id) {
        return commentRepository.findByIdWithDetails(id).map(this::toDetailDTO);
    }

    public List<CommentDTO> findByPostId(Integer postId) {
        return commentRepository.findByPostIdWithAccount(postId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CommentDTO> findByAccountId(Integer accountId) {
        return commentRepository.findByAccountId(accountId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO save(CommentDTO commentDTO) {
        Comment comment = toEntity(commentDTO);
        Comment saved = commentRepository.save(comment);
        return toDTO(saved);
    }

    public CommentDTO update(Integer id, CommentDTO commentDTO) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        Comment comment = toEntity(commentDTO);
        comment.setId(id);
        Comment updated = commentRepository.save(comment);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }
}