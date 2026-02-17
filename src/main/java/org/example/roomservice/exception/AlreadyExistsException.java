package org.example.roomservice.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) { super(message); }
}
