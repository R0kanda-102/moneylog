package com.example.moneylog.domain.user.controller;

import com.example.moneylog.domain.user.dto.LoginRequest;
import com.example.moneylog.domain.user.dto.SignupRequest;
import com.example.moneylog.domain.user.dto.SignupResponse;
import com.example.moneylog.domain.user.dto.TokenResponse;
import com.example.moneylog.domain.user.service.AuthService;
import com.example.moneylog.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호/닉네임으로 신규 계정을 생성하고 기본 카테고리를 자동 시드합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (VALIDATION_ERROR)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일 (DUPLICATE_EMAIL)")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse data = authService.signup(req);
        return ApiResponse.success("회원가입에 성공했습니다.", data);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 JWT accessToken을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치 (INVALID_CREDENTIALS)")
    })
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse data = authService.login(req);
        return ApiResponse.success("로그인에 성공했습니다.", data);
    }
}