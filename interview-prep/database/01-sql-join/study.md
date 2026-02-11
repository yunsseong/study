# 1. SQL 기본 문법과 JOIN

---

## SQL 기본 문법

SQL(Structured Query Language)은 **관계형 데이터베이스를 다루는 표준 언어**다.
면접에서는 문법 자체보다 **실행 순서와 각 절의 역할**을 정확히 이해하는지 확인한다.

### SQL 실행 순서

우리가 작성하는 순서와 **실제 실행 순서는 다르다**. 이걸 모르면 면접에서 당한다.

```
작성 순서:                    실행 순서:

SELECT   (5)                 FROM      (1) 어떤 테이블에서?
FROM     (1)                 WHERE     (2) 조건으로 행 필터
WHERE    (2)                 GROUP BY  (3) 그룹으로 묶기
GROUP BY (3)                 HAVING    (4) 그룹 조건 필터
HAVING   (4)                 SELECT    (5) 컬럼 선택
ORDER BY (6)                 ORDER BY  (6) 정렬
LIMIT    (7)                 LIMIT     (7) 개수 제한
```

왜 중요할까?
- `WHERE`에서 별칭(alias)을 못 쓰는 이유: `SELECT`보다 먼저 실행되니까
- `HAVING`에서 집계 함수를 쓸 수 있는 이유: `GROUP BY` 이후에 실행되니까

### 각 절 정리

```sql
-- 부서별 평균 급여가 500만원 이상인 부서를 급여 내림차순으로 조회
SELECT department, AVG(salary) AS avg_salary   -- (5) 컬럼 선택
FROM employees                                  -- (1) 테이블 지정
WHERE status = 'ACTIVE'                         -- (2) 행 필터 (집계 전)
GROUP BY department                             -- (3) 그룹화
HAVING AVG(salary) >= 5000000                   -- (4) 그룹 필터 (집계 후)
ORDER BY avg_salary DESC                        -- (6) 정렬
LIMIT 10;                                       -- (7) 개수 제한
```

### WHERE vs HAVING 차이

| 구분 | WHERE | HAVING |
|------|-------|--------|
| 실행 시점 | GROUP BY 이전 | GROUP BY 이후 |
| 대상 | 개별 행 | 그룹 |
| 집계 함수 | 사용 불가 | 사용 가능 |
| 성능 | 먼저 필터링하므로 유리 | 그룹화 후 필터링 |

> **원칙**: 가능하면 `WHERE`로 먼저 걸러라. 행 수를 줄이고 `GROUP BY` 하는 게 성능에 유리하다.

---

## JOIN 종류

두 개 이상의 테이블을 **관계(FK 등)를 기반으로 연결**하는 것이 JOIN이다.

### 예시 테이블

```
employees (직원)                  departments (부서)
+----+--------+--------+         +--------+----------+
| id | name   | dept_id|         | dept_id| dept_name|
+----+--------+--------+         +--------+----------+
|  1 | 김철수 |    10  |         |   10   | 개발팀   |
|  2 | 이영희 |    20  |         |   20   | 기획팀   |
|  3 | 박민수 |    30  |         |   40   | 인사팀   |
|  4 | 정수진 |  NULL  |         +--------+----------+
+----+--------+--------+
```

### INNER JOIN

**양쪽 테이블에 모두 일치하는 행만** 반환한다. 가장 많이 쓴다.

```sql
SELECT e.name, d.dept_name
FROM employees e
INNER JOIN departments d ON e.dept_id = d.dept_id;
```

```
결과:
+--------+----------+
| name   | dept_name|
+--------+----------+
| 김철수 | 개발팀   |   -- dept_id=10 양쪽 일치
| 이영희 | 기획팀   |   -- dept_id=20 양쪽 일치
+--------+----------+

제외: 박민수(dept_id=30, departments에 없음)
      정수진(dept_id=NULL)
      인사팀(dept_id=40, employees에 없음)
```

```
  employees        departments
  +-------+        +-------+
  |       |\\      /|       |
  |       | \\####/ |       |
  |       | /####\ |       |
  |       |/      \|       |
  +-------+        +-------+
           ^^^^^^^^
           INNER JOIN
           (교집합)
```

### LEFT (OUTER) JOIN

**왼쪽 테이블은 전부** 나오고, 오른쪽에 일치하는 게 없으면 NULL이 채워진다.

```sql
SELECT e.name, d.dept_name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;
```

```
결과:
+--------+----------+
| name   | dept_name|
+--------+----------+
| 김철수 | 개발팀   |
| 이영희 | 기획팀   |
| 박민수 | NULL     |   -- departments에 dept_id=30 없음
| 정수진 | NULL     |   -- dept_id가 NULL
+--------+----------+
```

> 실무에서 가장 많이 쓰는 OUTER JOIN이다. "부서가 없는 직원도 포함해서 보여줘"

### RIGHT (OUTER) JOIN

**오른쪽 테이블은 전부** 나온다. LEFT JOIN의 반대.

