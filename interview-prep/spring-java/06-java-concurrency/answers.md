# Java 동시성 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Java에서 스레드를 생성하는 방법들을 설명하고, Runnable과 Callable의 차이점을 설명해주세요.

> Java에서 스레드를 생성하는 방법은 크게 3가지입니다. 첫째, Thread 클래스를 상속하는 방법이 있지만 Java는 단일 상속만 허용하므로 잘 사용하지 않습니다. 둘째, Runnable 인터페이스를 구현하는 방법으로, run() 메서드를 오버라이드하며 반환값이 없고 checked exception을 던질 수 없습니다. 셋째, Callable 인터페이스를 구현하는 방법으로, call() 메서드를 오버라이드하며 제네릭 타입의 반환값을 가지고 throws Exception이 가능합니다. 실무에서는 ExecutorService에 Callable을 submit하고 Future로 결과를 받는 방식을 주로 사용합니다.

**Q2.** synchronized 키워드의 동작 원리를 설명해주세요. 메서드 레벨과 블록 레벨의 차이는 무엇인가요?

> synchronized는 모니터 락(Monitor Lock)을 기반으로 동작합니다. 한 스레드가 synchronized 블록에 진입하면 모니터 락을 획득하고, 다른 스레드는 락이 해제될 때까지 BLOCKED 상태로 대기합니다. 메서드 레벨 synchronized(`public synchronized void method()`)는 인스턴스 메서드의 경우 this 객체의 모니터 락을, static 메서드의 경우 Class 객체의 모니터 락을 사용합니다. 블록 레벨 synchronized(`synchronized(lock) { }`)는 특정 객체의 모니터 락을 지정할 수 있어, 락의 범위를 좁혀 성능을 향상시킬 수 있습니다. 메서드 전체보다 필요한 부분만 동기화하는 블록 레벨이 일반적으로 권장됩니다.

**Q3.** volatile 키워드는 어떤 문제를 해결하며, 언제 사용하나요? volatile과 synchronized의 차이를 설명해주세요.

> volatile은 CPU 캐시와 메인 메모리 간의 가시성(Visibility) 문제를 해결합니다. 멀티스레드 환경에서 각 스레드는 CPU 캐시에 변수의 복사본을 가질 수 있는데, 한 스레드가 값을 변경해도 다른 스레드가 캐시에서 이전 값을 읽는 문제가 발생합니다. volatile 변수는 읽기/쓰기 시 항상 메인 메모리를 통하므로 이 문제를 해결합니다. 그러나 volatile은 가시성만 보장하고 원자성(Atomicity)은 보장하지 않습니다. synchronized는 가시성과 원자성 모두를 보장하지만 모니터 락으로 인해 성능 오버헤드가 있습니다. volatile은 단순 플래그 변수(boolean running)처럼 한 스레드가 쓰고 다른 스레드가 읽는 패턴에 적합하고, count++ 같은 복합 연산에는 synchronized나 Atomic 클래스를 사용해야 합니다.

**Q4.** Atomic 클래스(AtomicInteger 등)의 동작 원리인 CAS(Compare And Swap)를 설명해주세요.

> CAS는 하드웨어 수준의 원자적 연산으로, 메모리의 현재 값이 기대값(expected)과 같으면 새로운 값으로 교체하고, 다르면 실패하여 재시도하는 방식입니다. 예를 들어 AtomicInteger의 incrementAndGet()은 현재 값을 읽고(expected=10), 메모리 값이 여전히 10인지 확인한 뒤, 맞으면 11로 교체합니다. 다른 스레드가 먼저 값을 바꿨다면 CAS가 실패하고 새로운 값을 읽어 재시도합니다. 이 방식은 락을 사용하지 않으므로(Lock-Free) 스레드 블로킹과 컨텍스트 스위칭 비용이 없습니다. 경합이 낮거나 중간 수준일 때 synchronized보다 성능이 좋지만, 경합이 매우 높으면 스핀(재시도) 비용이 커질 수 있습니다.

**Q5.** ThreadLocal이란 무엇이며, Spring에서 어떻게 사용되나요?

