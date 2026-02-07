# 쿼리 최적화

## 왜 중요한가?

```
백엔드 성능 문제의 80%는 쿼리에서 발생한다.

코드를 아무리 최적화해도 쿼리가 느리면 서비스가 느리다.
서버를 늘려도 DB가 병목이면 의미가 없다.
```

---

## EXPLAIN (실행 계획)

쿼리가 **어떻게 실행되는지** 확인하는 도구. 최적화의 시작점.

```sql
EXPLAIN SELECT * FROM users WHERE email = 'john@test.com';
```

### EXPLAIN 주요 컬럼 (MySQL)

| 컬럼 | 의미 | 중요 포인트 |
|------|------|-----------|
| **type** | 접근 방식 | ALL이면 풀스캔 → 인덱스 필요 |
| **key** | 사용된 인덱스 | NULL이면 인덱스 미사용 |
| **rows** | 예상 스캔 행 수 | 적을수록 좋음 |
| **Extra** | 추가 정보 | Using filesort, Using temporary 주의 |
| possible_keys | 사용 가능한 인덱스 | |
| filtered | 필터링 비율 | |

### type 성능 순서 (좋은 순)

```
system > const > eq_ref > ref > range > index > ALL

const:   PK/UNIQUE로 1행 조회 (가장 빠름)
eq_ref:  JOIN에서 PK/UNIQUE로 1행 매칭
ref:     인덱스로 여러 행 매칭
range:   인덱스 범위 스캔 (BETWEEN, >, <)
index:   인덱스 풀 스캔 (테이블 풀스캔보단 나음)
ALL:     테이블 풀 스캔 (가장 느림) ← 이거 나오면 개선 필요
```

### 실전 예시

```sql
-- 느린 쿼리
EXPLAIN SELECT * FROM posts WHERE author_id = 1 ORDER BY created_at DESC;

-- 결과:
-- type: ALL (풀스캔!), key: NULL, rows: 100000

-- 인덱스 추가
CREATE INDEX idx_posts_author_created ON posts (author_id, created_at);

-- 다시 확인
EXPLAIN SELECT * FROM posts WHERE author_id = 1 ORDER BY created_at DESC;
-- type: ref, key: idx_posts_author_created, rows: 50
```

---

## 슬로우 쿼리 (Slow Query)

### 슬로우 쿼리 로그 설정 (MySQL)

```sql
-- 슬로우 쿼리 로그 활성화
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 1초 이상 걸리는 쿼리 기록

-- 확인
SHOW VARIABLES LIKE 'slow_query%';
```

---

## 자주 하는 실수와 개선

### 1. SELECT *

```sql
-- ❌ 모든 컬럼 조회 (불필요한 데이터 전송)
SELECT * FROM users;

-- ✅ 필요한 컬럼만
SELECT id, name, email FROM users;
```

### 2. N+1 문제 (JPA/ORM 사용 시)

```java
// ❌ N+1 발생
List<Post> posts = postRepository.findAll();  // 쿼리 1번
for (Post post : posts) {
    post.getAuthor().getName();  // 각 게시글마다 쿼리 1번씩 → N번
}
// 총 쿼리: 1 + N번

// ✅ Fetch Join으로 해결
@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthor();  // 쿼리 1번으로 해결
```

```python
# FastAPI + SQLAlchemy에서 N+1
# ❌
posts = db.query(Post).all()
for post in posts:
    print(post.author.name)  # 각각 쿼리 발생

# ✅ joinedload로 해결
from sqlalchemy.orm import joinedload
posts = db.query(Post).options(joinedload(Post.author)).all()
```

### 3. 인덱스 무효화

```sql
-- ❌ 컬럼에 함수 적용 → 인덱스 안 탐
SELECT * FROM users WHERE YEAR(created_at) = 2025;

-- ✅ 범위 조건으로 변경
SELECT * FROM users
WHERE created_at >= '2025-01-01' AND created_at < '2026-01-01';

-- ❌ 타입 불일치
SELECT * FROM users WHERE phone = 01012345678;  -- 숫자

-- ✅ 타입 맞추기
SELECT * FROM users WHERE phone = '01012345678';  -- 문자열
```

### 4. OFFSET 페이징

```sql
-- ❌ 대용량에서 느림 (OFFSET이 크면 앞의 행을 다 스캔)
SELECT * FROM posts ORDER BY id DESC LIMIT 20 OFFSET 100000;
-- 100,000행을 읽고 버린 후 20행 반환

-- ✅ 커서 기반 페이징 (No Offset)
SELECT * FROM posts WHERE id < 900000 ORDER BY id DESC LIMIT 20;
-- 인덱스로 바로 시작점 찾음 → 일정한 성능
```

### 5. 불필요한 정렬

```sql
-- ❌ 인덱스 없는 컬럼으로 정렬 → filesort 발생
SELECT * FROM posts ORDER BY view_count DESC LIMIT 20;
-- Extra: Using filesort (메모리/디스크에서 정렬 → 느림)

-- ✅ 방법 1: 인덱스 추가
CREATE INDEX idx_posts_viewcount ON posts (view_count);

-- ✅ 방법 2: 캐시 활용 (인기 게시글은 Redis에 캐싱)
```

### 6. WHERE 절 IN 대량

```sql
-- ❌ IN에 수천 개
SELECT * FROM users WHERE id IN (1, 2, 3, ..., 10000);

-- ✅ 임시 테이블 또는 JOIN
CREATE TEMPORARY TABLE temp_ids (id BIGINT);
INSERT INTO temp_ids VALUES (1), (2), ..., (10000);
SELECT u.* FROM users u JOIN temp_ids t ON u.id = t.id;
```

---

## 최적화 체크리스트

```
1단계: EXPLAIN으로 실행 계획 확인
  □ type이 ALL인가? → 인덱스 추가
  □ key가 NULL인가? → 인덱스가 안 타고 있음
  □ rows가 너무 많은가? → 조건 확인

2단계: 인덱스 점검
  □ WHERE 절 컬럼에 인덱스가 있는가?
  □ JOIN 컬럼(FK)에 인덱스가 있는가?
  □ ORDER BY 컬럼에 인덱스가 있는가?
  □ 복합 인덱스의 최좌선 원칙을 지키는가?

3단계: 쿼리 개선
  □ SELECT *을 쓰고 있지 않은가?
  □ N+1 문제가 발생하고 있지 않은가?
  □ 컬럼에 함수를 적용하고 있지 않은가?
  □ OFFSET 페이징을 쓰고 있지 않은가?

4단계: 아키텍처 개선
  □ 자주 조회하는 데이터는 캐시(Redis)로?
  □ 읽기/쓰기 분리 (레플리카)?
  □ 반정규화가 필요한가?
```

---

## 면접 예상 질문

1. **쿼리 최적화를 어떻게 하나요?**
   - EXPLAIN으로 실행 계획 확인 → 인덱스 점검 → 쿼리 구조 개선

2. **N+1 문제란? 해결 방법은?**
   - ORM에서 연관 엔티티를 각각 쿼리하는 문제 → Fetch Join, EntityGraph

3. **OFFSET 페이징이 느린 이유와 대안은?**
   - OFFSET 만큼 행을 읽고 버림 → 커서 기반 페이징 (No Offset)

4. **EXPLAIN에서 어떤 것을 확인하나요?**
   - type(접근 방식), key(인덱스), rows(스캔 행 수), Extra

5. **실무에서 슬로우 쿼리를 어떻게 발견하나요?**
   - 슬로우 쿼리 로그, APM 모니터링, EXPLAIN 분석
