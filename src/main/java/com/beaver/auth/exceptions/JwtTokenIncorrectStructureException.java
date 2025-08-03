package com.beaver.auth.exceptions;

public class JwtTokenIncorrectStructureException extends Exception {
    public JwtTokenIncorrectStructureException(String message) {
        super(message);
    }
}
