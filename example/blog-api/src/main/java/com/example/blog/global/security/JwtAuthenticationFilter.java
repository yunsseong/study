package com.example.blog.global.security;

import com.example.blog.global.error.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.blog.global.error.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * [설계 포인트] JWT 인증 필터 - 모든 HTTP 요청을 가로채서 토큰을 검사한다
 *
 * 동작 흐름:
 *   1. 클라이언트가 요청을 보낸다
 *      GET /api/v1/posts
 *      Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 *   2. 이 필터가 Authorization 헤더에서 토큰을 꺼낸다
 *
 *   3. 토큰이 유효하면 → SecurityContext에 인증 정보를 저장
 *      "이 요청은 memberId=3인 사용자가 보낸 거야"
 *
 *   4. 토큰이 없으면 → 그냥 통과 (비로그인 사용자도 목록 조회는 가능하니까)
 *      SecurityConfig에서 어떤 URL은 인증 필수, 어떤 URL은 선택인지 설정한다.
 *
 * OncePerRequestFilter를 상속하는 이유:
 *   - 한 요청에 필터가 여러 번 실행되는 것을 방지한다.
 *   - Spring Security 필터는 이걸 상속하는 것이 관례다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long memberId = jwtTokenProvider.getMemberId(token);

                // SecurityContext에 인증 정보 저장
                // 이후 Controller에서 SecurityContextHolder로 memberId를 꺼낼 수 있다
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        memberId,   // principal: 인증된 사용자 정보 (여기서는 memberId)
                        null,       // credentials: 비밀번호 (이미 인증됐으니 필요 없음)
                        List.of()   // authorities: 권한 목록 (ROLE_USER 등, 지금은 비움)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            // JWT 검증 실패 시 (만료, 위변조 등) JSON 에러 응답 반환
            sendErrorResponse(response, e);
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출한다.
     *
     * HTTP 표준에서 인증 토큰은 이 형식으로 보낸다:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *
     * "Bearer " 접두사를 제거하고 순수 토큰 문자열만 반환한다.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 필터 단계에서 발생한 에러를 JSON으로 응답한다.
     *
     * 왜 필요한가?
     *   - GlobalExceptionHandler는 Controller 이후 단계에서만 동작한다.
     *   - 필터는 Controller 이전에 실행되므로 GlobalExceptionHandler가 잡지 못한다.
     *   - 필터에서 발생한 에러는 직접 응답을 만들어야 한다.
     */
    private void sendErrorResponse(HttpServletResponse response, BusinessException e) throws IOException {
        response.setStatus(e.getErrorCode().getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(e.getErrorCode())));
    }
}
