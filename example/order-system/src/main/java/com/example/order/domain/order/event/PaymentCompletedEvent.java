package com.example.order.domain.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 결제 완료 이벤트
 *
 * WHY 결제 완료를 이벤트로 처리하는가?
 * 1. 결제 후 후속 처리 분리: 알림 발송, 재고 확정, 배송 준비 등
 * 2. 트랜잭션 분리: 결제 성공 후 부가 작업 실패가 결제를 롤백하지 않도록
 * 3. 확장성: 새로운 후속 처리 추가 시 기존 코드 수정 없음
 *
 * 실무 시나리오:
 * - 결제 완료 -> 이메일 발송 실패 -> 이메일만 재시도, 결제는 그대로 유지
 * - 이벤트가 없으면 모두 하나의 트랜잭션에서 처리되어 롤백 위험
 */
@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final Long orderId;
    private final Long paymentId;
    private final Integer amount;

    /**
     * WHY amount를 포함하는가?
     * - 이벤트 리스너가 결제 금액을 알아야 하는 경우 (통계, 정산 등)
     * - Order나 Payment를 다시 조회하지 않고도 금액 정보 활용
     *
     * 면접 포인트:
     * - Event Payload: 이벤트에 어떤 정보를 담을지 선택이 중요
     * - Trade-off: 많은 정보 vs 최소 정보
     *   * 많이 담으면: 조회 불필요, 하지만 이벤트 크기 증가
     *   * 최소만 담으면: 이벤트 작음, 하지만 필요시 다시 조회해야 함
     */
    public PaymentCompletedEvent(Object source, Long orderId, Long paymentId, Integer amount) {
        super(source);
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
    }
}
