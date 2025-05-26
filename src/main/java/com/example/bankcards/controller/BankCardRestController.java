package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardsDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cards")
public class BankCardRestController {
    @Autowired
    private BankCardService bankCardService;

    @GetMapping
    public ResponseEntity<Page<BankCardsDTO>> getAllCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Page<BankCard> bankCards = bankCardService.getAllCards(PageRequest.of(page, size), search, status);
        Page<BankCardsDTO> bankCardsDTO = new PageImpl<>(
                mapToDTO(bankCards.getContent()),
                bankCards.getPageable(),
                bankCards.getTotalElements()
        );
        return ResponseEntity.ok(bankCardsDTO);
    }

    @GetMapping("/create/{username}")
    public ResponseEntity<BankCardsDTO> createCard(@PathVariable("username") String username) throws Exception {
        BankCard bankCard = bankCardService.createBankCards(username);
        String fullName = bankCard.getCardOwner().getLastName() + " " + bankCard.getCardOwner().getFirstName()
                + " " + bankCard.getCardOwner().getMiddleName();
        return ResponseEntity.ok(new BankCardsDTO(bankCard.getMaskedCardNumber(),
                bankCard.getEncryptedCardNumber(),
                bankCard.getExpirationDate(),
                bankCard.getStatus(),
                bankCard.getBalance(),
                fullName));
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteCard(@RequestBody CardRequest deleteCardRequest) throws AccessDeniedException {
        bankCardService.deleteBankCard(deleteCardRequest.getEncryptedCardNumber());
        return ResponseEntity.ok("Card " + deleteCardRequest.getEncryptedCardNumber() + " deleted successfully");
    }

    @PostMapping("/block")
    public ResponseEntity<BankCardsDTO> blockCard(@RequestBody CardRequest cardRequest) throws AccessDeniedException {
        BankCard bankCard = bankCardService.blockCard(cardRequest.getEncryptedCardNumber());

        String fullName = bankCard.getCardOwner().getLastName() + " " + bankCard.getCardOwner().getFirstName()
                + " " + bankCard.getCardOwner().getMiddleName();

        return ResponseEntity.ok(new BankCardsDTO(bankCard.getMaskedCardNumber(),
                bankCard.getEncryptedCardNumber(),
                bankCard.getExpirationDate(),
                bankCard.getStatus(),
                bankCard.getBalance(),
                fullName));
    }

    @PostMapping("/activate")
    public ResponseEntity<BankCardsDTO> activateCard(@RequestBody CardRequest cardRequest) throws AccessDeniedException {
        BankCard bankCard = bankCardService.activateCard(cardRequest.getEncryptedCardNumber());

        String fullName = bankCard.getCardOwner().getLastName() + " " + bankCard.getCardOwner().getFirstName()
                + " " + bankCard.getCardOwner().getMiddleName();

        return ResponseEntity.ok(new BankCardsDTO(bankCard.getMaskedCardNumber(),
                bankCard.getEncryptedCardNumber(),
                bankCard.getExpirationDate(),
                bankCard.getStatus(),
                bankCard.getBalance(),
                fullName));
    }

    @PostMapping("/transaction")
    public ResponseEntity<BankCardsDTO> transaction(@RequestBody TransactionDTO transactionDTO) throws AccessDeniedException {
        BankCard bankCard = bankCardService.transaction(transactionDTO);

        String fullName = bankCard.getCardOwner().getLastName() + " " + bankCard.getCardOwner().getFirstName()
                + " " + bankCard.getCardOwner().getMiddleName();

        return ResponseEntity.ok(new BankCardsDTO(bankCard.getMaskedCardNumber(),
                bankCard.getEncryptedCardNumber(),
                bankCard.getExpirationDate(),
                bankCard.getStatus(),
                bankCard.getBalance(),
                fullName));
    }

    private List<BankCardsDTO> mapToDTO(List<BankCard> bankCards) {
        return bankCards.stream()
                .map(card -> {
                    String fullName = card.getCardOwner().getLastName() + " " +
                            card.getCardOwner().getFirstName() + " " +
                            card.getCardOwner().getMiddleName();
                    return new BankCardsDTO(
                            card.getMaskedCardNumber(),
                            card.getEncryptedCardNumber(),
                            card.getExpirationDate(),
                            card.getStatus(),
                            card.getBalance(),
                            fullName
                    );
                })
                .collect(Collectors.toList());
    }
}
