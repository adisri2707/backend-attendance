package com.aditi.attendance.user.exception;

public class UnauthorizedEmailException extends RuntimeException {

    public UnauthorizedEmailException(String message) {
        super(message);
    }
}
