package com.example.coupon.domain.coupon.controller;

import com.example.coupon.domain.coupon.dto.CouponCreateRequest;
import com.example.coupon.domain.coupon.dto.CouponIssueRequest;
import com.example.coupon.domain.coupon.dto.CouponResponse;
import com.example.coupon.domain.coupon.service.CouponPessimisticLockService;
import com.example.coupon.domain.coupon.service.CouponRedisLockService;
import com.example.coupon.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponPessimisticLockService pessimisticLockService;
    private final CouponRedisLockService redisLockService;

    /**
     * 쿠폰 생성
     */
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(couponService.createCoupon(request));
    }

    /**
     * 쿠폰 조회
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCoupon(@PathVariable Long couponId) {
        return ResponseEntity.ok(couponService.getCoupon(couponId));
    }

    /**
     * 쿠폰 발급 - 기본 (동시성 문제 있음!)
     */
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Void> issue(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponIssueRequest request) {
        couponService.issue(couponId, request.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 쿠폰 발급 - 비관적 락
     */
    @PostMapping("/{couponId}/issue/pessimistic")
    public ResponseEntity<Void> issueWithPessimisticLock(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponIssueRequest request) {
        pessimisticLockService.issue(couponId, request.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 쿠폰 발급 - Redis 분산 락
     */
    @PostMapping("/{couponId}/issue/redis")
    public ResponseEntity<Void> issueWithRedisLock(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponIssueRequest request) {
        redisLockService.issue(couponId, request.getUserId());
        return ResponseEntity.ok().build();
    }
}
