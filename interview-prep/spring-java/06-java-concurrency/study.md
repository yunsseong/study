# 6. Java 동시성 (Concurrency)

---

## 멀티스레드 기본

### Thread, Runnable, Callable

Java에서 스레드를 생성하는 3가지 방법:

```java
// 1. Thread 클래스 상속 (비추천 - 단일 상속 제약)
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread: " + Thread.currentThread().getName());
    }
}
new MyThread().start();

// 2. Runnable 인터페이스 구현 (추천 - 다중 구현 가능)
Runnable task = () -> System.out.println("Runnable: " + Thread.currentThread().getName());
new Thread(task).start();

// 3. Callable 인터페이스 (반환값 + 예외 처리 가능)
Callable<Integer> callable = () -> {
    Thread.sleep(1000);
    return 42;
};
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(callable);
Integer result = future.get(); // 42
```

| 구분 | Runnable | Callable |
|------|----------|----------|
| **반환값** | 없음 (void) | 있음 (제네릭 타입) |
| **예외** | checked exception 불가 | throws Exception 가능 |
| **메서드** | run() | call() |
| **용도** | 단순 실행 | 결과를 받아야 할 때 |

```
Thread 생명 주기:

NEW → RUNNABLE → RUNNING → TERMINATED
       ↑    ↓
       ← BLOCKED / WAITING / TIMED_WAITING
```

> **면접 포인트**: "Runnable은 반환값이 없고, Callable은 반환값과 checked exception 처리가 가능합니다. 실무에서는 ExecutorService와 함께 Callable + Future를 사용합니다."

---

## synchronized 키워드

### 메서드 레벨 vs 블록 레벨

```java
public class Counter {
    private int count = 0;

    // 메서드 레벨 synchronized: this 객체의 모니터 락 사용
    public synchronized void increment() {
        count++;
    }

    // 블록 레벨 synchronized: 특정 객체의 모니터 락 사용
    private final Object lock = new Object();
    public void incrementWithBlock() {
        synchronized (lock) {
            count++;
        }
    }

    // static synchronized: 클래스 레벨 락 (Class 객체의 모니터 락)
    private static int staticCount = 0;
    public static synchronized void staticIncrement() {
        staticCount++;
    }
}
```

```
synchronized 동작 원리 (모니터 락):

Thread-1                    Thread-2
    │                           │
    ├─ lock 획득 ──┐             │
    │   (임계 영역) │             ├─ lock 획득 시도
    │   count++     │             │   → BLOCKED 상태
    │              │             │
    ├─ lock 해제 ──┘             │
    │                           ├─ lock 획득 (재개)
    │                           │   count++
    │                           ├─ lock 해제
```

### synchronized의 문제점

| 문제 | 설명 |
|------|------|
| **성능 저하** | 모니터 락 획득/해제 오버헤드, 락 경합 시 스레드 블로킹 |
| **유연성 부족** | 읽기/쓰기 락 구분 불가, 타임아웃 설정 불가 |
| **교착 상태 (Deadlock)** | 두 스레드가 서로의 락을 기다리며 무한 대기 |
| **공정성 미보장** | 어떤 스레드가 먼저 락을 획득할지 보장 불가 |
| **블록 단위 제약** | try-lock, 인터럽트 응답 등 세밀한 제어 불가 |

```java
// Deadlock 예시
Object lockA = new Object();
Object lockB = new Object();

// Thread-1: lockA → lockB 순서로 획득
new Thread(() -> {
    synchronized (lockA) {
        synchronized (lockB) { /* 작업 */ }
    }
}).start();

// Thread-2: lockB → lockA 순서로 획득 → Deadlock!
new Thread(() -> {
    synchronized (lockB) {
        synchronized (lockA) { /* 작업 */ }
    }
}).start();
```

---

## volatile 키워드

### 가시성(Visibility) 문제와 CPU 캐시

멀티스레드 환경에서 각 스레드는 CPU 캐시에 변수의 복사본을 가질 수 있다.
한 스레드가 값을 변경해도 다른 스레드가 그 변경을 보지 못하는 문제가 **가시성 문제**이다.

