package com.beaver.auth.exceptions;

public class JwtTokenMissingException extends Exception {
    public JwtTokenMissingException(String message) {
        super(message);
    }
}
