package com.example.bankcards.entity;

import lombok.Getter;

@Getter
public enum CardStatus {
    ACTIVE("Активна"),
    REQUESTED_FOR_BLOCK("Запрос на блокировку"),
    BLOCKED("Заблокирована"),
    EXPIRED("Истек срок");

    private final String displayName;

    CardStatus(String displayName) {
        this.displayName = displayName;
    }
}