```
가시성 문제 발생 원리:

┌──────────────┐    ┌──────────────┐
│   Thread-1   │    │   Thread-2   │
│  ┌─────────┐ │    │  ┌─────────┐ │
│  │CPU Cache│ │    │  │CPU Cache│ │
│  │flag=true│ │    │  │flag=false│ │  ← 아직 이전 값!
│  └─────────┘ │    │  └─────────┘ │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
┌──────────────────────────────────┐
│         Main Memory              │
│         flag = true              │  ← 실제 값은 true
└──────────────────────────────────┘

Thread-1이 flag=true로 변경했지만,
Thread-2는 자신의 CPU 캐시에서 flag=false를 읽음
→ 가시성 문제!
```

### volatile로 해결

```java
public class VolatileExample {
    // volatile 없으면 무한 루프에 빠질 수 있음
    private volatile boolean running = true;

    public void stop() {
        running = false;  // Main Memory에 즉시 반영
    }

    public void run() {
        while (running) {   // Main Memory에서 항상 최신 값 읽음
            // 작업 수행
        }
        System.out.println("Stopped");
    }
}
```

```
volatile 동작 원리:

┌──────────────┐    ┌──────────────┐
│   Thread-1   │    │   Thread-2   │
│  (write)     │    │  (read)      │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
┌──────────────────────────────────┐
│         Main Memory              │
│    volatile flag = true          │
└──────────────────────────────────┘

volatile 쓰기: CPU 캐시를 건너뛰고 Main Memory에 직접 기록
volatile 읽기: CPU 캐시를 건너뛰고 Main Memory에서 직접 읽음
```

### volatile vs synchronized 비교

| 구분 | volatile | synchronized |
|------|----------|-------------|
| **해결하는 문제** | 가시성(Visibility) | 가시성 + 원자성(Atomicity) |
| **락(Lock)** | 없음 (논블로킹) | 모니터 락 사용 (블로킹) |
| **성능** | 빠름 | 상대적으로 느림 |
| **적용 대상** | 단일 변수 읽기/쓰기 | 복합 연산 (읽기-수정-쓰기) |
| **원자성 보장** | 단일 읽기/쓰기만 보장 | 블록 전체 원자성 보장 |
| **사용 예시** | 플래그 변수, 상태 변수 | count++, 복합 로직 |

```java
// volatile이 적합한 경우: 단순 플래그
private volatile boolean stopped = false;

// volatile이 부적합한 경우: 복합 연산 (읽기 → 수정 → 쓰기)
private volatile int count = 0;
count++;  // 이것은 원자적이지 않다! (read → increment → write)
// 3개의 연산 사이에 다른 스레드가 끼어들 수 있음
```

> **핵심**: volatile은 가시성만 보장한다. `count++` 같은 복합 연산은 synchronized나 Atomic 클래스를 사용해야 한다.

---

## Atomic 클래스와 CAS(Compare And Swap)

### CAS 원리

CAS는 하드웨어 수준의 원자적 연산으로, **락 없이(Lock-Free)** 동시성을 해결한다.

```
CAS 동작 원리:

compareAndSwap(메모리 주소, 기대값, 새로운 값)

1. 현재 메모리 값 읽기 (expected = 10)
2. 메모리 값이 기대값과 같은지 비교
3. 같으면 → 새로운 값으로 교체 (성공)
   다르면 → 아무것도 안 함 (실패, 재시도)

Thread-1:                         Thread-2:
  read: value = 10                  read: value = 10
  CAS(10, 11) → 성공!               CAS(10, 11) → 실패! (이미 11)
  value = 11                        재시도: read value = 11
                                    CAS(11, 12) → 성공!
                                    value = 12
```

### Atomic 클래스 사용

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// AtomicInteger: 정수형 원자적 연산
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();       // ++counter (원자적)
counter.getAndIncrement();       // counter++ (원자적)
counter.compareAndSet(1, 10);    // CAS 직접 사용
counter.addAndGet(5);            // counter += 5 (원자적)

// AtomicReference: 참조형 원자적 연산
AtomicReference<String> ref = new AtomicReference<>("initial");
ref.compareAndSet("initial", "updated");  // CAS로 참조 교체
```

```java
// synchronized vs AtomicInteger 성능 비교
// synchronized 방식
class SyncCounter {
    private int count = 0;
    public synchronized void increment() { count++; }
    public synchronized int get() { return count; }
}

