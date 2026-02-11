# 1. Java 기초 & JVM

---

## JVM이란?

**JVM (Java Virtual Machine)**: Java 바이트코드를 실행하는 가상 머신.
Java가 "Write Once, Run Anywhere"인 이유가 바로 JVM 덕분이다.

```
[Java 소스코드]  →  javac 컴파일  →  [바이트코드(.class)]  →  JVM 실행  →  OS
  Hello.java                          Hello.class              어떤 OS든 실행 가능
```

> Spring Boot 앱을 `java -jar app.jar`로 실행하면, JVM이 바이트코드를 해석하여 실행한다.

---

## JVM 구조

JVM은 크게 3가지로 구성된다:

```
┌─────────────────────────────────────────────────────┐
│                       JVM                           │
│                                                     │
│  ┌──────────────┐                                   │
│  │ Class Loader  │ ← .class 파일을 메모리에 로드     │
│  └──────┬───────┘                                   │
│         ↓                                           │
│  ┌──────────────────────────────────────┐           │
│  │       Runtime Data Area              │           │
│  │  ┌────────┐ ┌──────┐ ┌───────┐      │           │
│  │  │ Method │ │ Heap │ │ Stack │ ...   │           │
│  │  │  Area  │ │      │ │       │       │           │
│  │  └────────┘ └──────┘ └───────┘       │           │
│  └──────────────────────────────────────┘           │
│         ↓                                           │
│  ┌──────────────────┐                               │
│  │ Execution Engine  │ ← 바이트코드를 기계어로 변환   │
│  │  - Interpreter    │                               │
│  │  - JIT Compiler   │                               │
│  │  - GC             │                               │
│  └──────────────────┘                               │
└─────────────────────────────────────────────────────┘
```

### 1. Class Loader (클래스 로더)

.class 파일을 찾아서 JVM 메모리에 올리는 역할.

```
Loading → Linking → Initialization

Loading: .class 파일을 읽어서 바이너리 데이터 생성
Linking:
  - Verification: 바이트코드가 유효한지 검증
  - Preparation: static 변수에 기본값 할당
  - Resolution: 심볼릭 참조를 실제 참조로 변환
Initialization: static 변수에 실제 값 할당, static 블록 실행
```

클래스 로더는 **3가지 계층**으로 나뉜다:

```
Bootstrap ClassLoader     ← java.lang.* 등 핵심 클래스 (rt.jar)
       ↑
Extension ClassLoader     ← 확장 라이브러리
       ↑
Application ClassLoader   ← 우리가 작성한 클래스, 라이브러리
```

> **부모 위임 모델 (Parent Delegation Model)**: 클래스를 로드할 때 먼저 부모에게 위임.
> 부모가 못 찾으면 자식이 로드한다. 이렇게 하면 핵심 클래스가 임의로 대체되는 것을 방지.

### 2. Execution Engine (실행 엔진)

바이트코드를 실행하는 엔진.

| 구성 요소 | 역할 |
|----------|------|
| **Interpreter** | 바이트코드를 한 줄씩 해석하여 실행. 느림 |
| **JIT Compiler** | 반복 실행되는 코드를 기계어로 컴파일하여 캐시. 빠름 |
| **Garbage Collector** | 사용하지 않는 객체를 메모리에서 제거 |

```
처음 실행: Interpreter가 한 줄씩 해석
  ↓
자주 호출되는 메서드 발견 (Hot Spot)
  ↓
JIT Compiler가 기계어로 컴파일 → 캐시에 저장
  ↓
다음부터는 컴파일된 기계어를 바로 실행 → 빠름
```

> HotSpot JVM이라는 이름이 여기서 나왔다. "뜨거운(자주 실행되는) 지점"을 찾아 최적화.

---

## JVM 메모리 구조 (Runtime Data Area)

```
┌─────────────────────────────────────────────────────┐
│                Runtime Data Area                     │
│                                                     │
│  ┌──────────────────────────────────────┐           │
│  │  Method Area (메서드 영역)  [공유]     │           │
│  │  - 클래스 정보, static 변수, 상수 풀    │           │
│  └──────────────────────────────────────┘           │
│  ┌──────────────────────────────────────┐           │
│  │  Heap (힙)  [공유]                    │           │
│  │  - new로 생성된 객체, 배열             │           │
│  │  - GC의 대상                          │           │
│  └──────────────────────────────────────┘           │
│                                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Stack 1  │ │ Stack 2  │ │ Stack 3  │ [스레드별] │
│  │ PC Reg 1 │ │ PC Reg 2 │ │ PC Reg 3 │           │
│  │ Native 1 │ │ Native 2 │ │ Native 3 │           │
│  └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────┘
```

