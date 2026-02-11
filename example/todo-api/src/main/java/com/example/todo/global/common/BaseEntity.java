package com.example.todo.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * [설계 포인트] BaseEntity - 모든 엔티티의 공통 필드
 *
 * 실무에서 거의 모든 테이블에는 생성일시/수정일시가 있다.
 * 이걸 매번 엔티티마다 작성하면 중복이니, 부모 클래스로 뽑아낸다.
 *
 * @MappedSuperclass: 이 클래스는 테이블이 되지 않고,
 *   자식 엔티티가 이 필드들을 상속받아 자기 테이블에 컬럼으로 갖는다.
 *
 * @EntityListeners(AuditingEntityListener.class):
 *   엔티티가 저장/수정될 때 자동으로 시간을 채워준다.
 *   개발자가 직접 LocalDateTime.now()를 호출할 필요가 없다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)  // 생성일시는 한번 저장되면 변경 불가
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
