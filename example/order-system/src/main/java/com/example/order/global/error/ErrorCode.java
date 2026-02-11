package com.example.order.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "P002", "재고가 부족합니다."),
    STOCK_UPDATE_CONFLICT(HttpStatus.CONFLICT, "P003", "재고 업데이트 충돌이 발생했습니다. 다시 시도해주세요."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O002", "유효하지 않은 주문 상태 변경입니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "O003", "이미 결제된 주문입니다."),
    ORDER_CANCELLED(HttpStatus.BAD_REQUEST, "O004", "취소된 주문입니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PM001", "결제 정보를 찾을 수 없습니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "PM002", "중복 결제 요청입니다."),
    PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PM003", "결제 처리에 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PM004", "결제 금액이 주문 금액과 일치하지 않습니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
