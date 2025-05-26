package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    // Тесты для метода getUsers
    @Test
    void getUsers_AdminRole_ReturnsAllUsers() throws AccessDeniedException {
        // Подготовка данных
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();

        // Выполнение
        List<User> result = userService.getUsers();

        // Проверка
        assertEquals(users, result);
        verify(userRepository).findAll();
    }

    @Test
    void getUsers_NoAdminRole_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();


        assertThrows(AccessDeniedException.class, () -> userService.getUsers());
    }

    // Тесты для метода getUser (по username)
    @Test
    void getUser_AdminRole_ReturnsUser() throws AccessDeniedException {
        User user = new User();
        user.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();


        User result = userService.getUser("testUser");

        assertEquals(user, result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void getUser_AdminRole_UserNotFound_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();

        when(userRepository.findByUsername("notFound")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser("notFound"));
    }

    @Test
    void getUser_NoAdminRole_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();


        assertThrows(AccessDeniedException.class, () -> userService.getUser("anyUser"));
    }

    // Тесты для метода getUser (по id)
    @Test
    void getUserById_AdminRole_ReturnsUser() throws AccessDeniedException {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();


        User result = userService.getUser(1L);

        assertEquals(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_AdminRole_UserNotFound_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(authentication).getAuthorities();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(1L));
    }

    @Test
    void getUserById_NoAdminRole_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(authentication).getAuthorities();

        assertThrows(AccessDeniedException.class, () -> userService.getUser(1L));
    }
}
