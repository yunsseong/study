package com.example.order.domain.payment.service;

import com.example.order.domain.order.entity.Order;
import com.example.order.domain.order.event.PaymentCompletedEvent;
import com.example.order.domain.order.repository.OrderRepository;
import com.example.order.domain.payment.dto.PaymentRequest;
import com.example.order.domain.payment.dto.PaymentResponse;
import com.example.order.domain.payment.entity.Payment;
import com.example.order.domain.payment.repository.PaymentRepository;
import com.example.order.global.error.BusinessException;
import com.example.order.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 결제 서비스
 *
 * WHY 멱등성(Idempotency)이 중요한가?
 * 1. 네트워크 재시도: 클라이언트가 응답을 못 받고 재시도할 수 있음
 * 2. 중복 결제 방지: 동일한 주문에 대해 여러 번 결제되는 것을 방지
 * 3. 분산 환경: 여러 서버에서 동시에 동일한 요청을 받을 수 있음
 *
 * 면접 포인트:
 * - Idempotency Pattern: 동일한 요청을 여러 번 보내도 결과가 같음
 * - Unique Key: idempotencyKey로 중복 요청 식별
 * - 409 vs 200: 중복 요청을 에러로 처리할지, 기존 결과를 반환할지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 결제 처리
     *
     * WHY 멱등성 키를 먼저 체크하는가?
     * 1. 중복 결제 방지: 같은 idempotencyKey로 이미 결제되었다면 기존 결과 반환
     * 2. 네트워크 재시도 처리: 클라이언트가 타임아웃으로 재시도해도 안전
     * 3. 원자성: 결제 생성과 멱등성 체크를 한 트랜잭션에서 처리
     *
     * WHY 에러를 던지지 않고 기존 결과를 반환하는가?
     * - 클라이언트 관점: 재시도 요청이므로 성공 응답을 기대
     * - 멱등성 보장: 동일한 요청은 동일한 응답을 반환해야 함
     * - 사용자 경험: 에러 메시지보다 정상 처리가 자연스러움
     *
     * 면접 질문: "409 Conflict를 반환하는 것이 더 명확하지 않나요?"
     * 답변:
     * - 관점 차이: 중복을 에러로 볼지, 정상적인 재시도로 볼지
     * - 실무 선택:
     *   * 200 + 기존 결과: 클라이언트가 재시도 로직 불필요 (권장)
     *   * 409 Conflict: 클라이언트가 명시적으로 중복 인지 (추가 처리 필요)
     * - 대부분의 결제 API는 200 + 기존 결과 방식 사용
     */
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        // 1. 멱등성 체크: 이미 처리된 요청인지 확인
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingPayment.isPresent()) {
            log.info("중복 결제 요청 감지 - idempotencyKey: {}, 기존 결제 반환",
                    request.getIdempotencyKey());
            return PaymentResponse.from(existingPayment.get());
        }

        // 2. 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 주문 상태 검증
        if (!order.getStatus().canPay()) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_PAY);
        }

        // 4. 결제 엔티티 생성
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        // 5. 결제 처리 (외부 PG 연동은 생략)
        payment.complete();
        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 생성 완료 - paymentId: {}, orderId: {}, amount: {}",
                savedPayment.getId(), order.getId(), savedPayment.getAmount());

        // 6. 주문 상태 변경 (CREATED -> PAID)
        order.pay();
        log.info("주문 상태 변경 - orderId: {}, status: {} -> PAID",
                order.getId(), order.getStatus());

        // 7. 결제 완료 이벤트 발행
        eventPublisher.publishEvent(new PaymentCompletedEvent(
                this,
                order.getId(),
                savedPayment.getId(),
                savedPayment.getAmount()
        ));

        return PaymentResponse.from(savedPayment);
    }

    /**
     * 면접 질문: "멱등성 키는 누가 생성하나요?"
     * 답변:
     * 1. 클라이언트 생성: 클라이언트가 UUID 등으로 생성하여 전달 (권장)
     * 2. 이유:
     *    - 네트워크 재시도시 동일한 키 사용 가능
     *    - 서버에서 생성하면 재시도 요청을 구분할 수 없음
     * 3. 예시:
     *    - 주문 ID + UUID: order_123_550e8400-e29b-41d4-a716-446655440000
     *    - Timestamp + Random: payment_20240211_abc123
     *
     * 면접 질문: "멱등성 키의 유효 기간은?"
     * 답변:
     * 1. 실무에서는 24시간 ~ 7일 정도 유지
     * 2. 너무 짧으면: 정상적인 재시도도 중복으로 처리 못함
     * 3. 너무 길면: DB 용량 증가, 조회 성능 저하
     * 4. 만료된 키: 별도 테이블로 이동 또는 삭제
     *
     * 면접 질문: "동시에 같은 idempotencyKey로 요청이 들어오면?"
     * 답변:
     * 1. Unique 제약조건: DB에서 중복 insert 방지
     * 2. 하나는 성공, 나머지는 DataIntegrityViolationException 발생
     * 3. 재시도 로직: 예외 발생시 조회 후 기존 결과 반환
     * 4. 실무 처리:
     *    try {
     *        payment = paymentRepository.save(payment);
     *    } catch (DataIntegrityViolationException e) {
     *        payment = paymentRepository.findByIdempotencyKey(key).get();
     *    }
     *
     * 면접 질문: "결제 실패시 어떻게 처리하나요?"
     * 답변:
     * 1. 외부 PG 호출 실패: Payment 상태를 FAILED로 저장, 재시도 큐에 추가
     * 2. 트랜잭션 롤백: 주문 상태는 CREATED 유지
     * 3. 클라이언트 응답: 실패 응답 반환, 재시도 가능 안내
     * 4. 보상 처리: 필요시 재고 복원 (이미 감소된 경우)
     *
     * 면접 질문: "왜 Order 상태를 여기서 변경하나요?"
     * 답변:
     * 1. 트랜잭션 일관성: 결제와 주문 상태 변경이 함께 성공/실패
     * 2. 원자성: 결제는 성공했는데 주문 상태 변경 실패하는 상황 방지
     * 3. MSA에서는: Saga Pattern으로 분리, 이벤트 기반으로 상태 변경
     * 4. 여기서는: Monolith이므로 한 트랜잭션에서 처리
     */
}
