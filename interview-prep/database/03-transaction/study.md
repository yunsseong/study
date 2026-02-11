# 3. 트랜잭션 (Transaction)

---

## 트랜잭션이란?

트랜잭션은 **하나의 논리적 작업 단위를 구성하는 연산들의 집합**이다.
모두 성공하거나, 모두 실패해야 한다. 중간 상태는 없다.

```
은행 계좌이체 예시:

  A 계좌에서 100만원 출금  ─┐
                            ├→ 하나의 트랜잭션
  B 계좌에 100만원 입금    ─┘

  Case 1: 출금 성공, 입금 성공 → COMMIT (확정)
  Case 2: 출금 성공, 입금 실패 → ROLLBACK (출금도 취소)
  Case 3: 출금 실패          → ROLLBACK (아무 일도 없었던 것처럼)
```

```sql
START TRANSACTION;

UPDATE accounts SET balance = balance - 1000000 WHERE id = 'A';
UPDATE accounts SET balance = balance + 1000000 WHERE id = 'B';

-- 둘 다 성공하면
COMMIT;

-- 하나라도 실패하면
ROLLBACK;
```

---

## ACID 속성

트랜잭션이 보장해야 하는 **4가지 핵심 속성**이다.

### Atomicity (원자성)

**전부 성공하거나 전부 실패**한다. 중간 상태 없음.

```
원자(Atom) = 더 이상 나눌 수 없는 단위

  출금 O + 입금 O → COMMIT  (전부 성공)
  출금 O + 입금 X → ROLLBACK (전부 취소, 출금도 되돌림)
```

### Consistency (일관성)

트랜잭션 전후로 **데이터베이스의 무결성 제약 조건이 유지**된다.

```
제약 조건: "계좌 잔액은 0 이상이어야 한다"

  A 잔액: 50만원
  A에서 100만원 출금 시도 → 잔액 -50만원 → 제약 조건 위반 → ROLLBACK
```

### Isolation (격리성)

**동시에 실행되는 트랜잭션들이 서로 영향을 주지 않는다**.
각 트랜잭션은 자신만 실행되는 것처럼 보여야 한다.

```
트랜잭션 1: A 잔액 조회 → 100만원
                              트랜잭션 2: A 잔액을 50만원으로 변경
트랜잭션 1: A 잔액 다시 조회 → ???

격리 수준에 따라 100만원 또는 50만원이 보인다 (아래에서 자세히 설명)
```

### Durability (지속성)

트랜잭션이 **COMMIT되면 영구적으로 저장**된다.
시스템 장애가 발생해도 데이터가 보존된다.

```
COMMIT 완료
  ↓
서버 전원 꺼짐 (장애 발생)
  ↓
서버 재시작
  ↓
COMMIT된 데이터 그대로 존재 (WAL/Redo Log 덕분)
```

### ACID 한눈에

| 속성 | 의미 | 한 줄 정리 |
|------|------|-----------|
| Atomicity | 원자성 | All or Nothing |
| Consistency | 일관성 | 제약 조건 항상 유지 |
| Isolation | 격리성 | 트랜잭션 간 간섭 없음 |
| Durability | 지속성 | COMMIT = 영구 저장 |

---

## 트랜잭션 격리 수준 (Isolation Level)

격리성을 **얼마나 강하게 보장할 것인가**에 대한 4단계 수준이다.
격리 수준이 높을수록 안전하지만 **성능은 떨어진다** (동시성 저하).

### 격리 수준별 문제 현상

| 격리 수준 | Dirty Read | Non-Repeatable Read | Phantom Read |
|-----------|------------|---------------------|--------------|
| READ UNCOMMITTED | O | O | O |
| READ COMMITTED | X | O | O |
| REPEATABLE READ | X | X | O (InnoDB는 X) |
| SERIALIZABLE | X | X | X |

### 문제 현상 설명

#### Dirty Read (더티 리드)

**커밋되지 않은 데이터를 다른 트랜잭션이 읽는 것**.

```
트랜잭션 A                           트랜잭션 B
─────────                           ─────────
UPDATE salary SET amount=500
WHERE id=1;
(아직 COMMIT 안 함)
                                    SELECT amount FROM salary
                                    WHERE id=1;
                                    → 500 읽음 (커밋 안 된 데이터!)

ROLLBACK;
(500은 취소됨)
                                    → 이미 500을 기반으로 작업함
                                      (존재하지 않는 데이터를 읽은 것!)
```

