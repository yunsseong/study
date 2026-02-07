# NoSQL vs RDB

## RDB (Relational Database)

### 특징
- **테이블(관계)** 기반, 행과 열로 구성
- **스키마 고정**: 테이블 구조가 미리 정의됨
- **SQL**로 데이터 조작
- **ACID** 트랜잭션 보장
- **JOIN**으로 테이블 간 관계 표현

### 대표 DBMS

| DBMS | 특징 |
|------|------|
| **MySQL** | 가장 널리 사용, 웹 서비스 표준 |
| **PostgreSQL** | 기능 풍부, 복잡한 쿼리에 강함 |
| **Oracle** | 엔터프라이즈, 대용량 |
| MariaDB | MySQL 포크, 호환 |

---

## NoSQL (Not Only SQL)

### 특징
- **비관계형**, 다양한 데이터 모델
- **스키마 유연**: 구조 변경이 자유로움
- **수평 확장(Scale-Out)** 에 유리
- **ACID 대신 BASE** (일부 NoSQL)
- JOIN 없음 → **비정규화된 데이터** 저장

### NoSQL 유형

| 유형 | 구조 | 대표 | 사용 사례 |
|------|------|------|----------|
| **Key-Value** | {key: value} | Redis, DynamoDB | 캐시, 세션, 설정 |
| **Document** | JSON 형태 문서 | MongoDB, CouchDB | 유연한 스키마, CMS |
| **Column-Family** | 컬럼 그룹 | Cassandra, HBase | 대용량 로그, 시계열 |
| **Graph** | 노드 + 관계 | Neo4j | 소셜 네트워크, 추천 |

---

### Key-Value Store (Redis)

```
"user:1"        → {"name": "John", "age": 25}
"session:abc"   → {"userId": 1, "loginAt": "..."}
"cache:posts:1" → "{id: 1, title: 'Hello'}"
```

```python
# Redis 기본 사용
import redis
r = redis.Redis()

r.set("user:1", "John")       # 저장
r.get("user:1")               # 조회 → "John"
r.setex("token:abc", 3600, "userId:1")  # TTL 1시간
r.delete("user:1")            # 삭제
```

**Redis 활용**:
- **캐시**: DB 조회 결과 캐싱 → 응답 속도 향상
- **세션 저장소**: 다중 서버 세션 공유
- **Rate Limiting**: API 요청 횟수 제한
- **랭킹**: Sorted Set으로 실시간 순위
- **메시지 큐**: Pub/Sub

### Document Store (MongoDB)

```javascript
// 컬렉션(= 테이블)에 JSON 문서 저장
db.users.insertOne({
    name: "John",
    age: 25,
    address: {              // 중첩 객체 가능
        city: "Seoul",
        zip: "12345"
    },
    hobbies: ["reading", "coding"]  // 배열 가능
});

// 유연한 스키마: 같은 컬렉션에 다른 구조 가능
db.users.insertOne({
    name: "Alice",
    age: 30
    // address 없어도 됨!
});
```

---

## RDB vs NoSQL 비교

| 비교 | RDB | NoSQL |
|------|-----|-------|
| 데이터 모델 | 테이블 (행/열) | 다양 (문서, KV, 그래프) |
| 스키마 | 고정 (엄격) | 유연 (동적) |
| 확장 방식 | **수직 확장** (Scale-Up) | **수평 확장** (Scale-Out) |
| 트랜잭션 | ACID (강력) | BASE (일부, 유연) |
| JOIN | 지원 | 미지원 (비정규화) |
| 일관성 | 강한 일관성 | 최종 일관성 (Eventual) |
| 적합한 데이터 | 정형, 관계 복잡 | 비정형, 대용량, 빠른 변경 |

### ACID vs BASE

| ACID (RDB) | BASE (NoSQL) |
|-----------|-------------|
| Atomicity | **B**asically **A**vailable (기본적으로 가용) |
| Consistency | **S**oft state (유연한 상태) |
| Isolation | **E**ventual consistency (최종적 일관성) |
| Durability | |

```
ACID: "항상 정확한 데이터" → 은행, 결제
BASE: "대부분 맞고, 조금 뒤에 정확해짐" → SNS 좋아요 수, 조회수
```

---

## 수직 확장 vs 수평 확장

```
수직 확장 (Scale-Up):
  서버 1대의 성능을 높임 (CPU, RAM 추가)
  ├── 한계: 물리적 한계 있음
  └── RDB에 적합

수평 확장 (Scale-Out):
  서버를 여러 대 추가
  ├── 한계: 거의 무한 확장 가능
  └── NoSQL에 적합 (샤딩이 쉬움)
```

### RDB의 수평 확장이 어려운 이유

```
JOIN은 여러 서버에 분산된 데이터를 합쳐야 함 → 네트워크 비용
트랜잭션은 여러 서버 간 일관성 유지 필요 → 분산 트랜잭션 복잡

NoSQL은:
- JOIN이 없음 → 한 서버에서 완결
- 트랜잭션이 약함 → 분산이 단순
```

---

## CAP 정리

분산 시스템에서 3가지를 **동시에 모두 만족할 수 없다**.

| 속성 | 의미 |
|------|------|
| **C** (Consistency) | 모든 노드가 같은 데이터를 봄 |
| **A** (Availability) | 모든 요청에 응답함 |
| **P** (Partition Tolerance) | 네트워크 단절에도 동작 |

```
분산 시스템에서 P는 필수 → C와 A 중 택1

CP (일관성 우선): MongoDB, HBase
  → 네트워크 장애 시 일부 요청 거부 (일관성 유지)

AP (가용성 우선): Cassandra, DynamoDB
  → 네트워크 장애 시에도 응답 (일시적 불일치 허용)

CA: 단일 서버 RDB (분산이 아닌 경우)
```

---

## 언제 어떤 걸 쓰나?

```
RDB를 선택하는 경우:
├── 데이터 관계가 복잡 (JOIN 필요)
├── 트랜잭션이 중요 (결제, 은행, 재고)
├── 데이터 정합성이 최우선
├── 스키마가 안정적
└── 예: 이커머스, 금융, ERP

NoSQL을 선택하는 경우:
├── 대용량 데이터 + 빠른 읽기/쓰기
├── 스키마가 자주 변경
├── 수평 확장이 필요
├── 비정형 데이터 (로그, 센서)
└── 예: 실시간 분석, IoT, 소셜 미디어

둘 다 쓰는 경우 (가장 일반적):
├── RDB: 핵심 비즈니스 데이터 (유저, 주문, 결제)
├── Redis: 캐시, 세션
├── MongoDB: 로그, 비정형 데이터
└── Elasticsearch: 검색
```

---

## 면접 예상 질문

1. **RDB와 NoSQL의 차이는?**
   - 스키마, 확장 방식, 트랜잭션, JOIN 지원 여부

2. **언제 NoSQL을 선택하나요?**
   - 대용량, 유연한 스키마, 수평 확장 필요 시

3. **Redis를 어디에 사용하나요?**
   - 캐시, 세션 저장, Rate Limiting, 랭킹

4. **CAP 정리란?**
   - 분산 시스템에서 C, A, P 중 2개만 동시 만족 가능

5. **최종 일관성(Eventual Consistency)이란?**
   - 일시적 불일치 허용, 시간이 지나면 모든 노드가 동일한 데이터를 가짐
