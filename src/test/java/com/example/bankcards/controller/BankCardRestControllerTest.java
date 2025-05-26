package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardsDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardRestControllerTest {

    @Mock
    private BankCardService bankCardService;

    @InjectMocks
    private BankCardRestController bankCardRestController;

    @Test
    void getAllCards_shouldReturnPageOfCards() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setFirstName("user");
        user.setLastName("user");
        user.setMiddleName("user");
        // Arrange
        BankCard mockCard = new BankCard();
        mockCard.setMaskedCardNumber("1234****5678");
        mockCard.setEncryptedCardNumber("encrypted123");
        mockCard.setStatus(CardStatus.ACTIVE);
        mockCard.setCardOwner(user);
        mockCard.setBalance(1000.0);

        Page<BankCard> mockPage = new PageImpl<>(Collections.singletonList(mockCard));
        when(bankCardService.getAllCards(any(PageRequest.class), anyString(), any())).thenReturn(mockPage);

        // Act
        ResponseEntity<Page<BankCardsDTO>> response = bankCardRestController.getAllCards(
                CardStatus.ACTIVE, 0, 10, "search");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        BankCardsDTO dto = response.getBody().getContent().get(0);
        assertEquals("1234****5678", dto.getMaskedCardNumber());
        assertEquals("encrypted123", dto.getEncryptedCardNumber());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        assertEquals(1000.0, dto.getBalance());
    }

    @Test
    void createCard_shouldReturnCreatedCard() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setFirstName("user");
        user.setLastName("user");
        user.setMiddleName("user");
        // Arrange
        BankCard mockCard = new BankCard();
        mockCard.setMaskedCardNumber("1234****5678");
        mockCard.setEncryptedCardNumber("encrypted123");
        mockCard.setStatus(CardStatus.ACTIVE);
        mockCard.setBalance(0.0);
        mockCard.setCardOwner(user);

        when(bankCardService.createBankCards(anyString())).thenReturn(mockCard);

        // Act
        ResponseEntity<BankCardsDTO> response = bankCardRestController.createCard("username");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BankCardsDTO dto = response.getBody();
        assertEquals("1234****5678", dto.getMaskedCardNumber());
        assertEquals("encrypted123", dto.getEncryptedCardNumber());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        assertEquals(0.0, dto.getBalance());
        assertEquals("user user user", dto.getCardOwnerUsername());
    }

    @Test
    void deleteCard_shouldReturnSuccessMessage() throws AccessDeniedException, java.nio.file.AccessDeniedException {
        // Arrange
        CardRequest request = new CardRequest();
        request.setEncryptedCardNumber("encrypted123");

        doNothing().when(bankCardService).deleteBankCard(anyString());

        // Act
        ResponseEntity<String> response = bankCardRestController.deleteCard(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Card encrypted123 deleted successfully", response.getBody());
    }

    @Test
    void blockCard_shouldReturnBlockedCard() throws AccessDeniedException, java.nio.file.AccessDeniedException {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setFirstName("user");
        user.setLastName("user");
        user.setMiddleName("user");
        // Arrange
        CardRequest request = new CardRequest();
        request.setEncryptedCardNumber("encrypted123");

        BankCard mockCard = new BankCard();
        mockCard.setMaskedCardNumber("1234****5678");
        mockCard.setEncryptedCardNumber("encrypted123");
        mockCard.setStatus(CardStatus.BLOCKED);
        mockCard.setBalance(500.0);
        mockCard.setCardOwner(user);

        when(bankCardService.blockCard(anyString())).thenReturn(mockCard);

        // Act
        ResponseEntity<BankCardsDTO> response = bankCardRestController.blockCard(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BankCardsDTO dto = response.getBody();
        assertEquals("1234****5678", dto.getMaskedCardNumber());
        assertEquals("encrypted123", dto.getEncryptedCardNumber());
        assertEquals(CardStatus.BLOCKED, dto.getStatus());
        assertEquals(500.0, dto.getBalance());
        assertEquals("user user user", dto.getCardOwnerUsername());
    }

    @Test
    void activateCard_shouldReturnActivatedCard() throws AccessDeniedException, java.nio.file.AccessDeniedException {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setFirstName("user");
        user.setLastName("user");
        user.setMiddleName("user");
        // Arrange
        CardRequest request = new CardRequest();
        request.setEncryptedCardNumber("encrypted123");

        BankCard mockCard = new BankCard();
        mockCard.setMaskedCardNumber("1234****5678");
        mockCard.setEncryptedCardNumber("encrypted123");
        mockCard.setStatus(CardStatus.ACTIVE);
        mockCard.setBalance(1000.0);
        mockCard.setCardOwner(user);

        when(bankCardService.activateCard(anyString())).thenReturn(mockCard);

        // Act
        ResponseEntity<BankCardsDTO> response = bankCardRestController.activateCard(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BankCardsDTO dto = response.getBody();
        assertEquals("1234****5678", dto.getMaskedCardNumber());
        assertEquals("encrypted123", dto.getEncryptedCardNumber());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        assertEquals(1000.0, dto.getBalance());
        assertEquals("user user user", dto.getCardOwnerUsername());
    }

    @Test
    void transaction_shouldReturnUpdatedCard() throws AccessDeniedException, java.nio.file.AccessDeniedException {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setFirstName("user");
        user.setLastName("user");
        user.setMiddleName("user");
        // Arrange
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setDebitCardNumber("card1");
        transactionDTO.setCreditCardNumber("card2");
        transactionDTO.setSum(100.0);

        BankCard mockCard = new BankCard();
        mockCard.setMaskedCardNumber("1234****5678");
        mockCard.setEncryptedCardNumber("encrypted123");
        mockCard.setStatus(CardStatus.ACTIVE);
        mockCard.setBalance(900.0);
        mockCard.setCardOwner(user);

        when(bankCardService.transaction(any(TransactionDTO.class))).thenReturn(mockCard);

        // Act
        ResponseEntity<BankCardsDTO> response = bankCardRestController.transaction(transactionDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BankCardsDTO dto = response.getBody();
        assertEquals("1234****5678", dto.getMaskedCardNumber());
        assertEquals("encrypted123", dto.getEncryptedCardNumber());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        assertEquals(900.0, dto.getBalance());
        assertEquals("user user user", dto.getCardOwnerUsername());
    }

    @Test
    void createCard_shouldHandleException() throws Exception {
        // Arrange
        when(bankCardService.createBankCards(anyString())).thenThrow(new RuntimeException("Error creating card"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bankCardRestController.createCard("username");
        });
    }

    @Test
    void deleteCard_shouldHandleAccessDenied() throws AccessDeniedException, java.nio.file.AccessDeniedException {
        // Arrange
        CardRequest request = new CardRequest();
        request.setEncryptedCardNumber("encrypted123");

        doThrow(new AccessDeniedException("Access denied")).when(bankCardService).deleteBankCard(anyString());

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            bankCardRestController.deleteCard(request);
        });
    }
}
