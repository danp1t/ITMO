package com.danp1t.error;

public class UserNotFoundException extends ImportException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("Пользователь с ID " + userId + " не найден");
    }
}