// Atomic 방식 (Lock-Free, 더 빠름)
class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    public void increment() { count.incrementAndGet(); }
    public int get() { return count.get(); }
}
```

| 구분 | synchronized | Atomic (CAS) |
|------|-------------|--------------|
| **락 사용** | 모니터 락 (블로킹) | 락 없음 (논블로킹) |
| **경합 시** | 스레드 블로킹, 컨텍스트 스위칭 | 스핀(재시도), CPU 사용 |
| **성능** | 경합 높을 때 상대적으로 느림 | 경합 낮~중간일 때 빠름 |
| **적합한 경우** | 복합 연산, 임계 영역이 긴 경우 | 단일 변수, 간단한 연산 |

> **면접 포인트**: "Atomic 클래스는 CAS(Compare And Swap) 연산을 사용하여 락 없이 원자적 연산을 수행합니다. 경합이 낮은 상황에서 synchronized보다 성능이 좋습니다."

---

## java.util.concurrent 패키지 주요 클래스

```
java.util.concurrent 패키지 구조:

┌─────────────────────────────────────────────────────┐
│              java.util.concurrent                    │
│                                                     │
│  [동기화 도구]                                        │
│    ReentrantLock        - 재진입 가능한 락             │
│    ReadWriteLock        - 읽기/쓰기 분리 락            │
│    CountDownLatch       - 카운트다운 대기              │
│    CyclicBarrier        - 순환 배리어                  │
│    Semaphore            - 동시 접근 수 제한            │
│                                                     │
│  [스레드 풀]                                          │
│    ExecutorService      - 스레드 풀 인터페이스          │
│    ThreadPoolExecutor   - 스레드 풀 구현체             │
│    ScheduledExecutorService - 스케줄링                │
│                                                     │
│  [동시성 컬렉션]                                      │
│    ConcurrentHashMap    - 동시성 HashMap              │
│    CopyOnWriteArrayList - 읽기 최적화 List            │
│    BlockingQueue        - 블로킹 큐                   │
│                                                     │
│  [비동기 처리]                                        │
│    Future               - 비동기 결과 조회             │
│    CompletableFuture    - 비동기 체이닝                │
│                                                     │
│  [Atomic]                                           │
│    AtomicInteger, AtomicLong, AtomicReference        │
└─────────────────────────────────────────────────────┘
```

### 주요 동기화 도구

```java
// CountDownLatch: N개의 작업 완료를 기다림
CountDownLatch latch = new CountDownLatch(3);

for (int i = 0; i < 3; i++) {
    executor.submit(() -> {
        // 작업 수행
        latch.countDown();  // 카운트 감소
    });
}
latch.await();  // 카운트가 0이 될 때까지 대기
System.out.println("모든 작업 완료");

// Semaphore: 동시 접근 수 제한
Semaphore semaphore = new Semaphore(3);  // 최대 3개 스레드 동시 접근

semaphore.acquire();  // 허가 획득 (남은 허가가 없으면 대기)
try {
    // 제한된 리소스 접근
} finally {
    semaphore.release();  // 허가 반환
}
```

---

## ReentrantLock vs synchronized

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// ReentrantLock 사용
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // 임계 영역
} finally {
    lock.unlock();  // 반드시 finally에서 해제!
}

// tryLock: 타임아웃 설정 가능
if (lock.tryLock(3, TimeUnit.SECONDS)) {
    try {
        // 락 획득 성공
    } finally {
        lock.unlock();
    }
} else {
    // 3초 내 락 획득 실패 → 다른 로직
}

// ReadWriteLock: 읽기/쓰기 분리
ReadWriteLock rwLock = new ReentrantReadWriteLock();

rwLock.readLock().lock();    // 읽기 락 (여러 스레드 동시 가능)
rwLock.writeLock().lock();   // 쓰기 락 (하나의 스레드만 가능)
```

| 구분 | synchronized | ReentrantLock |
|------|-------------|---------------|
| **사용법** | 키워드 (자동 해제) | 메서드 호출 (수동 해제) |
| **타임아웃** | 불가 | tryLock(timeout) 가능 |
| **인터럽트 응답** | 불가 | lockInterruptibly() 가능 |
| **공정성** | 미보장 | 공정 모드 설정 가능 (fair=true) |
| **Condition** | wait/notify | newCondition() (다중 조건) |
| **읽기/쓰기 분리** | 불가 | ReadWriteLock으로 가능 |
| **편의성** | 간단, 자동 해제 | 복잡, finally에서 수동 해제 필수 |
| **추천 상황** | 간단한 동기화 | 세밀한 제어가 필요한 경우 |

> **면접 포인트**: "간단한 동기화에는 synchronized, 타임아웃/공정성/조건 등 세밀한 제어가 필요하면 ReentrantLock을 사용합니다."

---

## ThreadLocal

### 개념

