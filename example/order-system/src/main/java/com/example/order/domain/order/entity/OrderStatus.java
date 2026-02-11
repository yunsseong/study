package com.example.order.domain.order.entity;

/**
 * 주문 상태 (상태 전이 규칙)
 *
 * CREATED → PAID → SHIPPED → DELIVERED
 *    ↓        ↓
 * CANCELLED  CANCELLED (결제 후 취소 = 환불 필요)
 *
 * 유효하지 않은 전이:
 * - DELIVERED → CANCELLED (배송 완료 후 취소 불가)
 * - CANCELLED → PAID (취소된 주문 결제 불가)
 */
public enum OrderStatus {

    CREATED("주문 생성"),
    PAID("결제 완료"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 결제 가능한 상태인지 확인
     */
    public boolean canPay() {
        return this == CREATED;
    }

    /**
     * 취소 가능한 상태인지 확인
     * 배송 완료 후에는 취소 불가
     */
    public boolean canCancel() {
        return this == CREATED || this == PAID;
    }

    /**
     * 배송 시작 가능한 상태인지 확인
     */
    public boolean canShip() {
        return this == PAID;
    }
}