```sql
SELECT e.name, d.dept_name
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
```

```
결과:
+--------+----------+
| name   | dept_name|
+--------+----------+
| 김철수 | 개발팀   |
| 이영희 | 기획팀   |
| NULL   | 인사팀   |   -- 인사팀에 소속 직원 없음
+--------+----------+
```

> 실무에서는 RIGHT JOIN보다 테이블 순서를 바꿔서 LEFT JOIN으로 쓰는 경우가 많다.

### FULL OUTER JOIN

**양쪽 모두** 표시한다. 일치하지 않는 쪽은 NULL.

```sql
-- MySQL은 FULL OUTER JOIN을 지원하지 않음. UNION으로 구현
SELECT e.name, d.dept_name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
UNION
SELECT e.name, d.dept_name
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
```

```
결과:
+--------+----------+
| name   | dept_name|
+--------+----------+
| 김철수 | 개발팀   |
| 이영희 | 기획팀   |
| 박민수 | NULL     |
| 정수진 | NULL     |
| NULL   | 인사팀   |
+--------+----------+
```

### CROSS JOIN (교차 조인)

**모든 가능한 조합**을 만든다 (카르테시안 곱). 조건 없이 모든 행끼리 조합.

```sql
SELECT e.name, d.dept_name
FROM employees e
CROSS JOIN departments d;
-- 결과: 4명 x 3부서 = 12행
```

> 실무에서 의도적으로 쓰는 경우는 드물다. 실수로 JOIN 조건을 빼먹으면 이게 된다.

### SELF JOIN (자기 자신 조인)

**같은 테이블을 두 번** 사용하는 JOIN. 계층 구조(상사-부하)에서 자주 쓴다.

```sql
-- employees 테이블에 manager_id 컬럼이 있다고 가정
SELECT e.name AS 직원, m.name AS 상사
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

```
결과:
+--------+--------+
| 직원   | 상사   |
+--------+--------+
| 김철수 | NULL   |   -- 최상위 관리자
| 이영희 | 김철수 |
| 박민수 | 김철수 |
| 정수진 | 이영희 |
+--------+--------+
```

### JOIN 종류 한눈에 비교

| JOIN 종류 | 설명 | NULL 포함 |
|-----------|------|-----------|
| INNER JOIN | 양쪽 일치만 | X |
| LEFT JOIN | 왼쪽 전부 + 오른쪽 일치 | 오른쪽 NULL |
| RIGHT JOIN | 오른쪽 전부 + 왼쪽 일치 | 왼쪽 NULL |
| FULL OUTER JOIN | 양쪽 전부 | 양쪽 NULL |
| CROSS JOIN | 모든 조합 (조건 없음) | X |
| SELF JOIN | 자기 자신 조인 | JOIN 종류에 따름 |

---

## 서브쿼리 vs JOIN 성능

같은 결과를 서브쿼리와 JOIN 두 가지로 얻을 수 있을 때, 어느 게 나을까?

### 예시: 주문이 있는 고객 조회

```sql
-- 서브쿼리 방식
SELECT name FROM customers
WHERE id IN (SELECT customer_id FROM orders);

-- JOIN 방식
SELECT DISTINCT c.name
FROM customers c
INNER JOIN orders o ON c.id = o.customer_id;
```

### 성능 비교

| 구분 | 서브쿼리 | JOIN |
|------|----------|------|
| 실행 방식 | 내부 쿼리를 먼저 실행 후 외부에서 비교 | 테이블을 직접 연결 |
| 성능 (일반적) | 대량 데이터에서 느릴 수 있음 | 옵티마이저 최적화에 유리 |
| 가독성 | 직관적 (사람이 읽기 쉬움) | 테이블 관계가 명확 |
| 인덱스 활용 | IN 절 내부에서 제한적 | JOIN 키에 인덱스 활용 용이 |

```
서브쿼리 실행 흐름:
  1. 내부 쿼리 실행 → 결과 집합 생성
  2. 외부 쿼리에서 결과 집합과 비교
  (상관 서브쿼리의 경우 외부 행마다 내부 쿼리 반복 실행 → 매우 느림)

JOIN 실행 흐름:
  1. 옵티마이저가 실행 계획 수립
  2. 인덱스 활용해서 효율적으로 테이블 연결
  3. 한 번에 처리
```

### 상관 서브쿼리 (Correlated Subquery) - 주의

```sql
-- 상관 서브쿼리: 외부 행마다 내부 쿼리가 반복 실행됨 (느림!)
SELECT name FROM customers c
WHERE EXISTS (
    SELECT 1 FROM orders o
    WHERE o.customer_id = c.id   -- 외부 테이블 참조
);
```

> **결론**: 대부분의 경우 JOIN이 성능에 유리하다. 단, MySQL 옵티마이저가 서브쿼리를 JOIN으로 자동 변환하는 경우도 있다. EXPLAIN으로 실행 계획을 확인하는 습관이 중요하다.

---

## Spring Boot / 실무 연결

### JPA에서의 연관관계와 JOIN

```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;
}

