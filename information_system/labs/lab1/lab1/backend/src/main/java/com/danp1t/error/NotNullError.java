package com.danp1t.error;

public class NotNullError extends Exception {
    public String sendMessage(String field) {
        return "Значение поля " + field + "не может быть null";
    }
}
