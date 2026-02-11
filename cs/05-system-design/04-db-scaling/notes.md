# DB 확장 (Database Scaling)

## 왜 필요한가?

```
서비스 성장 → 데이터 증가 + 트래픽 증가
→ 단일 DB로 감당 불가
→ DB 확장 전략 필요

수직 확장 (Scale-Up): 서버 성능 향상 → 한계 있음
수평 확장 (Scale-Out): 서버 여러 대 → DB에서는 어렵지만 필요
```

---

## 레플리케이션 (Replication)

### 개념

데이터를 **여러 DB 서버에 복제**하여 읽기 성능과 가용성을 높임.

```
        쓰기(Write)
           ↓
      [Master DB] ──복제──→ [Slave DB 1] ← 읽기(Read)
                  ──복제──→ [Slave DB 2] ← 읽기(Read)
                  ──복제──→ [Slave DB 3] ← 읽기(Read)

Master: 쓰기 담당 (INSERT, UPDATE, DELETE)
Slave: 읽기 담당 (SELECT) → 읽기 부하 분산
```

### Master-Slave 구조

```
장점:
├── 읽기 성능 향상 (Slave 추가로 읽기 분산)
├── 고가용성 (Master 장애 시 Slave를 Master로 승격)
├── 백업 용이 (Slave에서 백업 → Master 부하 없음)
└── 지역 분산 (지역별 Slave 배치 → 지연 시간 감소)

단점:
├── 복제 지연 (Replication Lag): Slave가 최신이 아닐 수 있음
├── 쓰기 확장은 안 됨 (Master는 1대)
└── 일관성 문제: 쓰기 직후 읽기 시 옛날 데이터 가능
```

### 복제 지연 문제와 해결

```
시나리오:
  1. 사용자가 프로필 수정 (Master에 쓰기)
  2. 바로 프로필 조회 (Slave에서 읽기)
  3. 아직 복제 안 됨 → 수정 전 데이터가 보임!

해결:
  1. 본인 데이터 조회는 Master에서 (Read-Your-Writes)
  2. 쓰기 직후 일정 시간은 Master에서 읽기
  3. 동기 복제 사용 (성능 저하 있음)
```

```python
# 읽기/쓰기 분리 예시
class DatabaseRouter:
    def db_for_read(self, model):
        return 'replica'  # Slave에서 읽기

    def db_for_write(self, model):
        return 'primary'  # Master에 쓰기
```

---

## 파티셔닝 (Partitioning)

### 개념

하나의 큰 테이블을 **여러 파티션으로 분할**. 같은 DB 내에서 분할.

### 수평 파티셔닝 (행 분할)

```
posts 테이블 (1억 행) → 파티션 분할

posts_2024: created_at이 2024년인 행
posts_2025: created_at이 2025년인 행
posts_2026: created_at이 2026년인 행

→ 2026년 게시글 조회 시 posts_2026만 스캔 → 빠름
```

```sql
-- MySQL 파티셔닝
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT,
    title VARCHAR(255),
    created_at DATE,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027)
);

-- 파티션 프루닝: 해당 파티션만 스캔
SELECT * FROM posts WHERE created_at >= '2026-01-01';
-- → p2026 파티션만 읽음
```

### 수직 파티셔닝 (열 분할)

```
users 테이블:
  id | name | email | bio(TEXT) | profile_image(BLOB)

→ 자주 쓰는 컬럼과 큰 컬럼 분리

users:          id | name | email
users_profile:  id | bio | profile_image

→ users 테이블 조회가 가벼워짐
```

### 파티셔닝 전략

| 전략 | 기준 | 예시 |
|------|------|------|
| **Range** | 범위 | 날짜별 (2024, 2025, 2026) |
| **List** | 목록 | 지역별 (서울, 부산, 대구) |
| **Hash** | 해시값 | id % 4 → 4개 파티션 |

---

