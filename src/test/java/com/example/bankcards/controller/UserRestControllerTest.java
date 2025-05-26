package com.example.bankcards.controller;

import com.example.bankcards.config.PasswordConfig;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.RoleService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordConfig passwordConfig;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RestUserController restUserController;

    @Test
    void getUsers_shouldReturnListOfUsers() throws Exception {
        // Arrange
        List<User> mockUsers = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setUsername("johndoe");
        user1.setPassword("encodedPass");
        Role role = new Role();
        role.setRoleName("USER");
        user1.setRole(role);
        mockUsers.add(user1);

        when(userService.getUsers()).thenReturn(mockUsers);

        // Act
        ResponseEntity<List<UserDTO>> response = restUserController.getUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        UserDTO dto = response.getBody().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("johndoe", dto.getUsername());
        assertEquals("encodedPass", dto.getPassword());
        assertEquals("USER", dto.getRoleName());
    }

    @Test
    void getUser_shouldReturnUserByUsername() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(2L);
        mockUser.setFirstName("Alice");
        mockUser.setLastName("Smith");
        mockUser.setUsername("alice");
        mockUser.setPassword("alicePass");
        Role role = new Role();
        role.setRoleName("ADMIN");
        mockUser.setRole(role);

        when(userService.getUser("alice")).thenReturn(mockUser);

        // Act
        ResponseEntity<UserDTO> response = restUserController.getUser("alice");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserDTO dto = response.getBody();
        assertEquals(2L, dto.getId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("alice", dto.getUsername());
        assertEquals("alicePass", dto.getPassword());
        assertEquals("ADMIN", dto.getRoleName());
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        Role role = new Role();
        role.setRoleName("USER");
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setId(3L);
        userDTO.setFirstName("Updated");
        userDTO.setLastName("User");
        userDTO.setUsername("updated");
        userDTO.setRoleName("USER");

        User existingUser = new User();
        existingUser.setId(3L);
        existingUser.setFirstName("Original");
        existingUser.setLastName("User");
        existingUser.setUsername("original");
        existingUser.setRole(role);

        User updatedUser = new User();
        updatedUser.setId(3L);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setUsername("updated");
        updatedUser.setRole(role);

        when(userService.getUser(3L)).thenReturn(existingUser);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDTO> response = restUserController.updateUser(userDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserDTO responseDto = response.getBody();
        assertEquals(3L, responseDto.getId());
        assertEquals("Updated", responseDto.getFirstName());
        assertEquals("User", responseDto.getLastName());
        assertEquals("updated", responseDto.getUsername());
    }

    @Test
    void createUser_shouldCreateAndReturnNewUser() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("New");
        userDTO.setLastName("User");
        userDTO.setUsername("newuser");
        userDTO.setPassword("password");
        userDTO.setRoleName("USER");

        Role userRole = new Role();
        userRole.setRoleName("USER");
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setUsername("newuser");
        newUser.setPassword("encodedPassword");
        newUser.setRole(userRole);

        when(passwordConfig.passwordEncoder()).thenReturn(new BCryptPasswordEncoder());
        when(roleService.getRoleByRoleName("USER")).thenReturn(userRole);
        when(userService.createUser(any(User.class))).thenReturn(newUser);

        // Act
        ResponseEntity<UserDTO> response = restUserController.createUser(userDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserDTO responseDto = response.getBody();
        assertEquals("New", responseDto.getFirstName());
        assertEquals("User", responseDto.getLastName());
        assertEquals("newuser", responseDto.getUsername());
        assertEquals("encodedPassword", responseDto.getPassword());
        assertEquals("USER", responseDto.getRoleName());
    }

    @Test
    void deleteUser_shouldDeleteUserAndReturnSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser("tobedeleted");

        // Act
        ResponseEntity<String> response = restUserController.deleteUser("tobedeleted");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User tobedeleted was deleted!", response.getBody());
        verify(userService, times(1)).deleteUser("tobedeleted");
    }

    @Test
    void getUsers_shouldHandleAccessDeniedException() throws Exception {
        // Arrange
        when(userService.getUsers()).thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            restUserController.getUsers();
        });
    }

    @Test
    void getUser_shouldHandleAccessDeniedException() throws Exception {
        // Arrange
        when(userService.getUser(anyString())).thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            restUserController.getUser("testuser");
        });
    }

    @Test
    void createUser_shouldHandleAccessDeniedException() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("New");
        userDTO.setLastName("User");
        userDTO.setMiddleName("User1");
        userDTO.setUsername("newuser");
        userDTO.setPassword("password");
        userDTO.setRoleName("USER");
        when(passwordConfig.passwordEncoder()).thenReturn(new BCryptPasswordEncoder());
        when(roleService.getRoleByRoleName(anyString())).thenThrow(new AccessDeniedException("Access denied"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            restUserController.createUser(userDTO);
        });
    }
}