ThreadLocal은 **각 스레드가 독립적으로 값을 가지는 저장소**이다.
같은 ThreadLocal 변수에 접근해도 스레드마다 다른 값을 가진다.

```
ThreadLocal 동작 원리:

┌──────────────────┐   ThreadLocal<User>   ┌──────────────────┐
│    Thread-1      │                       │    Thread-2      │
│  ┌────────────┐  │                       │  ┌────────────┐  │
│  │ThreadLocal  │  │                       │  │ThreadLocal  │  │
│  │Map:         │  │                       │  │Map:         │  │
│  │ tl → UserA  │  │                       │  │ tl → UserB  │  │
│  └────────────┘  │                       │  └────────────┘  │
└──────────────────┘                       └──────────────────┘

Thread-1이 tl.get() → UserA
Thread-2가 tl.get() → UserB
같은 ThreadLocal 변수이지만 스레드마다 다른 값!
```

```java
// ThreadLocal 기본 사용
ThreadLocal<String> threadLocal = new ThreadLocal<>();

// Thread-1
threadLocal.set("Thread-1 데이터");
threadLocal.get();    // "Thread-1 데이터"

// Thread-2
threadLocal.set("Thread-2 데이터");
threadLocal.get();    // "Thread-2 데이터"

// 사용 후 반드시 제거!
threadLocal.remove();
```

### Spring에서 SecurityContextHolder와 ThreadLocal

```java
// Spring Security의 SecurityContextHolder는 기본적으로 ThreadLocal을 사용
// SecurityContextHolder.MODE_THREADLOCAL (기본값)

// 요청 처리 흐름:
// 1. 필터에서 인증 정보를 SecurityContext에 저장 (ThreadLocal)
// 2. Controller/Service에서 현재 사용자 정보 접근
// 3. 요청 완료 후 SecurityContext 정리

SecurityContextHolder.getContext().getAuthentication();
// → 현재 스레드의 인증 정보 반환 (다른 스레드와 독립적)
```

```
HTTP 요청과 ThreadLocal:

[요청 A] → Tomcat Thread-1 → SecurityContext에 UserA 저장
                              → Controller에서 UserA 조회
                              → 응답 후 SecurityContext 정리

[요청 B] → Tomcat Thread-2 → SecurityContext에 UserB 저장
                              → Controller에서 UserB 조회
                              → 응답 후 SecurityContext 정리

각 요청이 독립적으로 인증 정보를 가짐 (ThreadLocal 덕분)
```

### ThreadLocal 메모리 누수 주의점

```java
// 스레드 풀 환경에서 ThreadLocal 메모리 누수 발생 가능!
// 스레드 풀의 스레드는 재사용되므로 ThreadLocal 값이 남아있음

ExecutorService executor = Executors.newFixedThreadPool(10);

// 위험한 패턴
executor.submit(() -> {
    threadLocal.set(new HeavyObject());  // 값 설정
    // 작업 수행...
    // threadLocal.remove() 안 하면 → 스레드가 풀로 반환 후에도 값이 남음!
});

// 안전한 패턴
executor.submit(() -> {
    try {
        threadLocal.set(new HeavyObject());
        // 작업 수행...
    } finally {
        threadLocal.remove();  // 반드시 제거!
    }
});
```

```
메모리 누수 시나리오:

[Thread Pool]
Thread-1: set("DataA") → 작업 완료 → 풀로 반환 (값 안 지움!)
                                        ↓
Thread-1 재사용: 이전 "DataA"가 여전히 남아있음!
                → 다음 요청에서 잘못된 데이터 참조 가능
                → GC 대상이 되지 않아 메모리 누수
```

> **핵심**: ThreadLocal은 **반드시 try-finally에서 remove()** 호출. 특히 스레드 풀 환경(Spring Boot의 Tomcat 스레드 풀)에서 메모리 누수 위험이 크다.

---

## ExecutorService와 스레드 풀

### 왜 스레드 풀을 사용하는가?

```
스레드를 매번 생성/소멸하면:
  요청 1 → Thread 생성 → 작업 → Thread 소멸
  요청 2 → Thread 생성 → 작업 → Thread 소멸
  요청 3 → Thread 생성 → 작업 → Thread 소멸
  → 스레드 생성/소멸 비용이 큼!

스레드 풀을 사용하면:
  [Thread Pool: T1, T2, T3]
  요청 1 → T1 할당 → 작업 → T1 반환
  요청 2 → T2 할당 → 작업 → T2 반환
  요청 3 → T3 할당 → 작업 → T3 반환
  → 스레드 재사용으로 성능 향상!
```

