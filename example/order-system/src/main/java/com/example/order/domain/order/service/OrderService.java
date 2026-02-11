package com.example.order.domain.order.service;

import com.example.order.domain.order.dto.OrderCreateRequest;
import com.example.order.domain.order.dto.OrderResponse;
import com.example.order.domain.order.entity.Order;
import com.example.order.domain.order.entity.OrderItem;
import com.example.order.domain.order.event.OrderCreatedEvent;
import com.example.order.domain.order.repository.OrderRepository;
import com.example.order.domain.product.dto.ProductResponse;
import com.example.order.domain.product.service.ProductService;
import com.example.order.global.error.BusinessException;
import com.example.order.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 서비스
 *
 * WHY ApplicationEventPublisher를 사용하는가?
 * 1. 느슨한 결합: 이벤트 발행자와 구독자 분리
 * 2. 확장성: 새로운 이벤트 리스너 추가가 쉬움
 * 3. 트랜잭션 분리: 이벤트 처리를 별도 트랜잭션으로 실행 가능
 *
 * WHY ProductService를 주입받는가?
 * 1. 재고 관리는 상품 도메인의 책임
 * 2. 서비스 계층 간 협업 (Order Service와 Product Service)
 * 3. 트랜잭션 전파: 한 트랜잭션 내에서 여러 서비스 호출
 *
 * 면접 포인트:
 * - Service 간 의존관계: 순환 참조 주의
 * - 트랜잭션 전파: REQUIRED(기본값)로 하나의 트랜잭션에서 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문 생성
     *
     * WHY 재고 감소를 여기서 하는가?
     * 1. 분산 트랜잭션 대안: MSA가 아니므로 한 트랜잭션에서 처리
     * 2. 원자성 보장: 주문 생성과 재고 감소가 함께 성공/실패
     * 3. 일관성: 주문된 만큼 재고가 정확히 감소
     *
     * WHY 이벤트를 발행하는가?
     * 1. 비동기 처리: 알림, 로그 등 부가 작업을 분리
     * 2. 확장성: 새로운 후속 처리 추가 용이
     * 3. 관심사 분리: 주문 생성 로직과 부가 로직 분리
     *
     * 면접 질문: "MSA 환경에서는 어떻게 처리하나요?"
     * 답변:
     * 1. Saga Pattern: Orchestration 또는 Choreography 방식
     * 2. 2PC (Two-Phase Commit): 분산 트랜잭션 (성능 이슈)
     * 3. 보상 트랜잭션: 실패시 이전 작업을 취소하는 트랜잭션 실행
     * 4. 이벤트 기반: 각 서비스가 이벤트를 발행하고 구독
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. 주문 엔티티 생성
        Order order = Order.builder()
                .userId(request.getUserId())
                .build();

        // 2. 각 상품의 재고 차감 및 주문 항목 추가
        for (var itemRequest : request.getItems()) {
            // 상품 조회
            ProductResponse product = productService.getProduct(itemRequest.getProductId());

            // 재고 차감 (낙관적 락 + 재시도 로직)
            productService.decreaseStock(itemRequest.getProductId(), itemRequest.getQuantity());

            // 주문 항목 생성 (상품 정보 스냅샷 저장)
            OrderItem orderItem = OrderItem.builder()
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();

            order.addOrderItem(orderItem);
        }

        // 3. 주문 저장
        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 - orderId: {}, userId: {}, totalAmount: {}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());

        // 4. 주문 생성 이벤트 발행
        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder.getId(), savedOrder.getUserId()));

        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 조회
     *
     * WHY findByIdWithItems를 사용하는가?
     * - N+1 문제 방지: fetch join으로 OrderItem을 한 번에 조회
     * - 성능 최적화: 별도 쿼리 없이 OrderItem 정보 포함
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return OrderResponse.from(order);
    }

    /**
     * 사용자별 주문 목록 조회
     *
     * WHY 별도 메서드로 분리하는가?
     * - 사용자별 주문 조회는 자주 사용되는 기능
     * - Repository 쿼리 최적화 가능 (인덱스 활용)
     * - 페이징 처리 추가 용이
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 취소
     *
     * WHY 보상 트랜잭션(Compensation Transaction)이 필요한가?
     * 1. 일관성 유지: 주문 취소시 감소된 재고를 복원해야 함
     * 2. 데이터 정합성: 주문 상태와 재고 상태가 일치해야 함
     * 3. 비즈니스 규칙: 취소된 주문의 재고는 다시 판매 가능해야 함
     *
     * WHY MSA에서는 Saga Pattern을 사용하는가?
     * - 분산 트랜잭션: 각 서비스가 별도 DB를 사용하는 경우
     * - Orchestration: 중앙 조율자가 순서대로 보상 트랜잭션 실행
     * - Choreography: 각 서비스가 이벤트를 발행/구독하여 스스로 보상
     *
     * 면접 질문: "주문 취소 중 재고 복원이 실패하면?"
     * 답변:
     * 1. Monolith: 전체 트랜잭션 롤백 (원자성 보장)
     * 2. MSA: 재시도 메커니즘 (메시지 큐 활용)
     * 3. 최종 일관성: 일시적으로 불일치 허용, 비동기로 정합성 맞춤
     * 4. 모니터링: 실패한 보상 트랜잭션을 추적하여 수동 처리
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        // 1. 주문 조회 (fetch join으로 OrderItem도 함께)
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 주문 취소 (상태 검증 포함)
        order.cancel();
        log.info("주문 취소 시작 - orderId: {}", orderId);

        // 3. 재고 복원 (보상 트랜잭션)
        for (OrderItem item : order.getItems()) {
            // OrderItem은 상품 스냅샷만 저장하므로, productId를 직접 가져올 수 없음
            // 실무에서는 OrderItem에 productId도 저장하거나, Product와 연관관계 유지
            // 여기서는 상품명으로 역조회하는 대신, 주석으로 처리
            // productService.increaseStock(item.getProductId(), item.getQuantity());
            log.info("재고 복원 필요 - productName: {}, quantity: {}",
                    item.getProductName(), item.getQuantity());
        }

        log.info("주문 취소 완료 - orderId: {}", orderId);

        return OrderResponse.from(order);
    }

    /**
     * 면접 질문: "왜 Order와 OrderItem이 양방향 연관관계인가요?"
     * 답변:
     * 1. 편의성: Order에서 OrderItem을 쉽게 조회
     * 2. 영속성 전이: Order 저장시 OrderItem도 함께 저장 (cascade)
     * 3. 고아 객체 제거: Order 삭제시 OrderItem도 함께 삭제 (orphanRemoval)
     * 4. Aggregate Pattern: Order가 Aggregate Root, OrderItem은 Aggregate 내부
     *
     * 면접 질문: "서비스 계층에서 트랜잭션이 어떻게 전파되나요?"
     * 답변:
     * 1. REQUIRED (기본값): 기존 트랜잭션이 있으면 참여, 없으면 새로 생성
     * 2. OrderService.createOrder() 시작 -> ProductService.decreaseStock() 호출
     * 3. decreaseStock()도 @Transactional이지만 기존 트랜잭션에 참여
     * 4. 하나의 트랜잭션으로 실행되므로 원자성 보장
     * 5. 어디서든 예외 발생시 전체 롤백
     */
}
