package com.danp1t.backend.service;

import com.danp1t.backend.dto.AccountDTO;
import com.danp1t.backend.dto.AccountDetailDTO;
import com.danp1t.backend.dto.RoleDTO;
import com.danp1t.backend.dto.PostSimpleDTO;
import com.danp1t.backend.dto.CommentSimpleDTO;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    private AccountDTO toDTO(Account account) {
        return new AccountDTO(account.getId(), account.getName(), account.getEmail());
    }

    private AccountDetailDTO toDetailDTO(Account account) {
        List<RoleDTO> roles = account.getRoles().stream()
                .map(role -> new RoleDTO(role.getId(), role.getName(), role.getDescription()))
                .collect(Collectors.toList());

        List<PostSimpleDTO> posts = account.getPosts().stream()
                .map(post -> new PostSimpleDTO(post.getId(), post.getTitle(), post.getCreatedAt(), post.getCountLike()))
                .collect(Collectors.toList());

        List<CommentSimpleDTO> comments = account.getComments().stream()
                .map(comment -> new CommentSimpleDTO(comment.getId(), comment.getCreatedAt(), comment.getUserComment(), account.getName()))
                .collect(Collectors.toList());

        return new AccountDetailDTO(account.getId(), account.getName(), account.getEmail(), roles, posts, comments);
    }

    private Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setId(dto.getId());
        account.setName(dto.getName());
        account.setEmail(dto.getEmail());
        return account;
    }

    public List<AccountDTO> findAll() {
        return accountRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<AccountDTO> findById(Integer id) {
        return accountRepository.findById(id).map(this::toDTO);
    }

    public Optional<AccountDetailDTO> findByIdWithDetails(Integer id) {
        return accountRepository.findByIdWithDetails(id).map(this::toDetailDTO);
    }

    public AccountDTO save(AccountDTO accountDTO) {
        Account account = toEntity(accountDTO);
        Account saved = accountRepository.save(account);
        return toDTO(saved);
    }

    public AccountDTO update(Integer id, AccountDTO accountDTO) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }
        Account account = toEntity(accountDTO);
        account.setId(id);
        Account updated = accountRepository.save(account);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    public Optional<AccountDTO> findByEmail(String email) {
        return accountRepository.findByEmail(email).map(this::toDTO);
    }
}