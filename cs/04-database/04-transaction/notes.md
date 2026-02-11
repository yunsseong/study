# 트랜잭션 (Transaction)

## 개념

- **하나의 논리적 작업 단위**를 구성하는 연산들의 집합
- 모두 성공하거나 모두 실패해야 함 (All or Nothing)

```sql
-- 계좌 이체 예시: A → B로 10만원
BEGIN TRANSACTION;
    UPDATE accounts SET balance = balance - 100000 WHERE id = 'A';
    UPDATE accounts SET balance = balance + 100000 WHERE id = 'B';
COMMIT;

-- 중간에 오류 발생 시
ROLLBACK;  -- 전부 취소
```

만약 첫 번째 UPDATE만 성공하고 서버가 죽으면? → ROLLBACK으로 원래 상태 복원

---

## ACID 속성 (면접 핵심)

| 속성 | 의미 | 설명 |
|------|------|------|
| **A** (Atomicity) | 원자성 | 전부 성공 또는 전부 실패 |
| **C** (Consistency) | 일관성 | 트랜잭션 전후 DB 상태가 일관적 |
| **I** (Isolation) | 격리성 | 동시 트랜잭션이 서로 영향을 주지 않음 |
| **D** (Durability) | 지속성 | 커밋된 데이터는 영구적으로 보존 |

### 각 속성 상세

```
Atomicity (원자성):
  이체 중 오류 → 출금만 되고 입금 안 됨 방지
  → ROLLBACK으로 전부 취소

Consistency (일관성):
  이체 전 총합 = 이체 후 총합
  → 제약 조건(FK, UNIQUE, CHECK)이 항상 만족

Isolation (격리성):
  A가 이체하는 동안 B가 같은 계좌를 조회하면?
  → 격리 수준에 따라 다르게 동작

Durability (지속성):
  커밋 완료 후 서버가 죽어도 데이터는 살아있음
  → WAL(Write-Ahead Logging)로 보장
```

---

## 트랜잭션 격리 수준 (Isolation Level)

동시에 여러 트랜잭션이 실행될 때 **얼마나 격리할 것인가**.

### 동시성 문제 3가지

#### 1. Dirty Read

커밋되지 않은 데이터를 다른 트랜잭션이 읽음.

```
TX1: UPDATE balance = 0 (아직 커밋 안 함)
TX2: SELECT balance → 0을 읽음
TX1: ROLLBACK → balance 원래대로

TX2는 존재하지 않는 데이터(0)를 읽어버림
```

#### 2. Non-Repeatable Read

같은 쿼리를 두 번 실행했는데 결과가 다름.

```
TX1: SELECT balance → 1000
TX2: UPDATE balance = 500 + COMMIT
TX1: SELECT balance → 500  (같은 트랜잭션인데 값이 변함!)
```

#### 3. Phantom Read

같은 조건으로 조회했는데 행의 수가 다름.

```
TX1: SELECT * WHERE age > 20 → 3행
TX2: INSERT (age=25) + COMMIT
TX1: SELECT * WHERE age > 20 → 4행  (유령 행 등장!)
```

### 격리 수준 4단계

| 격리 수준 | Dirty Read | Non-Repeatable Read | Phantom Read | 성능 |
|----------|-----------|-------------------|-------------|------|
| **READ UNCOMMITTED** | O (발생) | O | O | 가장 빠름 |
| **READ COMMITTED** | X (방지) | O | O | 빠름 |
| **REPEATABLE READ** | X | X (방지) | O | 보통 |
| **SERIALIZABLE** | X | X | X (방지) | 가장 느림 |

```
격리 수준 높음 → 데이터 정합성 높음, 동시성(성능) 낮음
격리 수준 낮음 → 데이터 정합성 낮음, 동시성(성능) 높음
```

### 각 DBMS의 기본 격리 수준

| DBMS | 기본 격리 수준 |
|------|--------------|
| **MySQL (InnoDB)** | REPEATABLE READ |
| **PostgreSQL** | READ COMMITTED |
| **Oracle** | READ COMMITTED |

```sql
-- MySQL 격리 수준 확인
SELECT @@transaction_isolation;

-- 격리 수준 변경
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

---

## 락 (Lock)

동시성 문제를 해결하기 위한 메커니즘.

### 공유 락 vs 배타 락

| 락 | 설명 | 동시 접근 |
|-----|------|---------|
| **공유 락 (S Lock)** | 읽기 락, SELECT ... FOR SHARE | 다른 공유 락 허용, 배타 락 거부 |
| **배타 락 (X Lock)** | 쓰기 락, SELECT ... FOR UPDATE | 모든 락 거부 |

```sql
-- 공유 락: 읽기는 가능, 수정은 불가
SELECT * FROM accounts WHERE id = 1 FOR SHARE;

