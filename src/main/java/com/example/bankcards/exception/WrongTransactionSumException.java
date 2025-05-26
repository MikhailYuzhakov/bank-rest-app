package com.example.bankcards.exception;

public class WrongTransactionSumException extends RuntimeException {
    public WrongTransactionSumException(String message) {
        super(message);
    }
}
