package com.danp1t.dto;

public class AuthResponseDTO {
    private Integer id;
    private String username;
    private String role;
    private String token;

    public AuthResponseDTO() {}

    public AuthResponseDTO(Integer id, String username, String role, String token) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}