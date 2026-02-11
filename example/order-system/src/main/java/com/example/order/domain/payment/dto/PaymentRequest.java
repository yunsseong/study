package com.example.order.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 요청 DTO
 *
 * WHY idempotencyKey를 받는가?
 * 1. 멱등성 보장: 네트워크 재시도로 인한 중복 결제 방지
 * 2. 클라이언트 책임: 클라이언트가 고유한 키 생성 (UUID 등)
 * 3. 분산 환경: 여러 서버에서도 동일한 요청 식별 가능
 *
 * 면접 포인트:
 * - Idempotency Pattern: 동일한 요청을 여러 번 보내도 결과가 같음
 * - UUID vs Timestamp: UUID가 충돌 가능성이 낮아 더 안전
 * - 실무 사례: 결제 API, 주문 API 등 중요한 트랜잭션에 필수
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * WHY orderId를 받는가?
     * - 어떤 주문에 대한 결제인지 식별
     * - Order와 Payment의 관계 연결
     */
    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    /**
     * WHY @NotBlank를 사용하는가?
     * - idempotencyKey는 문자열이므로 빈 문자열도 거부해야 함
     * - null, "", "   " 모두 거부
     *
     * WHY 클라이언트에서 생성하는가?
     * - 네트워크 재시도 시나리오:
     *   1. 클라이언트가 결제 요청 전송
     *   2. 서버에서 처리 완료 후 응답 전송
     *   3. 네트워크 오류로 클라이언트가 응답 못 받음
     *   4. 클라이언트가 재시도 (동일한 idempotencyKey로)
     *   5. 서버는 이미 처리된 결제임을 인식하고 기존 결과 반환
     *
     * 면접 질문: "idempotencyKey를 서버에서 생성하면 안 되나요?"
     * 답변: 서버에서 생성하면 재시도 요청을 구분할 수 없음.
     *       클라이언트가 생성해야 동일한 요청임을 식별 가능.
     */
    @NotBlank(message = "멱등성 키는 필수입니다")
    private String idempotencyKey;
}
