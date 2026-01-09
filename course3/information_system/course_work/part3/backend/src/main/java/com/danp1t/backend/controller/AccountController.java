package com.danp1t.backend.controller;

import com.danp1t.backend.dto.AccountDTO;
import com.danp1t.backend.dto.AccountDetailDTO;
import com.danp1t.backend.dto.AccountStatusDTO;
import com.danp1t.backend.dto.RoleAssignmentDTO;
import com.danp1t.backend.model.Account;
import com.danp1t.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Integer id) {
        return accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<AccountDetailDTO> getAccountWithDetails(@PathVariable Integer id) {
        return accountService.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AccountDTO> getAccountByEmail(@PathVariable String email) {
        return accountService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountDTO accountDTO) {
        if (accountService.existsByEmail(accountDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        AccountDTO created = accountService.save(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Integer id, @RequestBody AccountDTO accountDTO) {
        try {
            AccountDTO updated = accountService.update(id, accountDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer id) {
        try {
            accountService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{accountId}/roles/{roleId}")
    public ResponseEntity<AccountDetailDTO> addRoleToAccountPath(
            @PathVariable Integer accountId,
            @PathVariable Integer roleId) {

        Account account = accountService.addRoleToAccount(accountId, roleId);
        return ResponseEntity.ok(convertToAccountDetailDTO(account));
    }

    @DeleteMapping("/{accountId}/roles/{roleId}")
    public ResponseEntity<AccountDetailDTO> removeRoleFromAccount(
            @PathVariable Integer accountId,
            @PathVariable Integer roleId) {

        Account account = accountService.removeRoleFromAccount(accountId, roleId);
        return ResponseEntity.ok(convertToAccountDetailDTO(account));
    }

    @PostMapping("/{accountId}/roles/by-name/{roleName}")
    public ResponseEntity<AccountDetailDTO> addRoleToAccountByName(
            @PathVariable Integer accountId,
            @PathVariable String roleName) {

        Account account = accountService.addRoleToAccountByName(accountId, roleName);
        return ResponseEntity.ok(convertToAccountDetailDTO(account));
    }

    @DeleteMapping("/{accountId}/roles/by-name/{roleName}")
    public ResponseEntity<AccountDetailDTO> removeRoleFromAccountByName(
            @PathVariable Integer accountId,
            @PathVariable String roleName) {

        Account account = accountService.removeRoleFromAccountByName(accountId, roleName);
        return ResponseEntity.ok(convertToAccountDetailDTO(account));
    }

    private AccountDetailDTO convertToAccountDetailDTO(Account account) {
        AccountDetailDTO dto = new AccountDetailDTO();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setEmail(account.getEmail());
        return dto;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> banUser(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");

        if (isActive == null) {
            return ResponseEntity.badRequest().build();
        }

        if (isActive) {
            accountService.unbanUser(id);
            return ResponseEntity.ok().build();
        }
        else {
            try {
                accountService.banUser(id);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                if (e.getMessage().contains("не найден")) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}