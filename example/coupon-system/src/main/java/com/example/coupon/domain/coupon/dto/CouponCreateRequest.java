package com.example.coupon.domain.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    private String name;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private int totalQuantity;
}
