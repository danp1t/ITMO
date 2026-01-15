package com.danp1t.error;

public class NotNullError extends RuntimeException {
    public NotNullError(String field) {
        super("Значение поля " + field + " не может быть null");
    }
}