| 영역 | 공유 범위 | 저장하는 것 | Java 예시 |
|------|----------|------------|----------|
| **Method Area** | 모든 스레드 공유 | 클래스 정보, static 변수, 상수 풀 | `static int count = 0;` |
| **Heap** | 모든 스레드 공유 | new로 생성된 객체, 배열 | `new User()`, `new ArrayList()` |
| **Stack** | 스레드마다 별도 | 지역 변수, 메서드 호출 정보 (프레임) | `int x = 10;`, 메서드 호출 스택 |
| **PC Register** | 스레드마다 별도 | 현재 실행 중인 명령어 주소 | JVM 명령어 주소 |
| **Native Method Stack** | 스레드마다 별도 | C/C++ 네이티브 메서드 호출 정보 | `System.currentTimeMillis()` |

### Stack 프레임 상세

```java
public class Example {
    public static void main(String[] args) {
        int a = 10;           // main 프레임의 지역 변수
        int result = add(a, 20);  // add 프레임 생성
    }

    static int add(int x, int y) {
        int sum = x + y;      // add 프레임의 지역 변수
        return sum;
    }
}
```

```
Stack (main 스레드):
┌──────────────┐
│ add 프레임    │ ← x=10, y=20, sum=30  (먼저 제거됨)
├──────────────┤
│ main 프레임   │ ← a=10, result=30, args
└──────────────┘

메서드 호출 → 프레임 push
메서드 종료 → 프레임 pop (LIFO)
```

### Heap과 Stack의 관계

```java
User user = new User("홍길동");
```

```
Stack                          Heap
┌──────────┐                  ┌──────────────────┐
│ user(참조)│ ────────────→   │ User 객체         │
│ 0x1234   │                  │  name: "홍길동"   │
└──────────┘                  │  주소: 0x1234     │
                              └──────────────────┘

Stack에는 참조(주소)만 저장
Heap에는 실제 객체가 저장
```

> **면접 포인트**: "Java에서 참조 타입 변수는 Stack에 주소만 저장하고, 실제 객체는 Heap에 있습니다."

---

## Garbage Collection (GC)

### GC란?

**Heap 메모리에서 더 이상 사용하지 않는 객체를 자동으로 제거**하는 메커니즘.
C/C++은 개발자가 직접 `free()` / `delete`를 해야 하지만, Java는 GC가 자동으로 처리한다.

### Mark & Sweep 알고리즘

GC의 기본 동작 원리:

```
1단계: Mark (표시)
   GC Root에서 시작하여 참조를 따라감
   도달 가능한 객체는 "살아있음"으로 표시

   GC Root:
   - Stack의 지역 변수
   - Static 변수
   - JNI 참조

2단계: Sweep (제거)
   Mark되지 않은 객체를 메모리에서 제거

3단계: Compact (압축) - 선택적
   살아남은 객체를 한쪽으로 모아서 메모리 단편화 해결
```

```
Mark 전:                  Mark 후:                 Sweep 후:
┌──┬──┬──┬──┬──┐        ┌──┬──┬──┬──┬──┐        ┌──┬──┬──┬──┬──┐
│A │B │C │D │E │        │A*│B │C*│D │E*│        │A │  │C │  │E │
└──┴──┴──┴──┴──┘        └──┴──┴──┴──┴──┘        └──┴──┴──┴──┴──┘
A,C,E는 참조됨           * = 참조됨(Mark)          B,D 제거됨
B,D는 참조 없음
```

### 세대별 GC (Generational GC)

**핵심 가설**: "대부분의 객체는 금방 죽는다" (Weak Generational Hypothesis)

```
Heap 구조:

┌─────────────────────────────────┬──────────────────┐
│         Young Generation         │  Old Generation   │
│  ┌───────┬─────────┬─────────┐  │                   │
│  │ Eden  │Survivor0│Survivor1│  │  오래 살아남은 객체│
│  │       │  (S0)   │  (S1)   │  │                   │
│  └───────┴─────────┴─────────┘  │                   │
│     새 객체 생성 여기            │                   │
└─────────────────────────────────┴──────────────────┘
```

