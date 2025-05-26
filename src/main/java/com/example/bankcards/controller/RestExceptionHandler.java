package com.example.bankcards.controller;

import com.example.bankcards.dto.ErrorResponse;
import com.example.bankcards.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("AUTH_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("AUTH_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardsNotFoundException(CardNotFoundException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("ACCESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("ACCESS_DENIED", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
//        log.error("Error occurred: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFoundException(RoleNotFoundException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("ACCESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("SEARCH_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("TRANSACTION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(WrongTransactionSumException.class)
    public ResponseEntity<ErrorResponse> handleWrongTransactionSumException(WrongTransactionSumException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("TRANSACTION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(WrongTransactionUserException.class)
    public ResponseEntity<ErrorResponse> handleWrongTransactionUserException(WrongTransactionUserException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("TRANSACTION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(CardNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleCardNotActiveException(CardNotActiveException ex) {
//        log.error("Произошла ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("TRANSACTION_ERROR", ex.getMessage()));
    }

}
