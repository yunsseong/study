package com.example.order.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 생성 요청 DTO
 *
 * WHY DTO를 사용하는가?
 * 1. 계층 간 데이터 전송: Controller와 Service 계층 사이의 데이터 전송 객체
 * 2. Entity 보호: Entity를 직접 노출하지 않아 내부 구조 변경의 영향을 최소화
 * 3. 유효성 검증: @Valid 애노테이션과 함께 사용하여 입력 데이터 검증
 * 4. API 명세 명확화: 클라이언트가 어떤 데이터를 보내야 하는지 명확히 정의
 *
 * 면접 포인트:
 * - DTO vs Entity의 차이: DTO는 데이터 전송용, Entity는 비즈니스 로직과 영속성 관리
 * - Request DTO는 불변 객체로 만들 수도 있지만, 여기서는 @NoArgsConstructor를 위해 가변으로 설정
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    /**
     * WHY @NotBlank를 사용하는가?
     * - @NotNull: null만 체크
     * - @NotEmpty: null과 빈 문자열("") 체크
     * - @NotBlank: null, 빈 문자열, 공백 문자열("   ") 모두 체크 (가장 엄격)
     */
    @NotBlank(message = "상품명은 필수입니다")
    private String name;

    /**
     * WHY @Min(0)을 사용하는가?
     * - 가격은 음수가 될 수 없음 (비즈니스 규칙)
     * - 0원 상품도 허용 (무료 샘플 등의 경우)
     */
    @NotNull(message = "가격은 필수입니다")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private Integer price;

    /**
     * WHY @Min(0)을 사용하는가?
     * - 재고는 음수가 될 수 없음
     * - 0개도 허용 (품절 상태를 나타낼 수 있음)
     */
    @NotNull(message = "재고는 필수입니다")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다")
    private Integer stock;
}
