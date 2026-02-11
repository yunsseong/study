package com.example.order.domain.payment.dto;

import com.example.order.domain.payment.entity.Payment;
import com.example.order.domain.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 *
 * WHY idempotencyKey를 응답에 포함하는가?
 * 1. 클라이언트 확인: 요청한 idempotencyKey와 일치하는지 확인 가능
 * 2. 디버깅: 어떤 요청에 대한 응답인지 추적 가능
 * 3. 멱등성 검증: 재시도 요청의 응답이 기존 결과와 동일함을 확인
 *
 * 면접 포인트:
 * - 멱등성 응답 처리: 동일한 idempotencyKey의 요청은 동일한 응답 반환
 */
@Getter
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Integer amount;
    private PaymentStatus status;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    /**
     * static factory method 패턴
     *
     * WHY amount를 응답에 포함하는가?
     * - 클라이언트가 결제 금액을 확인할 수 있음
     * - 주문 금액과 결제 금액이 일치하는지 검증 가능
     * - 영수증 등에 활용
     *
     * WHY status를 응답에 포함하는가?
     * - 결제 상태 확인 (COMPLETED, CANCELLED 등)
     * - 결제 실패 시 상태 정보로 후속 처리 결정
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getCreatedAt()
        );
    }
}