> ThreadLocal은 각 스레드가 독립적으로 값을 저장하고 접근할 수 있는 저장소입니다. 같은 ThreadLocal 변수에 접근해도 스레드마다 별도의 값을 가집니다. 내부적으로 각 Thread 객체가 ThreadLocalMap을 가지고 있어, ThreadLocal을 key로 값을 저장합니다. Spring에서는 SecurityContextHolder가 기본적으로 ThreadLocal(MODE_THREADLOCAL)을 사용하여 요청별 인증 정보를 관리합니다. 각 HTTP 요청은 별도의 Tomcat 스레드에서 처리되므로, ThreadLocal을 통해 요청 간 인증 정보가 격리됩니다. 주의할 점은 스레드 풀 환경에서 사용 후 반드시 remove()를 호출해야 한다는 것입니다. 스레드가 재사용되므로 이전 값이 남아 메모리 누수나 잘못된 데이터 참조가 발생할 수 있습니다.

## 비교/구분 (6~9)

**Q6.** synchronized와 ReentrantLock의 차이점을 설명해주세요. 각각 언제 사용하는 것이 적절한가요?

> synchronized는 Java 키워드로, 블록이 끝나면 자동으로 락이 해제되어 사용이 간단합니다. ReentrantLock은 java.util.concurrent.locks 패키지의 클래스로, 수동으로 lock()/unlock()을 호출해야 합니다. ReentrantLock의 장점은 tryLock(timeout)으로 대기 시간을 설정할 수 있고, lockInterruptibly()로 인터럽트에 응답할 수 있으며, 공정 모드(fair=true)로 대기 순서를 보장할 수 있고, Condition 객체로 다중 조건 대기가 가능하며, ReadWriteLock으로 읽기/쓰기 락을 분리할 수 있다는 것입니다. 간단한 동기화에는 synchronized를 사용하고, 타임아웃 설정이나 공정성 보장 등 세밀한 제어가 필요한 경우에는 ReentrantLock을 사용합니다. ReentrantLock 사용 시 반드시 finally 블록에서 unlock()을 호출해야 합니다.

**Q7.** HashMap, synchronizedMap, ConcurrentHashMap의 차이점을 설명해주세요.

> HashMap은 스레드 안전하지 않아 멀티스레드 환경에서 데이터 손상이 발생할 수 있습니다. Collections.synchronizedMap()은 모든 메서드에 synchronized를 적용하여 전체 Map에 하나의 락을 거므로 스레드 안전하지만, 하나의 스레드만 접근 가능하여 성능이 낮습니다. ConcurrentHashMap은 Java 8 기준으로 읽기는 volatile read로 락 없이 수행하고, 빈 버킷 삽입은 CAS로, 기존 버킷 수정은 해당 버킷의 head 노드에만 synchronized를 걸어 세밀한 동시성 제어를 합니다. 따라서 ConcurrentHashMap이 읽기 성능은 HashMap과 거의 동일하면서도 스레드 안전성을 보장합니다. 추가로 ConcurrentHashMap은 null key와 null value를 허용하지 않으며, putIfAbsent, compute, merge 등 원자적 연산 메서드를 제공합니다.

**Q8.** Future와 CompletableFuture의 차이점을 설명해주세요. CompletableFuture의 장점은 무엇인가요?

> Future는 비동기 작업의 결과를 나중에 조회하는 인터페이스이지만, get() 메서드가 블로킹이라 결과를 받을 때까지 현재 스레드가 멈춥니다. 또한 콜백 등록, 작업 체이닝, 여러 Future 조합이 불가능합니다. CompletableFuture는 Java 8에서 도입되어 이러한 한계를 해결합니다. supplyAsync()로 비동기 실행 후 thenApply(), thenAccept() 등으로 논블로킹 체이닝이 가능하고, exceptionally()나 handle()로 우아한 예외 처리가 가능합니다. thenCombine()으로 두 비동기 결과를 결합하거나, allOf()로 여러 작업의 완료를 기다릴 수 있습니다. 실무에서 여러 외부 API를 병렬 호출한 뒤 결과를 조합하는 패턴에 매우 유용합니다.

