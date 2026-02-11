package com.example.order.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 요청 DTO
 *
 * WHY 별도의 OrderItemRequest를 만드는가?
 * 1. 재사용성: 주문 생성시 여러 개의 주문 항목을 받을 수 있음
 * 2. 검증 분리: 주문 항목별 유효성 검증 로직을 독립적으로 관리
 * 3. 단일 책임: 주문 항목 하나의 정보만 담당
 *
 * 면접 포인트:
 * - Nested DTO 검증: @Valid를 상위 DTO에서 사용하면 하위 DTO도 검증됨
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    /**
     * WHY productId만 받는가?
     * - 클라이언트는 상품 ID만 알면 됨
     * - 서버에서 상품 정보를 조회하여 가격 등을 확정 (클라이언트 조작 방지)
     * - 가격을 클라이언트에서 받으면 보안 문제 발생 가능
     */
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    /**
     * WHY @Min(1)인가?
     * - 최소 1개는 주문해야 함 (0개 주문은 의미 없음)
     * - 비즈니스 규칙에 따라 최대 수량 제한도 추가 가능 (@Max)
     */
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
}
