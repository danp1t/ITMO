package com.danp1t.model;

import jakarta.persistence.*;
import com.danp1t.error.*;
import com.danp1t.interfaces.NeedValidate;

@Entity
@Table(name = "users")
public class User implements NeedValidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER"; // USER или ADMIN

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    public User() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    public User(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void validate() {
        if (this.username == null || this.username.trim().isEmpty()) {
            throw new NotNullError("username");
        } else if (this.username.length() > 50) {
            throw new StringTooLongError("username", 50);
        }

        if (this.password == null || this.password.trim().isEmpty()) {
            throw new NotNullError("password");
        } else if (this.password.length() < 6) {
            throw new StringTooShortError("password", 6);
        } else if (this.password.length() > 100) {
            throw new StringTooLongError("password", 100);
        }

        if (this.role == null) {
            throw new NotNullError("role");
        }
    }
}