**동작 과정**:

```
1. 새 객체 → Eden에 생성

2. Eden이 가득 참 → Minor GC 발생
   - 살아남은 객체 → Survivor 영역으로 이동
   - Eden 비움

3. Survivor에서도 살아남은 객체는 age 증가
   - S0 ↔ S1 번갈아가며 이동

4. age가 임계값 넘으면 → Old Generation으로 이동 (Promotion)

5. Old Generation이 가득 참 → Major GC (Full GC) 발생
   - Stop-the-World: 모든 스레드 정지
   - 시간이 오래 걸림 → 성능에 영향
```

```
시간 흐름:

[Eden 할당] → [Eden 가득] → [Minor GC] → [Survivor로 이동]
                                              ↓
                                     age 증가, 반복
                                              ↓
                                  [Old Generation 이동]
                                              ↓
                                [Old 가득] → [Major GC]
                                          (Stop-the-World)
```

| GC 종류 | 대상 | 속도 | STW 시간 |
|---------|------|------|---------|
| **Minor GC** | Young Generation | 빠름 (ms) | 짧음 |
| **Major GC (Full GC)** | Old Generation (+ Young) | 느림 (ms~s) | 길음 |

### G1 GC (Garbage First)

Java 9부터 기본 GC. 대용량 힙에 적합.

```
기존 GC: 연속된 Young/Old 영역

┌──────────Young──────────┬─────────Old─────────┐
│  Eden  │  S0  │  S1     │                      │
└─────────────────────────┴──────────────────────┘

G1 GC: 힙을 동일한 크기의 Region으로 분할

┌───┬───┬───┬───┬───┬───┬───┬───┐
│ E │ S │ O │ E │ O │ H │ E │ O │
├───┼───┼───┼───┼───┼───┼───┼───┤
│ O │ E │ E │ O │ S │ O │   │ E │
└───┴───┴───┴───┴───┴───┴───┴───┘

E = Eden, S = Survivor, O = Old, H = Humongous (큰 객체)
각 Region이 독립적으로 역할 변경 가능
```

**G1 GC 특징**:
- Heap을 동일 크기 Region(1~32MB)으로 분할
- 가비지가 가장 많은 Region부터 수거 (Garbage First)
- STW 시간 목표를 설정할 수 있다 (`-XX:MaxGCPauseMillis=200`)
- Mixed GC: Young + 일부 Old Region을 동시 수거

---

## GC 튜닝 기본

### 주요 JVM 옵션

```bash
# 힙 메모리 설정
java -Xms512m -Xmx1024m -jar app.jar

# -Xms: 초기 힙 크기 (start)
# -Xmx: 최대 힙 크기 (max)
# 보통 -Xms와 -Xmx를 같게 설정 (힙 리사이징 비용 방지)
```

```bash
# GC 로그 활성화 (Java 11+)
java -Xlog:gc*:file=gc.log:time -jar app.jar

# GC 종류 지정
java -XX:+UseG1GC -jar app.jar          # G1 GC (Java 9+ 기본)
java -XX:+UseZGC -jar app.jar           # Z GC (Java 15+, 초저지연)

# G1 GC 튜닝
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \         # 목표 STW 시간
     -XX:G1HeapRegionSize=16m \         # Region 크기
     -jar app.jar
```

### Spring Boot에서 GC 설정

```properties
# application.properties에서는 설정 불가
# Dockerfile이나 실행 스크립트에서 설정

# Dockerfile 예시
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]
```

### GC 로그 읽는 법

```
[0.013s][info][gc] Using G1
[1.234s][info][gc] GC(0) Pause Young (Normal) 24M->8M(256M) 5.123ms
                           ^타입          ^전->후(전체힙)    ^STW시간

[5.678s][info][gc] GC(5) Pause Full (Allocation Failure) 240M->180M(256M) 120.456ms
                          ^Full GC 발생! STW 120ms → 문제 가능성
```

> **실무 팁**: Full GC가 자주 발생하면 `-Xmx`를 늘리거나 메모리 누수를 확인해야 한다.

---

## Java 컬렉션 프레임워크

### 전체 구조