### 주요 스레드 풀 유형

```java
import java.util.concurrent.*;

// 1. FixedThreadPool: 고정 크기 스레드 풀
ExecutorService fixed = Executors.newFixedThreadPool(10);
// - 최대 10개 스레드 유지
// - 초과 작업은 큐에서 대기
// - 적합: 동시 작업 수를 제한해야 할 때

// 2. CachedThreadPool: 유동적 크기 스레드 풀
ExecutorService cached = Executors.newCachedThreadPool();
// - 필요할 때 스레드 생성, 60초 미사용 시 소멸
// - 적합: 짧은 비동기 작업이 많을 때
// - 주의: 작업이 폭증하면 스레드가 무한히 늘어날 수 있음!

// 3. SingleThreadExecutor: 단일 스레드
ExecutorService single = Executors.newSingleThreadExecutor();
// - 스레드 1개로 순차 실행
// - 적합: 작업 순서 보장이 필요할 때

// 4. ScheduledThreadPool: 스케줄링 기능
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(5);
scheduled.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);  // 1초 간격 실행
```

```java
// 실무에서 권장: ThreadPoolExecutor 직접 생성
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                      // corePoolSize: 기본 스레드 수
    10,                     // maximumPoolSize: 최대 스레드 수
    60L, TimeUnit.SECONDS,  // keepAliveTime: 유휴 스레드 생존 시간
    new LinkedBlockingQueue<>(100),  // workQueue: 작업 대기 큐
    new ThreadPoolExecutor.CallerRunsPolicy()  // 거부 정책
);
```

| 거부 정책 (RejectedExecutionHandler) | 동작 |
|--------------------------------------|------|
| **AbortPolicy** (기본) | RejectedExecutionException 발생 |
| **CallerRunsPolicy** | 호출한 스레드가 직접 실행 |
| **DiscardPolicy** | 조용히 버림 |
| **DiscardOldestPolicy** | 큐의 가장 오래된 작업을 버리고 새 작업 추가 |

> **면접 포인트**: "Executors 팩토리 메서드보다 ThreadPoolExecutor를 직접 생성하는 것이 권장됩니다. newCachedThreadPool은 스레드가 무한히 늘어날 수 있고, newFixedThreadPool은 큐가 무한히 커질 수 있어 OOM 위험이 있기 때문입니다."

---

## Future와 CompletableFuture

### Future의 한계

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

// Future: 비동기 결과를 나중에 받음
Future<String> future = executor.submit(() -> {
    Thread.sleep(2000);
    return "결과";
});

// 문제: get()은 블로킹! 결과를 받을 때까지 현재 스레드가 멈춤
String result = future.get();  // 2초 동안 블로킹

// Future의 한계:
// 1. 콜백 불가: 완료 시 자동으로 다음 작업 수행 불가
// 2. 체이닝 불가: 여러 Future를 순차/병렬 조합 어려움
// 3. 예외 처리 불편: get()에서 ExecutionException으로 감싸짐
```

### CompletableFuture: 비동기 체이닝

```java
import java.util.concurrent.CompletableFuture;

// 비동기 실행 + 콜백 체이닝
CompletableFuture<String> cf = CompletableFuture
    .supplyAsync(() -> {
        // 비동기로 사용자 조회
        return userService.findById(1L);
    })
    .thenApply(user -> {
        // 결과를 변환
        return user.getName();
    })
    .thenApply(name -> {
        // 추가 변환
        return "Hello, " + name;
    })
    .exceptionally(ex -> {
        // 예외 처리
        return "Error: " + ex.getMessage();
    });

// 여러 비동기 작업 조합
CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(
    () -> userService.findById(1L)
);
CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(
    () -> orderService.findByUserId(1L)
);

// 두 작업 모두 완료 후 결합
CompletableFuture<String> combined = userFuture.thenCombine(orderFuture,
    (user, order) -> user + "의 주문: " + order
);

// 모든 작업 완료 대기
CompletableFuture.allOf(userFuture, orderFuture).join();
```

```
Future vs CompletableFuture:

Future:
  submit(task) → future.get() [블로킹] → 결과 사용
                      ↑
                 현재 스레드 멈춤

CompletableFuture:
  supplyAsync(task)
      → thenApply(변환)       논블로킹 체이닝!
      → thenAccept(소비)      각 단계가 이전 단계 완료 후 자동 실행
      → exceptionally(예외)