## 샤딩 (Sharding)

### 개념

데이터를 **여러 DB 서버에 분산 저장**. 파티셔닝의 확장 (서버가 다름).

```
파티셔닝: 1대 DB 내에서 테이블 분할
샤딩:     여러 대 DB에 데이터 분산

         [Shard 1]          [Shard 2]          [Shard 3]
         user_id 1~1000     user_id 1001~2000  user_id 2001~3000
         별도 DB 서버        별도 DB 서버        별도 DB 서버
```

### 샤딩 전략

#### 1. 범위 기반 (Range Based)

```
user_id 1~1000     → Shard 1
user_id 1001~2000  → Shard 2
user_id 2001~3000  → Shard 3

장점: 구현 단순, 범위 쿼리 용이
단점: 핫스팟 (최신 데이터에 쓰기 집중)
```

#### 2. 해시 기반 (Hash Based)

```
hash(user_id) % 3 = 0 → Shard 0
hash(user_id) % 3 = 1 → Shard 1
hash(user_id) % 3 = 2 → Shard 2

장점: 균등 분배
단점: 샤드 추가 시 리밸런싱 필요, 범위 쿼리 어려움
```

#### 3. 디렉토리 기반 (Directory Based)

```
라우팅 테이블로 관리.

user_id 1   → Shard 2
user_id 2   → Shard 1
user_id 3   → Shard 3
...

장점: 유연한 배치
단점: 라우팅 테이블이 단일 장애점(SPOF)
```

### 샤딩의 어려움

```
1. Cross-Shard JOIN 불가
   → user가 Shard1, order가 Shard2에 있으면 JOIN 불가
   → 애플리케이션 레벨에서 합쳐야 함

2. 분산 트랜잭션
   → 여러 샤드에 걸친 트랜잭션 어려움
   → 2PC(Two-Phase Commit) 필요 → 성능 저하

3. 리밸런싱
   → 샤드 추가 시 데이터 재분배 필요
   → 일관된 해싱으로 최소화

4. 글로벌 유니크 ID
   → Auto Increment가 샤드 별로 독립
   → UUID, Snowflake ID 등 분산 ID 생성 필요
```

### Snowflake ID

```
트위터에서 만든 분산 ID 생성 방식.

64비트:
[1bit 부호][41bit 타임스탬프][10bit 머신ID][12bit 시퀀스]

특징:
- 시간순 정렬 가능
- 초당 약 400만 개 생성 가능
- 여러 서버에서 독립 생성, 충돌 없음
```

---

## 확장 전략 요약

```
1단계: 읽기가 느림
  → 레플리케이션 (읽기/쓰기 분리)

2단계: 테이블이 너무 큼
  → 파티셔닝 (같은 DB 내 분할)

3단계: 단일 DB가 한계
  → 샤딩 (여러 DB 서버 분산)
```

```
                    서비스 성장
                       ↓
              ┌── 읽기 부하 → 레플리케이션
              │
    단일 DB → ├── 테이블 크기 → 파티셔닝
              │
              └── 전체 한계 → 샤딩
```

---

## 면접 예상 질문

1. **레플리케이션이란?**
   - 데이터를 여러 서버에 복제, 읽기 분산 + 고가용성

2. **파티셔닝과 샤딩의 차이는?**
   - 파티셔닝: 1대 DB 내 테이블 분할 / 샤딩: 여러 DB 서버에 데이터 분산

3. **샤딩의 어려운 점은?**
   - Cross-Shard JOIN, 분산 트랜잭션, 리밸런싱, 글로벌 ID

4. **복제 지연(Replication Lag) 문제는?**
   - 쓰기 직후 Slave에서 옛날 데이터 → Read-Your-Writes로 해결

5. **DB 확장 순서를 설명해주세요**
   - 인덱스/쿼리 최적화 → 캐시(Redis) → 레플리케이션 → 파티셔닝 → 샤딩
