package ru.practicum.common.exception;

public class NotEmptyException extends RuntimeException {
    public NotEmptyException(String message) {
        super(message);
    }
}
