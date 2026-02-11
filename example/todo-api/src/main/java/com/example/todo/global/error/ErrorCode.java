package com.example.todo.global.error;

import org.springframework.http.HttpStatus;

/**
 * [설계 포인트] ErrorCode를 enum으로 중앙 관리
 *
 * 실무에서 에러 코드가 여기저기 흩어져 있으면:
 * - "이 에러 코드 이미 쓰고 있나?" 확인이 어렵다
 * - 프론트엔드 개발자가 에러 코드 목록을 파악하기 어렵다
 * - 같은 에러인데 다른 메시지로 응답하는 경우가 생긴다
 *
 * enum으로 한 곳에 모아두면 이런 문제가 사라진다.
 * 도메인이 늘어나면 ErrorCode를 도메인별로 분리하거나, prefix로 구분한다.
 * 예: T001 = Todo 도메인, U001 = User 도메인
 */
public enum ErrorCode {

    // --- 공통 에러 ---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다"),

    // --- Todo 도메인 에러 ---
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "할 일을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
