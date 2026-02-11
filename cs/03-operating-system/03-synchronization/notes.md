# 동기화 (Synchronization)

## 왜 필요한가?

멀티 스레드 환경에서 **공유 자원에 동시 접근**하면 예기치 않은 결과 발생.

### Race Condition (경쟁 조건)

```python
# 공유 변수
balance = 1000

# 스레드 A: 출금 500          # 스레드 B: 출금 300
read balance  → 1000          read balance  → 1000
balance - 500 → 500           balance - 300 → 700
write balance → 500           write balance → 700

# 기대값: 200 (1000 - 500 - 300)
# 실제값: 700 또는 500 (실행 순서에 따라 다름)
```

→ 두 스레드가 동시에 읽고 쓰면서 하나의 연산이 사라짐

---

## 임계 영역 (Critical Section)

**공유 자원에 접근하는 코드 영역**. 한 번에 하나의 스레드만 실행해야 함.

### 임계 영역 문제의 3가지 조건

| 조건 | 설명 |
|------|------|
| **상호 배제 (Mutual Exclusion)** | 한 스레드가 임계 영역에 있으면 다른 스레드 진입 불가 |
| **진행 (Progress)** | 임계 영역이 비어있으면 대기 중인 스레드가 진입 가능해야 함 |
| **한정 대기 (Bounded Waiting)** | 무한히 기다리지 않아야 함 (기아 방지) |

---

## 동기화 도구

### 1. 뮤텍스 (Mutex)

**상호 배제 잠금**. 한 번에 하나의 스레드만 접근.

```
lock 획득 → 임계 영역 실행 → lock 해제

스레드 A: lock() → [임계 영역 실행] → unlock()
스레드 B: lock() → (대기...) → A가 unlock → [실행] → unlock()
```

```java
// Java
private final Object lock = new Object();

synchronized(lock) {
    // 임계 영역
    balance -= amount;
}

// 또는
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    balance -= amount;
} finally {
    lock.unlock();
}
```

```python
# Python
import threading

lock = threading.Lock()

lock.acquire()
try:
    balance -= amount
finally:
    lock.release()

# 또는 with문
with lock:
    balance -= amount
```

**특징**:
- **소유권 있음**: lock을 획득한 스레드만 해제 가능
- 한 번에 1개만 접근 (binary)

---

### 2. 세마포어 (Semaphore)

**카운터 기반** 동기화. 동시 접근 수를 제한.

```
세마포어 값 S = 3 (동시에 3개까지 접근 가능)

스레드 A: wait(S) → S=2 → [실행]
스레드 B: wait(S) → S=1 → [실행]
스레드 C: wait(S) → S=0 → [실행]
스레드 D: wait(S) → S=0이므로 대기...
스레드 A: signal(S) → S=1 → 스레드 D 진입
```

```python
import threading

# 최대 3개 스레드 동시 접근
semaphore = threading.Semaphore(3)

semaphore.acquire()  # wait (S--)
try:
    # 임계 영역
    pass
finally:
    semaphore.release()  # signal (S++)
```

**종류**:
| 종류 | S 값 | 동작 |
|------|------|------|
| 이진 세마포어 | 0 또는 1 | 뮤텍스와 유사 |
| 카운팅 세마포어 | 0 ~ N | 동시 접근 수 제한 |

---

### 뮤텍스 vs 세마포어

| 비교 | 뮤텍스 | 세마포어 |
|------|--------|---------|
| 동시 접근 | 1개만 | N개까지 |
| 소유권 | 있음 (획득한 스레드만 해제) | 없음 (다른 스레드가 해제 가능) |
| 용도 | 상호 배제 | 자원 접근 수 제한, 순서 제어 |
| 비유 | 화장실 열쇠 1개 | 주차장 빈자리 N개 |

---

### 3. 모니터 (Monitor)

뮤텍스 + 조건 변수를 **언어 차원에서 제공**하는 고수준 동기화.

```java
// Java의 synchronized가 모니터
class BankAccount {
    private int balance;

    // synchronized → 모니터 lock 자동 관리
    public synchronized void withdraw(int amount) {
        while (balance < amount) {
            wait();  // 조건이 만족될 때까지 대기
        }
        balance -= amount;
        notifyAll();  // 대기 중인 스레드 깨움
    }

    public synchronized void deposit(int amount) {
        balance += amount;
        notifyAll();
    }
}
```

- 장점: 개발자가 lock/unlock을 직접 관리하지 않아도 됨
- Java의 `synchronized`, Python의 `Condition`

---

## 동기화 문제 (고전 문제)

### 1. 생산자-소비자 문제 (Producer-Consumer)

```
생산자 → [버퍼 (크기 N)] → 소비자

- 버퍼가 가득 차면 생산자 대기
- 버퍼가 비면 소비자 대기
- 동시 접근 방지 (뮤텍스)
```

### 2. 읽기-쓰기 문제 (Readers-Writers)

```
- 여러 읽기는 동시 가능
- 쓰기는 단독 접근 필요 (읽기와도 배타적)
- 쓰기 기아 방지 필요
```

### 3. 식사하는 철학자 문제 (Dining Philosophers)

```
5명의 철학자, 5개의 포크 (원형 테이블)
- 식사하려면 양쪽 포크 2개 필요
- 모두 왼쪽 포크를 집으면 → 데드락!

해결:
- 최대 4명만 동시 식사 시도
- 짝수 번호는 왼쪽, 홀수 번호는 오른쪽 먼저 집기
- 양쪽 포크를 한번에 집도록 (모니터)
```

---

## Java / Spring에서의 동기화

```java
// 1. synchronized 블록
synchronized(this) {
    count++;
}

// 2. Atomic 클래스 (CAS 기반, 락 없이)
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // 원자적 증가

// 3. ConcurrentHashMap (스레드 안전한 Map)
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 4. volatile (가시성 보장, 원자성은 X)
private volatile boolean flag = false;
```

### synchronized vs ReentrantLock

| 비교 | synchronized | ReentrantLock |
|------|-------------|---------------|
| 사용법 | 키워드 (간단) | 명시적 lock/unlock |
| 공정성 | 보장 안 됨 | 공정 모드 선택 가능 |
| 타임아웃 | 불가 | tryLock(timeout) 가능 |
| 조건 변수 | wait/notify | Condition (다중 조건) |

---

## 면접 예상 질문

1. **Race Condition이란?**
   - 여러 스레드가 공유 자원에 동시 접근하여 결과가 실행 순서에 의존하는 상황

2. **뮤텍스와 세마포어의 차이는?**
   - 뮤텍스: 소유권, 1개만 접근 / 세마포어: 카운터, N개 접근

3. **임계 영역 문제의 3가지 조건은?**
   - 상호 배제, 진행, 한정 대기

4. **Java에서 동기화 방법은?**
   - synchronized, ReentrantLock, Atomic 클래스, ConcurrentHashMap

5. **volatile과 synchronized의 차이는?**
   - volatile: 가시성만 보장 / synchronized: 가시성 + 원자성 보장
