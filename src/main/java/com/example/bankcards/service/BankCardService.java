package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.BankCardNumberEncryptor;
import com.example.bankcards.util.BankCardNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Slf4j
@Service
public class BankCardService {
    @Autowired
    private BankCardRepository bankCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BankCardNumberEncryptor encryptor;

    /**
     * Метод проверяет роль пользователя и в зависимости от нее возвращает список все карт в БД
     * или только список карт пользователя.
     * @return список банковских карт
     */
    public Page<BankCard> getAllCards(Pageable pageable, String search, CardStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isSearch = false;
        boolean isStatus = false;
        String searchPattern = "";

        // Проверяем есть ли параметры для поиска по номеру карты или статус
        if (search != null) {
            isSearch = true;
            // Убедимся, что это 4 цифры
            String normalizedSearch = search.replaceAll("[^0-9]", "");
            if (normalizedSearch.length() != 4) {
                throw new IllegalArgumentException("Search must be exactly 4 digits");
            }
            searchPattern = "**** **** **** " + normalizedSearch;
        }
        if (status != null) {
            isStatus = true;
        }

        //Проверяем роль пользователя
        if (auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            // При необходимости выполняем поиск и фильтрацию
            if (isSearch && isStatus) {
                return bankCardRepository.findByMaskedCardNumberAndStatus(searchPattern, status, pageable);
            } else if (isSearch) {
                return bankCardRepository.findByMaskedCardNumber(searchPattern, pageable);
            } else if (isStatus) {
                return bankCardRepository.findByStatus(status, pageable);
            }
            return bankCardRepository.findAll(pageable);


        } else {
            User user =  userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new UserNotFoundException("user not found"));

            // При необходимости выполняем поиск по номеру карты или возвращаем все карты пользователя
            if (isSearch && isStatus) {
                return bankCardRepository.findByCardOwnerAndMaskedCardNumberAndStatus(user, searchPattern, status, pageable);
            } else if (isSearch) {
                return bankCardRepository.findByCardOwnerAndMaskedCardNumber(user, searchPattern, pageable);
            } else if (isStatus) {
                return bankCardRepository.findByCardOwnerAndStatus(user, status, pageable);
            }
            return bankCardRepository.findByCardOwner(user, pageable);
        }
    }

    /**
     * Метод создаем новую банковскую карту для указанного пользователя. При этом номер карты случайно генерируется и шифруется.
     * @param username имя пользователя карты.
     * @return объект банковской карты.
     * @throws Exception исключения.
     */
    public BankCard createBankCards(String username) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            BankCard bankCard = new BankCard();
            BankCardNumberGenerator generator = new BankCardNumberGenerator();
            String openCardNumber = generator.generateCardNumber();

            User cardOwner = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found!"));
            bankCard.setCardOwner(cardOwner);
            bankCard.setStatus(CardStatus.ACTIVE);
            bankCard.setBalance(0.0);
            bankCard.setMaskedCardNumber(encryptor.maskCardNumber(openCardNumber));
            bankCard.setEncryptedCardNumber(encryptor.encryptCardNumber(openCardNumber));
            bankCard.setExpirationDate(generateExpirationDate().getTime());

            return bankCardRepository.save(bankCard);
        } else {
            throw new AccessDeniedException("Only ADMIN role can create card");
        }
    }

    /**
     * Удаляет банковскую карту при условии, что пользователь имеет роль Admin и карта существует.
     */
    @Transactional
    public void deleteBankCard(String encryptedCardNumber) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {

            bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)
                    .orElseThrow(() -> new CardNotFoundException("Card no found"));
            bankCardRepository.deleteByEncryptedCardNumber(encryptedCardNumber);

        }  else {
            throw new AccessDeniedException("Only ADMIN role can delete card");
        }
    }

    /**
     * Метод блокирует карту при условиях, что карта существует и роль ADMIN.
     * Метод создает запрос на блокировку, если вызван от имени пользователя карты.
     */
    @Transactional
    public BankCard blockCard(String encryptedCardNumber) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            BankCard bankCard = bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)
                    .orElseThrow(() -> new CardNotFoundException("Card " + encryptedCardNumber + " not found"));

            bankCard.setStatus(CardStatus.BLOCKED);
            return bankCardRepository.save(bankCard);
        } else {
            return requestUserBlockCard(encryptedCardNumber, authentication);
        }
    }

    /**
     * Метод активирует карту при условиях, что карту существует и роль ADMIN.
     */
    public BankCard activateCard(String encryptedCardNumber) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            BankCard bankCard = bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)
                    .orElseThrow(() -> new CardNotFoundException("Card " + encryptedCardNumber + " not found"));

            bankCard.setStatus(CardStatus.ACTIVE);
            return bankCardRepository.save(bankCard);
        } else {
            throw new AccessDeniedException("Only ADMIN role can delete card");
        }
    }

    /**
     * Метод выполняет транзакцию между картами и необходимые проверки.
     * @param transaction объект транзакции
     * @return карту, с которой списывались средства
     */
    @Transactional
    public BankCard transaction(TransactionDTO transaction) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Ищем пользователя, который пытается выполнить перевод
        User user =  userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        //Карта на которую кладут средства
        BankCard debitCard = bankCardRepository.findByEncryptedCardNumber(transaction.getDebitCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Debit card not found"));

        //Карта с которой снимают средства
        BankCard creditCard = bankCardRepository.findByEncryptedCardNumber(transaction.getCreditCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Credit card not found"));

        //Проверяем, что обе карты активны
        if ( !(creditCard.getStatus().equals(CardStatus.ACTIVE) && debitCard.getStatus().equals(CardStatus.ACTIVE)) ) {
            throw new CardNotActiveException("Debit and credit cart must have ACTIVE status");
        }

        //Проверяем является ли пользователь владельцем карты для списания и пополнения
        if ( !(user.equals(debitCard.getCardOwner()) && user.equals(creditCard.getCardOwner())) ) {
            throw new WrongTransactionUserException("User must be credit and debit cards owner");
        }

        // Проверка на положительную сумму перевода
        if (transaction.getSum() <= 0) {
            throw new WrongTransactionSumException("Transaction sum must be positive value");
        }

        // Проверка отрицательного баланса
        if (creditCard.getBalance() - transaction.getSum() < 0) {
            throw new InsufficientFundsException("Insufficient funds for credit card");
        }

        //Снимаем средства с карты
        creditCard.setBalance(creditCard.getBalance() - transaction.getSum());

        //Перечисляем средства на карту
        debitCard.setBalance(debitCard.getBalance() + transaction.getSum());

        bankCardRepository.save(debitCard);
        bankCardRepository.save(creditCard);

        return creditCard;
    }

    /**
     * Метод устанавливает статус карты Запрос на блокировку от имени пользователя карты
     * @param encryptedCardNumber шифрованный номер карты
     * @return карту с запросом на блокировку
     */
    private BankCard requestUserBlockCard(String encryptedCardNumber, Authentication authentication) {
        //Ищем пользователя, который пытается выполнить перевод
        User user =  userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        //Карта на которую кладут средства
        BankCard bankCard = bankCardRepository.findByEncryptedCardNumber(encryptedCardNumber)
                .orElseThrow(() -> new CardNotFoundException("Debit card not found"));

        if ( !user.equals(bankCard.getCardOwner()) ) {
            throw new WrongTransactionUserException("User must be card owner");
        }

        bankCard.setStatus(CardStatus.REQUESTED_FOR_BLOCK);
        return bankCardRepository.save(bankCard);
    }

    private Calendar generateExpirationDate() {
        // Получаем текущую дату
        Calendar currentDate = Calendar.getInstance();

        // Получаем текущие значения
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        int year = currentDate.get(Calendar.YEAR);

        // Создаем новый объект Calendar с прибавленным годом

        return new GregorianCalendar(year + 1, month, day);
    }
}
