package com.danp1t.backend.service;

import com.danp1t.backend.dto.*;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.model.Role;
import com.danp1t.backend.repository.AccountRepository;
import com.danp1t.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private RoleRepository roleRepository;

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

    public AccountDTO save(AccountDTO accountDTO) {
        Account account = toEntity(accountDTO);
        Account saved = accountRepository.save(account);
        return toDTO(saved);
    }

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

    public AccountDTO register(RegisterRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        Account account = new Account();
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEnabled(false);

        // Генерируем verification code
        String verificationCode = generateVerificationCode();
        account.setVerificationCode(verificationCode);
        account.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));

        // Назначаем роль по умолчанию
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null) {
            account.setRoles(List.of(userRole));
        }

        Account saved = accountRepository.save(account);
        return toDTO(saved);
    }

    // RG03 - верификация email
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

    // AU05 - смена пароля
    public void changePassword(String email, String oldPassword, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    // AU06 - инициация сброса пароля
    public void initiatePasswordReset(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        account.setResetPasswordToken(resetToken);
        account.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        accountRepository.save(account);
    }
    
    // AU06 - сброс пароля
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

    public String getVerificationCode(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getVerificationCode)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String getResetToken(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getResetPasswordToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void saveVerificationCode(Integer accountId, String verificationCode) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setVerificationCode(verificationCode);
        account.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        accountRepository.save(account);
    }

    public void savePasswordResetToken(String email, String resetToken) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        account.setResetPasswordToken(resetToken);
        account.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        accountRepository.save(account);
    }

    public Optional<AccountDetailDTO> findByIdWithDetails(Integer id) {
        // Загружаем аккаунт с ролями (основная информация)
        Optional<Account> accountOpt = accountRepository.findByIdWithRoles(id);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            // Отдельно загружаем посты
            Optional<Account> accountWithPosts = accountRepository.findByIdWithPosts(id);
            if (accountWithPosts.isPresent()) {
                account.setPosts(accountWithPosts.get().getPosts());
            }

            // Отдельно загружаем комментарии
            Optional<Account> accountWithComments = accountRepository.findByIdWithComments(id);
            if (accountWithComments.isPresent()) {
                account.setComments(accountWithComments.get().getComments());
            }

            return Optional.of(toDetailDTO(account));
        }

        return Optional.empty();
    }


}