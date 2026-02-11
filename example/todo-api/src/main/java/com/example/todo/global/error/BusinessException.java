package com.example.todo.global.error;

/**
 * [설계 포인트] 비즈니스 예외를 위한 커스텀 Exception
 *
 * 왜 RuntimeException을 상속하는가?
 *   - Checked Exception (Exception 상속)은 throws 선언을 강제한다.
 *   - Service 메서드마다 throws를 붙이면 코드가 지저분해진다.
 *   - Spring의 @Transactional은 기본적으로 RuntimeException만 롤백한다.
 *   - 실무에서 비즈니스 예외는 대부분 Unchecked(RuntimeException)로 만든다.
 *
 * 왜 ErrorCode를 품고 있는가?
 *   - throw new BusinessException(ErrorCode.TODO_NOT_FOUND) 한 줄이면
 *     HTTP 상태코드, 에러코드, 메시지가 모두 결정된다.
 *   - GlobalExceptionHandler에서 이 ErrorCode를 꺼내서 응답을 만든다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
