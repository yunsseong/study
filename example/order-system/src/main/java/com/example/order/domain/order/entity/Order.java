package com.example.order.domain.order.entity;

import com.example.order.global.common.BaseEntity;
import com.example.order.global.error.BusinessException;
import com.example.order.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")  // order는 SQL 예약어이므로 orders 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private int totalAmount;

    @Builder
    public Order(Long userId) {
        this.userId = userId;
        this.status = OrderStatus.CREATED;
        this.totalAmount = 0;
    }

    /**
     * 주문 항목 추가 (연관관계 편의 메서드)
     */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalAmount();
    }

    /**
     * 총 주문 금액 계산
     */
    private void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    /**
     * 결제 완료 처리
     */
    public void pay() {
        if (!this.status.canPay()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * 주문 취소
     */
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 배송 시작
     */
    public void ship() {
        if (!this.status.canShip()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * 배송 완료
     */
    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.DELIVERED;
    }
}
