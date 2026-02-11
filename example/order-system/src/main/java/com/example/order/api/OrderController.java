package com.example.order.api;

import com.example.order.domain.order.dto.OrderCreateRequest;
import com.example.order.domain.order.dto.OrderResponse;
import com.example.order.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 컨트롤러
 *
 * WHY RESTful API 설계 원칙을 따르는가?
 * 1. 리소스 기반 URL: /api/orders (명사 사용)
 * 2. HTTP 메서드 활용: POST(생성), GET(조회), PUT/PATCH(수정), DELETE(삭제)
 * 3. 계층 구조: /api/orders/{id}, /api/orders/users/{userId}
 * 4. 상태 코드: 201(생성), 200(조회), 400(검증 실패), 404(미존재)
 *
 * 면접 포인트:
 * - REST API 설계 Best Practice
 * - HTTP 메서드의 의미: POST(생성), GET(조회), PUT(전체 수정), PATCH(부분 수정)
 * - 멱등성: GET, PUT, DELETE는 멱등, POST는 비멱등
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     *
     * WHY @Valid를 사용하는가?
     * - OrderCreateRequest의 검증 애노테이션 활성화
     * - items 내부의 OrderItemRequest도 @Valid로 검증됨 (중첩 검증)
     * - 검증 실패시 자동으로 400 Bad Request 반환
     *
     * WHY 201 Created를 반환하는가?
     * - RESTful 원칙: 리소스 생성 성공시 201 반환
     * - Location Header 추가 가능: 생성된 주문의 URL (/api/orders/{id})
     *
     * 면접 질문: "주문 생성이 실패하면 어떻게 되나요?"
     * 답변:
     * 1. 재고 부족: BusinessException -> 400 Bad Request + "재고가 부족합니다"
     * 2. 상품 없음: BusinessException -> 404 Not Found + "상품을 찾을 수 없습니다"
     * 3. 검증 실패: MethodArgumentNotValidException -> 400 + 필드별 에러 메시지
     * 4. 트랜잭션 롤백: 주문과 재고 감소가 모두 취소됨 (원자성)
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 주문 조회
     *
     * WHY @PathVariable Long id를 사용하는가?
     * - URL 경로에서 주문 ID 추출
     * - RESTful 설계: /api/orders/1, /api/orders/2
     *
     * WHY ResponseEntity.ok()를 사용하는가?
     * - 200 OK 상태 코드와 response body를 함께 반환
     * - 간결한 표현
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderResponse response = orderService.getOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 주문 목록 조회
     *
     * WHY /users/{userId}를 사용하는가?
     * - RESTful 계층 구조: 사용자의 주문 목록
     * - 의미 명확화: "사용자의 주문들" 을 URL로 표현
     *
     * WHY List<OrderResponse>를 반환하는가?
     * - 여러 주문을 조회하므로 List 반환
     * - JSON 배열로 자동 변환: [{order1}, {order2}, ...]
     *
     * 면접 질문: "페이징 처리는 어떻게 하나요?"
     * 답변:
     * 1. Spring Data JPA의 Pageable 사용
     * 2. 파라미터: ?page=0&size=10&sort=createdAt,desc
     * 3. 반환 타입: Page<OrderResponse> (totalElements, totalPages 포함)
     * 4. Repository: Page<Order> findByUserId(Long userId, Pageable pageable)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> responses = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 주문 취소
     *
     * WHY POST를 사용하는가?
     * - 주문 취소는 상태 변경 작업 (action)
     * - RESTful에서 action은 POST 사용
     * - DELETE는 리소스 삭제용 (주문 자체를 삭제하는 것이 아님)
     *
     * WHY /{id}/cancel 경로를 사용하는가?
     * - 주문 리소스에 대한 취소 액션 표현
     * - RESTful 설계: POST /orders/{id}/cancel
     * - 대안: PATCH /orders/{id} with {"status": "CANCELLED"}
     *
     * 면접 질문: "POST vs PATCH 어떤 것을 사용해야 하나요?"
     * 답변:
     * - POST /orders/{id}/cancel: 취소 액션을 명시적으로 표현 (권장)
     * - PATCH /orders/{id}: 상태만 변경 (보상 트랜잭션 로직 누락 가능)
     * - POST 장점: 비즈니스 로직(재고 복원 등)을 명확히 표현
     * - PATCH 장점: RESTful하게 리소스 상태만 변경
     *
     * 면접 질문: "주문 취소가 실패하면 어떻게 되나요?"
     * 답변:
     * 1. 이미 배송된 주문: BusinessException -> 400 + "배송된 주문은 취소할 수 없습니다"
     * 2. 재고 복원 실패: 전체 트랜잭션 롤백 (주문 상태도 원복)
     * 3. 동시 취소 시도: 낙관적 락 충돌 -> 재시도 또는 실패
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 면접 질문: "Controller에서 비즈니스 로직을 처리해도 되나요?"
     * 답변:
     * 1. NO: Controller는 요청/응답 처리만 담당
     * 2. 계층 분리: Controller -> Service -> Repository
     * 3. Controller 역할:
     *    - HTTP 요청 파싱 (@RequestBody, @PathVariable)
     *    - DTO 검증 (@Valid)
     *    - Service 호출
     *    - HTTP 응답 생성 (ResponseEntity)
     * 4. Service 역할:
     *    - 비즈니스 로직 실행
     *    - 트랜잭션 관리 (@Transactional)
     *    - 이벤트 발행
     *
     * 면접 질문: "ResponseEntity를 사용하는 이유는?"
     * 답변:
     * 1. HTTP 상태 코드 제어: 201, 200, 404 등
     * 2. Header 추가 가능: Location, Content-Type 등
     * 3. 유연성: body, status, headers를 모두 제어 가능
     * 4. 대안: @ResponseStatus 애노테이션 (상태 코드 고정)
     *
     * 면접 질문: "API 버저닝은 어떻게 하나요?"
     * 답변:
     * 1. URL 버저닝: /api/v1/orders, /api/v2/orders
     * 2. Header 버저닝: Accept: application/vnd.company.v1+json
     * 3. Query Parameter: /api/orders?version=1
     * 4. 실무 권장: URL 버저닝 (명확하고 간단)
     */
}
