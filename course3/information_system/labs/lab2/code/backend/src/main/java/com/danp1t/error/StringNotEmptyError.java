package com.danp1t.error;

public class StringNotEmptyError extends RuntimeException {
  public StringNotEmptyError(String field) {
    super("Значение поля " + field + " не может быть пустой строкой");
  }
}
