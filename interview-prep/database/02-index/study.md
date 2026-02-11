# 2. 인덱스 (Index)

---

## 인덱스란? 왜 필요한가?

인덱스는 **데이터베이스 테이블의 검색 속도를 높이기 위한 자료 구조**다.
책의 맨 뒤에 있는 **색인(찾아보기)**과 같은 원리다.

```
인덱스가 없을 때 (Full Table Scan):

  "김철수를 찾아라"
  → 1번 행 확인 → 아님
  → 2번 행 확인 → 아님
  → 3번 행 확인 → 아님
  → ...
  → 99,999번 행 확인 → 아님
  → 100,000번 행 확인 → 찾았다!
  → 10만 번 비교 (O(N))

인덱스가 있을 때:

  "김철수를 찾아라"
  → 인덱스에서 "김" 탐색
  → "김철" 범위로 좁힘
  → "김철수" 위치 바로 찾음
  → 약 17번 비교 (O(log N), log2(100,000) ≈ 17)
```

| 구분 | Full Table Scan | 인덱스 사용 |
|------|----------------|------------|
| 10만 건 | 10만 번 비교 | ~17번 비교 |
| 100만 건 | 100만 번 비교 | ~20번 비교 |
| 1,000만 건 | 1,000만 번 비교 | ~23번 비교 |

> 데이터가 많을수록 인덱스의 효과는 극적으로 커진다.

---

## B-Tree vs B+Tree 구조

### B-Tree (Balanced Tree)

모든 노드에 **키와 데이터 포인터**를 저장하는 균형 트리다.

```
                    [30 | 60]
                   /    |    \
            [10|20]  [40|50]  [70|80]
            / | \    / | \    / | \
           D  D  D  D  D  D  D  D  D
           (D = 실제 데이터 포인터)
```

- 모든 리프 노드가 같은 깊이 (균형 유지)
- 루트에서 어떤 데이터를 찾아도 같은 시간 소요
- 중간 노드에서도 데이터를 찾을 수 있음

### B+Tree (MySQL InnoDB가 사용하는 구조)

**리프 노드에만 데이터**를 저장하고, 리프 노드끼리 **연결 리스트로 연결**한다.

```
              [30 | 60]              ← 루트 (키만 저장, 데이터 없음)
             /    |    \
      [10|20]  [40|50]  [70|80]      ← 내부 (키만 저장, 데이터 없음)
       / | \    / | \    / | \
      v  v  v  v  v  v  v  v  v
     [10]-[20]-[30]-[40]-[50]-[60]-[70]-[80]  ← 리프 (키 + 데이터)
      ←→   ←→   ←→   ←→   ←→   ←→   ←→       ← 리프끼리 연결
```

### B-Tree vs B+Tree 비교

| 구분 | B-Tree | B+Tree |
|------|--------|--------|
| 데이터 위치 | 모든 노드 | 리프 노드만 |
| 리프 연결 | 없음 | 연결 리스트 |
| 범위 검색 | 비효율 (트리 재탐색) | 효율적 (리프 순회) |
| 디스크 I/O | 상대적으로 많음 | 적음 (내부 노드에 더 많은 키) |
| 균일한 성능 | 찾는 위치에 따라 다름 | 항상 동일 (리프까지) |

왜 MySQL은 B+Tree를 쓸까?
1. **범위 검색에 강하다**: `WHERE age BETWEEN 20 AND 30` 같은 쿼리에서 리프 노드를 순서대로 읽으면 됨
2. **내부 노드에 키를 더 많이 저장**: 데이터 포인터가 없으니 더 많은 키를 담을 수 있고, 트리 높이가 낮아짐
3. **순차 접근이 빠르다**: 리프 노드가 연결되어 있어 ORDER BY에 유리

---

## 클러스터드 vs 논클러스터드 인덱스

### 클러스터드 인덱스 (Clustered Index)

**실제 데이터가 인덱스 순서대로 물리적으로 정렬**되어 저장된다.
테이블당 **하나만** 가능하다 (보통 PK가 클러스터드 인덱스).