```

| 구분 | Future | CompletableFuture |
|------|--------|-------------------|
| **콜백** | 불가 | thenApply, thenAccept 등 |
| **체이닝** | 불가 | 연속 체이닝 가능 |
| **조합** | 불가 | thenCombine, allOf, anyOf |
| **예외 처리** | get()에서 catch | exceptionally, handle |
| **블로킹** | get()은 블로킹 | 논블로킹 체이닝 가능 |

---

## ConcurrentHashMap

### HashMap과의 차이

```java
// HashMap: 스레드 안전하지 않음
Map<String, Integer> hashMap = new HashMap<>();
// 멀티스레드에서 동시 접근 시 데이터 손상, 무한 루프 가능

// Collections.synchronizedMap: 전체 Map에 락
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
// 모든 연산에 synchronized → 성능 저하

// ConcurrentHashMap: 세분화된 락으로 동시성 + 성능
ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
// 읽기는 락 없이, 쓰기는 버킷 단위 락
```

### Java 8 이후 ConcurrentHashMap 내부 구조

```
Java 7 이전: Segment 기반 락 (16개 Segment)

┌─────────┬─────────┬─────────┬─────────┐
│Segment 0│Segment 1│Segment 2│  ...    │
│  [Lock] │  [Lock] │  [Lock] │         │
│ bucket  │ bucket  │ bucket  │         │
│ bucket  │ bucket  │ bucket  │         │
└─────────┴─────────┴─────────┴─────────┘
→ 최대 16개 스레드 동시 쓰기 가능

Java 8 이후: CAS + synchronized (버킷 단위)

┌────┬────┬────┬────┬────┬────┬────┬────┐
│ b0 │ b1 │ b2 │ b3 │ b4 │ b5 │ b6 │ b7 │  ← 버킷 배열
└─┬──┴────┴─┬──┴────┴────┴─┬──┴────┴────┘
  │         │              │
 Node      Node           Node
  │         │
 Node      Node

읽기: volatile read (락 없음)
빈 버킷 삽입: CAS (락 없음)
기존 버킷 수정: synchronized(해당 버킷의 head 노드)
→ 버킷 단위로 락 → 더 세밀한 동시성!
```

| 구분 | HashMap | synchronizedMap | ConcurrentHashMap |
|------|---------|-----------------|-------------------|
| **스레드 안전** | X | O (전체 락) | O (세분화 락) |
| **null key** | 허용 | 허용 | 불가 |
| **null value** | 허용 | 허용 | 불가 |
| **읽기 성능** | 빠름 | 느림 (락) | 빠름 (락 없음) |
| **쓰기 성능** | 빠름 | 느림 (전체 락) | 빠름 (버킷 락) |
| **순회 중 수정** | ConcurrentModificationException | ConcurrentModificationException | 안전 (약한 일관성) |

```java
// ConcurrentHashMap 원자적 연산 메서드
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 원자적 putIfAbsent
map.putIfAbsent("key", 1);  // key가 없을 때만 삽입

// 원자적 compute
map.compute("key", (k, v) -> v == null ? 1 : v + 1);  // 원자적 증가

// 원자적 merge
map.merge("key", 1, Integer::sum);  // 있으면 합산, 없으면 1
```

> **면접 포인트**: "Java 8의 ConcurrentHashMap은 Segment 대신 CAS + synchronized를 사용합니다. 읽기는 volatile read로 락 없이, 빈 버킷 삽입은 CAS로, 기존 버킷 수정은 해당 버킷 head 노드에만 synchronized를 걸어 동시성을 극대화합니다."

---

## Spring Boot에서의 동시성

### @Async 비동기 처리

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {

    @Async  // 별도 스레드에서 비동기 실행
    public CompletableFuture<String> sendEmail(String to) {
        // 이메일 발송 (시간이 오래 걸리는 작업)
        return CompletableFuture.completedFuture("sent to " + to);
    }
}
```

### @Transactional과 스레드

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        // @Transactional은 ThreadLocal에 트랜잭션 컨텍스트를 저장
        // → 같은 스레드 내에서만 트랜잭션이 유지됨!

        orderRepository.save(order);

        // 주의: @Async 메서드를 호출하면 다른 스레드에서 실행됨
        // → 현재 트랜잭션과 별개의 트랜잭션이 됨!
        notificationService.sendEmail(request.getEmail());
    }
}
```

```
@Transactional + @Async 주의:

