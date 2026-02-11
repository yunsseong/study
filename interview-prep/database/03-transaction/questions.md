# 트랜잭션(Transaction) 면접 질문

## 기본 개념 (1~5)

**Q1.** 트랜잭션이란 무엇인가요? 예시를 들어 설명해주세요.

**Q2.** ACID 속성에 대해 각각 설명해주세요.

**Q3.** 트랜잭션 격리 수준 4가지를 설명해주세요.

**Q4.** Dirty Read, Non-Repeatable Read, Phantom Read의 차이를 설명해주세요.

**Q5.** MySQL InnoDB의 기본 격리 수준은 무엇이고, 왜 그 수준을 사용하나요?

## 비교/구분 (6~9)

**Q6.** 낙관적 락과 비관적 락의 차이를 설명해주세요. 각각 어떤 상황에 적합한가요?

**Q7.** READ COMMITTED와 REPEATABLE READ의 차이는 무엇인가요?

**Q8.** @Transactional의 propagation 속성 중 REQUIRED와 REQUIRES_NEW의 차이는 무엇인가요?

**Q9.** Checked Exception과 Unchecked Exception에서 @Transactional의 롤백 동작은 어떻게 다른가요?

## 심화/실무 (10~12)

**Q10.** Spring @Transactional의 동작 원리를 설명해주세요. 프록시 기반으로 동작할 때 주의할 점은 무엇인가요?

**Q11.** MVCC(Multi-Version Concurrency Control)란 무엇이고, 어떻게 동작하나요?

**Q12.** JPA에서 낙관적 락을 구현하는 방법과 충돌 시 처리 방법을 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** @Transactional(readOnly = true)를 설정하면 어떤 이점이 있나요?

**Q14.** 데드락(Deadlock)이란 무엇이고, 비관적 락 사용 시 데드락을 방지하려면 어떻게 해야 하나요?

**Q15.** Gap Lock이란 무엇이고, InnoDB에서 Phantom Read를 어떻게 방지하나요?
