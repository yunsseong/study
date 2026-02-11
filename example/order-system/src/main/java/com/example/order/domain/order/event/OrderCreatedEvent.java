package com.example.order.domain.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 주문 생성 이벤트
 *
 * WHY ApplicationEvent를 사용하는가?
 * 1. Spring의 이벤트 메커니즘 활용: @EventListener와 함께 동작
 * 2. 느슨한 결합: 이벤트 발행자와 구독자가 서로를 직접 참조하지 않음
 * 3. 확장성: 새로운 리스너 추가가 쉬움 (기존 코드 수정 없이)
 * 4. 비동기 처리 가능: @Async와 함께 사용하여 성능 개선
 *
 * 면접 포인트:
 * - Event-Driven Architecture: 이벤트 기반으로 시스템 간 통신
 * - Observer Pattern: 이벤트 발행자가 구독자들에게 상태 변화를 알림
 * - ApplicationEventPublisher: Spring이 제공하는 이벤트 발행 인터페이스
 *
 * WHY 동기 vs 비동기?
 * - 기본: 동기 (트랜잭션 내에서 실행, 실패시 롤백)
 * - @Async 사용시: 비동기 (별도 스레드에서 실행, 트랜잭션 분리)
 * - 실무 선택:
 *   * 중요한 로직 (결제, 재고 감소) -> 동기
 *   * 부가 로직 (알림, 로그, 통계) -> 비동기
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    /**
     * WHY orderId와 userId를 포함하는가?
     * - 이벤트 리스너가 주문 정보에 접근하기 위함
     * - 필요한 최소한의 정보만 전달 (불필요한 데이터 전송 방지)
     * - Entity 전체를 전달하면 영속성 컨텍스트 문제 발생 가능
     */
    private final Long orderId;
    private final Long userId;

    /**
     * WHY source를 전달하는가?
     * - ApplicationEvent의 요구사항 (이벤트 발생원 객체)
     * - 일반적으로 'this'를 전달 (이벤트를 발행한 객체)
     *
     * 면접 질문: "source는 언제 사용하나요?"
     * 답변: 이벤트 발행자 추적, 디버깅, 특정 발행자의 이벤트만 처리할 때 사용
     */
    public OrderCreatedEvent(Object source, Long orderId, Long userId) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
    }
}
