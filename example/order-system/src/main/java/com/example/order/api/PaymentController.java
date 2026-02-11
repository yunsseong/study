package com.example.order.api;

import com.example.order.domain.payment.dto.PaymentRequest;
import com.example.order.domain.payment.dto.PaymentResponse;
import com.example.order.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 컨트롤러
 *
 * WHY 결제는 멱등성(Idempotency)이 중요한가?
 * 1. 네트워크 재시도: 클라이언트가 타임아웃으로 재시도할 수 있음
 * 2. 중복 결제 방지: 같은 주문에 여러 번 결제되는 것을 방지
 * 3. 사용자 실수: 결제 버튼을 여러 번 클릭할 수 있음
 *
 * WHY idempotencyKey를 클라이언트에서 받는가?
 * - 서버에서 생성하면 재시도 요청을 구분할 수 없음
 * - 클라이언트가 동일한 키로 재시도해야 중복 식별 가능
 * - 클라이언트는 UUID 등으로 고유한 키 생성
 *
 * 면접 포인트:
 * - Idempotency Pattern: 분산 시스템의 필수 패턴
 * - 멱등성 보장: POST도 멱등하게 만들 수 있음
 * - Unique Constraint: DB 레벨에서 중복 방지
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 처리
     *
     * WHY POST를 사용하는가?
     * - 결제는 새로운 리소스(Payment) 생성
     * - RESTful: 생성 작업은 POST 사용
     * - 멱등성: idempotencyKey로 멱등성 보장
     *
     * WHY @Valid를 사용하는가?
     * - PaymentRequest의 검증 애노테이션 활성화
     * - @NotNull, @NotBlank 등 자동 검증
     * - 검증 실패시 400 Bad Request 자동 반환
     *
     * WHY 200 OK를 반환하는가?
     * - 새로운 결제 생성: 201 Created도 가능
     * - 기존 결제 반환: 200 OK (멱등성 처리)
     * - 일관성: 모든 응답을 200으로 통일 (클라이언트 처리 간소화)
     *
     * 면접 질문: "결제 API는 왜 201이 아닌 200을 반환하나요?"
     * 답변:
     * 1. 멱등성 때문: 중복 요청시 기존 결제를 200으로 반환
     * 2. 클라이언트 편의: 201과 200을 구분할 필요 없음
     * 3. 일관성: 신규/기존 모두 동일한 상태 코드로 처리
     * 4. 대안: 신규는 201, 기존은 200 반환 (클라이언트가 구분 가능)
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.pay(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 면접 질문: "결제 중복 요청을 어떻게 처리하나요?"
     * 답변:
     * 1. idempotencyKey로 중복 감지
     * 2. 이미 존재하면 기존 결제 정보 반환 (200 OK)
     * 3. 에러로 처리하지 않음 (클라이언트 재시도는 정상 시나리오)
     * 4. 동시 요청: DB Unique 제약조건으로 하나만 성공
     *
     * 면접 질문: "결제 실패시 어떻게 처리하나요?"
     * 답변:
     * 1. 주문 상태 검증 실패: 400 Bad Request + "결제할 수 없는 주문 상태입니다"
     * 2. 주문 없음: 404 Not Found + "주문을 찾을 수 없습니다"
     * 3. 외부 PG 실패: 500 Internal Server Error + 재시도 큐에 추가
     * 4. 트랜잭션 롤백: 주문 상태는 변경되지 않음
     *
     * 면접 질문: "외부 PG 연동은 어떻게 하나요?"
     * 답변:
     * 1. 실무: 토스페이, 카카오페이 등 외부 API 호출
     * 2. 흐름:
     *    - 클라이언트: 결제 요청 (/api/payments)
     *    - 서버: 외부 PG에 결제 승인 요청
     *    - PG: 승인/거부 응답
     *    - 서버: Payment 엔티티 상태 업데이트
     * 3. 타임아웃 처리: PG 응답이 느리면 비동기 처리
     * 4. 웹훅: PG가 결제 결과를 콜백으로 알려줌
     *
     * 면접 질문: "결제와 주문 상태 변경이 같이 실패하면?"
     * 답변:
     * 1. Monolith: 한 트랜잭션이므로 전체 롤백
     * 2. MSA: Saga Pattern으로 보상 트랜잭션 실행
     *    - 결제 성공, 주문 상태 변경 실패 -> 결제 취소 (보상)
     *    - 오케스트레이터가 순서대로 보상 실행
     * 3. 이벤트 기반: 결제 완료 이벤트 -> 주문 서비스가 상태 변경
     *    - 실패시 재시도 또는 Dead Letter Queue
     *
     * 면접 질문: "POST는 멱등하지 않은데, 어떻게 멱등하게 만드나요?"
     * 답변:
     * 1. HTTP 표준: POST는 비멱등 (여러 번 호출시 여러 리소스 생성)
     * 2. 멱등성 구현:
     *    - idempotencyKey로 중복 요청 식별
     *    - 같은 키는 한 번만 처리, 이후는 기존 결과 반환
     * 3. 실무 필수: 결제, 주문, 송금 등 중요한 작업은 멱등성 필수
     * 4. PUT/DELETE: 기본적으로 멱등 (여러 번 호출해도 결과 동일)
     *
     * 면접 질문: "idempotencyKey는 언제까지 유지하나요?"
     * 답변:
     * 1. 실무: 24시간 ~ 7일 정도
     * 2. 만료 후: 새로운 결제로 처리 또는 에러 반환
     * 3. 관리: 배치 작업으로 만료된 키 삭제
     * 4. 저장소: Redis (TTL 활용) 또는 DB (만료 시간 컬럼)
     */
}
