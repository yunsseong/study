# 선착순 쿠폰 시스템

동시성 문제를 단계적으로 해결하는 과정을 보여주는 Spring Boot 프로젝트입니다.

## 핵심 시나리오

```
100개 한정 쿠폰에 1,000명이 동시에 요청
→ 동시성 제어 없이는 100개 이상 발급되는 Race Condition 발생!
```

## 해결 과정 (4단계)

### Step 1: 기본 구현 (문제 있음)
- `CouponService.java`
- 동시성 제어 없음 → Race Condition 발생
- 테스트: `CouponConcurrencyTest.issue_withoutLock_raceCondition()`

### Step 2: 비관적 락 (Pessimistic Lock)
- `CouponPessimisticLockService.java`
- `SELECT ... FOR UPDATE`로 DB row 락
- 장점: 데이터 정합성 보장
- 단점: DB 부하, 성능 저하
- 테스트: `CouponConcurrencyTest.issue_withPessimisticLock_exactCount()`

### Step 3: Redis 분산 락 (Redisson)
- `CouponRedisLockService.java`
- Redis에서 분산 락 관리 → DB 부하 없음
- 장점: 분산 환경 지원, 높은 성능
- 단점: Redis 인프라 필요

### Step 4: Redis Atomic 연산 (최종)
- `CouponRedisAtomicService.java`
- Redis INCR 원자적 카운터 활용
- 장점: 락 없이 가장 빠름, 초당 수만 건 처리
- 실무: 네카라쿠배 대규모 이벤트에서 사용하는 방식

## 기술 스택

- Java 17, Spring Boot 3.2
- Spring Data JPA, H2/MySQL
- Spring Data Redis, Redisson
- JUnit 5, AssertJ

## 실행 방법

### 테스트 실행 (H2, Redis 없이)
```bash
./gradlew test
```

### Docker로 MySQL + Redis 실행
```bash
docker-compose up -d
```

### 애플리케이션 실행
```bash
./gradlew bootRun
```

## API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/coupons` | 쿠폰 생성 |
| GET | `/api/coupons/{id}` | 쿠폰 조회 |
| POST | `/api/coupons/{id}/issue` | 기본 발급 (Race Condition) |
| POST | `/api/coupons/{id}/issue/pessimistic` | 비관적 락 발급 |
| POST | `/api/coupons/{id}/issue/redis` | Redis 분산 락 발급 |

## 면접 포인트

1. **Race Condition이 왜 발생하는지** 설명할 수 있어야 합니다
2. **각 해결 방법의 장단점**을 비교할 수 있어야 합니다
3. **왜 Redis 분산 락이 실무에서 선호되는지** 설명할 수 있어야 합니다
4. **테스트 코드로 동시성 문제를 증명**할 수 있어야 합니다
