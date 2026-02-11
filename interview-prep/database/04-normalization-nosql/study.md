# 4. 정규화와 NoSQL

---

## 정규화 (Normalization)

정규화는 **데이터 중복을 제거하고 무결성을 보장하기 위해 테이블을 분리하는 과정**이다.
이상 현상(Anomaly)을 방지하는 것이 목적이다.

### 이상 현상 (Anomaly)

정규화가 안 된 테이블에서 발생하는 문제들이다.

```
비정규화 테이블 (학생_수강):
+--------+--------+--------+----------+--------+
| 학생ID | 이름   | 과목   | 교수     | 학점   |
+--------+--------+--------+----------+--------+
|  1     | 김철수 | DB     | 이교수   | A      |
|  1     | 김철수 | OS     | 박교수   | B+     |
|  2     | 이영희 | DB     | 이교수   | A+     |
|  3     | 박민수 | 알고리즘| 최교수   | B      |
+--------+--------+--------+----------+--------+
```

| 이상 현상 | 설명 | 예시 |
|-----------|------|------|
| 삽입 이상 | 불필요한 데이터 없이 삽입 불가 | 새 과목 추가 시 학생 없으면 삽입 불가 |
| 갱신 이상 | 일부만 수정하면 불일치 | 이교수 이름 변경 시 두 행 모두 수정해야 함 |
| 삭제 이상 | 필요한 데이터도 함께 삭제 | 박민수 삭제 시 알고리즘 과목 정보도 사라짐 |

---

### 제1정규형 (1NF)

**모든 속성의 값이 원자값(Atomic Value)**이어야 한다.
한 칸에 하나의 값만 들어간다.

```
1NF 위반:
+--------+--------+----------------+
| 학생ID | 이름   | 전화번호       |
+--------+--------+----------------+
|  1     | 김철수 | 010-1111, 010-2222 |  ← 한 칸에 두 개 값!
+--------+--------+----------------+

1NF 충족:
+--------+--------+--------------+
| 학생ID | 이름   | 전화번호     |
+--------+--------+--------------+
|  1     | 김철수 | 010-1111     |
|  1     | 김철수 | 010-2222     |
+--------+--------+--------------+
(또는 전화번호 별도 테이블로 분리)
```

> **핵심**: 반복 그룹이나 다중 값을 제거한다.

---

### 제2정규형 (2NF)

1NF를 만족하고, **부분 함수 종속을 제거**한다.
기본키의 일부에만 종속되는 컬럼을 분리한다.

```
2NF 위반 (기본키: 학생ID + 과목):

+--------+--------+--------+----------+
| 학생ID | 과목   | 이름   | 교수     |
+--------+--------+--------+----------+
  (PK1)   (PK2)

  이름 → 학생ID에만 종속 (부분 종속!)
  교수 → 과목에만 종속   (부분 종속!)

2NF 충족 (테이블 분리):

  학생 테이블:          수강 테이블:          과목 테이블:
  +--------+--------+  +--------+--------+  +--------+--------+
  | 학생ID | 이름   |  | 학생ID | 과목   |  | 과목   | 교수   |
  +--------+--------+  +--------+--------+  +--------+--------+
```

> **핵심**: 복합 키의 "일부"에만 종속되는 컬럼을 별도 테이블로 분리한다.

---

### 제3정규형 (3NF)

2NF를 만족하고, **이행 함수 종속을 제거**한다.
A → B → C일 때, A → C를 제거한다.

```
3NF 위반:

+--------+--------+--------+----------+
| 학생ID | 학과   | 학과장 | 학과전화 |
+--------+--------+--------+----------+

  학생ID → 학과 → 학과장 (이행 종속!)
  학생ID → 학과 → 학과전화 (이행 종속!)

3NF 충족 (테이블 분리):

  학생 테이블:              학과 테이블:
  +--------+--------+      +--------+--------+----------+
  | 학생ID | 학과   |      | 학과   | 학과장 | 학과전화 |
  +--------+--------+      +--------+--------+----------+
```

> **핵심**: 기본키가 아닌 속성이 다른 비키 속성을 결정하면 분리한다.

---

### BCNF (Boyce-Codd Normal Form)

3NF를 만족하고, **모든 결정자가 후보키**여야 한다.
3NF보다 더 엄격한 조건이다.