#### Non-Repeatable Read (반복 불가능 읽기)

**같은 쿼리를 두 번 실행했는데 결과가 다른 것** (다른 트랜잭션이 수정+커밋).

```
트랜잭션 A                           트랜잭션 B
─────────                           ─────────
SELECT amount FROM salary
WHERE id=1;
→ 300 읽음
                                    UPDATE salary SET amount=500
                                    WHERE id=1;
                                    COMMIT;

SELECT amount FROM salary
WHERE id=1;
→ 500 읽음 (같은 쿼리인데 결과가 달라짐!)
```

#### Phantom Read (팬텀 리드)

**같은 조건으로 조회했는데 행의 수가 달라지는 것** (다른 트랜잭션이 삽입+커밋).

```
트랜잭션 A                           트랜잭션 B
─────────                           ─────────
SELECT * FROM employees
WHERE department = '개발팀';
→ 3건 조회
                                    INSERT INTO employees
                                    (name, department)
                                    VALUES ('신입', '개발팀');
                                    COMMIT;

SELECT * FROM employees
WHERE department = '개발팀';
→ 4건 조회 (유령처럼 행이 나타남!)
```

### 격리 수준 상세 설명

#### 1. READ UNCOMMITTED (가장 낮은 격리)

```
다른 트랜잭션의 커밋되지 않은 변경사항도 읽을 수 있다.
→ Dirty Read 발생 가능
→ 실무에서 거의 사용하지 않음
```

#### 2. READ COMMITTED (Oracle 기본)

```
커밋된 데이터만 읽는다.
→ Dirty Read 해결
→ Non-Repeatable Read 여전히 발생
→ Oracle, PostgreSQL의 기본 격리 수준
```

#### 3. REPEATABLE READ (MySQL InnoDB 기본)

```
트랜잭션 시작 시점의 스냅샷을 기준으로 읽는다.
→ Dirty Read 해결
→ Non-Repeatable Read 해결
→ MySQL InnoDB는 Gap Lock으로 Phantom Read도 방지!
→ MySQL의 기본 격리 수준
```

#### 4. SERIALIZABLE (가장 높은 격리)

```
트랜잭션을 순차적으로 실행하는 것처럼 동작한다.
→ 모든 문제 해결
→ 동시성이 매우 떨어져 성능 저하
→ 특수한 경우(금융 시스템 등)에만 사용
```

```
격리 수준 트레이드오프:

  안전성                          성능(동시성)
  높음  SERIALIZABLE              낮음
   |    REPEATABLE READ ← MySQL 기본
   |    READ COMMITTED  ← Oracle 기본
  낮음  READ UNCOMMITTED          높음
```

---

## MySQL InnoDB의 기본 격리 수준

MySQL InnoDB는 **REPEATABLE READ**가 기본이다.

### MVCC (Multi-Version Concurrency Control)

InnoDB는 MVCC를 사용하여 **잠금 없이 일관된 읽기**를 제공한다.

```
MVCC 동작 원리:

  데이터 변경 시 이전 버전을 Undo Log에 보관

  트랜잭션 A (시작: T=100)         저장소
  ─────────────────               ──────
                                  id=1, amount=300, version=T90

  트랜잭션 B가 amount를 500으로 변경 (T=110)
                                  id=1, amount=500, version=T110
                                  Undo: id=1, amount=300, version=T90

  트랜잭션 A가 다시 조회
  → T=100 시점 스냅샷 사용
  → Undo Log에서 version=T90 데이터를 읽음
  → 300이 보임 (일관성 유지!)
```

### Gap Lock (갭 락)

InnoDB의 REPEATABLE READ에서 **Phantom Read를 방지**하는 방법이다.

```
인덱스 값 사이의 "간격"을 잠근다:

  인덱스: [10] [20] [30] [40]

  SELECT * FROM t WHERE id BETWEEN 20 AND 30 FOR UPDATE;

  잠금 범위: [20] ~~gap~~ [30]
  → 이 사이에 새로운 행 삽입 불가
  → Phantom Read 방지!
```

---

## Spring @Transactional 동작 원리

### 기본 사용법

```java
@Service
public class AccountService {

    @Transactional
    public void transfer(Long fromId, Long toId, Long amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.withdraw(amount);   // 출금
        to.deposit(amount);      // 입금

        // 메서드 정상 종료 → 자동 COMMIT
        // 예외 발생 → 자동 ROLLBACK
    }
}
```

