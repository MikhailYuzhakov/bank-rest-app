package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BankCardRepository bankCardRepository;

    public UserService(UserRepository userRepository, BankCardRepository bankCardRepository) {
        this.userRepository = userRepository;
        this.bankCardRepository = bankCardRepository;
    }

    public List<User> getUsers() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {

            return userRepository.findAll();
        } else {
            throw new AccessDeniedException("Only ADMIN role can get all users");
        }
    }

    public User getUser(String username) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {

            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found!"));
        } else {
            throw new AccessDeniedException("Only ADMIN role can get user");
        }
    }

    public User getUser(Long id) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {

            return userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found!"));
        } else {
            throw new AccessDeniedException("Only ADMIN role can get user");
        }
    }

    @Transactional
    public User updateUser(User newUser) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            User currentUser = userRepository.findByUsername(newUser.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            newUser.setId(currentUser.getId());
            return userRepository.save(newUser);
        }  else {
            throw new AccessDeniedException("Only ADMIN role can update user");
        }
    }

    @Transactional
    public User createUser(User newUser) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {
            return userRepository.save(newUser);
        }  else {
            throw new AccessDeniedException("Only ADMIN role can create user");
        }
    }

    @Transactional
    public void deleteUser(String username) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"))) {

            User deletedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            bankCardRepository.deleteByCardOwner(deletedUser);
            userRepository.delete(deletedUser);
        }  else {
            throw new AccessDeniedException("Only ADMIN role can delete user");
        }
    }
}
