# JOIN

## 개념

두 개 이상의 테이블을 **연결하여** 데이터를 조회하는 것.

### 예제 테이블

```sql
-- users                        -- posts
| id | name  | age |            | id | title    | author_id |
|----|-------|-----|            |----|----------|-----------|
| 1  | Alice | 25  |            | 1  | Hello    | 1         |
| 2  | Bob   | 30  |            | 2  | World    | 1         |
| 3  | Carol | 28  |            | 3  | Bye      | 2         |
| 4  | Dave  | 35  |            | 4  | Orphan   | NULL      |
```

---

## JOIN 종류

### 1. INNER JOIN

**양쪽 모두 일치하는** 행만 반환.

```sql
SELECT u.name, p.title
FROM users u
INNER JOIN posts p ON u.id = p.author_id;

-- 결과:
-- Alice | Hello
-- Alice | World
-- Bob   | Bye

-- Carol(게시글 없음)과 Orphan(author_id=NULL) 제외
```

```
users          posts
┌─────┐      ┌─────┐
│     │▓▓▓▓▓▓│     │   ▓ = INNER JOIN 결과
│     │▓▓▓▓▓▓│     │
└─────┘      └─────┘
```

### 2. LEFT JOIN (LEFT OUTER JOIN)

**왼쪽 테이블 전체** + 오른쪽 일치하는 것. 없으면 NULL.

```sql
SELECT u.name, p.title
FROM users u
LEFT JOIN posts p ON u.id = p.author_id;

-- 결과:
-- Alice | Hello
-- Alice | World
-- Bob   | Bye
-- Carol | NULL    ← 게시글 없지만 유저는 나옴
-- Dave  | NULL
```

```
users          posts
┌─────┐      ┌─────┐
│▓▓▓▓▓│▓▓▓▓▓▓│     │   왼쪽 전체 + 교집합
│▓▓▓▓▓│▓▓▓▓▓▓│     │
└─────┘      └─────┘
```

**실무에서 가장 많이 사용**. "게시글이 없는 유저도 보여줘야 할 때"

### 3. RIGHT JOIN (RIGHT OUTER JOIN)

**오른쪽 테이블 전체** + 왼쪽 일치하는 것. 없으면 NULL.

```sql
SELECT u.name, p.title
FROM users u
RIGHT JOIN posts p ON u.id = p.author_id;

-- 결과:
-- Alice  | Hello
-- Alice  | World
-- Bob    | Bye
-- NULL   | Orphan   ← author_id=NULL인 게시글도 나옴
```

> 실무에서는 LEFT JOIN으로 테이블 순서를 바꾸는 게 일반적

### 4. FULL OUTER JOIN

**양쪽 모두** 반환. 일치하지 않는 것은 NULL.

```sql
-- MySQL은 FULL OUTER JOIN 미지원 → UNION으로 구현
SELECT u.name, p.title
FROM users u LEFT JOIN posts p ON u.id = p.author_id
UNION
SELECT u.name, p.title
FROM users u RIGHT JOIN posts p ON u.id = p.author_id;
```

### 5. CROSS JOIN

**모든 조합** (카르테시안 곱). N × M 행.

```sql
SELECT u.name, p.title
FROM users u CROSS JOIN posts p;

-- users 4행 × posts 4행 = 16행
```

### 6. SELF JOIN

**같은 테이블**을 자기 자신과 조인.

```sql
-- 같은 나이의 유저 쌍 찾기
SELECT a.name, b.name
FROM users a
JOIN users b ON a.age = b.age AND a.id < b.id;

-- 직원-매니저 관계
SELECT e.name AS employee, m.name AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

---

## JOIN 비교 정리

| JOIN | 결과 | NULL |
|------|------|------|
| INNER | 양쪽 일치만 | 없음 |
| LEFT | 왼쪽 전체 + 일치 | 오른쪽 없으면 NULL |
| RIGHT | 오른쪽 전체 + 일치 | 왼쪽 없으면 NULL |
| FULL OUTER | 양쪽 전체 | 양쪽 없으면 NULL |
| CROSS | 모든 조합 | 없음 |

---

## 실전 패턴

### 1. 게시글 목록 + 작성자 이름

```sql
SELECT p.id, p.title, u.name AS author_name, p.created_at
FROM posts p
INNER JOIN users u ON p.author_id = u.id
ORDER BY p.created_at DESC
LIMIT 20;
```

### 2. 유저별 게시글 수

```sql
SELECT u.name, COUNT(p.id) AS post_count
FROM users u
LEFT JOIN posts p ON u.id = p.author_id
GROUP BY u.id, u.name
ORDER BY post_count DESC;

-- LEFT JOIN이라 게시글 0개인 유저도 포함
```

### 3. 게시글이 없는 유저 찾기

```sql
-- 방법 1: LEFT JOIN + IS NULL
SELECT u.name
FROM users u
LEFT JOIN posts p ON u.id = p.author_id
WHERE p.id IS NULL;

-- 방법 2: NOT EXISTS (대용량에서 더 효율적)
SELECT u.name
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM posts p WHERE p.author_id = u.id);
```

### 4. 다중 JOIN

```sql
-- 게시글 + 작성자 + 댓글 수
SELECT p.title,
       u.name AS author,
       COUNT(c.id) AS comment_count
FROM posts p
INNER JOIN users u ON p.author_id = u.id
LEFT JOIN comments c ON p.id = c.post_id
GROUP BY p.id, p.title, u.name;
```

---

## JOIN 성능 팁

```
1. JOIN 컬럼에 인덱스가 있는지 확인 (FK에 인덱스 필수)
2. 필요한 컬럼만 SELECT (SELECT * 지양)
3. WHERE 조건으로 먼저 행을 줄인 후 JOIN
4. 대용량: EXISTS가 IN보다 빠른 경우가 많음
```

---

## 면접 예상 질문

1. **INNER JOIN과 LEFT JOIN의 차이는?**
   - INNER: 양쪽 일치만 / LEFT: 왼쪽 전체 + 오른쪽 일치, 없으면 NULL

2. **JOIN 시 성능을 높이려면?**
   - JOIN 컬럼에 인덱스, 필요한 컬럼만 조회, WHERE로 먼저 필터링

3. **CROSS JOIN은 언제 쓰나요?**
   - 모든 조합이 필요할 때 (예: 사이즈 × 색상 조합)
