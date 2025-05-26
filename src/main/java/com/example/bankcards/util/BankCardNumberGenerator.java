package com.example.bankcards.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class BankCardNumberGenerator {
    private final Random random = new Random();
    private final AtomicLong lastGenerated = new AtomicLong(0);

    public String generateCardNumber() {
        long cardNumber;
        do {
            // Генерируем число от 4000000000000000 до 4999999999999999
            cardNumber = 4000000000000000L + random.nextLong(1000000000000000L);
        } while (cardNumber == lastGenerated.get());

        lastGenerated.set(cardNumber);
        return String.format("%016d", cardNumber);
    }
}
