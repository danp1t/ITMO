package com.danp1t.error;

public class InvalidXmlException extends ImportException {
    public InvalidXmlException(String message) {
        super(message);
    }

    public InvalidXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}