```
클러스터드 인덱스 (PK = id):

  인덱스 트리                  실제 데이터 (정렬 상태)
     [50]                     id=1  | 김철수 | ...
    /    \                    id=2  | 이영희 | ...
  [25]  [75]                  id=3  | 박민수 | ...
  / \   / \                   ...
 리프=실제 데이터 페이지       id=100 | 정수진 | ...
                              (id 순서대로 물리적 정렬)
```

> 비유: 사전에서 단어가 알파벳 순서로 **직접 정렬**되어 있는 것

### 논클러스터드 인덱스 (Non-Clustered Index)

**별도의 인덱스 구조가 실제 데이터의 위치를 가리킨다**.
테이블에 **여러 개** 만들 수 있다.

```
논클러스터드 인덱스 (name 컬럼):

  인덱스 트리                  실제 데이터 (PK 순서)
    [박]                      id=1  | 김철수 | ...
   /    \                     id=2  | 이영희 | ...
 [김] [이,정]                 id=3  | 박민수 | ...
  |     |  |                  id=4  | 정수진 | ...
  ↓     ↓  ↓
 id=1  id=2 id=4              (포인터로 실제 데이터 위치 참조)
```

> 비유: 책 뒤의 색인처럼 "김철수 → 42쪽"으로 위치를 **참조**하는 것

### 비교 정리

| 구분 | 클러스터드 인덱스 | 논클러스터드 인덱스 |
|------|-------------------|---------------------|
| 개수 | 테이블당 1개 | 여러 개 가능 |
| 데이터 정렬 | 물리적으로 정렬 | 별도 구조 (포인터) |
| 검색 속도 | 범위 검색 매우 빠름 | 포인터 참조 필요 (약간 느림) |
| INSERT 속도 | 정렬 유지 비용 있음 | 상대적으로 빠름 |
| MySQL PK | 자동으로 클러스터드 인덱스 | 일반 인덱스 |

---

## 복합 인덱스와 설계 원칙

### 복합 인덱스 (Composite Index)

**여러 컬럼을 하나의 인덱스로** 묶은 것이다.

```sql
-- 복합 인덱스 생성
CREATE INDEX idx_dept_name ON employees(department, name);
```

```
복합 인덱스 구조 (department, name):

  (개발팀, 김)
     /       \
(개발팀, 가) (기획팀, 나)
                  \
              (기획팀, 다)

정렬 순서: department 먼저 정렬 → 같은 department 안에서 name 정렬
```

### 최좌선 접두사 규칙 (Leftmost Prefix Rule)

복합 인덱스 `(A, B, C)`가 있을 때:

| 쿼리 조건 | 인덱스 사용 여부 | 이유 |
|-----------|-----------------|------|
| WHERE A = 1 | O | 첫 번째 컬럼 |
| WHERE A = 1 AND B = 2 | O | 왼쪽부터 순서대로 |
| WHERE A = 1 AND B = 2 AND C = 3 | O | 전부 사용 |
| WHERE B = 2 | X | A를 건너뜀 |
| WHERE B = 2 AND C = 3 | X | A를 건너뜀 |
| WHERE A = 1 AND C = 3 | 부분 | A만 사용, C는 못 사용 |

> **전화번호부 비유**: (성, 이름) 순서 인덱스에서 "이름만으로" 찾기는 불가능.
> "김" 씨 중에서 "철수"를 찾는 건 가능하지만, 전체에서 "철수"만 찾는 건 전체를 봐야 한다.

### 인덱스 설계 원칙

```
1. 카디널리티(Cardinality)가 높은 컬럼을 앞에 배치

   카디널리티 = 컬럼의 고유 값 수
   - 주민번호: 매우 높음 (거의 유니크)  → 인덱스 효과 큼
   - 성별:    매우 낮음 (M/F 두 가지)  → 인덱스 효과 작음
   - 부서:    중간 (10~50개)          → 적절

   복합 인덱스 순서: 카디널리티 높은 것 → 낮은 것
   좋음: (employee_id, department, status)
   나쁨: (status, department, employee_id)

2. WHERE 절에서 자주 사용되는 컬럼

3. JOIN 조건에 사용되는 FK 컬럼

4. 너무 많은 인덱스는 오히려 해롭다
   - INSERT/UPDATE/DELETE 시 인덱스도 갱신해야 함
   - 저장 공간을 추가로 사용
   - 일반적으로 테이블당 3~5개가 적절
```

