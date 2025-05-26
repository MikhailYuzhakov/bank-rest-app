package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, String> {
    Page<BankCard> findByCardOwnerAndMaskedCardNumberAndStatus(User cardOwner, String maskedCardNumber, CardStatus status, Pageable pageable);

    Page<BankCard> findByCardOwnerAndMaskedCardNumber(User cardOwner, String maskedCardNumber, Pageable pageable);

    Page<BankCard> findByCardOwnerAndStatus(User cardOwner,CardStatus status, Pageable pageable);

    Page<BankCard> findByCardOwner(User cardOwner, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByEncryptedCardNumber(String encryptedCardNumber);

    @Modifying
    @Transactional
    void deleteByCardOwner(User cardOwner);

    Optional<BankCard> findByEncryptedCardNumber(String encryptedCardNumber);

    Page<BankCard> findAll(Pageable pageable);

    // Поиск по полному maskedCardNumber (точное совпадение)
    Page<BankCard> findByMaskedCardNumber(String maskedCardNumber, Pageable pageable);

    // Поиск по maskedCardNumber и статусу
    Page<BankCard> findByMaskedCardNumberAndStatus(String maskedCardNumber, CardStatus status, Pageable pageable);

    // Поиск только по статусу
    Page<BankCard> findByStatus(CardStatus status, Pageable pageable);
}
