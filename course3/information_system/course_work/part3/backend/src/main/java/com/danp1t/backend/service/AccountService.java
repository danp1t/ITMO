package com.danp1t.backend.service;

import com.danp1t.backend.dto.*;
import com.danp1t.backend.exception.ResourceNotFoundException;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.model.Role;
import com.danp1t.backend.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    private AccountDTO toDTO(Account account) {
        return new AccountDTO(account.getId(), account.getName(), account.getEmail(), account.isEnabled());
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

        return new AccountDetailDTO(account.getId(), account.getName(), account.getEmail(), account.isEnabled(), roles, posts, comments);
    }

    private Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setId(dto.getId());
        account.setName(dto.getName());
        account.setEmail(dto.getEmail());
        return account;
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> findAll() {
        return accountRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AccountDTO> findById(Integer id) {
        return accountRepository.findById(id).map(this::toDTO);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AccountDTO save(AccountDTO accountDTO) {
        Account account = toEntity(accountDTO);
        Account saved = accountRepository.save(account);
        return toDTO(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AccountDTO update(Integer id, AccountDTO accountDTO) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (accountDTO.getName() != null) {
            existingAccount.setName(accountDTO.getName());
        }
        if (accountDTO.getEmail() != null) {
            existingAccount.setEmail(accountDTO.getEmail());
        }

        Account updated = accountRepository.save(existingAccount);
        return toDTO(updated);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(Integer id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<AccountDTO> findByEmail(String email) {
        return accountRepository.findByEmail(email).map(this::toDTO);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AccountDTO register(RegisterRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с такой почтой уже существует");
        }

        Account account = new Account();
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEnabled(false);

        String verificationCode = generateVerificationCode();
        account.setVerificationCode(verificationCode);
        account.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));

        Role userRole = roleService.findByName("ROLE_USER");
        if (userRole != null) {
            account.setRoles(List.of(userRole));
        }

        Account saved = accountRepository.save(account);
        return toDTO(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean verifyEmail(String email, String code) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (code.equals(account.getVerificationCode()) &&
                    account.getVerificationCodeExpiry().isAfter(LocalDateTime.now())) {
                account.setEnabled(true);
                account.setVerificationCode(null);
                account.setVerificationCodeExpiry(null);
                accountRepository.save(account);
                return true;
            }
        }
        return false;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void changePassword(String email, String oldPassword, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void initiatePasswordReset(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        account.setResetPasswordToken(resetToken);
        account.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean resetPassword(String token, String newPassword) {
        Optional<Account> accountOpt = accountRepository.findByResetPasswordToken(token);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getResetPasswordTokenExpiry().isAfter(LocalDateTime.now())) {
                account.setPassword(passwordEncoder.encode(newPassword));
                account.setResetPasswordToken(null);
                account.setResetPasswordTokenExpiry(null);
                accountRepository.save(account);
                return true;
            }
        }
        return false;
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + (int) (Math.random() * 900000));
    }

    @Transactional(readOnly = true)
    public String getVerificationCode(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getVerificationCode)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public String getResetToken(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getResetPasswordToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<AccountDetailDTO> findByIdWithDetails(Integer id) {
        Optional<Account> accountOpt = accountRepository.findByIdWithRoles(id);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            Optional<Account> accountWithPosts = accountRepository.findByIdWithPosts(id);
            accountWithPosts.ifPresent(value -> account.setPosts(value.getPosts()));

            Optional<Account> accountWithComments = accountRepository.findByIdWithComments(id);
            accountWithComments.ifPresent(value -> account.setComments(value.getComments()));

            return Optional.of(toDetailDTO(account));
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public RoleUserCountDTO countUsersByRole(Integer roleId) {
        Long count = accountRepository.countByRoleId(roleId);
        return new RoleUserCountDTO(count != null ? count : 0L);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getUsersByRole(Integer roleId) {
        List<Account> accounts = accountRepository.findByRoleId(roleId);
        return accounts.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account addRoleToAccount(Integer accountId, Integer roleId) {
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        boolean roleAlreadyExists = account.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roleId));

        if (!roleAlreadyExists) {
            account.getRoles().add(role);
            return accountRepository.save(account);
        }

        return account;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account removeRoleFromAccount(Integer accountId, Integer roleId) {
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        account.getRoles().removeIf(role -> role.getId().equals(roleId));

        return accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account addRoleToAccountByName(Integer accountId, String roleName) {
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        Role role = roleService.findByName(roleName);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with name: " + roleName);
        }

        boolean roleAlreadyExists = account.getRoles().stream()
                .anyMatch(r -> r.getName().equals(roleName));

        if (!roleAlreadyExists) {
            account.getRoles().add(role);
            return accountRepository.save(account);
        }

        return account;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Account removeRoleFromAccountByName(Integer accountId, String roleName) {
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        account.getRoles().removeIf(role -> role.getName().equals(roleName));

        return accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void banUser(Integer accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + accountId + " не найден");
        }
        accountRepository.banUser(accountId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        accountOpt.ifPresent(account -> {
            account.setEnabled(false);
            accountRepository.save(account);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void unbanUser(Integer accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + accountId + " не найден");
        }
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        accountOpt.ifPresent(account -> {
            account.setEnabled(true);
            accountRepository.save(account);
        });
    }
}