package com.example.order.domain.product.dto;

import com.example.order.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품 응답 DTO
 *
 * WHY static factory method 패턴을 사용하는가?
 * 1. 의미있는 이름: 생성자보다 from(), of() 같은 메서드명이 의도를 명확히 표현
 * 2. 캐싱 가능: 필요시 동일한 객체를 재사용할 수 있음
 * 3. 서브타입 반환 가능: 생성자는 자기 자신만 반환하지만, factory method는 하위 타입도 반환 가능
 * 4. 가독성: ProductResponse.from(product)가 new ProductResponse(product)보다 읽기 쉬움
 *
 * 면접 포인트:
 * - Effective Java Item 1: "생성자 대신 정적 팩토리 메서드를 고려하라"
 * - Entity -> DTO 변환은 DTO에서 담당 (단일 책임 원칙)
 */
@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private Integer price;
    private Integer stock;

    /**
     * WHY version 필드를 포함하는가?
     * - 클라이언트가 낙관적 락의 버전 정보를 알 수 있음
     * - 디버깅시 유용: 동시성 문제 발생시 버전 정보로 추적 가능
     * - 향후 클라이언트 측 낙관적 락 구현 가능 (예: 수정시 버전 체크)
     */
    private Long version;

    /**
     * WHY static factory method인가?
     * - Entity to DTO 변환 로직을 DTO에 캡슐화
     * - Service 계층에서 간단하게 ProductResponse.from(product) 호출
     * - 변환 로직이 한 곳에 집중되어 유지보수 용이
     *
     * 면접 질문: "왜 Product 엔티티에 toResponse() 메서드를 만들지 않았나요?"
     * 답변: Entity는 영속성과 비즈니스 로직에 집중해야 함. DTO 변환은 DTO의 책임.
     *       Entity가 여러 형태의 DTO를 알게 되면 결합도가 높아짐.
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getVersion()
        );
    }
}
