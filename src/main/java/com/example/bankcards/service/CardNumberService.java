package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CardNumberService {

//    @Value("${encryption.key}")
    private String encryptionKey;

    public String encryptCardNumber(String rawCardNumber) {
        // Реализация шифрования (например, AES)
        // ...
        return rawCardNumber;
    }

    public String decryptCardNumber(String encryptedCardNumber) {
        // Реализация дешифрования
        // ...
        return encryptedCardNumber;
    }

    public String maskCardNumber(String rawCardNumber) {
        return "**** **** **** " + rawCardNumber.substring(rawCardNumber.length() - 4);
    }
}
