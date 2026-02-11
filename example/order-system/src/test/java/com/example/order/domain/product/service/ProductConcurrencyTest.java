package com.example.order.domain.product.service;

import com.example.order.domain.product.entity.Product;
import com.example.order.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 낙관적 락(Optimistic Lock) 동시성 테스트
 *
 * 목적:
 * - @Version을 사용한 낙관적 락이 동시성을 어떻게 제어하는지 확인
 * - 비관적 락과 다르게 일부 요청이 실패함을 증명
 *
 * 핵심 개념:
 * - 낙관적 락: "충돌이 적을 것"이라고 가정, 커밋 시점에 버전 체크
 * - 동시 요청 시 먼저 커밋된 트랜잭션만 성공, 나머지는 OptimisticLockingFailureException
 * - 재시도 로직이 필요함 (이 테스트에서는 실패 카운트만 측정)
 */
@SpringBootTest
class ProductConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("낙관적 락 - 동시 재고 차감 시 일부만 성공")
    void optimisticLock_concurrentDecrease_partialSuccess() throws InterruptedException {
        // given: 재고 100개인 상품 생성
        Product product = productService.createProduct("테스트 상품", 10000, 100);
        int threadCount = 100; // 100개의 동시 요청
        int decreaseAmount = 1; // 각 요청은 1개씩 차감

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공/실패 카운트
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 100개 스레드가 동시에 재고 차감 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(product.getId(), decreaseAmount);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    // 낙관적 락 충돌: 다른 트랜잭션이 먼저 버전을 변경함
                    failCount.incrementAndGet();
                } catch (BusinessException e) {
                    // 재고 부족 예외
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 성공 + 실패 = 전체 요청 수
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        // 최종 재고 확인
        Product finalProduct = productService.getProduct(product.getId());

        // 핵심: 성공한 요청 수만큼만 재고가 감소해야 함
        assertThat(finalProduct.getStock() + successCount.get()).isEqualTo(100);

        // 결과 출력
        System.out.println("=== 낙관적 락 동시성 테스트 결과 ===");
        System.out.println("초기 재고: 100개");
        System.out.println("동시 요청 수: " + threadCount + "개");
        System.out.println("성공한 요청: " + successCount.get() + "개");
        System.out.println("실패한 요청: " + failCount.get() + "개 (버전 충돌)");
        System.out.println("최종 재고: " + finalProduct.getStock() + "개");
        System.out.println();
        System.out.println("💡 해석:");
        System.out.println("- 낙관적 락은 '충돌이 적다'고 가정하고 락을 걸지 않음");
        System.out.println("- 동시에 같은 데이터를 수정하면 버전(version) 불일치로 일부 요청 실패");
        System.out.println("- 실패한 요청은 재시도 로직이 필요함");
        System.out.println("- 충돌이 적은 환경(일반 쇼핑몰)에서 성능이 좋음");
        System.out.println();
        System.out.println("🆚 비관적 락과 비교:");
        System.out.println("- 비관적 락: 읽을 때 락을 걸어 100% 성공 보장 (쿠폰 시스템)");
        System.out.println("- 낙관적 락: 읽을 때 락 없음, 저장 시 버전 체크로 충돌 감지");

        // 일부 요청은 반드시 실패해야 함 (동시성 충돌)
        assertThat(failCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("단일 스레드 재고 차감 - 정상 동작")
    void singleThread_decreaseStock_success() {
        // given
        Product product = productService.createProduct("테스트 상품", 10000, 100);

        // when
        productService.decreaseStock(product.getId(), 10);

        // then
        Product result = productService.getProduct(product.getId());
        assertThat(result.getStock()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void insufficientStock_throwsException() {
        // given: 재고 10개
        Product product = productService.createProduct("테스트 상품", 10000, 10);

        // when & then: 20개 차감 시도 → 예외 발생
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 20))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("재고가 부족합니다");

        // 재고는 그대로 유지
        Product result = productService.getProduct(product.getId());
        assertThat(result.getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("재고 복원 - 정상 동작")
    void increaseStock_success() {
        // given
        Product product = productService.createProduct("테스트 상품", 10000, 50);

        // when
        productService.increaseStock(product.getId(), 30);

        // then
        Product result = productService.getProduct(product.getId());
        assertThat(result.getStock()).isEqualTo(80);
    }
}
