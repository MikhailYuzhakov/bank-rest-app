package com.example.bankcards.service;

import com.example.bankcards.security.JwtHelper;
import com.example.bankcards.security.UserAuthentificationManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.TokenRequest;
import com.example.bankcards.dto.TokenResponse;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final UserAuthentificationManager authenticationManager;
    private final AuthUserDetailsService userDetailsService;
    private final JwtHelper jwtHelper;

    public TokenService(UserAuthentificationManager authenticationManager, AuthUserDetailsService userDetailsService, JwtHelper jwtHelper) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtHelper = jwtHelper;
    }

    public TokenResponse generateToken(TokenRequest tokenRequest) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(tokenRequest.getUsername(), tokenRequest.getPassword()));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(tokenRequest.getUsername());
        String token = jwtHelper.createToken(Collections.emptyMap(), userDetails.getUsername());
        return new TokenResponse(token);
    }
}
