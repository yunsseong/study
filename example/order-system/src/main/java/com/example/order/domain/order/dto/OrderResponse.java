package com.example.order.domain.order.dto;

import com.example.order.domain.order.entity.Order;
import com.example.order.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 *
 * WHY 복잡한 변환 로직을 DTO에 두는가?
 * 1. 단일 책임 원칙: Entity -> DTO 변환은 DTO의 책임
 * 2. Service 계층 단순화: Service는 비즈니스 로직에 집중
 * 3. 재사용성: 여러 Service에서 동일한 변환 로직 사용
 *
 * 면접 포인트:
 * - Stream API 활용: items.stream().map()으로 List 변환
 * - 불변 객체: final을 사용하지 않아도 setter가 없으면 사실상 불변
 */
@Getter
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private OrderStatus status;
    private Integer totalAmount;
    private List<OrderItemResponse> items;

    /**
     * WHY createdAt을 포함하는가?
     * - 주문 이력 조회시 시간 정보 필수
     * - 최근 주문순 정렬 등에 활용
     */
    private LocalDateTime createdAt;

    /**
     * static factory method 패턴
     *
     * WHY Order 엔티티에서 items를 가져오는가?
     * - Order는 OrderItem과 1:N 관계
     * - fetch join으로 한 번에 조회하거나, lazy loading으로 조회
     * - 여기서는 Order.getItems()로 OrderItem 리스트를 가져옴
     *
     * WHY stream().map()을 사용하는가?
     * - List<OrderItem> -> List<OrderItemResponse> 변환
     * - 함수형 프로그래밍: 선언적이고 간결한 코드
     * - null safety: items가 null이면 NPE 발생 가능하므로 주의 필요
     *   (실무에서는 Optional이나 null 체크 추가)
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(OrderItemResponse::from) // 메서드 레퍼런스 사용
                        .collect(Collectors.toList()),
                order.getCreatedAt()
        );
    }
}
