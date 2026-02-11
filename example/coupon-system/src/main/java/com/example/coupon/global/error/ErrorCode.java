package com.example.coupon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Coupon
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "쿠폰을 찾을 수 없습니다."),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "C002", "쿠폰이 모두 소진되었습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "C003", "이미 발급받은 쿠폰입니다."),
    COUPON_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "쿠폰 발급에 실패했습니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
