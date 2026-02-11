package com.example.coupon.domain.coupon.service;

import com.example.coupon.domain.coupon.dto.CouponCreateRequest;
import com.example.coupon.domain.coupon.dto.CouponResponse;
import com.example.coupon.domain.coupon.entity.Coupon;
import com.example.coupon.domain.coupon.entity.CouponIssue;
import com.example.coupon.domain.coupon.repository.CouponIssueRepository;
import com.example.coupon.domain.coupon.repository.CouponRepository;
import com.example.coupon.global.error.BusinessException;
import com.example.coupon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.builder()
                .name(request.getName())
                .totalQuantity(request.getTotalQuantity())
                .build();
        return CouponResponse.from(couponRepository.save(coupon));
    }

    /**
     * 문제 있는 코드: 동시성 제어 없음
     *
     * 여러 스레드가 동시에 실행하면:
     * 1. 스레드 A: coupon 조회 (issuedQuantity = 99)
     * 2. 스레드 B: coupon 조회 (issuedQuantity = 99) <- A가 아직 저장 안 했는데 같은 값 읽음
     * 3. 스레드 A: issue() -> issuedQuantity = 100, 저장
     * 4. 스레드 B: issue() -> issuedQuantity = 100, 저장 <- 101번째인데 통과!
     *
     * 결과: 100개 한정 쿠폰인데 100개 이상 발급됨
     */
    @Transactional
    public void issue(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        // 중복 발급 체크
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 수량 차감 (Race Condition 발생 지점!)
        coupon.issue();

        // 발급 이력 저장
        couponIssueRepository.save(
                CouponIssue.builder()
                        .couponId(couponId)
                        .userId(userId)
                        .build()
        );

        log.info("쿠폰 발급 완료 - couponId: {}, userId: {}, issued: {}/{}",
                couponId, userId, coupon.getIssuedQuantity(), coupon.getTotalQuantity());
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.from(coupon);
    }
}
