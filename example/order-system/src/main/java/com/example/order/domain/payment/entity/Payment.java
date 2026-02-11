package com.example.order.domain.payment.entity;

import com.example.order.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idempotency_key"})
})
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private int amount;

    /**
     * 멱등성 키 (Idempotency Key)
     *
     * 클라이언트가 요청마다 고유한 키를 보냄.
     * 같은 키로 두 번 요청하면 → 이미 처리된 결과를 반환 (중복 결제 방지)
     *
     * 왜 필요한가?
     * - 네트워크 타임아웃으로 클라이언트가 응답을 못 받으면 재시도함
     * - 재시도 시 같은 idempotencyKey를 보내면 중복 결제를 막을 수 있음
     *
     * 실무: 카카오페이, 토스 등 모든 결제 API가 멱등성 키를 요구함
     */
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Builder
    public Payment(Long orderId, int amount, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.COMPLETED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
