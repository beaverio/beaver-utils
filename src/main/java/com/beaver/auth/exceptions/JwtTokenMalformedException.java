package com.beaver.auth.exceptions;

public class JwtTokenMalformedException extends Exception {
    public JwtTokenMalformedException(String message) {
        super(message);
    }
}
