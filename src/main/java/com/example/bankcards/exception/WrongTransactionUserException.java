package com.example.bankcards.exception;

public class WrongTransactionUserException extends RuntimeException {
    public WrongTransactionUserException(String message) {
        super(message);
    }
}
