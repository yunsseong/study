package com.example.blog.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * [설계 포인트] 도메인이 늘어나면 에러 코드도 늘어난다.
 *
 * 접두사로 도메인을 구분한다:
 *   C = Common (공통)
 *   A = Auth (인증)
 *   M = Member (회원)
 *   P = Post (게시글)
 *   CM = Comment (댓글)
 *
 * Todo 프로젝트와 비교:
 *   - Lombok @Getter, @RequiredArgsConstructor로 boilerplate 제거
 *   - 생성자, getter를 직접 안 써도 된다
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 공통 ---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다"),

    // --- 인증 ---
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "이메일 또는 비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 리프레시 토큰입니다"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "만료된 리프레시 토큰입니다"),

    // --- 회원 ---
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 사용 중인 이메일입니다"),

    // --- 게시글 ---
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),

    // --- 댓글 ---
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "댓글을 찾을 수 없습니다"),

    // --- 카테고리 ---
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "카테고리를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
