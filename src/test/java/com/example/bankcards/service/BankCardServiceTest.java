package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.BankCardNumberEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankCardNumberEncryptor encryptor;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Page<BankCard> page;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private BankCardService bankCardService;

    private User testUser;
    private BankCard testCard;
    private final String testUsername = "testUser";
    private final String encryptedCardNumber = "encrypted123";
    private final String maskedCardNumber = "**** **** **** 3456";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(testUsername);

        testCard = new BankCard();
        testCard.setCardOwner(testUser);
        testCard.setEncryptedCardNumber(encryptedCardNumber);
        testCard.setMaskedCardNumber(maskedCardNumber);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(1000.0);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllCards_AdminRole_ReturnsAllCards() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(bankCardRepository.findAll(pageable)).thenReturn(page);
        // Act
        Page<BankCard> result = bankCardService.getAllCards(pageable, null, null);

        // Assert
        assertEquals(page, result);
        verify(bankCardRepository).findAll(pageable);
    }

    @Test
    void getAllCards_UserRole_ReturnsUserCards() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(testUsername);
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(bankCardRepository.findByCardOwner(testUser, pageable)).thenReturn(page);

        // Act
        Page<BankCard> result = bankCardService.getAllCards(pageable, null, null);

        // Assert
        assertEquals(page, result);
        verify(bankCardRepository).findByCardOwner(testUser, pageable);
    }

    @Test
    void getAllCards_WithSearch_ValidatesSearchPattern() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                bankCardService.getAllCards(pageable, "123", null));
        assertThrows(IllegalArgumentException.class, () ->
                bankCardService.getAllCards(pageable, "12345", null));
        assertThrows(IllegalArgumentException.class, () ->
                bankCardService.getAllCards(pageable, "abcd", null));
    }

    @Test
    void createBankCards_AdminRole_CreatesCardSuccessfully() throws Exception {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        // Используем аргумент, соответствующий формату номера карты
        when(encryptor.maskCardNumber(argThat(arg -> arg.matches("\\d{16}")))).thenReturn(maskedCardNumber);
        when(encryptor.encryptCardNumber(argThat(arg -> arg.matches("\\d{16}")))).thenReturn(encryptedCardNumber);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // Act
        BankCard result = bankCardService.createBankCards(testUsername);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getCardOwner());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(1000.0, result.getBalance());
        verify(bankCardRepository).save(any(BankCard.class));
    }

    @Test
    void createBankCards_NonAdminRole_ThrowsAccessDenied() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                bankCardService.createBankCards(testUsername));
    }

    @Test
    void createBankCards_UserNotFound_ThrowsException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                bankCardService.createBankCards(testUsername));
    }

    @Test
    void deleteBankCard_AdminRole_DeletesCard() throws AccessDeniedException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(testCard));

        // Act
        bankCardService.deleteBankCard(encryptedCardNumber);

        // Assert
        verify(bankCardRepository).deleteByEncryptedCardNumber(encryptedCardNumber);
    }

    @Test
    void deleteBankCard_NonAdminRole_ThrowsAccessDenied() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                bankCardService.deleteBankCard(encryptedCardNumber));
    }

    @Test
    void deleteBankCard_CardNotFound_ThrowsException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () ->
                bankCardService.deleteBankCard(encryptedCardNumber));
    }

    @Test
    void blockCard_AdminRole_BlocksCard() throws AccessDeniedException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(testCard)).thenReturn(testCard);

        // Act
        BankCard result = bankCardService.blockCard(encryptedCardNumber);

        // Assert
        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(bankCardRepository).save(testCard);
    }

    @Test
    void blockCard_UserRole_RequestsBlock() throws AccessDeniedException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(testUsername);
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(testCard)).thenReturn(testCard);

        // Act
        BankCard result = bankCardService.blockCard(encryptedCardNumber);

        // Assert
        assertEquals(CardStatus.REQUESTED_FOR_BLOCK, result.getStatus());
        verify(bankCardRepository).save(testCard);
    }

    @Test
    void blockCard_UserNotOwner_ThrowsException() {
        // 1. Подготовка тестовых данных
        User cardOwner = new User();
        cardOwner.setUsername("cardOwner");
        cardOwner.setId(2L); // Другой ID, чтобы отличался от testUser

        BankCard foreignCard = new BankCard();
        foreignCard.setCardOwner(cardOwner);
        foreignCard.setStatus(CardStatus.ACTIVE); // Важно: карта должна быть активной

        // 2. Настройка моков
        // Пользователь, который пытается блокировать (не владелец)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(testUsername);

        // Возвращаем пользователя, который НЕ владеет картой
        when(userRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUser)); // testUser пытается блокировать

        // Возвращаем карту, принадлежащую другому пользователю
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber))
                .thenReturn(Optional.of(foreignCard));

        // 3. Проверка
        assertThrows(WrongTransactionUserException.class, () ->
                bankCardService.blockCard(encryptedCardNumber));

        // Дополнительная проверка, что карта не сохранялась
        verify(bankCardRepository, never()).save(any());
    }

    @Test
    void activateCard_AdminRole_ActivatesCard() throws AccessDeniedException {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();
        when(bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(testCard)).thenReturn(testCard);

        // Act
        BankCard result = bankCardService.activateCard(encryptedCardNumber);

        // Assert
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(bankCardRepository).save(testCard);
    }

    @Test
    void activateCard_NonAdminRole_ThrowsAccessDenied() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                bankCardService.activateCard(encryptedCardNumber));
    }

    @Test
    void transaction_ValidTransaction_ProcessesSuccessfully() {
        // Arrange
        User user = new User();
        user.setUsername("user");

        BankCard debitCard = new BankCard();
        debitCard.setCardOwner(user);
        debitCard.setEncryptedCardNumber("debit123");
        debitCard.setStatus(CardStatus.ACTIVE);
        debitCard.setBalance(500.0);

        BankCard creditCard = new BankCard();
        creditCard.setCardOwner(user);
        creditCard.setEncryptedCardNumber("credit123");
        creditCard.setStatus(CardStatus.ACTIVE);
        creditCard.setBalance(1000.0);

        TransactionDTO transaction = new TransactionDTO();
        transaction.setCreditCardNumber("credit123");
        transaction.setDebitCardNumber("debit123");
        transaction.setSum(300.0);

        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bankCardRepository.findByEncryptedCardNumber("debit123")).thenReturn(Optional.of(debitCard));
        when(bankCardRepository.findByEncryptedCardNumber("credit123")).thenReturn(Optional.of(creditCard));
        when(bankCardRepository.save(any(BankCard.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        BankCard result = bankCardService.transaction(transaction);

        // Assert
        assertEquals(700.0, result.getBalance()); // credit card balance after transaction
        assertEquals(800.0, debitCard.getBalance()); // debit card balance after transaction
        verify(bankCardRepository, times(2)).save(any(BankCard.class));
    }

    @Test
    void transaction_CardNotActive_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("user");

        BankCard debitCard = new BankCard();
        debitCard.setCardOwner(user);
        debitCard.setStatus(CardStatus.BLOCKED);

        BankCard creditCard = new BankCard();
        creditCard.setCardOwner(user);
        creditCard.setStatus(CardStatus.ACTIVE);

        TransactionDTO transaction = new TransactionDTO();
        transaction.setCreditCardNumber("credit123");
        transaction.setDebitCardNumber("debit123");
        transaction.setSum(100.0);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bankCardRepository.findByEncryptedCardNumber("debit123")).thenReturn(Optional.of(debitCard));
        when(bankCardRepository.findByEncryptedCardNumber("credit123")).thenReturn(Optional.of(creditCard));

        // Act & Assert
        assertThrows(CardNotActiveException.class, () ->
                bankCardService.transaction(transaction));
        verify(bankCardRepository, never()).save(any());
    }

    @Test
    void transaction_UserNotOwner_ThrowsException() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setId(1L);
        User user2 = new User();
        user2.setUsername("user2");
        user2.setId(2L);

        BankCard debitCard = new BankCard();
        debitCard.setCardOwner(user1);
        debitCard.setStatus(CardStatus.ACTIVE);

        BankCard creditCard = new BankCard();
        creditCard.setCardOwner(user2);
        creditCard.setStatus(CardStatus.ACTIVE);

        TransactionDTO transaction = new TransactionDTO();
        transaction.setCreditCardNumber("credit123");
        transaction.setDebitCardNumber("debit123");
        transaction.setSum(100.0);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(bankCardRepository.findByEncryptedCardNumber("debit123")).thenReturn(Optional.of(debitCard));
        when(bankCardRepository.findByEncryptedCardNumber("credit123")).thenReturn(Optional.of(creditCard));

        // Act & Assert
        assertThrows(WrongTransactionUserException.class, () ->
                bankCardService.transaction(transaction));
    }

    @Test
    void transaction_NegativeSum_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("user");

        BankCard debitCard = new BankCard();
        debitCard.setCardOwner(user);
        debitCard.setStatus(CardStatus.ACTIVE);

        BankCard creditCard = new BankCard();
        creditCard.setCardOwner(user);
        creditCard.setStatus(CardStatus.ACTIVE);

        TransactionDTO transaction = new TransactionDTO();
        transaction.setCreditCardNumber("credit123");
        transaction.setDebitCardNumber("debit123");
        transaction.setSum(-100.0);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bankCardRepository.findByEncryptedCardNumber("debit123")).thenReturn(Optional.of(debitCard));
        when(bankCardRepository.findByEncryptedCardNumber("credit123")).thenReturn(Optional.of(creditCard));

        // Act & Assert
        assertThrows(WrongTransactionSumException.class, () ->
                bankCardService.transaction(transaction));
    }

    @Test
    void transaction_InsufficientFunds_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("user");

        BankCard debitCard = new BankCard();
        debitCard.setCardOwner(user);
        debitCard.setStatus(CardStatus.ACTIVE);

        BankCard creditCard = new BankCard();
        creditCard.setCardOwner(user);
        creditCard.setStatus(CardStatus.ACTIVE);
        creditCard.setBalance(50.0);

        TransactionDTO transaction = new TransactionDTO();
        transaction.setCreditCardNumber("credit123");
        transaction.setDebitCardNumber("debit123");
        transaction.setSum(100.0);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bankCardRepository.findByEncryptedCardNumber("debit123")).thenReturn(Optional.of(debitCard));
        when(bankCardRepository.findByEncryptedCardNumber("credit123")).thenReturn(Optional.of(creditCard));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                bankCardService.transaction(transaction));
    }
}