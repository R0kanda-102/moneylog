package com.example.moneylog.domain.user.dto;

import com.example.moneylog.domain.user.entity.User;

public record SignupResponse(Long userId, String email, String nickname) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}