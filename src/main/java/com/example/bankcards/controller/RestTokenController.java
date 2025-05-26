package com.example.bankcards.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.bankcards.service.TokenService;
import com.example.bankcards.dto.TokenResponse;
import com.example.bankcards.dto.TokenRequest;

@RestController
@RequiredArgsConstructor
public class RestTokenController {
    private final TokenService tokenService;

    public RestTokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token/create")
    public TokenResponse createToken(@RequestBody TokenRequest tokenRequest) {
        return tokenService.generateToken(tokenRequest);
    }

}
