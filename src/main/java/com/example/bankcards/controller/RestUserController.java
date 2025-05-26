package com.example.bankcards.controller;

import com.example.bankcards.config.PasswordConfig;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.RoleService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RestUserController {
    private final UserService userService;
    private final PasswordConfig passwordConfig;
    private final RoleService roleService;

    public RestUserController(UserService userService, PasswordConfig passwordConfig, RoleService roleService) {
        this.userService = userService;
        this.passwordConfig = passwordConfig;
        this.roleService = roleService;
    }

    @GetMapping()
    public ResponseEntity<List<UserDTO>> getUsers() throws AccessDeniedException {
        List<User> users = userService.getUsers();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            userDTOS.add(new UserDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMiddleName(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole().getRoleName())
            );
        }
        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) throws AccessDeniedException {
        User user = userService.getUser(username);

        return ResponseEntity.ok(new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().getRoleName()));
    }

    @PostMapping("/update")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO) throws AccessDeniedException {
        User currentUser = userService.getUser(userDTO.getId());

        currentUser.setFirstName(userDTO.getFirstName());
        currentUser.setLastName(userDTO.getLastName());
        currentUser.setMiddleName(userDTO.getMiddleName());
        currentUser.setUsername(userDTO.getUsername());
        currentUser = userService.updateUser(currentUser);

        return ResponseEntity.ok(new UserDTO(
                currentUser.getId(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getMiddleName(),
                currentUser.getUsername(),
                currentUser.getPassword(),
                currentUser.getRole().getRoleName()));
    }

    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO)  throws AccessDeniedException {
        User newUser = new User();
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setMiddleName(userDTO.getMiddleName());
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordConfig.passwordEncoder().encode(userDTO.getPassword()));
        newUser.setRole(roleService.getRoleByRoleName(userDTO.getRoleName()));

        newUser = userService.createUser(newUser);

        return ResponseEntity.ok(new UserDTO(
                newUser.getId(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getMiddleName(),
                newUser.getUsername(),
                newUser.getPassword(),
                newUser.getRole().getRoleName()));
    }

    @GetMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable("username") String username) throws AccessDeniedException {
        userService.deleteUser(username);
        return ResponseEntity.ok("User " + username + " was deleted!");
    }
}
