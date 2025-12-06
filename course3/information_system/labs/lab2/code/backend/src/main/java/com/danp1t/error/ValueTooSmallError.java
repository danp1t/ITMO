package com.danp1t.error;

public class ValueTooSmallError extends RuntimeException {
    public ValueTooSmallError (String field, Integer minValue) {
        super("Значение поля " + field + " должно быть больше " + minValue);
    }
}