```
BCNF 위반:

  수강 (학생ID, 과목, 교수)

  함수 종속:
  - {학생ID, 과목} → 교수  (학생이 과목을 들으면 교수 결정)
  - 교수 → 과목            (교수는 하나의 과목만 담당)

  "교수"가 결정자이지만 후보키가 아님 → BCNF 위반

BCNF 충족:

  수강 테이블:              교수_과목 테이블:
  +--------+--------+      +--------+--------+
  | 학생ID | 교수   |      | 교수   | 과목   |
  +--------+--------+      +--------+--------+
```

### 정규화 단계 정리

```
원본 테이블
    │
    ▼  [1NF] 원자값으로 분리
    │
    ▼  [2NF] 부분 함수 종속 제거 (복합키의 일부에 종속)
    │
    ▼  [3NF] 이행 함수 종속 제거 (A→B→C에서 A→C)
    │
    ▼  [BCNF] 모든 결정자가 후보키
```

| 정규형 | 핵심 | 제거 대상 |
|--------|------|-----------|
| 1NF | 원자값 | 반복 그룹, 다중 값 |
| 2NF | 완전 함수 종속 | 부분 함수 종속 |
| 3NF | 이행 종속 제거 | A→B→C 관계 |
| BCNF | 결정자 = 후보키 | 후보키 아닌 결정자 |

---

## 반정규화 (Denormalization)

정규화의 반대. **의도적으로 중복을 허용하여 읽기 성능을 높이는 것**이다.

### 언제 반정규화하는가?

```
정규화된 테이블에서 JOIN이 너무 많아 성능이 느릴 때:

  주문 조회 쿼리 (정규화 상태):
  SELECT o.*, c.name, p.name, a.address
  FROM orders o
  JOIN customers c ON o.customer_id = c.id
  JOIN products p ON o.product_id = p.id
  JOIN addresses a ON c.address_id = a.id;
  → 3번 JOIN, 테이블 4개

  반정규화 후:
  SELECT order_id, customer_name, product_name, address
  FROM order_summary;
  → JOIN 없이 1개 테이블에서 조회
```

### 반정규화 기법

| 기법 | 설명 | 예시 |
|------|------|------|
| 테이블 병합 | 자주 함께 조회하는 테이블 합침 | 주문+주문상세 병합 |
| 컬럼 중복 | 자주 참조하는 컬럼 복사 | 주문 테이블에 고객명 추가 |
| 파생 컬럼 | 계산 결과를 미리 저장 | 주문 테이블에 총액 컬럼 |
| 요약 테이블 | 집계 결과를 별도 저장 | 일별 매출 요약 테이블 |

### 트레이드오프

```
정규화                          반정규화
─────                          ──────
데이터 무결성 높음              읽기 성능 높음
중복 없음                      중복 허용 (갱신 비용 증가)
쓰기 성능 좋음                 JOIN 최소화
JOIN 많음 (읽기 느림)           데이터 불일치 위험

선택 기준:
├─ 쓰기가 많은 시스템 → 정규화 유지 (OLTP)
└─ 읽기가 많은 시스템 → 반정규화 고려 (분석, 조회 위주)
```

---

## RDBMS vs NoSQL

### RDBMS (관계형 데이터베이스)

```
특징:
- 정해진 스키마(테이블 구조)
- SQL로 질의
- ACID 트랜잭션 보장
- 정규화로 중복 제거
- 수직 확장(Scale-Up) 위주

예: MySQL, PostgreSQL, Oracle, MS SQL Server
```

### NoSQL (Not Only SQL)

```
특징:
- 유연한 스키마 (스키마리스 또는 동적 스키마)
- SQL 외 다양한 질의 방식
- 분산 환경에 최적화
- 수평 확장(Scale-Out) 용이
- 대용량 데이터 처리에 강함

등장 배경:
- SNS, IoT 등 비정형 데이터 폭증
- 수억 건 이상 데이터의 수평 확장 필요
- 유연한 스키마 변경 요구
```

### 비교 표

