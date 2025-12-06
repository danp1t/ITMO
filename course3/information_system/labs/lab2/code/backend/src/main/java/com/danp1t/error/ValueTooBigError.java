package com.danp1t.error;

public class ValueTooBigError extends RuntimeException {
  public ValueTooBigError (String field, Long minValue) {
    super("Значение поля " + field + " должно быть меньше " + minValue);
  }
}