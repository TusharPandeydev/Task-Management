package com.campusconnect.campus.Exception;

public class AlreadyVerifiedUserException extends RuntimeException {
    public AlreadyVerifiedUserException(String message) {
        super(message);
    }
}