Thread-1 (트랜잭션 A)                Thread-2 (@Async)
    │                                    │
    ├─ @Transactional 시작               │
    ├─ orderRepository.save()            │
    ├─ sendEmail() 호출 ─────────→      ├─ 별도 트랜잭션 B 시작
    ├─ 트랜잭션 A 커밋                    │   (트랜잭션 A와 무관!)
    │                                    ├─ 트랜잭션 B 커밋/롤백

트랜잭션 A가 롤백되어도 트랜잭션 B는 이미 실행됨!
→ 데이터 불일치 위험
```

### 싱글톤 Bean 무상태(Stateless) 설계

```java
// 잘못된 설계: 싱글톤 Bean에 상태(인스턴스 변수) 보유
@Service
public class BadService {
    private int count = 0;  // 모든 요청이 공유! Race Condition 발생!

    public void process() {
        count++;  // 여러 스레드가 동시에 접근 → 데이터 손상
    }
}

// 올바른 설계: 싱글톤 Bean은 무상태로
@Service
public class GoodService {
    // 인스턴스 변수 없음 (의존성 주입만)
    private final UserRepository userRepository;

    public GoodService(UserRepository userRepository) {
        this.userRepository = userRepository;  // final + 불변
    }

    public void process(int count) {
        // 지역 변수는 스레드마다 독립 (Stack에 저장)
        int localCount = count + 1;
    }
}
```

```
Spring 싱글톤 Bean과 동시성:

[Spring IoC Container]
  GoodService (싱글톤, 인스턴스 1개)
      ↑           ↑           ↑
  Thread-1    Thread-2    Thread-3
  (요청 A)    (요청 B)    (요청 C)

모든 스레드가 같은 Bean 인스턴스를 공유
→ Bean에 상태(인스턴스 변수)가 있으면 동시성 문제!
→ Bean은 무상태로 설계, 상태는 지역 변수/DB/Redis에 저장
```

> **핵심**: Spring의 Bean은 기본적으로 싱글톤이다. 여러 스레드가 동시에 같은 Bean을 사용하므로, Bean에 상태(인스턴스 변수)를 두면 안 된다.

---

## 실무 예제: 선착순 쿠폰 시스템

### 문제 상황: Race Condition

```java
@Service
public class CouponService {
    private int remainingCount = 100;  // 남은 쿠폰 수

    public String issueCoupon(Long userId) {
        if (remainingCount > 0) {      // 1. 읽기
            remainingCount--;           // 2. 수정
            return "쿠폰 발급 성공";
        }
        return "쿠폰 소진";
    }
}
```

```
Race Condition 발생:

Thread-1                    Thread-2
    │                           │
    ├─ read: remaining=1        │
    │                           ├─ read: remaining=1  (아직 1!)
    ├─ remaining-- (0)          │
    │                           ├─ remaining-- (-1!)  ← 초과 발급!

100개 한정인데 101개 이상 발급될 수 있음!
```

### 1단계 해결: synchronized

```java
@Service
public class CouponServiceV2 {
    private int remainingCount = 100;

    public synchronized String issueCoupon(Long userId) {
        if (remainingCount > 0) {
            remainingCount--;
            return "쿠폰 발급 성공";
        }
        return "쿠폰 소진";
    }
}
// 문제: 단일 서버에서만 동작. 서버가 여러 대이면?
```

```
synchronized의 한계 (다중 서버):

[Server 1]                    [Server 2]
synchronized → 서버1 내 OK    synchronized → 서버2 내 OK
    │                              │
    └──── 서버 간에는 락 공유 불가! ────┘

DB에 remaining=1일 때:
Server 1: read(1) → decrement → remaining=0  ← OK
Server 2: read(1) → decrement → remaining=-1 ← 초과 발급!
```

### 2단계 해결: DB 비관적 락

```java
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);
}

@Service
@Transactional
public class CouponServiceV3 {

    public String issueCoupon(Long userId) {
        Coupon coupon = couponRepository.findByIdWithLock(1L)
            .orElseThrow();

        if (coupon.getRemainingCount() > 0) {
            coupon.decrease();
            return "쿠폰 발급 성공";
        }
        return "쿠폰 소진";
    }
}
// 문제: DB에 부하 집중, 성능 저하
```

### 3단계 해결: Redis 분산 락

```java
@Service
public class CouponServiceV4 {

    private final RedissonClient redissonClient;
    private final CouponRepository couponRepository;

