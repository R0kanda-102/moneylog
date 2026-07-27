package com.example.moneylog.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.moneylog.domain.category.service.CategoryService;
import com.example.moneylog.domain.user.dto.LoginRequest;
import com.example.moneylog.domain.user.dto.SignupRequest;
import com.example.moneylog.domain.user.dto.SignupResponse;
import com.example.moneylog.domain.user.dto.TokenResponse;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import com.example.moneylog.global.security.JwtProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    CategoryService categoryService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthService authService;

    @Test
    void signup_success_savesUserAndSeedsCategories() {
        SignupRequest req = new SignupRequest("test@moneylog.com", "password1", "테스터");
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        SignupResponse response = authService.signup(req);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@moneylog.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        verify(categoryService).seedDefaultCategories(any(User.class));
    }

    @Test
    void signup_duplicateEmail_throwsCustomException() {
        SignupRequest req = new SignupRequest("test@moneylog.com", "password1", "테스터");
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
        verify(categoryService, never()).seedDefaultCategories(any());
    }

    @Test
    void login_success_returnsAccessToken() {
        User user = User.builder().email("test@moneylog.com").password("encoded-pw").nickname("테스터").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByEmail("test@moneylog.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "encoded-pw")).thenReturn(true);
        when(jwtProvider.createToken(1L, "test@moneylog.com")).thenReturn("token-abc");

        TokenResponse response = authService.login(new LoginRequest("test@moneylog.com", "password1"));

        assertThat(response.accessToken()).isEqualTo("token-abc");
        assertThat(response.nickname()).isEqualTo("테스터");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@moneylog.com", "password1")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        User user = User.builder().email("test@moneylog.com").password("encoded-pw").nickname("테스터").build();
        when(userRepository.findByEmail("test@moneylog.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong-pw"), eq("encoded-pw"))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@moneylog.com", "wrong-pw")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}
