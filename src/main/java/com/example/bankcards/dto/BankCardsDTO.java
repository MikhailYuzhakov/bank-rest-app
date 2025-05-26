package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class BankCardsDTO {
    private String maskedCardNumber;
    private String encryptedCardNumber;
    private Date expirationDate;
    private CardStatus status;
    private double balance;
    private String cardOwnerUsername;

    public BankCardsDTO(String maskedCardNumber, String encryptedCardNumber, Date expirationDate, CardStatus status, double balance, String cardOwnerUsername) {
        this.maskedCardNumber = maskedCardNumber;
        this.encryptedCardNumber = encryptedCardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.cardOwnerUsername = cardOwnerUsername;
    }

    public BankCardsDTO() {
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCardOwnerUsername() {
        return cardOwnerUsername;
    }

    public void setCardOwnerUsername(String cardOwnerUsername) {
        this.cardOwnerUsername = cardOwnerUsername;
    }
}