-- 배타 락: 읽기/수정 모두 불가 (다른 트랜잭션에서)
SELECT * FROM accounts WHERE id = 1 FOR UPDATE;
```

### 낙관적 락 vs 비관적 락

| 방식 | 전략 | 구현 | 적합한 상황 |
|------|------|------|-----------|
| **비관적 락** | "충돌이 날 거야" → 미리 잠금 | SELECT FOR UPDATE | 충돌 빈번 |
| **낙관적 락** | "충돌 안 날 거야" → 나중에 확인 | version 컬럼 비교 | 충돌 드묾 |

#### 비관적 락

```sql
BEGIN;
SELECT * FROM products WHERE id = 1 FOR UPDATE;  -- 락 획득
-- 다른 트랜잭션은 이 행에 접근 불가 (대기)
UPDATE products SET stock = stock - 1 WHERE id = 1;
COMMIT;  -- 락 해제
```

#### 낙관적 락

```sql
-- 읽을 때 version 확인
SELECT id, stock, version FROM products WHERE id = 1;
-- stock=10, version=3

-- 수정할 때 version 비교
UPDATE products
SET stock = stock - 1, version = version + 1
WHERE id = 1 AND version = 3;
-- 영향받은 행 = 0이면? → 누군가 먼저 수정함 → 재시도
```

```java
// JPA 낙관적 락
@Entity
public class Product {
    @Id
    private Long id;
    private int stock;

    @Version  // 낙관적 락 자동 적용
    private Long version;
}
```

---

## 데드락 (DB에서)

```sql
-- TX1
UPDATE accounts SET balance = balance - 100 WHERE id = 1;  -- row 1 락
UPDATE accounts SET balance = balance + 100 WHERE id = 2;  -- row 2 대기

-- TX2 (동시)
UPDATE accounts SET balance = balance - 50 WHERE id = 2;   -- row 2 락
UPDATE accounts SET balance = balance + 50 WHERE id = 1;   -- row 1 대기

-- 데드락!
```

**해결법**:
- 테이블/행 접근 순서 통일 (항상 id 오름차순)
- 트랜잭션을 짧게 유지
- MySQL InnoDB: 자동 데드락 탐지 → 한쪽 ROLLBACK

---

## Spring에서의 트랜잭션

```java
@Service
public class AccountService {

    @Transactional
    public void transfer(Long fromId, Long toId, int amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.withdraw(amount);
        to.deposit(amount);
        // 메서드 종료 시 자동 COMMIT
        // 예외 발생 시 자동 ROLLBACK
    }
}
```

### @Transactional 전파 속성 (Propagation)

| 속성 | 동작 |
|------|------|
| **REQUIRED** (기본) | 기존 트랜잭션 있으면 참여, 없으면 새로 생성 |
| REQUIRES_NEW | 항상 새 트랜잭션 생성 (기존 것은 일시 중단) |
| NESTED | 기존 트랜잭션 내에 중첩 트랜잭션 (SAVEPOINT) |
| SUPPORTS | 기존 트랜잭션 있으면 참여, 없으면 트랜잭션 없이 실행 |

### 주의사항

```java
// ❌ 같은 클래스 내부 호출 → @Transactional 무시 (프록시 문제)
public class OrderService {
    public void order() {
        this.saveOrder();  // @Transactional이 적용 안 됨!
    }

    @Transactional
    public void saveOrder() { ... }
}

// ✅ 다른 빈을 통해 호출
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public void order() {
        orderRepository.save(order);
    }
}
```

---

## 면접 예상 질문

1. **ACID란?**
   - 원자성, 일관성, 격리성, 지속성

2. **트랜잭션 격리 수준 4가지를 설명해주세요**
   - READ UNCOMMITTED → READ COMMITTED → REPEATABLE READ → SERIALIZABLE

3. **Dirty Read, Non-Repeatable Read, Phantom Read의 차이는?**
   - Dirty: 커밋 안 된 데이터 / Non-Repeatable: 값 변경 / Phantom: 행 수 변경

4. **낙관적 락과 비관적 락의 차이는?**
   - 비관적: 미리 잠금 (FOR UPDATE) / 낙관적: version 비교

5. **@Transactional의 전파 속성이란?**
   - 트랜잭션이 중첩될 때의 동작 방식 (REQUIRED, REQUIRES_NEW 등)

6. **MySQL의 기본 격리 수준은?**
   - REPEATABLE READ