### @Transactional 프록시 동작 방식

```
Spring은 AOP 프록시를 통해 트랜잭션을 관리한다.

  호출자 → [프록시] → 실제 Service

  [프록시 동작]:
  1. 트랜잭션 시작 (BEGIN)
  2. 실제 메서드 호출
  3-1. 정상 종료 → COMMIT
  3-2. RuntimeException → ROLLBACK
  3-3. Checked Exception → COMMIT (기본값!)
```

### 주의할 점: 같은 클래스 내부 호출

```java
@Service
public class OrderService {

    // 트랜잭션 적용됨
    @Transactional
    public void createOrder() {
        // ...
    }

    // 문제: 같은 클래스에서 내부 호출하면 프록시를 안 거침!
    public void processOrder() {
        createOrder();  // @Transactional이 동작하지 않음!
    }
}
```

```
외부에서 호출:
  외부 → [프록시] → createOrder()  ← 트랜잭션 O

내부에서 호출:
  processOrder() → createOrder()   ← 프록시를 안 거침, 트랜잭션 X
```

> **해결 방법**: 별도의 클래스로 분리하거나, `self-injection`, 또는 `TransactionTemplate`을 사용한다.

### @Transactional 주요 옵션

```java
@Transactional(
    readOnly = true,                          // 읽기 전용 (성능 최적화)
    isolation = Isolation.READ_COMMITTED,      // 격리 수준
    propagation = Propagation.REQUIRED,        // 전파 속성 (기본값)
    rollbackFor = Exception.class,             // Checked Exception도 롤백
    timeout = 30                               // 타임아웃 (초)
)
```

### 전파 속성 (Propagation)

| 속성 | 설명 |
|------|------|
| REQUIRED (기본) | 기존 트랜잭션 있으면 참여, 없으면 새로 생성 |
| REQUIRES_NEW | 항상 새 트랜잭션 생성 (기존 트랜잭션 일시 중단) |
| NESTED | 기존 트랜잭션 안에서 중첩 트랜잭션 (Savepoint) |
| SUPPORTS | 기존 트랜잭션 있으면 참여, 없으면 트랜잭션 없이 실행 |
| NOT_SUPPORTED | 트랜잭션 없이 실행 (기존 트랜잭션 일시 중단) |

```java
@Service
public class OrderService {

    @Transactional  // REQUIRED (기본)
    public void createOrder() {
        orderRepository.save(order);
        paymentService.processPayment();  // 같은 트랜잭션에 참여
        logService.saveLog();             // 로그 저장도 같은 트랜잭션
    }
}

@Service
public class LogService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog() {
        // 별도 트랜잭션으로 실행
        // 주문이 실패해도 로그는 저장됨
    }
}
```

---

## 낙관적 락 vs 비관적 락

동시에 같은 데이터를 수정할 때 **충돌을 어떻게 처리할 것인가**의 두 가지 전략이다.

### 비관적 락 (Pessimistic Lock)

**"충돌이 날 거야"** 라고 가정하고, 데이터를 **미리 잠근다**.

```
트랜잭션 A                           트랜잭션 B
─────────                           ─────────
SELECT * FROM products
WHERE id=1 FOR UPDATE;
→ 행 잠금 획득 (Lock)
                                    SELECT * FROM products
                                    WHERE id=1 FOR UPDATE;
                                    → 대기... (Lock 해제 대기)

UPDATE products SET stock=9
WHERE id=1;
COMMIT;
→ Lock 해제
                                    → 이제 Lock 획득
                                    → stock=9 읽음
                                    → 안전하게 처리
```

```java
// JPA에서 비관적 락
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdWithLock(@Param("id") Long id);
}
```

### 낙관적 락 (Optimistic Lock)

**"충돌이 안 날 거야"** 라고 가정하고, 커밋 시점에 충돌을 감지한다.
**@Version** 필드를 사용한다.

```
트랜잭션 A                           트랜잭션 B
─────────                           ─────────
SELECT * FROM products
WHERE id=1;
→ stock=10, version=1

                                    SELECT * FROM products
                                    WHERE id=1;
                                    → stock=10, version=1

UPDATE products
SET stock=9, version=2
WHERE id=1 AND version=1;
→ 성공 (version 1→2)
COMMIT;
                                    UPDATE products
                                    SET stock=9, version=2
                                    WHERE id=1 AND version=1;
                                    → 실패! (version이 이미 2)
                                    → OptimisticLockException 발생
```

