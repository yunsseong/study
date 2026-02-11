package com.example.order.domain.payment.service;

import com.example.order.domain.order.entity.Order;
import com.example.order.domain.order.entity.OrderStatus;
import com.example.order.domain.order.service.OrderService;
import com.example.order.domain.payment.entity.Payment;
import com.example.order.domain.payment.entity.PaymentStatus;
import com.example.order.domain.product.entity.Product;
import com.example.order.domain.product.service.ProductService;
import com.example.order.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제 서비스 테스트
 *
 * 검증 항목:
 * 1. 결제 성공 시 주문 상태 변경
 * 2. 멱등성 키를 통한 중복 결제 방지
 * 3. 이미 결제된 주문 재결제 차단
 */
@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("결제 성공 - 주문 상태 PAID로 변경")
    void pay_success_orderStatusChangedToPaid() {
        // given: 주문 생성
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 5)
        );
        Order order = orderService.createOrder(1L, items);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        // when: 결제 처리
        String idempotencyKey = UUID.randomUUID().toString();
        Payment payment = paymentService.pay(order.getId(), order.getTotalAmount(), idempotencyKey);

        // then: 결제 정보 확인
        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(order.getId());
        assertThat(payment.getAmount()).isEqualTo(order.getTotalAmount());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);

        // 주문 상태가 PAID로 변경되었는지 확인
        Order paidOrder = orderService.getOrder(order.getId());
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        System.out.println("=== 결제 성공 결과 ===");
        System.out.println("결제 ID: " + payment.getId());
        System.out.println("주문 상태: CREATED → " + paidOrder.getStatus().getDescription());
        System.out.println("결제 금액: " + payment.getAmount() + "원");
        System.out.println("멱등성 키: " + idempotencyKey);
    }

    @Test
    @DisplayName("멱등성 키 중복 - 기존 결제 결과 반환 (중복 결제 방지)")
    void pay_duplicateIdempotencyKey_returnsExistingPayment() {
        // given: 주문 생성 및 결제
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 3)
        );
        Order order = orderService.createOrder(1L, items);

        String idempotencyKey = "test-idempotency-key-12345";

        // 첫 번째 결제
        Payment firstPayment = paymentService.pay(order.getId(), order.getTotalAmount(), idempotencyKey);

        // when: 같은 멱등성 키로 두 번째 결제 시도
        Payment secondPayment = paymentService.pay(order.getId(), order.getTotalAmount(), idempotencyKey);

        // then: 같은 결제 객체가 반환됨 (새로운 결제 생성 X)
        assertThat(secondPayment.getId()).isEqualTo(firstPayment.getId());
        assertThat(secondPayment.getIdempotencyKey()).isEqualTo(idempotencyKey);

        System.out.println("=== 멱등성 키 중복 결과 ===");
        System.out.println("첫 번째 결제 ID: " + firstPayment.getId());
        System.out.println("두 번째 결제 ID: " + secondPayment.getId());
        System.out.println("결과: 동일한 결제 객체 반환 (중복 결제 방지)");
        System.out.println();
        System.out.println("💡 멱등성(Idempotency)이란?");
        System.out.println("- 같은 요청을 여러 번 해도 결과가 동일함");
        System.out.println("- 클라이언트가 네트워크 타임아웃으로 재시도해도 안전");
        System.out.println();
        System.out.println("📌 시나리오:");
        System.out.println("1. 사용자가 '결제' 버튼 클릭");
        System.out.println("2. 서버에서 결제 처리 중...");
        System.out.println("3. 네트워크 타임아웃! 클라이언트는 응답 못 받음");
        System.out.println("4. 클라이언트가 자동 재시도 (같은 idempotencyKey)");
        System.out.println("5. 서버: '이미 처리했어요' → 기존 결제 정보 반환");
        System.out.println("6. 결과: 중복 결제 방지!");
        System.out.println();
        System.out.println("🏦 실무 사례:");
        System.out.println("- 카카오페이: partner_order_id로 중복 방지");
        System.out.println("- 토스: orderId + 고유 키 조합");
        System.out.println("- 스트라이프: HTTP 헤더에 Idempotency-Key 전달");
    }

    @Test
    @DisplayName("이미 결제된 주문 - 다른 멱등성 키로 재결제 시도 시 예외 발생")
    void pay_alreadyPaidOrder_throwsException() {
        // given: 주문 생성 및 결제 완료
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 2)
        );
        Order order = orderService.createOrder(1L, items);

        // 첫 번째 결제 (성공)
        String firstIdempotencyKey = UUID.randomUUID().toString();
        paymentService.pay(order.getId(), order.getTotalAmount(), firstIdempotencyKey);

        // 주문 상태 확인
        Order paidOrder = orderService.getOrder(order.getId());
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // when & then: 다른 멱등성 키로 재결제 시도
        String secondIdempotencyKey = UUID.randomUUID().toString();
        assertThatThrownBy(() ->
                paymentService.pay(order.getId(), order.getTotalAmount(), secondIdempotencyKey)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 주문 상태");

        System.out.println("=== 이미 결제된 주문 재결제 차단 ===");
        System.out.println("첫 번째 결제: 성공 (주문 상태 PAID)");
        System.out.println("두 번째 결제: 실패 (다른 idempotencyKey로 시도)");
        System.out.println("→ 같은 주문을 여러 번 결제할 수 없음");
    }

    @Test
    @DisplayName("결제 취소 - 상태가 CANCELLED로 변경")
    void cancelPayment_success() {
        // given: 결제 완료된 주문
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 1)
        );
        Order order = orderService.createOrder(1L, items);

        String idempotencyKey = UUID.randomUUID().toString();
        Payment payment = paymentService.pay(order.getId(), order.getTotalAmount(), idempotencyKey);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // when: 결제 취소
        paymentService.cancelPayment(payment.getId());

        // then: 결제 상태 확인
        Payment cancelledPayment = paymentService.getPayment(payment.getId());
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        System.out.println("=== 결제 취소 결과 ===");
        System.out.println("결제 상태: COMPLETED → CANCELLED");
        System.out.println("실무에서는 PG사에 환불 API 호출 필요");
    }

    @Test
    @DisplayName("존재하지 않는 주문 - 결제 시 예외 발생")
    void pay_orderNotFound_throwsException() {
        // given: 존재하지 않는 주문 ID
        Long invalidOrderId = 99999L;
        String idempotencyKey = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() ->
                paymentService.pay(invalidOrderId, 10000, idempotencyKey)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }
}