```
                    Iterable
                       │
                   Collection
                 ┌─────┼──────┐
                List   Set   Queue
                 │      │      │
             ArrayList HashSet LinkedList
             LinkedList TreeSet PriorityQueue
             Vector  LinkedHashSet

                    Map (별도 계층)
                     │
                 HashMap
                 TreeMap
                 LinkedHashMap
                 Hashtable
```

### List, Set, Map 비교

| 특성 | List | Set | Map |
|------|------|-----|-----|
| **순서** | 있음 (인덱스) | 없음 (LinkedHashSet 제외) | 없음 (LinkedHashMap 제외) |
| **중복** | 허용 | 불가 | Key 불가, Value 허용 |
| **null** | 허용 | 1개만 가능 (HashSet) | Key 1개 null (HashMap) |
| **대표 구현체** | ArrayList | HashSet | HashMap |
| **용도** | 순서가 중요한 데이터 | 중복 제거 | Key-Value 매핑 |

### List 구현체 비교

```java
// ArrayList: 내부적으로 배열 사용
List<String> arrayList = new ArrayList<>();
// 조회: O(1) - 인덱스로 바로 접근
// 추가/삭제(중간): O(n) - 요소 이동 필요

// LinkedList: 노드 연결 구조
List<String> linkedList = new LinkedList<>();
// 조회: O(n) - 처음부터 순회
// 추가/삭제(중간): O(1) - 노드 연결만 변경 (탐색은 별도)
```

```
ArrayList 내부:
[0][1][2][3][4][5][ ][ ][ ][ ]
 ↑ 인덱스로 바로 접근 (빠름)
 중간 삽입 시 뒤의 요소를 모두 이동 (느림)

LinkedList 내부:
[A]→[B]→[C]→[D]→[E]
 탐색 시 처음부터 순회 (느림)
 삽입 시 노드 연결만 변경 (빠름)
```

> **실무에서는 99% ArrayList를 사용한다.** LinkedList가 이론적으로 삽입/삭제에 유리하지만,
> CPU 캐시 효율과 실제 벤치마크에서 ArrayList가 거의 항상 더 빠르다.

### Set 구현체 비교

```java
// HashSet: 해시 테이블 기반. 순서 없음
Set<String> hashSet = new HashSet<>();

// LinkedHashSet: 삽입 순서 유지
Set<String> linkedHashSet = new LinkedHashSet<>();

// TreeSet: 정렬된 상태 유지 (Red-Black Tree)
Set<String> treeSet = new TreeSet<>();
```

| 구현체 | 내부 구조 | 순서 | 시간 복잡도 |
|--------|----------|------|-----------|
| **HashSet** | HashMap | 없음 | O(1) |
| **LinkedHashSet** | LinkedHashMap | 삽입 순서 | O(1) |
| **TreeSet** | Red-Black Tree | 정렬 순서 | O(log n) |

---

## HashMap 내부 동작 원리

### 기본 구조

HashMap은 **배열 + 연결 리스트(또는 트리)** 구조이다.

```
HashMap 내부:

index   buckets
  0  → [Entry] → [Entry] → null
  1  → null
  2  → [Entry] → null
  3  → null
  ...
  15 → [Entry] → [Entry] → [Entry] → null

Entry = (key, value, hash, next)
```

### put() 동작 과정

```java
map.put("name", "홍길동");
```

```
1. "name".hashCode() 호출 → 해시값 계산 (예: 3373707)
2. 해시값을 배열 크기로 나눈 나머지 → 버킷 인덱스
   3373707 % 16 = 11  (기본 배열 크기 16)
3. index 11 버킷에 Entry("name", "홍길동") 저장
```

### 해시 충돌 (Hash Collision)

서로 다른 key가 같은 버킷 인덱스에 매핑되는 경우:

```java
// "Aa".hashCode() == "BB".hashCode() == 2112
map.put("Aa", "값1");   // index 0에 저장
map.put("BB", "값2");   // index 0에 저장 → 충돌!
```

**해결: Separate Chaining (연결 리스트)**

```
index 0: [Aa:값1] → [BB:값2] → null

조회 시:
1. "BB".hashCode()로 index 0 찾음
2. 연결 리스트 순회하면서 key.equals() 비교
3. "BB".equals("Aa") → false → 다음
4. "BB".equals("BB") → true → "값2" 반환
```

### Java 8 트리화 (Treeification)

**문제**: 하나의 버킷에 충돌이 많으면 연결 리스트가 길어져 O(n) 탐색.

