package com.example.coupon.domain.coupon.service;

import com.example.coupon.domain.coupon.entity.Coupon;
import com.example.coupon.domain.coupon.entity.CouponIssue;
import com.example.coupon.domain.coupon.repository.CouponIssueRepository;
import com.example.coupon.domain.coupon.repository.CouponRepository;
import com.example.coupon.global.error.BusinessException;
import com.example.coupon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 4 (보너스): Redis Atomic 연산 - 가장 빠른 방식
 *
 * 동작 원리:
 *   1. Redis INCR 명령으로 원자적으로 카운터 증가
 *   2. 증가된 값이 총 수량 이하면 -> 발급 허용
 *   3. 초과하면 -> DECR로 원복, 발급 거부
 *   4. DB 저장은 이후에 처리 (비동기 가능)
 *
 * 핵심: Redis INCR은 싱글 스레드로 실행되므로 원자성 보장!
 *       락 없이도 동시성 문제가 해결됨
 *
 * 장점:
 *   - 락이 필요 없음 -> 대기 시간 없음 -> 가장 빠름
 *   - Redis 싱글 스레드 특성 활용
 *   - 초당 수만 건 처리 가능
 *
 * 단점:
 *   - Redis와 DB 간 데이터 불일치 가능 (최종 일관성)
 *   - Redis 장애 시 복구 필요
 *
 * 실무: 대규모 이벤트(네이버, 카카오 선착순)에서 이 방식 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponRedisAtomicService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String COUPON_COUNT_KEY = "coupon:count:";
    private static final String COUPON_USER_KEY = "coupon:user:";

    /**
     * Redis에 쿠폰 정보 초기화 (쿠폰 생성 시 호출)
     */
    public void initCouponCount(Long couponId, int totalQuantity) {
        redisTemplate.opsForValue().set(COUPON_COUNT_KEY + couponId, 0L);
    }

    public void issue(Long couponId, Long userId) {
        // 1. 중복 발급 체크 (Redis Set 활용)
        String userKey = COUPON_USER_KEY + couponId;
        Boolean isNewUser = redisTemplate.opsForSet().add(userKey, userId) == 1;

        if (Boolean.FALSE.equals(isNewUser)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 2. 원자적 카운터 증가
        String countKey = COUPON_COUNT_KEY + couponId;
        Long currentCount = redisTemplate.opsForValue().increment(countKey);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 수량 초과 체크
        if (currentCount != null && currentCount > coupon.getTotalQuantity()) {
            // 원복
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForSet().remove(userKey, userId);
            throw new BusinessException(ErrorCode.COUPON_SOLD_OUT);
        }

        // 4. DB 저장
        saveCouponIssue(couponId, userId);

        log.info("[Redis Atomic] 쿠폰 발급 - couponId: {}, userId: {}, count: {}",
                couponId, userId, currentCount);
    }

    @Transactional
    public void saveCouponIssue(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        coupon.issue();

        couponIssueRepository.save(
                CouponIssue.builder()
                        .couponId(couponId)
                        .userId(userId)
                        .build()
        );
    }
}