| 구분 | RDBMS | NoSQL |
|------|-------|-------|
| 스키마 | 고정 (테이블, 컬럼 정의) | 유연 (동적 변경 가능) |
| 질의 | SQL | 각 DB별 질의 방식 |
| 확장 | 수직 확장 (Scale-Up) | 수평 확장 (Scale-Out) |
| 트랜잭션 | ACID 완벽 보장 | 대부분 Eventually Consistent |
| 관계 | JOIN으로 관계 표현 | 관계 표현 어려움 (내장/중복) |
| 일관성 | 강한 일관성 | 최종 일관성 (Eventual) |
| 적합한 경우 | 구조화된 데이터, 복잡한 쿼리 | 비정형 데이터, 대규모 분산 |

```
CAP 정리 (분산 시스템의 한계):

  세 가지를 동시에 만족할 수 없다 (최대 2개):

       Consistency
       (일관성)
        /\
       /  \
      /    \
     /  CA  \
    /________\
   /\   CP  /\
  /  \     /  \
 / AP \   /    \
/______\_/______\
Availability  Partition
(가용성)      Tolerance
              (분할 내성)

  RDBMS:  CA (일관성 + 가용성, 분할 내성 약함)
  NoSQL:  CP 또는 AP (분산 환경에 최적화)
```

---

## NoSQL 종류

### 1. Key-Value Store

```
구조: Key → Value (단순한 쌍)

  "user:1"     → {"name": "김철수", "age": 30}
  "session:abc" → {"userId": 1, "expires": "2024-12-31"}
  "cache:post:5" → "<html>...</html>"

특징:
- 가장 단순한 구조
- 매우 빠른 읽기/쓰기 (O(1))
- 복잡한 쿼리 불가
- 캐시, 세션 저장에 적합

예: Redis, Memcached, Amazon DynamoDB
```

### 2. Document Store

```
구조: Key → Document (JSON/BSON)

  {
    "_id": "user_001",
    "name": "김철수",
    "address": {               ← 중첩 객체 가능
      "city": "서울",
      "zip": "06000"
    },
    "orders": [                ← 배열 가능
      {"item": "노트북", "price": 1500000},
      {"item": "마우스", "price": 50000}
    ]
  }

특징:
- JSON과 유사한 문서 구조
- 스키마가 유연 (문서마다 필드가 달라도 됨)
- 중첩, 배열 등 복잡한 구조 표현 가능
- 문서 내 필드로 쿼리 가능

예: MongoDB, CouchDB, Amazon DocumentDB
```

### 3. Column-Family Store

```
구조: Row Key → Column Family → Column

  Row Key    Column Family: 기본정보        Column Family: 주소
  ─────────  ──────────────────────────    ─────────────────────
  user:1     name=김철수 | age=30          city=서울 | zip=06000
  user:2     name=이영희 | age=25 | dept=개발   city=부산
  user:3     name=박민수                   city=대구 | zip=41000

특징:
- 행마다 컬럼이 달라도 됨
- 대규모 데이터의 분산 저장에 강함
- 쓰기 성능이 매우 좋음
- 시계열 데이터, 로그 분석에 적합

예: Apache Cassandra, HBase, Google Bigtable
```

### 4. Graph Database

```
구조: Node(노드) + Edge(관계) + Property(속성)

  (김철수)──[팔로우]──>(이영희)
     |                    |
  [좋아요]             [작성]
     |                    |
     v                    v
  (게시글A)          (게시글B)──[댓글]──>(댓글1)

특징:
- 관계 자체를 일급 시민으로 다룸
- 복잡한 관계 탐색이 매우 빠름
- SNS, 추천 시스템, 경로 탐색에 적합
- JOIN 대비 관계 탐색 성능 우수

예: Neo4j, Amazon Neptune, ArangoDB
```

### NoSQL 종류 비교

| 종류 | 데이터 모델 | 장점 | 적합한 경우 |
|------|------------|------|------------|
| Key-Value | Key-Value 쌍 | 초고속 읽기/쓰기 | 캐시, 세션 |
| Document | JSON 문서 | 유연한 스키마 | 콘텐츠 관리, 카탈로그 |
| Column-Family | 컬럼 패밀리 | 대규모 쓰기 | 로그, 시계열 데이터 |
| Graph | 노드+관계 | 관계 탐색 최적 | SNS, 추천, 경로 탐색 |

---

## Redis 기본 개념과 사용 사례

### Redis란?

**Re**mote **Di**ctionary **S**erver의 약자.
**인메모리(In-Memory) Key-Value 데이터 저장소**다.

