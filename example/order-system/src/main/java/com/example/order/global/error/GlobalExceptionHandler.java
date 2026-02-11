package com.example.order.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 낙관적 락 충돌 시 처리
     * JPA @Version에 의해 동시 수정 감지되면 이 예외 발생
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    protected ResponseEntity<ErrorResponse> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException e) {
        return ResponseEntity
                .status(ErrorCode.STOCK_UPDATE_CONFLICT.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.STOCK_UPDATE_CONFLICT));
    }
}
