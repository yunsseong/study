package com.example.todo.global.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [설계 포인트] 통일된 에러 응답 형식
 *
 * API 에러 응답이 제각각이면 프론트엔드에서 에러 처리가 지옥이 된다.
 *
 * 나쁜 예:
 *   - 어떤 API는 {"error": "not found"}
 *   - 어떤 API는 {"message": "실패", "code": 404}
 *   - 어떤 API는 Spring 기본 에러 페이지 (HTML)
 *
 * 모든 에러를 동일한 형식으로 내려주면 프론트엔드에서 하나의 에러 핸들러로 처리 가능하다.
 *
 * record를 사용한 이유:
 *   - 에러 응답은 불변 객체여야 한다 (생성 후 변경할 이유가 없다)
 *   - record는 getter, equals, hashCode, toString을 자동 생성해준다
 */
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors,
        LocalDateTime timestamp
) {

    /**
     * Validation 에러가 없는 일반 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of(),
                LocalDateTime.now()
        );
    }

    /**
     * @Valid 검증 실패 시, 어떤 필드가 왜 실패했는지 상세 정보 포함
     * 프론트엔드에서 필드별로 에러 메시지를 보여줄 수 있다.
     */
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                fieldErrors,
                LocalDateTime.now()
        );
    }

    /**
     * 필드 단위 검증 에러 정보
     * 예: { "field": "title", "value": "", "reason": "제목은 필수입니다" }
     */
    public record FieldError(
            String field,
            String value,
            String reason
    ) {
    }
}
