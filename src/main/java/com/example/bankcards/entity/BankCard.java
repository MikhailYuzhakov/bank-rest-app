package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "cardOwner") // Исключаем владельца из toString
public class BankCard {
    @Id
    private String maskedCardNumber;

    @Column(nullable = false, unique = true)
    private String encryptedCardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User cardOwner;

    private Date expirationDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private double balance;

    public BankCard(String maskedCardNumber, String encryptedCardNumber, User cardOwner, Date expirationDate, CardStatus status, double balance) {
        this.maskedCardNumber = maskedCardNumber;
        this.encryptedCardNumber = encryptedCardNumber;
        this.cardOwner = cardOwner;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }

    public BankCard() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankCard bankCard = (BankCard) o;
        return Objects.equals(maskedCardNumber, bankCard.maskedCardNumber) &&
                Objects.equals(encryptedCardNumber, bankCard.encryptedCardNumber);
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

    public User getCardOwner() {
        return cardOwner;
    }

    public void setCardOwner(User cardOwner) {
        this.cardOwner = cardOwner;
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

    @Override
    public int hashCode() {
        return Objects.hash(maskedCardNumber, encryptedCardNumber);
    }
}