---

## 인덱스를 안 타는 경우 (주의할 점)

아무리 인덱스를 만들어도 **다음 경우에는 인덱스를 사용하지 않는다**.

### 1. 인덱스 컬럼에 함수/연산 적용

```sql
-- 인덱스 안 탐 (컬럼에 함수 적용)
SELECT * FROM employees WHERE YEAR(created_at) = 2024;

-- 인덱스 탐 (범위 조건으로 변환)
SELECT * FROM employees
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01';
```

### 2. 묵시적 형 변환

```sql
-- phone_number가 VARCHAR인데 숫자로 비교
-- 인덱스 안 탐 (MySQL이 내부적으로 형 변환)
SELECT * FROM employees WHERE phone_number = 01012345678;

-- 인덱스 탐 (타입 일치)
SELECT * FROM employees WHERE phone_number = '01012345678';
```

### 3. LIKE '%검색어' (앞에 와일드카드)

```sql
-- 인덱스 안 탐 (앞 와일드카드)
SELECT * FROM employees WHERE name LIKE '%철수';

-- 인덱스 탐 (뒤 와일드카드)
SELECT * FROM employees WHERE name LIKE '김%';
```

### 4. OR 조건

```sql
-- 인덱스 활용이 제한적
SELECT * FROM employees WHERE department = '개발팀' OR name = '김철수';

-- 각각 인덱스 타도록 UNION 사용
SELECT * FROM employees WHERE department = '개발팀'
UNION
SELECT * FROM employees WHERE name = '김철수';
```

### 5. NOT, <>, != 조건

```sql
-- 인덱스 활용 어려움
SELECT * FROM employees WHERE status != 'DELETED';

-- 인덱스 활용 가능하도록 변경
SELECT * FROM employees WHERE status IN ('ACTIVE', 'SUSPENDED');
```

### 6. NULL 비교

```sql
-- IS NULL은 인덱스를 탈 수도 있고 안 탈 수도 있음 (DB에 따라 다름)
SELECT * FROM employees WHERE department IS NULL;
```

### 인덱스 안 타는 경우 정리

| 원인 | 예시 | 해결 방법 |
|------|------|-----------|
| 컬럼에 함수 | YEAR(date) = 2024 | 범위 조건으로 변환 |
| 형 변환 | varchar = 숫자 | 타입 맞추기 |
| 앞 와일드카드 | LIKE '%abc' | 풀텍스트 인덱스 또는 설계 변경 |
| OR 조건 | A OR B | UNION 사용 |
| 부정 조건 | != , NOT | IN 조건으로 변환 |
| 데이터 비율 | 전체의 20% 이상 | 옵티마이저가 Full Scan 선택 |

---

## Spring Boot / 실무 연결

### @Index 애노테이션으로 인덱스 설정

```java
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_department", columnList = "department"),
    @Index(name = "idx_dept_name", columnList = "department, name"),
    @Index(name = "idx_email", columnList = "email", unique = true)
})
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String department;

    @Column(unique = true)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 실행 계획 확인 (EXPLAIN)

```sql
-- 인덱스 사용 여부 확인
EXPLAIN SELECT * FROM employees WHERE department = '개발팀' AND name = '김철수';

-- 결과 해석:
-- type: ref (인덱스 사용)  vs  ALL (Full Table Scan)
-- key: idx_dept_name (사용된 인덱스)
-- rows: 3 (예상 조회 행 수)
```

```
EXPLAIN 결과의 type 컬럼 (좋은 순서):

  system > const > eq_ref > ref > range > index > ALL
  ←── 빠름                                  느림 ──→

  const:   PK/유니크 인덱스로 1건 조회
  eq_ref:  JOIN에서 PK로 1건 매칭
  ref:     인덱스로 여러 건 조회
  range:   인덱스 범위 검색 (BETWEEN, <, >)
  index:   인덱스 풀 스캔 (테이블은 안 읽음)
  ALL:     테이블 풀 스캔 (최악)
