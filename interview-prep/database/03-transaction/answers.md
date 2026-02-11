# 트랜잭션(Transaction) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** 트랜잭션이란 무엇인가요? 예시를 들어 설명해주세요.

> 트랜잭션은 하나의 논리적 작업 단위를 구성하는 연산들의 집합입니다. 모두 성공하거나, 모두 실패해야 하며 중간 상태는 없습니다. 대표적인 예시는 은행 계좌이체입니다. A 계좌에서 100만원을 출금하고 B 계좌에 100만원을 입금하는 두 연산이 하나의 트랜잭션입니다. 두 연산이 모두 성공하면 COMMIT으로 확정하고, 출금은 성공했지만 입금이 실패한 경우 ROLLBACK으로 출금도 취소하여 데이터 정합성을 유지합니다.

**Q2.** ACID 속성에 대해 각각 설명해주세요.

> ACID는 트랜잭션이 보장해야 하는 4가지 핵심 속성입니다. Atomicity(원자성)는 트랜잭션 내 작업이 전부 성공하거나 전부 실패하는 것입니다. Consistency(일관성)는 트랜잭션 전후로 데이터베이스의 무결성 제약 조건이 항상 유지되는 것입니다. 예를 들어 "잔액은 0 이상"이라는 제약이 위반되면 트랜잭션이 롤백됩니다. Isolation(격리성)은 동시에 실행되는 트랜잭션들이 서로 영향을 주지 않는 것입니다. 각 트랜잭션은 자신만 실행되는 것처럼 보여야 합니다. Durability(지속성)는 커밋된 데이터가 시스템 장애가 발생해도 영구적으로 보존되는 것이며, WAL(Write-Ahead Log)이나 Redo Log를 통해 보장됩니다.

**Q3.** 트랜잭션 격리 수준 4가지를 설명해주세요.

> READ UNCOMMITTED는 커밋되지 않은 데이터도 읽을 수 있어 Dirty Read가 발생하며, 실무에서 거의 사용하지 않습니다. READ COMMITTED는 커밋된 데이터만 읽어 Dirty Read를 방지하지만, 같은 쿼리를 두 번 실행했을 때 결과가 달라지는 Non-Repeatable Read가 발생할 수 있습니다. Oracle의 기본 격리 수준입니다. REPEATABLE READ는 트랜잭션 시작 시점의 스냅샷을 기준으로 읽어 반복 읽기가 일관되며, MySQL InnoDB의 기본 격리 수준입니다. InnoDB는 Gap Lock으로 Phantom Read까지 방지합니다. SERIALIZABLE은 트랜잭션을 순차적으로 실행하여 모든 문제를 해결하지만 동시성이 크게 떨어집니다.

**Q4.** Dirty Read, Non-Repeatable Read, Phantom Read의 차이를 설명해주세요.

> Dirty Read는 다른 트랜잭션이 아직 커밋하지 않은 데이터를 읽는 것입니다. 해당 트랜잭션이 ROLLBACK하면 존재하지 않는 데이터를 읽은 것이 됩니다. Non-Repeatable Read는 같은 쿼리를 두 번 실행했는데 다른 트랜잭션이 중간에 데이터를 수정하고 커밋하여 결과 값이 달라지는 것입니다. Phantom Read는 같은 조건으로 조회했는데 다른 트랜잭션이 새로운 행을 삽입하고 커밋하여 행의 수가 달라지는 것입니다. 유령처럼 없던 행이 나타나는 현상이라 팬텀 리드라고 합니다.

**Q5.** MySQL InnoDB의 기본 격리 수준은 무엇이고, 왜 그 수준을 사용하나요?

> MySQL InnoDB의 기본 격리 수준은 REPEATABLE READ입니다. MVCC(Multi-Version Concurrency Control)를 사용하여 잠금 없이도 일관된 읽기를 제공합니다. 데이터 변경 시 이전 버전을 Undo Log에 보관하고, 읽기 트랜잭션은 자신의 시작 시점 스냅샷을 기준으로 데이터를 읽으므로 Non-Repeatable Read를 방지합니다. 또한 Gap Lock을 통해 인덱스 값 사이의 간격을 잠가 새로운 행 삽입을 막아 Phantom Read까지 방지합니다. 이로써 SERIALIZABLE 수준의 안전성에 가까우면서도 높은 동시성을 유지할 수 있습니다.

## 비교/구분 (6~9)