**해결**: Java 8부터 **한 버킷에 8개 이상** 쌓이면 연결 리스트를 **Red-Black Tree**로 변환.

```
충돌 7개 이하: LinkedList (O(n))

index 5: [A] → [B] → [C] → [D] → [E] → [F] → [G]

충돌 8개 이상: Red-Black Tree로 변환 (O(log n))

index 5:        [D]
              /     \
           [B]       [F]
          /   \     /   \
        [A]  [C]  [E]  [G]
                          \
                          [H]
```

| 조건 | 자료구조 | 시간 복잡도 |
|------|---------|-----------|
| 버킷 내 8개 미만 | LinkedList | O(n) |
| 버킷 내 8개 이상 | Red-Black Tree | O(log n) |
| 버킷 내 6개 이하로 줄면 | 다시 LinkedList | O(n) |

### HashMap 리사이징

```
기본 배열 크기: 16
Load Factor: 0.75 (기본값)

저장된 Entry 수 > 16 * 0.75 = 12개 넘으면
→ 배열 크기를 2배로 늘림 (16 → 32)
→ 모든 Entry의 해시값을 다시 계산하여 재배치 (rehashing)
→ 비용이 크므로 초기 크기를 적절히 설정하는 것이 중요
```

```java
// 예상 데이터 100개라면, 리사이징 방지를 위해:
// 100 / 0.75 = 134 → 다음 2의 거듭제곱인 256
Map<String, Object> map = new HashMap<>(256);
```

---

## equals()와 hashCode() 관계

### 계약 (Contract)

```
규칙 1: equals()가 true이면 → hashCode()도 같아야 한다
규칙 2: hashCode()가 같아도 → equals()가 false일 수 있다 (충돌)
규칙 3: equals()가 false이면 → hashCode()는 같을 수도, 다를 수도 있다
```

```
           equals() == true
    ┌───────────────────────────┐
    │  hashCode() 반드시 동일     │  ← 규칙 1 (필수!)
    └───────────────────────────┘

           hashCode() 동일
    ┌───────────────────────────┐
    │  equals()가 true일 수도      │  ← 규칙 2 (충돌 가능)
    │  equals()가 false일 수도     │
    └───────────────────────────┘
```

### 왜 둘 다 재정의해야 하는가?

```java
public class User {
    private String id;
    private String name;

    // equals()만 재정의하고 hashCode()를 안 하면?
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
}
```

```java
User user1 = new User("1", "홍길동");
User user2 = new User("1", "홍길동");

user1.equals(user2);  // true (id가 같으니까)

Set<User> set = new HashSet<>();
set.add(user1);
set.contains(user2);  // false!! (hashCode가 다르니까)
```

```
HashSet.contains() 동작:
1. user2.hashCode() → 789 (Object의 기본 hashCode)
2. 789에 해당하는 버킷 확인 → 비어있음
3. → false 반환 (equals()까지 가보지도 못함!)

원인: user1.hashCode()는 123, user2.hashCode()는 789
      서로 다른 버킷에 있으므로 equals() 비교 기회조차 없다
```

### 올바른 구현

```java
public class User {
    private String id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);  // equals에서 사용한 필드로 hashCode 생성
    }
}
```

```java
User user1 = new User("1", "홍길동");
User user2 = new User("1", "홍길동");

user1.equals(user2);     // true
user1.hashCode() == user2.hashCode();  // true

Set<User> set = new HashSet<>();
set.add(user1);
set.contains(user2);     // true! (같은 버킷 → equals 비교 → true)
```

> **핵심**: HashMap, HashSet 등 Hash 기반 컬렉션은 먼저 hashCode()로 버킷을 찾고,
> 그 안에서 equals()로 비교한다. 둘 다 재정의하지 않으면 제대로 동작하지 않는다.

---

## 실무 연결 / Spring Boot 연결

### JVM 메모리와 Spring Boot

```
Spring Boot 앱 실행 시 JVM 메모리 사용:

Method Area:
  - @Controller, @Service, @Repository 클래스 정보
  - Bean 정의 메타데이터

Heap:
  - Spring IoC 컨테이너 (ApplicationContext)
  - 싱글톤 Bean 객체들
  - 요청 처리 중 생성되는 DTO, Entity 객체

Stack:
  - 각 요청을 처리하는 Tomcat 스레드의 메서드 호출 스택
  - Controller → Service → Repository 호출 스택
```

