package com.danp1t.error;

public class StringTooShortError extends RuntimeException {
    public StringTooShortError(String field, Integer maxLength) {
        super("Длина строки " + field + " должна быть больше " + maxLength);
    }
}