**Q9.** newFixedThreadPool과 newCachedThreadPool의 차이점을 설명하고, 각각 어떤 상황에 적합한지 설명해주세요.

> newFixedThreadPool은 고정된 수의 스레드를 유지하며, 모든 스레드가 사용 중이면 새 작업은 큐에서 대기합니다. 동시 처리량을 예측하고 제한해야 하는 경우에 적합합니다. 다만 내부적으로 LinkedBlockingQueue(무한 큐)를 사용하므로 작업이 계속 쌓이면 OOM이 발생할 수 있습니다. newCachedThreadPool은 필요할 때 스레드를 생성하고 60초간 미사용 시 소멸시킵니다. 짧은 비동기 작업이 많을 때 적합하지만, 작업이 폭증하면 스레드가 무한히 늘어나 OOM이 발생할 수 있습니다. 이러한 이유로 실무에서는 ThreadPoolExecutor를 직접 생성하여 corePoolSize, maximumPoolSize, 큐 크기, 거부 정책을 명시적으로 설정하는 것이 권장됩니다.

## 심화/실무 (10~12)

**Q10.** ConcurrentHashMap의 내부 구조를 설명해주세요. Java 7과 Java 8에서 어떻게 달라졌나요?

> Java 7의 ConcurrentHashMap은 Segment 기반으로, 내부를 기본 16개의 Segment로 분할하고 각 Segment가 독립적인 락을 가졌습니다. 따라서 최대 16개 스레드가 동시에 쓰기 작업을 수행할 수 있었습니다. Java 8에서는 Segment 방식을 버리고 CAS + synchronized 조합으로 변경되었습니다. 읽기 연산은 Node의 val과 next가 volatile로 선언되어 있어 락 없이 최신 값을 읽습니다. 새로운 노드를 빈 버킷에 삽입할 때는 CAS를 사용하여 락 오버헤드 없이 처리합니다. 이미 노드가 있는 버킷에 삽입하거나 수정할 때만 해당 버킷의 첫 번째 노드(head)에 synchronized를 걸어 최소 범위로 동기화합니다. 이 구조로 Java 7보다 더 세밀한 동시성 제어가 가능해졌고, HashMap에 가까운 읽기 성능을 달성합니다.

**Q11.** Spring Boot에서 싱글톤 Bean의 동시성 문제를 어떻게 방지하나요? @Async 사용 시 @Transactional과의 관계에서 주의할 점은 무엇인가요?

> Spring의 Bean은 기본적으로 싱글톤 스코프이므로, 하나의 인스턴스를 여러 스레드가 공유합니다. 따라서 Bean은 무상태(Stateless)로 설계해야 합니다. 인스턴스 변수에 요청별 데이터를 저장하면 Race Condition이 발생합니다. 상태가 필요하면 지역 변수(스레드 Stack에 저장), DB, Redis 등을 사용합니다. @Async와 @Transactional 관계에서 주의할 점은, @Transactional이 ThreadLocal에 트랜잭션 컨텍스트를 저장하므로 같은 스레드 내에서만 유효하다는 것입니다. @Async 메서드는 별도 스레드에서 실행되므로, 호출한 메서드의 트랜잭션과는 완전히 독립된 새로운 트랜잭션이 됩니다. 원래 트랜잭션이 롤백되어도 @Async로 실행된 작업은 이미 별도 트랜잭션으로 커밋될 수 있어 데이터 불일치가 발생할 수 있습니다.

**Q12.** 선착순 쿠폰 시스템에서 Race Condition이 발생하는 원인과 해결 방법을 단계별로 설명해주세요.

