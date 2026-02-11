package com.example.order.api;

import com.example.order.domain.product.dto.ProductCreateRequest;
import com.example.order.domain.product.dto.ProductResponse;
import com.example.order.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 컨트롤러
 *
 * WHY @RestController를 사용하는가?
 * - @Controller + @ResponseBody 조합
 * - 모든 메서드의 반환값을 HTTP Response Body에 JSON으로 변환
 * - REST API 개발에 적합
 *
 * WHY @RequestMapping("/api/products")를 사용하는가?
 * - 클래스 레벨 경로 매핑: 공통 prefix 지정
 * - 메서드별로 세부 경로만 추가하면 됨
 * - URL 구조: /api/products, /api/products/{id}
 *
 * 면접 포인트:
 * - REST API 설계: 리소스 기반 URL, HTTP 메서드 활용
 * - @Valid: Request DTO의 유효성 검증 자동화
 * - ResponseEntity: HTTP 상태 코드와 응답 body를 함께 관리
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 생성
     *
     * WHY @Valid를 사용하는가?
     * - Request DTO의 @NotBlank, @Min 등 검증 애노테이션 활성화
     * - 검증 실패시 MethodArgumentNotValidException 발생
     * - GlobalExceptionHandler에서 일괄 처리
     *
     * WHY @RequestBody를 사용하는가?
     * - HTTP Request Body의 JSON을 ProductCreateRequest 객체로 변환
     * - Jackson이 자동으로 역직렬화 수행
     *
     * WHY ResponseEntity<ProductResponse>를 반환하는가?
     * - HTTP 상태 코드 제어: 201 Created
     * - Response Body: ProductResponse (JSON으로 변환됨)
     * - RESTful: 생성 성공시 201 반환이 표준
     *
     * 면접 질문: "왜 201 Created를 반환하나요?"
     * 답변:
     * - RESTful 원칙: 리소스 생성은 201 Created
     * - 200 OK와의 차이: 200은 일반적인 성공, 201은 생성 성공
     * - Location Header: 생성된 리소스 URL을 함께 반환 가능
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 조회
     *
     * WHY @PathVariable을 사용하는가?
     * - URL 경로의 변수 추출: /api/products/1 -> id = 1
     * - RESTful URL 설계: 리소스 식별자를 경로에 포함
     *
     * WHY ResponseEntity.ok()를 사용하는가?
     * - 간결한 표현: ResponseEntity.status(HttpStatus.OK).body(response)와 동일
     * - 200 OK 상태 코드 자동 설정
     *
     * 면접 질문: "@PathVariable vs @RequestParam 차이는?"
     * 답변:
     * - @PathVariable: URL 경로의 일부 (/products/{id})
     * - @RequestParam: Query String (/products?id=1)
     * - REST 설계: 리소스 식별은 PathVariable, 필터링/정렬은 RequestParam
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 면접 질문: "Controller에서 예외 처리는 어떻게 하나요?"
     * 답변:
     * 1. Service에서 BusinessException 발생
     * 2. GlobalExceptionHandler가 @ExceptionHandler로 캐치
     * 3. ErrorResponse로 변환하여 클라이언트에 반환
     * 4. Controller는 예외 처리 코드 불필요 (관심사 분리)
     *
     * 면접 질문: "DTO 검증 실패시 어떤 응답이 나가나요?"
     * 답변:
     * 1. @Valid 검증 실패 -> MethodArgumentNotValidException
     * 2. GlobalExceptionHandler가 처리
     * 3. 400 Bad Request + 검증 실패 필드별 에러 메시지
     * 4. 예: {"name": "상품명은 필수입니다", "price": "가격은 0원 이상이어야 합니다"}
     *
     * 면접 질문: "왜 Service를 주입받아 사용하나요?"
     * 답변:
     * 1. 계층 분리: Controller(요청/응답) vs Service(비즈니스 로직)
     * 2. 재사용성: 다른 Controller나 배치에서도 Service 사용 가능
     * 3. 테스트: Service를 Mock으로 대체하여 Controller 단위 테스트 가능
     * 4. 트랜잭션: Service 레벨에서 @Transactional 관리
     */
}
