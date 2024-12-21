package org.example.backend;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.ejb.Stateless;

import java.util.Date;

@Stateless
public class TokenUtil {
    private static final String SECRET_KEY = "your_super_secret_key_which_is_long_enough"; // Убедитесь, что ключ длинный
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 час

    public String generateToken(Users user) {
        if (user == null || user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new IllegalArgumentException("User or login cannot be null or empty");
        }

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(user.getLogin())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        DecodedJWT jwt = JWT.require(algorithm)
                .build()
                .verify(token);
        return jwt.getSubject(); // Возвращает логин пользователя
    }
}
