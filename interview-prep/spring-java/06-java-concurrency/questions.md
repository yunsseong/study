# Java 동시성 면접 질문

## 기본 개념 (1~5)

**Q1.** Java에서 스레드를 생성하는 방법들을 설명하고, Runnable과 Callable의 차이점을 설명해주세요.

**Q2.** synchronized 키워드의 동작 원리를 설명해주세요. 메서드 레벨과 블록 레벨의 차이는 무엇인가요?

**Q3.** volatile 키워드는 어떤 문제를 해결하며, 언제 사용하나요? volatile과 synchronized의 차이를 설명해주세요.

**Q4.** Atomic 클래스(AtomicInteger 등)의 동작 원리인 CAS(Compare And Swap)를 설명해주세요.

**Q5.** ThreadLocal이란 무엇이며, Spring에서 어떻게 사용되나요?

## 비교/구분 (6~9)

**Q6.** synchronized와 ReentrantLock의 차이점을 설명해주세요. 각각 언제 사용하는 것이 적절한가요?

**Q7.** HashMap, synchronizedMap, ConcurrentHashMap의 차이점을 설명해주세요.

**Q8.** Future와 CompletableFuture의 차이점을 설명해주세요. CompletableFuture의 장점은 무엇인가요?

**Q9.** newFixedThreadPool과 newCachedThreadPool의 차이점을 설명하고, 각각 어떤 상황에 적합한지 설명해주세요.

## 심화/실무 (10~12)

**Q10.** ConcurrentHashMap의 내부 구조를 설명해주세요. Java 7과 Java 8에서 어떻게 달라졌나요?

**Q11.** Spring Boot에서 싱글톤 Bean의 동시성 문제를 어떻게 방지하나요? @Async 사용 시 @Transactional과의 관계에서 주의할 점은 무엇인가요?

**Q12.** 선착순 쿠폰 시스템에서 Race Condition이 발생하는 원인과 해결 방법을 단계별로 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** ThreadLocal을 스레드 풀 환경에서 사용할 때 메모리 누수가 발생할 수 있는 이유와 해결 방법을 설명해주세요.

**Q14.** Executors 팩토리 메서드 대신 ThreadPoolExecutor를 직접 생성하는 것이 권장되는 이유는 무엇인가요?

**Q15.** volatile 변수에 대해 count++ 연산이 원자적이지 않은 이유를 설명하고, 이를 올바르게 처리하는 방법을 제시해주세요.