```java
@Entity
public class Product {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private Integer stock;

    @Version  // 낙관적 락 버전 필드
    private Long version;
}

// 충돌 발생 시 재시도 로직
@Service
public class ProductService {

    @Retryable(value = OptimisticLockException.class, maxAttempts = 3)
    @Transactional
    public void decreaseStock(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.decreaseStock();
        // 저장 시 version 불일치하면 OptimisticLockException 발생
    }
}
```

### 비교 정리

| 구분 | 비관적 락 | 낙관적 락 |
|------|----------|----------|
| 가정 | 충돌이 자주 발생 | 충돌이 거의 없음 |
| 방식 | DB 레벨 Lock (SELECT FOR UPDATE) | 버전 번호 비교 (@Version) |
| 장점 | 데이터 정합성 확실 | 동시성 높음, Lock 대기 없음 |
| 단점 | 동시성 저하, 데드락 위험 | 충돌 시 재시도 필요 |
| 적합한 경우 | 재고 차감, 좌석 예매 등 충돌 많은 경우 | 게시글 수정 등 충돌 적은 경우 |
| JPA | @Lock(PESSIMISTIC_WRITE) | @Version |

```
선택 기준:

  충돌 빈도 높은가?
  ├─ YES → 비관적 락 (SELECT FOR UPDATE)
  │        예: 선착순 이벤트, 재고 차감, 좌석 예매
  └─ NO  → 낙관적 락 (@Version)
           예: 게시글 수정, 프로필 업데이트, 설정 변경
```

---

## 면접 핵심 정리

**Q: 트랜잭션의 ACID를 설명해주세요**
> Atomicity는 트랜잭션 내 작업이 모두 성공하거나 모두 실패하는 것입니다.
> Consistency는 트랜잭션 전후로 데이터베이스 무결성 제약 조건이 유지되는 것입니다.
> Isolation은 동시에 실행되는 트랜잭션이 서로 영향을 주지 않는 것입니다.
> Durability는 커밋된 데이터가 영구적으로 보존되는 것입니다.

**Q: 트랜잭션 격리 수준 4가지를 설명해주세요**
> READ UNCOMMITTED는 커밋 안 된 데이터도 읽을 수 있어 Dirty Read가 발생합니다.
> READ COMMITTED는 커밋된 데이터만 읽어 Dirty Read를 방지하지만 Non-Repeatable Read가 발생합니다.
> REPEATABLE READ는 트랜잭션 시작 시점의 스냅샷을 읽어 반복 읽기가 일관됩니다.
> SERIALIZABLE은 완전한 격리를 보장하지만 동시성이 크게 떨어집니다.
> MySQL InnoDB의 기본은 REPEATABLE READ이고, MVCC와 Gap Lock으로 Phantom Read까지 방지합니다.

**Q: Spring @Transactional의 동작 원리는?**
> Spring은 AOP 프록시를 통해 트랜잭션을 관리합니다.
> 프록시가 메서드 호출 전에 트랜잭션을 시작하고, 정상 종료 시 COMMIT, RuntimeException 발생 시 ROLLBACK합니다.
> 주의할 점은 같은 클래스 내부에서 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않습니다.
> Checked Exception은 기본적으로 ROLLBACK되지 않으므로 rollbackFor 설정이 필요합니다.

**Q: 낙관적 락과 비관적 락의 차이는?**
> 비관적 락은 충돌을 미리 방지하기 위해 SELECT FOR UPDATE로 행을 잠급니다.
> 충돌이 잦은 경우(재고 차감 등)에 적합하지만 동시성이 떨어지고 데드락 위험이 있습니다.
> 낙관적 락은 @Version 필드로 커밋 시점에 충돌을 감지합니다.
> 충돌이 드문 경우(게시글 수정 등)에 적합하고 동시성이 높지만 충돌 시 재시도가 필요합니다.

**Q: MVCC란 무엇인가요?**
> Multi-Version Concurrency Control의 약자로, 데이터 변경 시 이전 버전을 Undo Log에 보관하여
> 읽기 트랜잭션이 잠금 없이도 일관된 스냅샷을 읽을 수 있게 하는 방식입니다.
> MySQL InnoDB의 REPEATABLE READ에서 사용되며, 읽기와 쓰기가 서로 차단하지 않아
> 동시성을 높이면서도 격리성을 보장합니다.