```

### Spring Data JPA에서 인덱스 활용하는 쿼리

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // department 인덱스 사용
    List<Employee> findByDepartment(String department);

    // 복합 인덱스 (department, name) 사용
    List<Employee> findByDepartmentAndName(String department, String name);

    // 인덱스 안 타는 케이스 주의!
    // name LIKE '%철수' → 앞 와일드카드로 인덱스 안 탐
    List<Employee> findByNameContaining(String name);

    // 인덱스 타는 케이스
    // name LIKE '김%' → 뒤 와일드카드로 인덱스 탐
    List<Employee> findByNameStartingWith(String prefix);
}
```

### 커버링 인덱스 (Covering Index)

```sql
-- 인덱스만으로 쿼리 결과를 반환 (테이블 접근 불필요)
-- 인덱스: (department, name)

-- 커버링 인덱스 적용됨 (SELECT 컬럼이 모두 인덱스에 포함)
SELECT department, name FROM employees WHERE department = '개발팀';

-- 커버링 인덱스 적용 안 됨 (salary는 인덱스에 없음)
SELECT department, name, salary FROM employees WHERE department = '개발팀';
```

> 커버링 인덱스가 적용되면 EXPLAIN의 Extra에 `Using index`가 표시된다.

---

## 면접 핵심 정리

**Q: 인덱스란 무엇이고, 왜 필요한가요?**
> 인덱스는 테이블의 검색 속도를 높이기 위한 별도의 자료 구조입니다.
> B+Tree 구조를 사용하여 O(log N)의 시간 복잡도로 데이터를 찾을 수 있습니다.
> 100만 건 데이터에서 Full Scan은 100만 번, 인덱스는 약 20번 비교로 충분합니다.

**Q: B-Tree와 B+Tree의 차이는?**
> B-Tree는 모든 노드에 데이터를 저장하고, B+Tree는 리프 노드에만 데이터를 저장합니다.
> B+Tree는 리프 노드가 연결 리스트로 연결되어 범위 검색에 유리하고,
> 내부 노드에 키를 더 많이 저장하여 트리 높이가 낮아집니다.
> MySQL InnoDB는 B+Tree를 사용합니다.

**Q: 클러스터드 인덱스와 논클러스터드 인덱스의 차이는?**
> 클러스터드 인덱스는 실제 데이터가 인덱스 키 순서로 물리적으로 정렬됩니다.
> 테이블당 하나만 가능하며, MySQL에서는 PK가 자동으로 클러스터드 인덱스가 됩니다.
> 논클러스터드 인덱스는 별도 구조가 데이터 위치를 포인터로 가리킵니다. 여러 개 생성 가능합니다.

**Q: 복합 인덱스의 컬럼 순서가 왜 중요한가요?**
> 최좌선 접두사 규칙 때문입니다. 복합 인덱스 (A, B, C)에서 A를 건너뛰고
> B나 C만으로는 인덱스를 탈 수 없습니다. 카디널리티가 높고 자주 조건에
> 사용되는 컬럼을 앞에 배치해야 합니다.

**Q: 인덱스가 있는데도 안 타는 경우는?**
> 컬럼에 함수를 적용하거나, 묵시적 형 변환이 발생하거나, LIKE 앞에 와일드카드를 쓰거나,
> OR 조건을 사용하거나, 부정 조건(!=)을 사용하면 인덱스를 타지 않습니다.
> 또한 조회 결과가 전체 데이터의 약 20% 이상이면 옵티마이저가 Full Scan을 선택합니다.

**Q: 인덱스의 단점은?**
> INSERT, UPDATE, DELETE 시 인덱스도 함께 갱신해야 하므로 쓰기 성능이 저하됩니다.
> 추가 저장 공간을 사용하고, 너무 많은 인덱스는 옵티마이저의 실행 계획 수립을 복잡하게 합니다.
> 읽기와 쓰기 비율을 고려하여 적절히 설계해야 합니다.
