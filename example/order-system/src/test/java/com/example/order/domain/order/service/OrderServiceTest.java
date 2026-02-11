package com.example.order.domain.order.service;

import com.example.order.domain.order.entity.Order;
import com.example.order.domain.order.entity.OrderStatus;
import com.example.order.domain.product.entity.Product;
import com.example.order.domain.product.service.ProductService;
import com.example.order.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 주문 서비스 테스트
 *
 * 검증 항목:
 * 1. 주문 생성 시 재고 차감
 * 2. 주문 취소 시 재고 복구 (보상 트랜잭션)
 * 3. 주문 상태 전이 (State Machine)
 * 4. 잘못된 상태 전이 방지
 */
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("주문 생성 - 상품 재고 차감 확인")
    void createOrder_decreasesStock() {
        // given: 재고 100개인 상품 생성
        Product product = productService.createProduct("테스트 상품", 10000, 100);

        // when: 10개 주문
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 10)
        );
        Order order = orderService.createOrder(1L, items);

        // then: 주문 생성 성공
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualTo(100000); // 10000 * 10

        // 재고가 10개 감소했는지 확인
        Product updatedProduct = productService.getProduct(product.getId());
        assertThat(updatedProduct.getStock()).isEqualTo(90);

        System.out.println("=== 주문 생성 결과 ===");
        System.out.println("주문 ID: " + order.getId());
        System.out.println("주문 상태: " + order.getStatus().getDescription());
        System.out.println("주문 금액: " + order.getTotalAmount() + "원");
        System.out.println("초기 재고: 100개 → 최종 재고: " + updatedProduct.getStock() + "개");
    }

    @Test
    @DisplayName("주문 취소 - 재고 복구 확인 (보상 트랜잭션)")
    void cancelOrder_restoresStock() {
        // given: 주문 생성 (재고 차감)
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 20)
        );
        Order order = orderService.createOrder(1L, items);

        // 재고 확인: 100 - 20 = 80
        assertThat(productService.getProduct(product.getId()).getStock()).isEqualTo(80);

        // when: 주문 취소
        orderService.cancelOrder(order.getId());

        // then: 주문 상태가 CANCELLED로 변경
        Order cancelledOrder = orderService.getOrder(order.getId());
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // 재고가 복구되었는지 확인: 80 + 20 = 100
        Product restoredProduct = productService.getProduct(product.getId());
        assertThat(restoredProduct.getStock()).isEqualTo(100);

        System.out.println("=== 주문 취소 결과 (보상 트랜잭션) ===");
        System.out.println("주문 취소 후 상태: " + cancelledOrder.getStatus().getDescription());
        System.out.println("재고 복구: 80개 → " + restoredProduct.getStock() + "개");
        System.out.println();
        System.out.println("💡 보상 트랜잭션이란?");
        System.out.println("- 이미 실행된 작업을 되돌리는 트랜잭션");
        System.out.println("- 주문 생성 시 재고 차감 → 주문 취소 시 재고 복구");
        System.out.println("- 분산 시스템에서는 Saga 패턴으로 확장됨");
    }

    @Test
    @DisplayName("주문 상태 전이 - CREATED → PAID → SHIPPED → DELIVERED")
    void orderStatusTransition_fullLifecycle() {
        // given: 주문 생성 (CREATED)
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 5)
        );
        Order order = orderService.createOrder(1L, items);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        // when & then: CREATED → PAID
        order.pay();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        // when & then: PAID → SHIPPED
        order.ship();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        // when & then: SHIPPED → DELIVERED
        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        System.out.println("=== 주문 상태 전이 성공 ===");
        System.out.println("CREATED → PAID → SHIPPED → DELIVERED");
        System.out.println("최종 상태: " + order.getStatus().getDescription());
    }

    @Test
    @DisplayName("잘못된 상태 전이 - CREATED 상태에서 배송 시작 불가")
    void invalidStatusTransition_cannotShipCreatedOrder() {
        // given: CREATED 상태의 주문
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 3)
        );
        Order order = orderService.createOrder(1L, items);

        // when & then: CREATED → SHIPPED 시도 (결제 없이 배송 시작)
        assertThatThrownBy(() -> order.ship())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 주문 상태");

        // 상태는 그대로 유지
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        System.out.println("=== 잘못된 상태 전이 차단 ===");
        System.out.println("시도: CREATED → SHIPPED (결제 없이 배송)");
        System.out.println("결과: 예외 발생 (유효하지 않은 전이)");
        System.out.println();
        System.out.println("💡 상태 머신(State Machine):");
        System.out.println("- 허용된 전이만 가능");
        System.out.println("- CREATED → PAID → SHIPPED → DELIVERED");
        System.out.println("- CREATED/PAID → CANCELLED 가능");
        System.out.println("- DELIVERED → CANCELLED 불가 (배송 완료 후 취소 불가)");
    }

    @Test
    @DisplayName("배송 완료 후 취소 불가")
    void cannotCancelDeliveredOrder() {
        // given: 배송 완료된 주문
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 2)
        );
        Order order = orderService.createOrder(1L, items);
        order.pay();
        order.ship();
        order.deliver();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        // when & then: 배송 완료 후 취소 시도
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 주문 상태");

        System.out.println("=== 배송 완료 후 취소 차단 ===");
        System.out.println("배송 완료 상태에서는 취소 불가");
        System.out.println("실무에서는 반품/교환 프로세스로 처리");
    }

    @Test
    @DisplayName("이미 취소된 주문은 결제 불가")
    void cannotPayCancelledOrder() {
        // given: 취소된 주문
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product.getId(), 1)
        );
        Order order = orderService.createOrder(1L, items);
        orderService.cancelOrder(order.getId());

        Order cancelledOrder = orderService.getOrder(order.getId());
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // when & then: 취소된 주문 결제 시도
        assertThatThrownBy(() -> cancelledOrder.pay())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 주문 상태");
    }

    @Test
    @DisplayName("여러 상품 동시 주문 - 재고 차감 확인")
    void createOrder_multipleProducts_decreasesAllStock() {
        // given: 두 개의 상품
        Product product1 = productService.createProduct("상품1", 5000, 50);
        Product product2 = productService.createProduct("상품2", 8000, 30);

        // when: 두 상품을 함께 주문
        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(product1.getId(), 5),
                new OrderService.OrderItemRequest(product2.getId(), 3)
        );
        Order order = orderService.createOrder(1L, items);

        // then: 총 금액 확인
        assertThat(order.getTotalAmount()).isEqualTo(5000 * 5 + 8000 * 3); // 49000

        // 각 상품의 재고 확인
        assertThat(productService.getProduct(product1.getId()).getStock()).isEqualTo(45);
        assertThat(productService.getProduct(product2.getId()).getStock()).isEqualTo(27);

        System.out.println("=== 여러 상품 주문 결과 ===");
        System.out.println("총 주문 금액: " + order.getTotalAmount() + "원");
        System.out.println("상품1 재고: 50 → 45");
        System.out.println("상품2 재고: 30 → 27");
    }
}
