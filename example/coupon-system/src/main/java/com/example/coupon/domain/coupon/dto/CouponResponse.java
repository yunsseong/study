package com.example.coupon.domain.coupon.dto;

import com.example.coupon.domain.coupon.entity.Coupon;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponResponse {

    private Long id;
    private String name;
    private int totalQuantity;
    private int issuedQuantity;
    private int remainingQuantity;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .remainingQuantity(coupon.remainingQuantity())
                .build();
    }
}
