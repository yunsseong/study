package com.example.coupon.domain.coupon.entity;

import com.example.coupon.global.common.BaseEntity;
import com.example.coupon.global.error.BusinessException;
import com.example.coupon.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Builder
    public Coupon(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
    }

    /**
     * 쿠폰 발급 (수량 차감)
     * ⚠️ 이 메서드 자체는 동기화되지 않음 → 동시성 문제 발생 가능
     */
    public void issue() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw new BusinessException(ErrorCode.COUPON_SOLD_OUT);
        }
        this.issuedQuantity++;
    }

    public boolean isSoldOut() {
        return this.issuedQuantity >= this.totalQuantity;
    }

    public int remainingQuantity() {
        return this.totalQuantity - this.issuedQuantity;
    }
}
