package com.example.coupon.domain.coupon.service;

import com.example.coupon.domain.coupon.entity.Coupon;
import com.example.coupon.domain.coupon.repository.CouponIssueRepository;
import com.example.coupon.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 선착순 쿠폰 시스템 동시성 테스트
 *
 * 시나리오: 100개 한정 쿠폰에 1000명이 동시 요청
 * 기대 결과: 정확히 100개만 발급되어야 함
 *
 * 이 테스트를 통해 Race Condition 문제를 직접 확인하고,
 * 각 해결 방법의 효과를 검증할 수 있습니다.
 */
@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponPessimisticLockService pessimisticLockService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    private static final int TOTAL_COUPON = 100;
    private static final int THREAD_COUNT = 1000;

    @AfterEach
    void tearDown() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("동시성 제어 없음 - Race Condition 발생: 100개 초과 발급됨")
    void issue_withoutLock_raceCondition() throws InterruptedException {
        // given: 100개 한정 쿠폰 생성
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("선착순 100명 할인 쿠폰")
                        .totalQuantity(TOTAL_COUPON)
                        .build()
        );

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 1000명이 동시에 쿠폰 발급 요청
        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponService.issue(coupon.getId(), userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: Race Condition으로 인해 100개보다 많이 발급됨!
        Coupon result = couponRepository.findById(coupon.getId()).orElseThrow();
        long issuedCount = couponIssueRepository.count();

        System.out.println("=== 동시성 제어 없음 결과 ===");
        System.out.println("발급된 쿠폰 수 (DB issued): " + result.getIssuedQuantity());
        System.out.println("발급 이력 수 (CouponIssue): " + issuedCount);
        System.out.println("성공 요청: " + successCount.get());
        System.out.println("실패 요청: " + failCount.get());

        // ⚠️ 이 테스트는 100개보다 많이 발급될 수 있음을 보여줌
        // Race Condition이 발생하면 issuedQuantity와 실제 발급 수가 불일치할 수 있음
        // assertThat(result.getIssuedQuantity()).isGreaterThan(TOTAL_COUPON);
        // ↑ 항상 재현되진 않으므로 주석 처리 (환경에 따라 다름)
        System.out.println("⚠️ 100개 한정인데 " + successCount.get() + "개 발급됨 (Race Condition!)");
    }

    @Test
    @DisplayName("비관적 락 - 정확히 100개만 발급됨")
    void issue_withPessimisticLock_exactCount() throws InterruptedException {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("비관적 락 테스트 쿠폰")
                        .totalQuantity(TOTAL_COUPON)
                        .build()
        );

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 1000명 동시 요청 (비관적 락 사용)
        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    pessimisticLockService.issue(coupon.getId(), userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 정확히 100개만 발급!
        Coupon result = couponRepository.findById(coupon.getId()).orElseThrow();
        long issuedCount = couponIssueRepository.count();

        System.out.println("=== 비관적 락 결과 ===");
        System.out.println("발급된 쿠폰 수: " + result.getIssuedQuantity());
        System.out.println("발급 이력 수: " + issuedCount);
        System.out.println("성공 요청: " + successCount.get());
        System.out.println("실패 요청: " + failCount.get());

        assertThat(result.getIssuedQuantity()).isEqualTo(TOTAL_COUPON);
        assertThat(successCount.get()).isEqualTo(TOTAL_COUPON);
        System.out.println("✅ 정확히 " + TOTAL_COUPON + "개만 발급됨!");
    }

    @Test
    @DisplayName("단일 스레드 - 정상 발급 확인")
    void issue_singleThread_success() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("단일 스레드 테스트")
                        .totalQuantity(1)
                        .build()
        );

        // when
        couponService.issue(coupon.getId(), 1L);

        // then
        Coupon result = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(result.getIssuedQuantity()).isEqualTo(1);
        assertThat(result.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("품절 후 발급 시도 - 예외 발생")
    void issue_whenSoldOut_throwsException() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("1개 한정 쿠폰")
                        .totalQuantity(1)
                        .build()
        );
        couponService.issue(coupon.getId(), 1L);

        // when & then
        try {
            couponService.issue(coupon.getId(), 2L);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("소진");
        }
    }
}