### OOM (OutOfMemoryError) 대응

```java
// 흔한 OOM 시나리오
// 1. Heap 부족
java.lang.OutOfMemoryError: Java heap space
// → -Xmx 늘리거나 메모리 누수 확인

// 2. 너무 많은 객체를 한 번에 로딩
List<User> allUsers = userRepository.findAll();  // 100만 건?
// → 페이징 처리: userRepository.findAll(PageRequest.of(0, 100))

// 3. Stack Overflow
public void recursive() { recursive(); }  // 무한 재귀
// java.lang.StackOverflowError
// → -Xss로 스택 크기 조정 (근본적으로는 코드 수정)
```

### 컬렉션 선택 가이드 (실무)

```java
// 대부분의 경우
List<User> users = new ArrayList<>();          // 리스트는 ArrayList

// 중복 제거가 필요하면
Set<String> uniqueTags = new HashSet<>();      // Set은 HashSet

// Key-Value 저장
Map<Long, User> userCache = new HashMap<>();   // Map은 HashMap

// 순서가 필요한 Map
Map<String, Object> orderedMap = new LinkedHashMap<>();

// 정렬이 필요한 Set
Set<Integer> sortedScores = new TreeSet<>();

// 스레드 안전이 필요하면
Map<String, Object> concurrentMap = new ConcurrentHashMap<>();
// (Hashtable은 사용하지 않는다 - 성능이 낮음)
```

---

## 면접 핵심 정리

**Q: JVM 메모리 구조를 설명해주세요**
> JVM 메모리는 크게 5가지 영역으로 나뉩니다.
> 모든 스레드가 공유하는 Method Area(클래스 정보, static 변수)와 Heap(객체 저장),
> 스레드마다 독립적인 Stack(지역 변수, 메서드 호출), PC Register(현재 명령어 주소),
> Native Method Stack(네이티브 메서드 호출)이 있습니다.
> GC는 Heap 영역에서 참조되지 않는 객체를 제거합니다.

**Q: GC 동작 원리를 설명해주세요**
> GC는 Mark & Sweep 알고리즘을 기반으로 동작합니다.
> GC Root(Stack 변수, Static 변수 등)에서 참조를 따라가 도달 가능한 객체를 Mark하고,
> Mark되지 않은 객체를 Sweep(제거)합니다.
> Heap은 Young Generation과 Old Generation으로 나뉘어,
> 새 객체는 Young의 Eden에 생성되고, Minor GC를 거쳐 살아남으면 Old로 이동합니다.
> Old가 가득 차면 Full GC가 발생하는데, 이때 Stop-the-World로 모든 스레드가 멈추므로
> 성능에 큰 영향을 줍니다.

**Q: HashMap의 시간 복잡도와 내부 동작을 설명해주세요**
> HashMap은 평균 O(1)의 시간 복잡도를 가집니다.
> 내부적으로 배열과 연결 리스트(또는 트리)를 사용합니다.
> key의 hashCode()로 배열 인덱스(버킷)를 결정하고, 충돌 시 같은 버킷에 연결 리스트로 연결합니다.
> Java 8부터는 한 버킷에 8개 이상 충돌하면 Red-Black Tree로 변환하여
> 최악의 경우에도 O(log n)을 보장합니다.

**Q: equals()와 hashCode()를 왜 함께 재정의해야 하나요?**
> HashMap, HashSet 등 Hash 기반 컬렉션은 먼저 hashCode()로 버킷을 찾고,
> 같은 버킷 내에서 equals()로 동일 객체를 판별합니다.
> equals()만 재정의하면 논리적으로 같은 객체라도 hashCode()가 달라
> 다른 버킷에 저장되어 HashSet에서 중복으로 인식하지 못합니다.
> 반드시 equals()에서 사용한 필드를 기반으로 hashCode()도 재정의해야 합니다.

**Q: ArrayList와 LinkedList의 차이는?**
> ArrayList는 내부적으로 배열을 사용해 인덱스 접근이 O(1)이지만 중간 삽입/삭제는 O(n)입니다.
> LinkedList는 노드 연결 구조로 삽입/삭제 자체는 O(1)이지만 탐색이 O(n)입니다.
> 실무에서는 CPU 캐시 효율 때문에 대부분 ArrayList를 사용합니다.
