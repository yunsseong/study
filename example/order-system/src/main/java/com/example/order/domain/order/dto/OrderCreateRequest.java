package com.example.order.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 *
 * WHY Nested DTO 구조를 사용하는가?
 * 1. 계층적 데이터 구조: 주문(Order)과 주문 항목(OrderItem)의 1:N 관계를 명확히 표현
 * 2. 일괄 처리: 한 번의 요청으로 주문과 여러 주문 항목을 함께 생성
 * 3. 원자성: 주문과 주문 항목이 함께 생성되거나 함께 실패 (트랜잭션)
 *
 * 면접 포인트:
 * - Aggregate Pattern: Order가 OrderItem을 포함하는 Aggregate Root
 * - @Valid의 중첩 검증: List 내부 객체도 검증하려면 @Valid 필요
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /**
     * WHY userId를 받는가?
     * - 실무에서는 JWT 토큰 등에서 추출하지만, 예제에서는 명시적으로 전달
     * - 주문자 식별을 위한 필수 정보
     */
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    /**
     * WHY @Valid를 사용하는가?
     * - List 내부의 OrderItemRequest 객체들도 각각 검증하기 위함
     * - @Valid가 없으면 OrderItemRequest 내부의 @NotNull, @Min 등이 동작하지 않음
     *
     * WHY @NotEmpty를 사용하는가?
     * - @NotNull: null만 체크 (빈 리스트는 통과)
     * - @NotEmpty: null과 빈 리스트(size=0) 모두 체크
     * - 최소 1개 이상의 주문 항목이 있어야 주문이 의미 있음
     */
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    @Valid // 중요: List 내부 객체들의 검증 애노테이션을 활성화
    private List<OrderItemRequest> items;
}
