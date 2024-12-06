package com.xml.sqlbrigerai.exception;

public class SqlProcessingException extends RuntimeException {
    public SqlProcessingException(String message) {
        super(message);
    }

    public SqlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}