**Q6.** 낙관적 락과 비관적 락의 차이를 설명해주세요. 각각 어떤 상황에 적합한가요?

> 비관적 락은 "충돌이 날 것"이라 가정하고 SELECT FOR UPDATE로 데이터를 미리 잠급니다. 다른 트랜잭션은 Lock이 해제될 때까지 대기해야 합니다. 데이터 정합성이 확실하지만 동시성이 떨어지고 데드락 위험이 있습니다. 재고 차감, 좌석 예매 등 충돌이 자주 발생하는 경우에 적합합니다. 낙관적 락은 "충돌이 안 날 것"이라 가정하고 @Version 필드를 사용하여 커밋 시점에 충돌을 감지합니다. Lock 대기가 없어 동시성이 높지만, 충돌 발생 시 OptimisticLockException이 발생하여 재시도가 필요합니다. 게시글 수정, 프로필 업데이트 등 충돌이 드문 경우에 적합합니다.

**Q7.** READ COMMITTED와 REPEATABLE READ의 차이는 무엇인가요?

> READ COMMITTED는 다른 트랜잭션이 커밋한 데이터를 바로 반영하여 읽습니다. 따라서 같은 쿼리를 두 번 실행하면 그 사이에 다른 트랜잭션이 데이터를 수정하고 커밋했다면 다른 결과가 나올 수 있습니다(Non-Repeatable Read). REPEATABLE READ는 트랜잭션 시작 시점의 스냅샷을 기준으로 읽으므로, 트랜잭션 내에서 같은 쿼리를 여러 번 실행해도 항상 같은 결과를 얻습니다. Oracle은 READ COMMITTED를, MySQL InnoDB는 REPEATABLE READ를 기본으로 사용합니다.

**Q8.** @Transactional의 propagation 속성 중 REQUIRED와 REQUIRES_NEW의 차이는 무엇인가요?

> REQUIRED는 기본값으로, 기존 트랜잭션이 있으면 해당 트랜잭션에 참여하고, 없으면 새로 생성합니다. 따라서 호출하는 쪽과 같은 트랜잭션으로 묶여 하나가 실패하면 전체가 롤백됩니다. REQUIRES_NEW는 항상 새로운 트랜잭션을 생성하며, 기존 트랜잭션은 일시 중단됩니다. 따라서 호출하는 쪽의 트랜잭션과 독립적으로 동작합니다. 예를 들어 주문 처리 중 로그 저장에 REQUIRES_NEW를 사용하면, 주문이 실패해도 로그는 별도 트랜잭션이므로 저장됩니다.

**Q9.** Checked Exception과 Unchecked Exception에서 @Transactional의 롤백 동작은 어떻게 다른가요?

> Spring @Transactional은 기본적으로 RuntimeException(Unchecked Exception)과 Error가 발생하면 자동으로 ROLLBACK합니다. 하지만 Checked Exception(IOException, SQLException 등)이 발생하면 기본적으로 COMMIT됩니다. 이는 Spring이 Checked Exception을 비즈니스 로직의 정상적인 흐름으로 간주하기 때문입니다. Checked Exception에서도 롤백하려면 `@Transactional(rollbackFor = Exception.class)`처럼 rollbackFor 속성을 명시적으로 설정해야 합니다. 이 점을 모르면 실무에서 데이터 정합성 문제가 발생할 수 있으므로 주의해야 합니다.

## 심화/실무 (10~12)

**Q10.** Spring @Transactional의 동작 원리를 설명해주세요. 프록시 기반으로 동작할 때 주의할 점은 무엇인가요?

> Spring은 AOP 프록시를 통해 트랜잭션을 관리합니다. 외부에서 @Transactional 메서드를 호출하면 프록시가 가로채서 트랜잭션을 시작(BEGIN)하고, 메서드가 정상 종료되면 COMMIT, RuntimeException이 발생하면 ROLLBACK합니다. 주의할 점은 같은 클래스 내부에서 @Transactional 메서드를 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않는 것입니다. 예를 들어 processOrder() 메서드 내에서 같은 클래스의 createOrder() 메서드를 호출하면 createOrder()의 @Transactional이 동작하지 않습니다. 이를 해결하려면 별도 클래스로 분리하거나, self-injection, 또는 TransactionTemplate을 사용해야 합니다.

**Q11.** MVCC(Multi-Version Concurrency Control)란 무엇이고, 어떻게 동작하나요?

