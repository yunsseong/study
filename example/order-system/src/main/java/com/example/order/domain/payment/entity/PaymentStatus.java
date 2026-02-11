package com.example.order.domain.payment.entity;

public enum PaymentStatus {
    COMPLETED("결제 완료"),
    CANCELLED("결제 취소");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
