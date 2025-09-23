package com.danp1t.error;

public class StringTooLongError extends RuntimeException {
    public StringTooLongError(String field, Integer maxLength) {
        super("Длина строки " + field + " не должна быть больше " + maxLength);
    }
}