> 선착순 쿠폰 시스템에서 "남은 수량 확인(읽기) → 수량 감소(수정)"가 원자적이지 않기 때문에 Race Condition이 발생합니다. 두 스레드가 동시에 남은 수량이 1인 것을 확인하고 각각 감소시키면 초과 발급이 됩니다. 해결 방법은 단계적으로 발전합니다. 1단계로 synchronized를 사용하면 단일 서버에서는 해결되지만 서버가 여러 대인 경우 서버 간 락 공유가 불가합니다. 2단계로 DB 비관적 락(SELECT FOR UPDATE)을 사용하면 다중 서버에서도 동작하지만, DB에 부하가 집중되어 성능이 저하됩니다. 3단계로 Redis 분산 락(Redisson의 RLock)을 사용하면 다중 서버 대응이 가능하고 DB 부하도 줄어듭니다. 대규모 트래픽에서는 Redis + Kafka를 조합하여 요청을 큐에 넣고 순차 처리하는 이벤트 기반 방식으로 발전시킬 수 있습니다.

## 꼬리질문 대비 (13~15)

**Q13.** ThreadLocal을 스레드 풀 환경에서 사용할 때 메모리 누수가 발생할 수 있는 이유와 해결 방법을 설명해주세요.

> 스레드 풀의 스레드는 작업이 끝나도 소멸되지 않고 풀로 반환되어 재사용됩니다. ThreadLocal에 set()으로 저장한 값은 해당 스레드의 ThreadLocalMap에 저장되는데, remove()를 호출하지 않으면 스레드가 살아있는 한 GC 대상이 되지 않습니다. 이로 인해 두 가지 문제가 발생합니다. 첫째, 메모리 누수로 HeavyObject 같은 큰 객체가 제거되지 않고 계속 쌓입니다. 둘째, 데이터 오염으로 이전 요청의 데이터가 다음 요청에서 잘못 참조될 수 있습니다. 해결 방법은 try-finally 블록에서 반드시 threadLocal.remove()를 호출하는 것입니다. Spring Security의 SecurityContextPersistenceFilter도 요청 처리 후 SecurityContextHolder.clearContext()를 호출하여 ThreadLocal을 정리합니다.

**Q14.** Executors 팩토리 메서드 대신 ThreadPoolExecutor를 직접 생성하는 것이 권장되는 이유는 무엇인가요?

> Executors 팩토리 메서드는 내부적으로 무제한 자원을 사용하는 설정을 가지고 있어 OOM 위험이 있습니다. newFixedThreadPool과 newSingleThreadExecutor는 내부적으로 LinkedBlockingQueue를 사용하는데, 이 큐의 기본 용량이 Integer.MAX_VALUE(약 21억)이므로 작업이 계속 쌓이면 메모리가 고갈됩니다. newCachedThreadPool은 maximumPoolSize가 Integer.MAX_VALUE이고 SynchronousQueue를 사용하여, 들어오는 작업마다 새 스레드를 생성하므로 스레드 수가 무한히 증가할 수 있습니다. ThreadPoolExecutor를 직접 생성하면 corePoolSize, maximumPoolSize, 큐의 크기(new LinkedBlockingQueue(100) 등), 거부 정책(CallerRunsPolicy 등)을 명시적으로 설정하여 자원 사용을 예측하고 제어할 수 있습니다.

**Q15.** volatile 변수에 대해 count++ 연산이 원자적이지 않은 이유를 설명하고, 이를 올바르게 처리하는 방법을 제시해주세요.

> count++은 단일 연산처럼 보이지만, 실제로는 3단계로 분해됩니다: (1) 메모리에서 count 값 읽기(read), (2) 값에 1 더하기(increment), (3) 결과를 메모리에 쓰기(write). volatile은 각 읽기/쓰기의 가시성만 보장할 뿐, 이 3단계가 하나의 원자적 연산으로 실행되는 것을 보장하지 않습니다. 예를 들어 두 스레드가 동시에 count=10을 읽고, 각각 11을 쓰면 결과는 12가 아니라 11이 됩니다(lost update). 해결 방법은 세 가지입니다. 첫째, synchronized로 읽기-수정-쓰기를 하나의 임계 영역으로 보호합니다. 둘째, AtomicInteger의 incrementAndGet()을 사용하면 CAS 기반으로 락 없이 원자적 증가가 가능합니다. 셋째, ReentrantLock으로 동기화할 수 있습니다. 단순 카운터에는 AtomicInteger가 성능과 편의성 면에서 가장 적합합니다.
