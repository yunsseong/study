package com.example.order.domain.order.dto;

import com.example.order.domain.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주문 항목 응답 DTO
 *
 * WHY productId가 아닌 productName을 반환하는가?
 * 1. 스냅샷 패턴: 주문 당시의 상품 정보를 보존 (상품명이 변경되어도 주문 내역은 그대로)
 * 2. 조회 성능: 주문 조회시 상품 테이블을 조인하지 않아도 됨
 * 3. 데이터 무결성: 상품이 삭제되어도 주문 내역 조회 가능
 *
 * 면접 포인트:
 * - Snapshot Pattern: 시점 정보를 저장하여 이력 관리
 * - 정규화 vs 비정규화: 이력성 데이터는 비정규화하여 저장하는 것이 유리
 */
@Getter
@AllArgsConstructor
public class OrderItemResponse {

    /**
     * WHY productName, price를 저장하는가?
     * - 주문 시점의 상품 정보 스냅샷
     * - 상품 정보가 변경되어도 주문 내역은 변하지 않음
     * - 예: 상품 가격이 10,000원 -> 15,000원 변경되어도,
     *      과거 주문은 10,000원으로 표시되어야 함
     */
    private String productName;
    private Integer price;
    private Integer quantity;

    /**
     * WHY totalPrice를 계산하여 반환하는가?
     * - 클라이언트 편의성: 개별 항목의 총 금액을 서버에서 계산
     * - 계산 로직 중앙화: price * quantity 계산을 서버에서 담당
     */
    private Integer totalPrice; // price * quantity

    /**
     * static factory method 패턴
     *
     * WHY totalPrice를 여기서 계산하는가?
     * - DTO는 단순 데이터 전송 객체이므로, 간단한 계산은 허용
     * - 복잡한 비즈니스 로직은 Entity나 Service에 두되, 단순 계산은 DTO에서 가능
     */
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProductName(),
                orderItem.getPrice(),
                orderItem.getQuantity(),
                orderItem.getPrice() * orderItem.getQuantity()
        );
    }
}
