package com.example.todo.global.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * [설계 포인트] 전역 예외 처리 핸들러
 *
 * @RestControllerAdvice: 모든 Controller에서 발생하는 예외를 여기서 잡는다.
 *
 * 왜 전역 핸들러가 필요한가?
 *   - Controller마다 try-catch를 넣으면 중복 코드 폭발
 *   - 에러 응답 형식이 Controller마다 달라질 위험
 *   - 예외 처리 로직이 비즈니스 로직과 섞여서 코드가 복잡해짐
 *
 * 실무에서의 핵심 원칙:
 *   1. 모든 예외는 여기서 잡아서 통일된 형식으로 응답한다
 *   2. 예상된 예외(BusinessException)와 예상치 못한 예외(Exception)를 분리한다
 *   3. 예상치 못한 예외는 반드시 로그를 남긴다 (운영 장애 추적용)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 예외 처리
     * 예: "할 일을 찾을 수 없습니다" (404)
     *
     * 이 예외는 "정상적인 흐름"이다. 클라이언트가 잘못된 요청을 한 것이므로
     * 에러 로그가 아닌 warn 레벨로 남긴다.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * @Valid 검증 실패 처리
     * 예: title이 비어있는 경우
     *
     * BindingResult에서 필드별 에러를 꺼내서 상세 응답을 만든다.
     * 프론트엔드에서 "제목을 입력해주세요" 같은 메시지를 필드 옆에 표시할 수 있다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors));
    }

    /**
     * 예상치 못한 예외 처리 (최후의 안전망)
     *
     * NullPointerException, DB 커넥션 에러 등 개발자가 예상하지 못한 에러.
     * 이런 에러는 반드시 error 레벨로 로그를 남겨야 한다.
     *
     * 주의: 클라이언트에게 서버 내부 에러 메시지를 그대로 노출하면 안 된다.
     * (보안 위험 + 사용자 경험 저하)
     * 일반적인 메시지만 내려주고, 상세 내용은 로그에만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