@Entity
public class Department {
    @Id
    private Long deptId;
    private String deptName;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}
```

### JPQL로 JOIN 사용하기

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // JPQL - INNER JOIN
    @Query("SELECT e FROM Employee e JOIN e.department d WHERE d.deptName = :name")
    List<Employee> findByDepartmentName(@Param("name") String name);

    // JPQL - LEFT JOIN FETCH (N+1 문제 해결)
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();

    // Native Query - 복잡한 쿼리가 필요할 때
    @Query(value = """
        SELECT e.name, d.dept_name, COUNT(o.id) AS order_count
        FROM employees e
        LEFT JOIN departments d ON e.dept_id = d.dept_id
        LEFT JOIN orders o ON e.id = o.employee_id
        GROUP BY e.name, d.dept_name
        HAVING COUNT(o.id) >= 5
        ORDER BY order_count DESC
        """, nativeQuery = true)
    List<Object[]> findEmployeeOrderStats();
}
```

### N+1 문제와 JOIN FETCH

```
N+1 문제란?

직원 10명 조회 시:
  1번 쿼리: SELECT * FROM employees               (1번)
  2~11번:   SELECT * FROM departments WHERE id=?   (N번)
  → 총 11번 쿼리 실행!

JOIN FETCH 사용 시:
  1번 쿼리: SELECT e.*, d.* FROM employees e
            LEFT JOIN departments d ON e.dept_id = d.dept_id
  → 1번 쿼리로 해결!
```

```java
// N+1 발생하는 코드
List<Employee> employees = employeeRepository.findAll();
for (Employee e : employees) {
    System.out.println(e.getDepartment().getDeptName()); // 여기서 추가 쿼리 발생!
}

// JOIN FETCH로 해결
@Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department")
List<Employee> findAllWithDepartment();
```

### QueryDSL로 동적 쿼리 작성

```java
public List<Employee> searchEmployees(String name, String dept) {
    BooleanBuilder builder = new BooleanBuilder();

    if (name != null) {
        builder.and(employee.name.contains(name));
    }
    if (dept != null) {
        builder.and(employee.department.deptName.eq(dept));
    }

    return queryFactory
        .selectFrom(employee)
        .leftJoin(employee.department, department).fetchJoin()
        .where(builder)
        .orderBy(employee.name.asc())
        .fetch();
}
```

---

## 면접 핵심 정리

**Q: SQL 실행 순서를 설명해주세요**
> FROM에서 테이블을 가져온 뒤 WHERE로 행을 필터링하고, GROUP BY로 그룹화합니다.
> HAVING으로 그룹 조건을 적용한 뒤 SELECT로 컬럼을 선택하고, ORDER BY로 정렬합니다.
> WHERE는 집계 전, HAVING은 집계 후 필터링이라는 점이 핵심입니다.

**Q: INNER JOIN과 LEFT JOIN의 차이는?**
> INNER JOIN은 양쪽 테이블에 모두 일치하는 행만 반환합니다.
> LEFT JOIN은 왼쪽 테이블의 모든 행을 반환하고, 오른쪽에 일치하는 값이 없으면 NULL로 채웁니다.
> 실무에서는 "연관 데이터가 없는 경우도 포함해야 할 때" LEFT JOIN을 사용합니다.

**Q: 서브쿼리보다 JOIN이 성능에 유리한 이유는?**
> JOIN은 옵티마이저가 실행 계획을 최적화하기 쉽고 인덱스를 효과적으로 활용할 수 있습니다.
> 특히 상관 서브쿼리는 외부 행마다 내부 쿼리가 반복 실행되어 매우 느립니다.
> 다만 MySQL 옵티마이저가 서브쿼리를 자동으로 JOIN으로 변환하는 경우도 있으므로,
> 반드시 EXPLAIN으로 실행 계획을 확인해야 합니다.

**Q: JPA에서 N+1 문제란? 해결 방법은?**
> 연관 엔티티를 LAZY 로딩으로 설정했을 때, 부모 엔티티 조회 후 자식 엔티티에 접근할 때마다
> 추가 쿼리가 발생하는 문제입니다. N개의 부모가 있으면 1+N번 쿼리가 실행됩니다.
> JOIN FETCH를 사용하면 한 번의 쿼리로 연관 데이터를 함께 가져올 수 있습니다.
> EntityGraph나 Batch Size 설정도 대안입니다.

**Q: CROSS JOIN은 언제 사용하나요?**
> 모든 행의 조합이 필요한 경우에 사용합니다. 예를 들어 모든 상품과 모든 색상의 조합 테이블을
> 만들 때 사용합니다. 다만 데이터가 크면 결과 행이 기하급수적으로 늘어나므로 주의해야 합니다.
> 의도하지 않은 CROSS JOIN은 실무에서 흔한 실수입니다.
