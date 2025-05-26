package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class TokenResponse {
    private String token;

    public TokenResponse(String token) {
        this.token = token;
    }

    public TokenResponse() {
    }
}