    public String issueCoupon(Long userId) {
        RLock lock = redissonClient.getLock("coupon:lock");

        try {
            // 락 획득 시도 (대기 3초, 보유 5초)
            boolean acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!acquired) {
                return "잠시 후 다시 시도해주세요";
            }

            try {
                Coupon coupon = couponRepository.findById(1L)
                    .orElseThrow();

                if (coupon.getRemainingCount() > 0) {
                    coupon.decrease();
                    couponRepository.save(coupon);
                    return "쿠폰 발급 성공";
                }
                return "쿠폰 소진";
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "처리 중 오류 발생";
        }
    }
}
```

```
발전 과정 정리:

문제: Race Condition (동시성 문제)
    │
    ▼
1단계: synchronized
    장점: 간단
    한계: 단일 서버에서만 유효
    │
    ▼
2단계: DB 비관적 락 (PESSIMISTIC_WRITE)
    장점: 다중 서버 대응
    한계: DB 부하 집중, 성능 저하
    │
    ▼
3단계: Redis 분산 락 (Redisson)
    장점: 다중 서버 대응, DB 부하 감소, 성능 우수
    한계: Redis 의존성 추가, Redis 장애 시 대비 필요
    │
    ▼
4단계 (고급): Redis + Kafka (이벤트 기반)
    요청을 Kafka 큐에 넣고 순차 처리
    → 대규모 트래픽에서도 안정적
```

---

## 면접 핵심 정리

**Q: synchronized와 ReentrantLock의 차이를 설명해주세요**

> synchronized는 Java 키워드로 블록이 끝나면 자동으로 락을 해제합니다. 반면 ReentrantLock은 java.util.concurrent 패키지의 클래스로, tryLock으로 타임아웃을 설정할 수 있고, 공정 모드(fair mode)를 지원하며, lockInterruptibly로 인터럽트에 응답할 수 있습니다. 간단한 동기화에는 synchronized를, 타임아웃이나 공정성 등 세밀한 제어가 필요하면 ReentrantLock을 사용합니다.

**Q: volatile 키워드는 언제 사용하나요?**

> volatile은 CPU 캐시와 메인 메모리 간 가시성(Visibility) 문제를 해결합니다. volatile 변수는 항상 메인 메모리에서 읽고 쓰므로, 한 스레드의 변경이 다른 스레드에 즉시 보입니다. 단, 가시성만 보장하고 원자성은 보장하지 않으므로, 단순 플래그 변수(boolean stopped)에 적합합니다. count++ 같은 복합 연산에는 Atomic 클래스나 synchronized를 사용해야 합니다.

**Q: ConcurrentHashMap의 내부 구조를 설명해주세요**

> Java 8 이후 ConcurrentHashMap은 CAS + synchronized를 조합합니다. 읽기 연산은 volatile read로 락 없이 수행하고, 빈 버킷에 삽입할 때는 CAS를 사용하며, 이미 노드가 있는 버킷을 수정할 때만 해당 버킷의 head 노드에 synchronized를 겁니다. 이를 통해 HashMap 수준의 읽기 성능을 유지하면서도 스레드 안전성을 보장합니다.

**Q: Spring Boot에서 동시성 문제를 어떻게 해결하나요?**

> 첫째, Spring Bean은 싱글톤이므로 무상태(Stateless)로 설계합니다. 상태는 지역 변수, DB, Redis에 저장합니다. 둘째, 단일 서버 내 동시성은 synchronized나 ReentrantLock으로 해결합니다. 셋째, 다중 서버 환경에서는 DB 비관적 락이나 Redis 분산 락(Redisson)을 사용합니다. 넷째, 비동기 작업이 필요하면 @Async와 CompletableFuture를 활용하되, @Transactional과의 스레드 경계에 주의합니다.

**Q: ThreadLocal은 무엇이고 왜 주의해야 하나요?**

> ThreadLocal은 각 스레드가 독립적으로 값을 가지는 저장소입니다. Spring Security의 SecurityContextHolder가 ThreadLocal을 사용하여 요청별 인증 정보를 관리합니다. 주의할 점은 스레드 풀 환경에서 메모리 누수가 발생할 수 있다는 것입니다. 스레드 풀의 스레드는 재사용되므로, 작업 완료 후 반드시 ThreadLocal.remove()를 호출하여 값을 제거해야 합니다. 그렇지 않으면 이전 요청의 데이터가 다음 요청에서 잘못 참조될 수 있습니다.
