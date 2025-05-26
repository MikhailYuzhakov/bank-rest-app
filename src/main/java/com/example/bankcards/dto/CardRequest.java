package com.example.bankcards.dto;

import lombok.Data;

@Data
public class CardRequest {
    private String encryptedCardNumber;

    public CardRequest(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public CardRequest() {
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }
}
