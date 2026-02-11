# SQL 기초

## SQL 분류

| 분류 | 의미 | 명령어 |
|------|------|--------|
| **DDL** (Data Definition) | 구조 정의 | CREATE, ALTER, DROP, TRUNCATE |
| **DML** (Data Manipulation) | 데이터 조작 | SELECT, INSERT, UPDATE, DELETE |
| **DCL** (Data Control) | 권한 관리 | GRANT, REVOKE |
| **TCL** (Transaction Control) | 트랜잭션 | COMMIT, ROLLBACK, SAVEPOINT |

---

## DDL - 테이블 정의

```sql
-- 테이블 생성
CREATE TABLE users (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    age        INT DEFAULT 0,
    role       ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 테이블 수정
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
ALTER TABLE users DROP COLUMN phone;
ALTER TABLE users MODIFY COLUMN name VARCHAR(100);

-- 테이블 삭제
DROP TABLE users;       -- 테이블 자체 삭제
TRUNCATE TABLE users;   -- 데이터만 전부 삭제 (구조 유지, 빠름)
```

### DELETE vs TRUNCATE vs DROP

| 명령 | 동작 | 롤백 | 속도 |
|------|------|------|------|
| DELETE | 행 단위 삭제, WHERE 가능 | 가능 | 느림 |
| TRUNCATE | 전체 데이터 삭제 | 불가 | 빠름 |
| DROP | 테이블 자체 삭제 | 불가 | 즉시 |

---

## DML - 데이터 조작

### INSERT

```sql
-- 단건 삽입
INSERT INTO users (name, email, age) VALUES ('John', 'john@test.com', 25);

-- 다건 삽입
INSERT INTO users (name, email, age) VALUES
    ('Alice', 'alice@test.com', 30),
    ('Bob', 'bob@test.com', 28);
```

### SELECT

```sql
-- 기본 조회
SELECT * FROM users;
SELECT name, email FROM users;

-- 조건
SELECT * FROM users WHERE age >= 25 AND role = 'USER';
SELECT * FROM users WHERE name LIKE '%oh%';       -- 포함
SELECT * FROM users WHERE age IN (25, 28, 30);     -- 목록
SELECT * FROM users WHERE age BETWEEN 20 AND 30;   -- 범위
SELECT * FROM users WHERE phone IS NULL;            -- NULL 확인

-- 정렬
SELECT * FROM users ORDER BY age DESC, name ASC;

-- 페이징
SELECT * FROM users ORDER BY id DESC LIMIT 10 OFFSET 20;
-- 21번째부터 10개 조회
```

### UPDATE

```sql
UPDATE users SET age = 26 WHERE id = 1;
UPDATE users SET role = 'ADMIN' WHERE email = 'john@test.com';

-- ⚠️ WHERE 없으면 전체 수정! 반드시 WHERE 확인
```

### DELETE

```sql
DELETE FROM users WHERE id = 1;

-- ⚠️ WHERE 없으면 전체 삭제!
```

---

## 집계 함수 & GROUP BY

### 집계 함수

| 함수 | 설명 |
|------|------|
| COUNT(*) | 행 개수 |
| SUM(col) | 합계 |
| AVG(col) | 평균 |
| MAX(col) | 최대값 |
| MIN(col) | 최소값 |

```sql
SELECT COUNT(*) FROM users;                        -- 전체 유저 수
SELECT AVG(age) FROM users;                        -- 평균 나이
SELECT role, COUNT(*) FROM users GROUP BY role;    -- 역할별 인원 수
```

### GROUP BY + HAVING

```sql
-- 역할별 인원 수가 5명 이상인 역할만
SELECT role, COUNT(*) as cnt
FROM users
GROUP BY role
HAVING cnt >= 5;
```

**WHERE vs HAVING**:
- WHERE: GROUP BY **이전**에 행을 필터링
- HAVING: GROUP BY **이후**에 그룹을 필터링

```sql
-- 25세 이상 유저 중에서, 역할별 인원이 3명 이상인 역할
SELECT role, COUNT(*) as cnt
FROM users
WHERE age >= 25          -- 먼저 필터링
GROUP BY role
HAVING cnt >= 3;         -- 그룹 필터링
```

---

## 서브쿼리 (Subquery)

```sql
-- 평균 나이보다 많은 유저
SELECT * FROM users
WHERE age > (SELECT AVG(age) FROM users);

-- 게시글이 있는 유저만
SELECT * FROM users
WHERE id IN (SELECT DISTINCT author_id FROM posts);

-- 각 유저의 게시글 수와 함께 조회
SELECT u.name,
       (SELECT COUNT(*) FROM posts p WHERE p.author_id = u.id) as post_count
FROM users u;
```

### EXISTS vs IN

```sql
-- EXISTS: 서브쿼리에 결과가 존재하는지 확인 (대용량에 유리)
SELECT * FROM users u
WHERE EXISTS (SELECT 1 FROM posts p WHERE p.author_id = u.id);

-- IN: 값 목록에 포함되는지 확인 (소량에 유리)
SELECT * FROM users
WHERE id IN (SELECT author_id FROM posts);
```

---

## SQL 실행 순서

작성 순서와 실행 순서가 다릅니다.

```
작성 순서:  SELECT → FROM → WHERE → GROUP BY → HAVING → ORDER BY → LIMIT

실행 순서:  FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT
            ①       ②        ③          ④        ⑤        ⑥         ⑦
```

이게 중요한 이유:
```sql
-- 이건 에러! (WHERE는 SELECT보다 먼저 실행)
SELECT name, age * 2 AS double_age
FROM users
WHERE double_age > 50;   -- ❌ 별칭을 WHERE에서 못 씀

-- 이건 됨! (HAVING은 SELECT 이후)
SELECT role, COUNT(*) AS cnt
FROM users
GROUP BY role
HAVING cnt >= 3;         -- ✅ (MySQL은 허용, 표준은 아님)
```

---

## 면접 예상 질문

1. **WHERE와 HAVING의 차이는?**
   - WHERE: 행 필터링 (GROUP BY 전) / HAVING: 그룹 필터링 (GROUP BY 후)

2. **DELETE, TRUNCATE, DROP의 차이는?**
   - DELETE: 행 단위, 롤백 가능 / TRUNCATE: 전체 삭제 / DROP: 테이블 삭제

3. **SQL 실행 순서는?**
   - FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT
