package com.example.order.domain.product.entity;

import com.example.order.global.common.BaseEntity;
import com.example.order.global.error.BusinessException;
import com.example.order.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    /**
     * 낙관적 락을 위한 버전 필드
     *
     * JPA가 UPDATE 시 WHERE version = ? 조건을 자동 추가.
     * 다른 트랜잭션이 먼저 수정했으면 version이 달라서 업데이트 실패
     * → ObjectOptimisticLockingFailureException 발생
     *
     * 비관적 락과 달리 읽기 시 락을 걸지 않으므로 성능이 좋음.
     * 충돌이 적은 상황(일반 쇼핑몰)에서 적합.
     */
    @Version
    private Long version;

    @Builder
    public Product(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    /**
     * 재고 차감
     *
     * 실행되는 SQL:
     * UPDATE product SET stock = ?, version = version + 1
     * WHERE id = ? AND version = ?
     *
     * version이 다르면 0 rows updated → OptimisticLockException
     */
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    /**
     * 재고 복원 (주문 취소 시)
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