> MVCC는 데이터 변경 시 이전 버전을 Undo Log에 보관하여, 읽기 트랜잭션이 잠금 없이도 일관된 스냅샷을 읽을 수 있게 하는 동시성 제어 방식입니다. 예를 들어 트랜잭션 A가 시작 시점 T=100에서 데이터를 읽고, 그 사이 트랜잭션 B가 데이터를 변경하면, 변경 전 데이터가 Undo Log에 보관됩니다. 트랜잭션 A가 다시 같은 데이터를 읽으면 자신의 시작 시점(T=100) 기준으로 Undo Log에서 이전 버전을 찾아 읽습니다. 이를 통해 읽기와 쓰기가 서로 차단하지 않아 동시성을 높이면서도 격리성을 보장합니다.

**Q12.** JPA에서 낙관적 락을 구현하는 방법과 충돌 시 처리 방법을 설명해주세요.

> JPA에서 낙관적 락은 엔티티에 @Version 필드를 추가하여 구현합니다. Long 또는 Integer 타입의 version 필드를 선언하면, JPA가 UPDATE 시 자동으로 `WHERE id = ? AND version = ?` 조건을 추가합니다. 다른 트랜잭션이 먼저 수정하여 version이 변경되면 WHERE 조건이 맞지 않아 OptimisticLockException이 발생합니다. 충돌 시 처리는 보통 재시도 로직으로 구현합니다. Spring Retry의 @Retryable 애노테이션을 사용하여 `@Retryable(value = OptimisticLockException.class, maxAttempts = 3)`처럼 최대 재시도 횟수를 설정할 수 있습니다.

## 꼬리질문 대비 (13~15)

**Q13.** @Transactional(readOnly = true)를 설정하면 어떤 이점이 있나요?

> readOnly = true를 설정하면 세 가지 이점이 있습니다. 첫째, JPA의 영속성 컨텍스트에서 더티 체킹(변경 감지)을 수행하지 않아 메모리와 CPU 사용량이 줄어듭니다. 스냅샷을 저장하지 않으므로 메모리 절약 효과가 있습니다. 둘째, MySQL의 경우 읽기 전용 트랜잭션에 대해 내부적으로 최적화를 수행하여 불필요한 잠금을 줄입니다. 셋째, 코드의 의도를 명확히 표현하여 해당 메서드가 조회 전용임을 개발자에게 알릴 수 있습니다. 조회 전용 서비스 메서드에는 항상 readOnly = true를 설정하는 것이 좋은 습관입니다.

**Q14.** 데드락(Deadlock)이란 무엇이고, 비관적 락 사용 시 데드락을 방지하려면 어떻게 해야 하나요?

> 데드락은 두 개 이상의 트랜잭션이 서로가 보유한 Lock을 기다리며 무한 대기하는 상태입니다. 예를 들어 트랜잭션 A가 행 1을 잠그고 행 2를 기다리는데, 트랜잭션 B가 행 2를 잠그고 행 1을 기다리면 데드락이 발생합니다. 방지 방법으로는 첫째, 자원 접근 순서를 일관되게 유지합니다. 모든 트랜잭션이 같은 순서로 행을 잠그면 데드락을 예방할 수 있습니다. 둘째, 트랜잭션 범위를 최소화하여 Lock 보유 시간을 줄입니다. 셋째, 적절한 타임아웃을 설정합니다. MySQL InnoDB는 데드락을 자동 감지하여 하나의 트랜잭션을 강제 롤백합니다.

**Q15.** Gap Lock이란 무엇이고, InnoDB에서 Phantom Read를 어떻게 방지하나요?

> Gap Lock은 InnoDB의 REPEATABLE READ 격리 수준에서 인덱스 값 사이의 간격(gap)을 잠그는 방식입니다. 예를 들어 인덱스 값이 10, 20, 30, 40일 때 `WHERE id BETWEEN 20 AND 30 FOR UPDATE` 쿼리를 실행하면, 20과 30 사이의 간격이 잠깁니다. 이 잠금으로 인해 다른 트랜잭션이 해당 간격에 새로운 행을 삽입할 수 없으므로 Phantom Read가 방지됩니다. 일반적인 REPEATABLE READ 격리 수준에서는 Phantom Read가 발생할 수 있지만, InnoDB는 Gap Lock 덕분에 SERIALIZABLE 수준을 사용하지 않고도 Phantom Read를 방지할 수 있습니다.
