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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

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

        if (dto.getPostId() != null) {
            Post post = new Post();
            post.setId(dto.getPostId());
            comment.setPost(post);
        }

        if (dto.getAccountId() != null) {
            Account account = new Account();
            account.setId(dto.getAccountId());
            comment.setAccount(account);
        }

        return comment;
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findAll() {
        return commentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CommentDTO> findById(Integer id) {
        return commentRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<CommentDetailDTO> findByIdWithDetails(Integer id) {
        return commentRepository.findByIdWithDetails(id).map(this::toDetailDTO);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findByPostId(Integer postId) {
        return commentRepository.findByPostIdWithAccount(postId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findByAccountId(Integer accountId) {
        return commentRepository.findByAccountId(accountId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer save(CommentDTO commentDTO) {

        return commentRepository.createComment(
                commentDTO.getUserComment(),
                commentDTO.getPostId(),
                commentDTO.getAccountId()
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CommentDTO update(Integer id, CommentDTO commentDTO) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        Comment comment = toEntity(commentDTO);
        comment.setId(id);
        Comment updated = commentRepository.save(comment);
        return toDTO(updated);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(Integer id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }
}