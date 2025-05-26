package com.example.bankcards.util;

import com.example.bankcards.config.EncryptConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class BankCardNumberEncryptor {
    private final EncryptConfig encryptConfig;
    private final Cipher cipher;
    private final IvParameterSpec ivParameterSpec;

    public BankCardNumberEncryptor(EncryptConfig encryptConfig) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.encryptConfig = encryptConfig;
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Генерация IV (вектора инициализации)
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        this.ivParameterSpec = new IvParameterSpec(iv);

        // Проверка, что secretKey не null
        if (encryptConfig.getSecretKey() == null) {
            throw new IllegalStateException("Secret key is not configured. Please set encryptor.secret-key in application.properties");
        }
    }

    public String encryptCardNumber(String cardNumber) throws Exception {
        SecretKey originalKey = new SecretKeySpec(Base64.getDecoder().decode(encryptConfig.getSecretKey()), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, originalKey, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
        // Сохраняем IV вместе с зашифрованными данными (первые 16 байт)
        byte[] combined = new byte[ivParameterSpec.getIV().length + encryptedBytes.length];
        System.arraycopy(ivParameterSpec.getIV(), 0, combined, 0, ivParameterSpec.getIV().length);
        System.arraycopy(encryptedBytes, 0, combined, ivParameterSpec.getIV().length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptCardNumber(String encryptedCardNumber) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedCardNumber);

        // Извлекаем IV (первые 16 байт)
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Извлекаем зашифрованные данные (остальные байты)
        byte[] encryptedBytes = new byte[combined.length - iv.length];
        System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

        SecretKey originalKey = new SecretKeySpec(Base64.getDecoder().decode(encryptConfig.getSecretKey()), "AES");
        cipher.init(Cipher.DECRYPT_MODE, originalKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }
        return "**** **** **** " + cardNumber.substring(12);
    }
}
