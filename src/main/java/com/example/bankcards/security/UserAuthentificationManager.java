package com.example.bankcards.security;

import com.example.bankcards.config.PasswordConfig;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.service.AuthUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserAuthentificationManager implements AuthenticationManager {
    private final AuthUserDetailsService userDetailsService;
    private final PasswordConfig passwordConfig;

    public UserAuthentificationManager(AuthUserDetailsService userDetailsService, PasswordConfig passwordConfig) {
        this.userDetailsService = userDetailsService;
        this.passwordConfig = passwordConfig;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String storedHash = userDetails.getPassword();

        if (!passwordConfig.passwordEncoder().matches(rawPassword, storedHash)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