```
일반 DB:                    Redis:
  디스크에 저장               메모리(RAM)에 저장
  읽기: ~10ms                읽기: ~0.1ms (100배 빠름)
  영속성 우선                 속도 우선
```

### Redis 핵심 특징

| 특징 | 설명 |
|------|------|
| 인메모리 | RAM에 저장, 초고속 |
| 다양한 자료구조 | String, List, Set, Hash, Sorted Set 등 |
| 싱글 스레드 | 하나의 스레드로 명령어 처리 (원자성 보장) |
| 영속성 옵션 | RDB(스냅샷), AOF(로그) 방식 지원 |
| TTL 지원 | Key에 만료 시간 설정 가능 |
| Pub/Sub | 메시지 발행/구독 기능 |

### Redis 주요 자료구조

```
String:  SET user:1:name "김철수"
         GET user:1:name → "김철수"

List:    LPUSH queue "task1" "task2"
         RPOP queue → "task1"

Set:     SADD tags:post1 "java" "spring" "redis"
         SMEMBERS tags:post1 → {"java", "spring", "redis"}

Hash:    HSET user:1 name "김철수" age 30
         HGET user:1 name → "김철수"
         HGETALL user:1 → {"name": "김철수", "age": "30"}

Sorted Set:  ZADD ranking 100 "김철수" 95 "이영희" 88 "박민수"
             ZREVRANGE ranking 0 2 → ["김철수", "이영희", "박민수"]
```

### Redis 대표적 사용 사례

```
1. 캐시 (Cache)
   DB 조회 결과를 Redis에 임시 저장 → 반복 조회 시 DB 안 거침

   요청 → Redis 확인 → 있으면 바로 반환 (Cache Hit)
                     → 없으면 DB 조회 → Redis 저장 → 반환 (Cache Miss)

2. 세션 저장소 (Session Store)
   여러 서버에서 세션 공유 가능 (분산 환경)

   [서버1] ──┐
   [서버2] ──┼── [Redis 세션] → 어느 서버로 가도 같은 세션
   [서버3] ──┘

3. 분산 락 (Distributed Lock)
   여러 서버에서 동일 자원 접근 제어

4. 실시간 랭킹 (Sorted Set 활용)
   게임 점수, 인기 게시글 순위

5. 메시지 큐 (List, Pub/Sub 활용)
   비동기 작업 처리
```

---

## Spring Boot / 실무 연결

### Spring Boot에서 Redis 캐시 사용

#### 의존성 추가

```groovy
// build.gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
}
```

#### 설정

```yaml
# application.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1시간 (밀리초)
```

#### 캐시 적용

```java
@Configuration
@EnableCaching
public class CacheConfig {
    // @EnableCaching으로 캐시 기능 활성화
}

@Service
public class ProductService {

    // 캐시에 저장 (key = productId)
    @Cacheable(value = "products", key = "#productId")
    public Product findById(Long productId) {
        // 처음 호출 시 DB 조회 → Redis에 저장
        // 이후 호출 시 Redis에서 바로 반환 (DB 안 거침)
        return productRepository.findById(productId).orElseThrow();
    }

    // 캐시 갱신 (데이터 수정 시)
    @CachePut(value = "products", key = "#product.id")
    public Product update(Product product) {
        return productRepository.save(product);
    }

    // 캐시 삭제
    @CacheEvict(value = "products", key = "#productId")
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    // 전체 캐시 삭제
    @CacheEvict(value = "products", allEntries = true)
    public void clearCache() {
    }
}
```

#### 캐시 동작 흐름

```
findById(1L) 호출:

  1차 호출:
  [Controller] → [Service] → Redis 확인 → MISS
                           → DB 조회 (SELECT * FROM products WHERE id=1)
                           → Redis 저장 (key: "products::1")
                           → 응답 반환

  2차 호출:
  [Controller] → [Service] → Redis 확인 → HIT
                           → Redis에서 바로 반환 (DB 안 거침!)
                           → 응답 반환 (훨씬 빠름)
```

### Redis를 활용한 세션 관리

```java
// build.gradle
// implementation 'org.springframework.session:spring-session-data-redis'

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)  // 30분
public class SessionConfig {
}
```

