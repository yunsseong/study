package com.example.blog.domain.auth.service;

import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignupRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.member.repository.MemberRepository;
import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import com.example.blog.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("signup: 회원가입")
    class Signup {

        @Test
        @DisplayName("성공: 회원가입 후 토큰을 반환한다")
        void success() {
            // given
            SignupRequest request = new SignupRequest("test@test.com", "password123", "테스터");

            given(memberRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("$2a$10$encoded");

            Member savedMember = Member.create("test@test.com", "$2a$10$encoded", "테스터");
            setId(savedMember, 1L);
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);
            given(jwtTokenProvider.createToken(1L)).willReturn("jwt-token");

            // when
            TokenResponse response = authService.signup(request);

            // then
            assertThat(response.accessToken()).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("실패: 이메일이 이미 존재하면 예외를 던진다")
        void failDuplicateEmail() {
            // given
            SignupRequest request = new SignupRequest("existing@test.com", "password123", "테스터");
            given(memberRepository.existsByEmail("existing@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
    }

    @Nested
    @DisplayName("login: 로그인")
    class Login {

        @Test
        @DisplayName("성공: 올바른 자격증명이면 토큰을 반환한다")
        void success() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password123");
            Member member = Member.create("test@test.com", "$2a$10$encoded", "테스터");
            setId(member, 1L);

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("password123", "$2a$10$encoded")).willReturn(true);
            given(jwtTokenProvider.createToken(1L)).willReturn("jwt-token");

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response.accessToken()).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일이면 예외를 던진다")
        void failEmailNotFound() {
            // given
            LoginRequest request = new LoginRequest("wrong@test.com", "password123");
            given(memberRepository.findByEmail("wrong@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        @Test
        @DisplayName("실패: 비밀번호가 틀리면 예외를 던진다")
        void failWrongPassword() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");
            Member member = Member.create("test@test.com", "$2a$10$encoded", "테스터");

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("wrongPassword", "$2a$10$encoded")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }
}
