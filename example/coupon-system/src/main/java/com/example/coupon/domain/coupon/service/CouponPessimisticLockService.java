package com.example.coupon.domain.coupon.service;

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

/**
 * Step 2: 비관적 락 (Pessimistic Lock) - SELECT ... FOR UPDATE
 *
 * 동작 원리:
 *   스레드 A: SELECT ... FOR UPDATE -> row 락 획득 -> 발급 -> 커밋 -> 락 해제
 *   스레드 B: SELECT ... FOR UPDATE -> 대기... -> A 커밋 후 -> 락 획득 -> 발급
 *
 * 장점:
 *   - 데이터 정합성 보장
 *   - 구현이 간단 (JPA @Lock 어노테이션)
 *
 * 단점:
 *   - DB에 락을 걸어서 성능 저하 (직렬 처리)
 *   - 대기 시간이 길어지면 타임아웃 발생 가능
 *   - 데드락 위험 (여러 테이블을 동시에 락 걸 때)
 *   - DB 부하 집중
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponPessimisticLockService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public void issue(Long couponId, Long userId) {
        // 비관적 락으로 조회 (다른 트랜잭션은 이 row에 접근 불가)
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        coupon.issue();

        couponIssueRepository.save(
                CouponIssue.builder()
                        .couponId(couponId)
                        .userId(userId)
                        .build()
        );

        log.info("[비관적 락] 쿠폰 발급 - couponId: {}, userId: {}, issued: {}/{}",
                couponId, userId, coupon.getIssuedQuantity(), coupon.getTotalQuantity());
    }
}