```yaml
# application.yml
spring:
  session:
    store-type: redis
```

```
분산 환경에서 세션 공유:

  기존 (서버 메모리 세션):
  [사용자] → [로드밸런서] → [서버1: 세션 있음]
                         → [서버2: 세션 없음!] ← 문제!

  Redis 세션:
  [사용자] → [로드밸런서] → [서버1] ─┐
                         → [서버2] ─┼── [Redis: 공유 세션]
                         → [서버3] ─┘
                         어느 서버로 가도 세션 유지!
```

### JPA에서의 정규화 반영

```java
// 정규화된 엔티티 설계 (3NF)
@Entity
public class Order {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;   // FK로 참조 (중복 없음)

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    private LocalDateTime orderDate;
}

@Entity
public class Customer {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;     // 주소 별도 테이블 (3NF)
}

// 반정규화 예시: 조회 전용 뷰 테이블
@Entity
@Table(name = "order_summary")
@Immutable  // 읽기 전용 엔티티
public class OrderSummary {
    @Id
    private Long orderId;
    private String customerName;   // 중복 저장 (반정규화)
    private String productName;    // 중복 저장 (반정규화)
    private Long totalAmount;      // 파생 컬럼 (반정규화)
    private LocalDateTime orderDate;
}
```

---

## 면접 핵심 정리

**Q: 정규화란 무엇이고, 왜 하나요?**
> 정규화는 데이터 중복을 제거하고 무결성을 보장하기 위해 테이블을 분리하는 과정입니다.
> 삽입/갱신/삭제 이상 현상을 방지합니다. 1NF는 원자값 보장, 2NF는 부분 함수 종속 제거,
> 3NF는 이행 함수 종속 제거, BCNF는 모든 결정자가 후보키여야 합니다.

**Q: 반정규화는 언제 하나요?**
> 정규화로 테이블이 과도하게 분리되어 JOIN이 많아지고 읽기 성능이 저하될 때 고려합니다.
> 조회가 빈번하고 데이터 변경이 적은 경우, 자주 함께 조회되는 데이터를 한 테이블에 모으거나
> 계산 결과를 미리 저장합니다. 데이터 불일치 위험이 있으므로 신중하게 결정해야 합니다.

**Q: RDBMS와 NoSQL의 차이는?**
> RDBMS는 고정 스키마, SQL 질의, ACID 트랜잭션을 보장하며 구조화된 데이터에 적합합니다.
> NoSQL은 유연한 스키마, 수평 확장이 용이하며 대용량 비정형 데이터 처리에 강합니다.
> CAP 정리에 따라 RDBMS는 일관성과 가용성을, NoSQL은 분산 환경에서의 확장성을 중시합니다.
> 서비스 특성에 따라 적절히 선택하거나 병행 사용합니다.

**Q: Redis는 왜 빠르고, 어떤 경우에 사용하나요?**
> Redis는 데이터를 메모리에 저장하여 디스크 I/O 없이 처리하므로 매우 빠릅니다.
> 싱글 스레드로 동작하여 컨텍스트 스위칭 비용이 없고 원자성이 보장됩니다.
> 캐시, 세션 저장, 분산 락, 실시간 랭킹, 메시지 큐 등에 활용합니다.
> Spring Boot에서는 @Cacheable, @CacheEvict 등으로 간편하게 캐시를 적용할 수 있습니다.

**Q: Spring에서 Redis 캐시를 적용할 때 주의할 점은?**
> 캐시 무효화 전략이 중요합니다. 데이터 수정 시 @CachePut이나 @CacheEvict로 캐시를 갱신하지 않으면
> 오래된 데이터가 반환됩니다. TTL을 적절히 설정하고, 캐시 대상은 자주 조회되지만 변경이 적은 데이터를
> 선정해야 합니다. 직렬화/역직렬화 비용과 메모리 용량도 고려해야 합니다.

**Q: NoSQL의 종류와 각각의 적합한 사용 사례는?**
> Key-Value(Redis)는 캐시와 세션에, Document(MongoDB)는 유연한 스키마의 콘텐츠 관리에,
> Column-Family(Cassandra)는 대규모 쓰기가 많은 시계열 데이터에,
> Graph(Neo4j)는 SNS나 추천 시스템 같은 복잡한 관계 탐색에 적합합니다.
