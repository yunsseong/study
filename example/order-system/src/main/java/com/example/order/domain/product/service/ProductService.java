package com.example.order.domain.product.service;

import com.example.order.domain.product.dto.ProductCreateRequest;
import com.example.order.domain.product.dto.ProductResponse;
import com.example.order.domain.product.entity.Product;
import com.example.order.domain.product.repository.ProductRepository;
import com.example.order.global.error.BusinessException;
import com.example.order.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 서비스
 *
 * WHY @Transactional을 클래스 레벨에 두지 않는가?
 * - 조회 메서드는 readOnly=true로 최적화 가능
 * - 메서드별로 트랜잭션 전략을 다르게 가져갈 수 있음
 * - 명시적으로 어떤 메서드가 쓰기 작업인지 명확히 표현
 *
 * 면접 포인트:
 * - @Transactional의 위치: 클래스 vs 메서드 레벨
 * - readOnly=true: 조회 성능 최적화, flush 생략
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * WHY @Transactional을 사용하는가?
     * - 데이터베이스 변경 작업이므로 트랜잭션 필요
     * - 예외 발생시 자동 롤백
     *
     * WHY readOnly를 사용하지 않는가?
     * - 쓰기 작업(save)이므로 readOnly=false (기본값)
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("상품 생성 완료 - id: {}, name: {}", savedProduct.getId(), savedProduct.getName());

        return ProductResponse.from(savedProduct);
    }

    /**
     * WHY @Transactional(readOnly = true)를 사용하는가?
     * 1. 성능 최적화: Hibernate가 flush를 생략 (변경 감지 스킵)
     * 2. 읽기 전용 힌트: DB에 읽기 전용임을 알려줌 (DB에 따라 최적화 가능)
     * 3. 명시적 의도 표현: 이 메서드는 조회만 한다는 것을 코드로 표현
     *
     * 면접 질문: "readOnly=true인데 엔티티를 수정하면 어떻게 되나요?"
     * 답변: Hibernate는 flush를 하지 않으므로 변경사항이 DB에 반영되지 않음.
     *       하지만 영속성 컨텍스트 내에서는 변경됨. (DB와 불일치)
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    /**
     * 재고 감소 (낙관적 락 사용)
     *
     * WHY 낙관적 락(Optimistic Lock)을 사용하는가?
     * 1. 성능: 비관적 락보다 경합이 적을 때 성능이 좋음
     * 2. 데드락 방지: DB 락을 걸지 않아 데드락 발생 가능성 낮음
     * 3. 충돌 감지: @Version으로 동시 수정 감지
     *
     * WHY 비관적 락을 사용하지 않는가?
     * - 비관적 락: SELECT FOR UPDATE로 row lock
     * - 단점: 락 경합 시 대기 시간 증가, 데드락 가능성
     * - 적합한 경우: 충돌이 자주 발생하는 경우
     *
     * WHY 재시도 로직이 필요한가?
     * - 동시에 여러 사용자가 같은 상품 주문시 version 충돌 발생
     * - ObjectOptimisticLockingFailureException 발생
     * - 재시도로 최신 버전을 다시 읽어서 처리
     *
     * 면접 질문: "낙관적 락 vs 비관적 락 언제 사용하나요?"
     * 답변:
     * - 낙관적 락: 충돌이 드물고, 읽기가 많은 경우 (상품 조회)
     * - 비관적 락: 충돌이 자주 발생하고, 정확성이 중요한 경우 (재고 감소)
     *
     * WHY 실무에서는 비관적 락을 더 많이 쓰는가?
     * - 재고 관리는 정확성이 중요 (재시도보다 확실한 처리 선호)
     * - 실제로는 ProductRepository.findByIdWithPessimisticLock 사용 권장
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                // 1. 상품 조회 (낙관적 락: @Version 필드로 버전 관리)
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                // 2. 재고 감소 (비즈니스 로직은 Entity에 위임)
                product.decreaseStock(quantity);

                // 3. 저장 (flush 시점에 version 체크, 충돌시 예외 발생)
                log.info("재고 감소 성공 - productId: {}, quantity: {}, 남은 재고: {}",
                        productId, quantity, product.getStock());
                return;

            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                log.warn("낙관적 락 충돌 발생 - 재시도 {}/{} - productId: {}",
                        retryCount, maxRetries, productId);

                if (retryCount >= maxRetries) {
                    log.error("재고 감소 실패 - 최대 재시도 횟수 초과 - productId: {}", productId);
                    throw new BusinessException(ErrorCode.STOCK_DECREASE_FAILED);
                }

                // 짧은 대기 후 재시도 (충돌 완화)
                try {
                    Thread.sleep(50 * retryCount); // 50ms, 100ms, 150ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.STOCK_DECREASE_FAILED);
                }
            }
        }
    }

    /**
     * 재고 증가 (주문 취소시 사용)
     *
     * WHY 별도 메서드로 분리하는가?
     * - 단일 책임: 각 메서드는 하나의 역할만 수행
     * - 명시적 의도: increaseStock이라는 이름으로 의도를 명확히 표현
     * - 재사용성: 주문 취소 외에도 반품, 재입고 등에서 사용 가능
     *
     * WHY 여기서도 낙관적 락 재시도를 하는가?
     * - 동시에 여러 주문이 취소될 수 있음
     * - version 충돌 가능성 존재
     */
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                product.increaseStock(quantity);

                log.info("재고 증가 성공 - productId: {}, quantity: {}, 현재 재고: {}",
                        productId, quantity, product.getStock());
                return;

            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                log.warn("낙관적 락 충돌 발생 - 재시도 {}/{} - productId: {}",
                        retryCount, maxRetries, productId);

                if (retryCount >= maxRetries) {
                    log.error("재고 증가 실패 - 최대 재시도 횟수 초과 - productId: {}", productId);
                    throw new BusinessException(ErrorCode.STOCK_INCREASE_FAILED);
                }

                try {
                    Thread.sleep(50 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.STOCK_INCREASE_FAILED);
                }
            }
        }
    }

    /**
     * 면접 질문: "왜 재고 증가/감소 로직을 Product 엔티티에 두나요?"
     * 답변:
     * 1. 도메인 주도 설계(DDD): 비즈니스 로직은 엔티티에 위치
     * 2. 캡슐화: 재고 검증 로직을 엔티티 내부에 숨김
     * 3. 일관성: 재고 관련 규칙이 한 곳에 모여 있어 유지보수 용이
     * 4. Service는 조율자: 여러 엔티티를 조율하는 역할, 단일 엔티티 로직은 엔티티에 위임
     */
}
