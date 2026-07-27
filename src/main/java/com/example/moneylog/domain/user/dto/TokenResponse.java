package com.example.moneylog.domain.user.dto;

public record TokenResponse(String accessToken, String tokenType, String nickname) {
    public static TokenResponse of(String accessToken, String nickname) {
        return new TokenResponse(accessToken, "Bearer", nickname);
    }
}