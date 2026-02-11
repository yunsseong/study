package com.example.order.domain.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 리스너
 *
 * WHY 별도의 리스너 클래스를 만드는가?
 * 1. 단일 책임 원칙: 이벤트 처리 로직을 한 곳에 모음
 * 2. 재사용성: 여러 Service에서 발행한 이벤트를 한 곳에서 처리
 * 3. 관심사 분리: Service는 비즈니스 로직, Listener는 이벤트 후처리
 *
 * 면접 포인트:
 * - @Component: Spring Bean으로 등록하여 @EventListener가 동작하도록 함
 * - @EventListener: 특정 타입의 이벤트를 구독
 * - Spring이 이벤트 발행시 자동으로 매칭되는 리스너 메서드 호출
 */
@Slf4j
@Component
public class OrderEventListener {

    /**
     * WHY @EventListener를 사용하는가?
     * - ApplicationListener 인터페이스를 구현하는 것보다 간결
     * - 타입 안전: 메서드 파라미터로 이벤트 타입 지정
     * - 여러 이벤트 처리: 하나의 클래스에 여러 @EventListener 메서드 가능
     *
     * WHY 기본이 동기 처리인가?
     * - 트랜잭션 일관성: 이벤트 처리가 실패하면 전체 트랜잭션 롤백 가능
     * - 순서 보장: 이벤트 발행 순서대로 처리됨
     * - 디버깅 용이: 호출 스택이 명확함
     *
     * 실무에서 비동기로 전환하는 경우:
     * 1. @Async 애노테이션 추가
     * 2. @EnableAsync 설정 필요 (Application 클래스나 Config 클래스에)
     * 3. ThreadPoolTaskExecutor 설정으로 스레드 풀 관리
     *
     * 면접 질문: "언제 동기, 언제 비동기를 사용하나요?"
     * 답변:
     * - 동기: 트랜잭션 일관성이 중요한 경우 (재고 처리, 상태 변경 등)
     * - 비동기: 실패해도 주요 로직에 영향 없는 경우 (알림, 로그, 통계 등)
     */
    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}, userId: {}",
                event.getOrderId(), event.getUserId());

        // 실무에서 여기에 추가할 수 있는 로직:
        // 1. 이메일 알림 발송 (비동기 권장)
        // 2. SMS 알림 발송 (비동기 권장)
        // 3. 주문 통계 업데이트 (비동기 권장)
        // 4. 외부 시스템 연동 (비동기 권장, 보상 트랜잭션 필요)
        // 5. 감사 로그 기록 (동기/비동기 모두 가능)

        // TODO: 실제 비즈니스 로직 구현
    }

    /**
     * WHY PaymentCompletedEvent를 처리하는가?
     * - 결제 완료 후 필요한 후속 처리 수행
     * - 예: 배송 준비, 재고 확정, 판매자 알림 등
     */
    @EventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신 - orderId: {}, paymentId: {}, amount: {}",
                event.getOrderId(), event.getPaymentId(), event.getAmount());

        // 실무에서 여기에 추가할 수 있는 로직:
        // 1. 배송 준비 시작 (배송 시스템 연동)
        // 2. 판매자 알림 (새 주문 알림)
        // 3. 재고 확정 (임시 예약 -> 확정)
        // 4. 포인트 적립 (포인트 시스템 연동)
        // 5. 정산 데이터 생성 (정산 시스템 연동)

        // TODO: 실제 비즈니스 로직 구현
    }

    /**
     * 면접 질문: "이벤트 리스너에서 예외가 발생하면 어떻게 되나요?"
     * 답변:
     * 1. 동기 처리시: 예외가 발생하면 트랜잭션 롤백 (이벤트 발행자의 트랜잭션도 롤백)
     * 2. 비동기 처리시: 예외가 발생해도 원래 트랜잭션은 커밋됨
     * 3. 실무 처리:
     *    - 중요한 로직: try-catch로 예외 처리, 실패시 재시도 큐에 추가
     *    - 부가 로직: 예외 로깅만 하고 무시
     *
     * 면접 질문: "@Async를 사용할 때 주의점은?"
     * 답변:
     * 1. 트랜잭션 분리: 리스너는 별도 트랜잭션에서 실행 (원래 트랜잭션 공유 안 됨)
     * 2. 예외 처리: 비동기 예외는 호출자가 받을 수 없음 (별도 처리 필요)
     * 3. 순서 보장 안 됨: 이벤트 발생 순서와 처리 순서가 다를 수 있음
     * 4. 스레드 풀 설정 필요: ThreadPoolTaskExecutor 설정으로 스레드 수 관리
     */
}
