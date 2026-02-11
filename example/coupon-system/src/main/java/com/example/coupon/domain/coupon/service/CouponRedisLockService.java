package com.example.coupon.domain.coupon.service;

import com.example.coupon.domain.coupon.entity.Coupon;
import com.example.coupon.domain.coupon.entity.CouponIssue;
import com.example.coupon.domain.coupon.repository.CouponIssueRepository;
import com.example.coupon.domain.coupon.repository.CouponRepository;
import com.example.coupon.global.error.BusinessException;
import com.example.coupon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Step 3: Redis 분산 락 (Redisson) - 실무에서 가장 많이 사용
 *
 * 동작 원리:
 *   1. Redis에 락 키(coupon:lock:{couponId})를 설정
 *   2. 락을 획득한 스레드만 쿠폰 발급 로직 실행
 *   3. 완료 후 락 해제
 *   4. 다른 스레드는 대기 -> 락 해제되면 획득 시도
 *
 * 장점:
 *   - DB 부하 없음 (락을 Redis에서 관리)
 *   - 분산 환경에서도 동작 (서버가 여러 대여도 OK)
 *   - 성능이 좋음 (Redis는 메모리 기반, 매우 빠름)
 *   - Redisson이 락 만료, 재시도 등을 자동 관리
 *
 * 단점:
 *   - Redis 인프라 필요
 *   - Redis 장애 시 서비스 영향
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponRedisLockService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final RedissonClient redissonClient;

    private static final String LOCK_KEY_PREFIX = "coupon:lock:";

    public void issue(Long couponId, Long userId) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + couponId);

        try {
            // 최대 5초 대기, 3초 후 자동 해제
            boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);

            if (!acquired) {
                throw new BusinessException(ErrorCode.COUPON_ISSUE_FAILED);
            }

            // 락 획득 성공 -> 쿠폰 발급
            issueWithTransaction(couponId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.COUPON_ISSUE_FAILED);
        } finally {
            // 락 해제 (반드시 finally에서!)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void issueWithTransaction(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
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

        log.info("[Redis 분산 락] 쿠폰 발급 - couponId: {}, userId: {}, issued: {}/{}",
                couponId, userId, coupon.getIssuedQuantity(), coupon.getTotalQuantity());
    }
}
