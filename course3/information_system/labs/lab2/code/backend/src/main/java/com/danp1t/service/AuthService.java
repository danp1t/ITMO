package com.danp1t.service;

import com.danp1t.model.User;
import com.danp1t.dto.AuthRequestDTO;
import com.danp1t.dto.AuthResponseDTO;
import com.danp1t.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    public AuthResponseDTO register(AuthRequestDTO request) {
        User existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.validate();

        User savedUser = userRepository.save(user);

        // Генерируем токен (в реальном приложении используйте JWT)
        String token = generateToken(savedUser.getId());

        return new AuthResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole(),
                token
        );
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        // Генерируем токен
        String token = generateToken(user.getId());

        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

    public User getUserFromToken(String token) {
        try {
            if (token == null || !token.startsWith("user_")) {
                return null;
            }

            String userIdStr = token.substring(5);
            Integer userId = Integer.parseInt(userIdStr);
            return userRepository.findById(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateToken(Integer userId) {
        return "user_" + userId + "_" + UUID.randomUUID().toString();
    }
}