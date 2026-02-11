# 4. Spring Data JPA

---

## ORM이란?

**ORM (Object-Relational Mapping)**: 객체와 관계형 데이터베이스 테이블을 매핑하는 기술.

```
객체 지향 세계                    관계형 DB 세계
─────────────                   ─────────────
Class (User)         ←→         Table (users)
Field (name)         ←→         Column (name)
Instance             ←→         Row
Reference (user.team)←→         FK (team_id)
```

**왜 필요한가?**

```java
// ORM 없이: SQL을 직접 작성하고 결과를 수동 매핑
String sql = "SELECT id, name, email FROM users WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, userId);
ResultSet rs = ps.executeQuery();
if (rs.next()) {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setName(rs.getString("name"));
    user.setEmail(rs.getString("email"));
}
// → 반복적이고 실수하기 쉬움

// ORM 사용: 객체로 바로 조회
User user = entityManager.find(User.class, userId);
// → SQL을 JPA가 자동 생성
```

## JPA란?

**JPA (Java Persistence API)**: Java ORM의 **표준 인터페이스(스펙)**.
실제 구현체는 **Hibernate**가 가장 많이 사용된다.

```
JPA (인터페이스/표준)
  │
  ├── Hibernate (구현체) ← 가장 널리 사용
  ├── EclipseLink (구현체)
  └── OpenJPA (구현체)

Spring Data JPA (Spring 래핑)
  └── Hibernate (내부적으로 사용)
       └── JDBC (최종적으로 SQL 실행)
```

```
Spring Data JPA의 위치:

[개발자 코드]
     ↓
[Spring Data JPA]  ← Repository 인터페이스, 메서드 이름 쿼리
     ↓
[JPA (Hibernate)]  ← EntityManager, 영속성 컨텍스트
     ↓
[JDBC]             ← Connection, PreparedStatement
     ↓
[Database]         ← MySQL, PostgreSQL 등
```

---

## 영속성 컨텍스트 (Persistence Context)

### 개념

**영속성 컨텍스트**: 엔티티를 **영구 저장하는 환경**. EntityManager를 통해 접근.
1차 캐시, 변경 감지, 쓰기 지연 등의 기능을 제공한다.

```
[EntityManager] ──관리──→ [영속성 컨텍스트]
                            │
                            ├── 1차 캐시 (Map<PK, Entity>)
                            ├── 쓰기 지연 SQL 저장소
                            └── 스냅샷 (변경 감지용)
```

> **Spring에서 영속성 컨텍스트의 범위**: 기본적으로 **트랜잭션 단위**.
> `@Transactional` 시작 시 생성되고, 종료 시 사라진다.

---

## 엔티티 생명주기

```
                    persist()
  [비영속(New)] ──────────────→ [영속(Managed)]
                                  │    ↑
                          find()  │    │ merge()
                                  │    │
                          detach()│    │
                          clear() ↓    │
                              [준영속(Detached)]
                                  │
                          remove()│
                                  ↓
                              [삭제(Removed)]
```

| 상태 | 설명 | 영속성 컨텍스트 | DB |
|------|------|----------------|-----|
| **비영속 (New)** | new로 생성만 한 상태 | 관리 X | 저장 X |
| **영속 (Managed)** | persist() 또는 find()한 상태 | 관리 O | 저장 O (flush 시) |
| **준영속 (Detached)** | 영속 상태였다가 분리된 상태 | 관리 X | 저장되어 있음 |
| **삭제 (Removed)** | 삭제 요청된 상태 | 관리 O → X | 삭제 예정 |

```java
// 비영속: 순수 Java 객체, JPA와 무관
User user = new User("홍길동", "hong@example.com");

// 영속: 영속성 컨텍스트가 관리 시작
entityManager.persist(user);  // INSERT SQL은 아직 안 날아감!

// 준영속: 영속성 컨텍스트에서 분리
entityManager.detach(user);

// 삭제: 삭제 요청
entityManager.remove(user);
```

---

## 1차 캐시

### 동작 원리

영속성 컨텍스트 내부에 **Map 형태의 캐시**가 있다.

```
영속성 컨텍스트:
┌──────────────────────────────────┐
│ 1차 캐시                          │
│ ┌────────┬──────────────────┐    │
│ │  Key   │     Value        │    │
│ ├────────┼──────────────────┤    │
│ │ User@1 │ User{id=1, ...}  │    │
│ │ User@2 │ User{id=2, ...}  │    │
│ └────────┴──────────────────┘    │
└──────────────────────────────────┘
```

```java
// 1. persist → 1차 캐시에 저장
entityManager.persist(user);  // 1차 캐시에 등록

// 2. find → 1차 캐시에서 먼저 조회
User user1 = entityManager.find(User.class, 1L);  // DB에서 조회 → 1차 캐시 등록
User user2 = entityManager.find(User.class, 1L);  // 1차 캐시에서 조회 (DB 안 감)

System.out.println(user1 == user2);  // true (같은 참조, 같은 객체)
```

```
find(User.class, 1L) 호출 시:

① 1차 캐시에 id=1인 User가 있는가?
   ├── YES → 바로 반환 (DB 조회 X, SQL 안 날림)
   └── NO  → ② DB에서 SELECT 실행
              → 결과를 1차 캐시에 저장
              → 반환
```

> **장점**: 같은 트랜잭션 내에서 같은 엔티티를 여러 번 조회해도 DB 쿼리는 1번만 실행.
> **주의**: 1차 캐시는 트랜잭션 범위이므로, 다른 트랜잭션에서는 캐시가 공유되지 않는다.

---

## 변경 감지 (Dirty Checking)

```java
@Transactional
public void updateUserName(Long userId, String newName) {
    User user = userRepository.findById(userId).orElseThrow();

    user.setName(newName);  // 객체만 변경

    // save()를 호출하지 않아도 UPDATE SQL이 자동으로 실행된다!
}
```

```
동작 원리:

① 엔티티를 1차 캐시에 저장할 때 스냅샷도 함께 저장
   1차 캐시: { User{id=1, name="홍길동"} }
   스냅샷:   { User{id=1, name="홍길동"} }  ← 최초 상태 복사

② user.setName("김철수") → 엔티티 변경
   1차 캐시: { User{id=1, name="김철수"} }  ← 변경됨
   스냅샷:   { User{id=1, name="홍길동"} }  ← 그대로

③ 트랜잭션 커밋 시점에 flush() 호출
   → 1차 캐시의 엔티티와 스냅샷을 비교
   → "name이 홍길동 → 김철수로 바뀌었네!"
   → UPDATE SQL 자동 생성 및 실행

UPDATE users SET name = '김철수' WHERE id = 1;
```

```
Dirty Checking 흐름:

[트랜잭션 시작]
     │
  find() → 1차 캐시 + 스냅샷 저장
     │
  setter로 필드 변경
     │
[트랜잭션 커밋 (flush)]
     │
  스냅샷과 현재 엔티티 비교
     │
  변경 있으면 → UPDATE SQL 자동 생성 → DB 반영
  변경 없으면 → 아무것도 안 함
```

> **핵심**: JPA에서는 **엔티티 객체를 수정하기만 하면** 트랜잭션 커밋 시 자동으로 UPDATE가 실행된다.
> 별도로 `save()`를 호출할 필요가 없다 (save()를 호출해도 되지만 불필요).

---

## 쓰기 지연 (Write-Behind)

```java
@Transactional
public void createUsers() {
    User user1 = new User("홍길동");
    User user2 = new User("김철수");
    User user3 = new User("이영희");

    entityManager.persist(user1);  // INSERT SQL 안 날아감
    entityManager.persist(user2);  // INSERT SQL 안 날아감
    entityManager.persist(user3);  // INSERT SQL 안 날아감

    // 트랜잭션 커밋 시점에 3개의 INSERT가 한꺼번에 실행
}
```

```
persist() 호출 시:

┌─────────────────────────────────────────────┐
│ 영속성 컨텍스트                                │
│                                              │
│ [1차 캐시]              [쓰기 지연 SQL 저장소]  │
│ user1                   INSERT user1          │
│ user2                   INSERT user2          │
│ user3                   INSERT user3          │
│                                              │
└─────────────────────────────────────────────┘

트랜잭션 커밋 → flush() → SQL 저장소의 SQL들을 한꺼번에 DB로 전송
                         → DB commit
```

**장점**:
- SQL을 모아서 한 번에 전송 → 네트워크 효율
- 트랜잭션 커밋 전까지 SQL을 최적화할 수 있음
- `hibernate.jdbc.batch_size` 설정으로 JDBC 배치 활용 가능

---

## N+1 문제와 해결방법

### N+1 문제란?

```java
@Entity
public class Team {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}

@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
```

```java
// 팀 3개를 조회하고, 각 팀의 멤버를 출력
List<Team> teams = teamRepository.findAll();  // 1번 쿼리
for (Team team : teams) {
    System.out.println(team.getMembers().size());  // 팀마다 1번씩 추가 쿼리
}
```

```sql
-- 1번: 팀 전체 조회
SELECT * FROM team;                           -- 결과: 3개 팀

-- N번: 각 팀의 멤버 조회 (팀 3개니까 3번)
SELECT * FROM member WHERE team_id = 1;       -- 팀1의 멤버
SELECT * FROM member WHERE team_id = 2;       -- 팀2의 멤버
SELECT * FROM member WHERE team_id = 3;       -- 팀3의 멤버

-- 총 1 + 3 = 4번 쿼리 (N+1 문제!)
-- 팀이 100개면? 1 + 100 = 101번 쿼리!
```

### 해결방법 1: Fetch Join

```java
// JPQL에서 JOIN FETCH 사용
@Query("SELECT t FROM Team t JOIN FETCH t.members")
List<Team> findAllWithMembers();
```

```sql
-- 단 1번의 쿼리로 해결
SELECT t.*, m.*
FROM team t
INNER JOIN member m ON t.id = m.team_id;
```

```
장점: 한 번의 쿼리로 모든 데이터 조회
단점:
  - 페이징 불가 (컬렉션 Fetch Join 시)
  - 둘 이상의 컬렉션 Fetch Join 불가
  - 데이터 중복 발생 가능 (DISTINCT 필요)
```

```java
// DISTINCT로 중복 제거
@Query("SELECT DISTINCT t FROM Team t JOIN FETCH t.members")
List<Team> findAllWithMembers();
```

### 해결방법 2: @EntityGraph

```java
// 어노테이션으로 Fetch Join 효과
@EntityGraph(attributePaths = {"members"})
@Query("SELECT t FROM Team t")
List<Team> findAllWithMembers();

// 또는 메서드 이름 쿼리에도 사용 가능
@EntityGraph(attributePaths = {"members"})
List<Team> findAll();
```

### 해결방법 3: @BatchSize

```java
@Entity
public class Team {
    @OneToMany(mappedBy = "team")
    @BatchSize(size = 100)  // IN 절로 한 번에 100개씩 조회
    private List<Member> members = new ArrayList<>();
}
```

```sql
-- BatchSize 적용 시:
SELECT * FROM team;                                    -- 1번

-- members 조회 시 IN 절로 한 번에
SELECT * FROM member WHERE team_id IN (1, 2, 3);       -- 1번

-- 총 2번 쿼리 (팀이 100개여도 2번!)
```

```yaml
# 글로벌 설정 (application.yml)
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

### N+1 해결방법 비교

| 방법 | 쿼리 수 | 페이징 | 설정 위치 | 권장 상황 |
|------|---------|--------|----------|----------|
| **Fetch Join** | 1번 | 불가 (컬렉션) | Repository 메서드 | 특정 쿼리 최적화 |
| **@EntityGraph** | 1번 | 불가 (컬렉션) | Repository 메서드 | Fetch Join의 간편 버전 |
| **@BatchSize** | 2번 | **가능** | Entity 또는 글로벌 | **일반적으로 가장 권장** |

> **실무 추천**: 글로벌로 `default_batch_fetch_size: 100` 설정하고,
> 특수한 경우에만 Fetch Join을 사용하는 것이 관리하기 편하다.

---

## 즉시 로딩 vs 지연 로딩

### 즉시 로딩 (EAGER)

```java
@ManyToOne(fetch = FetchType.EAGER)  // 즉시 로딩
@JoinColumn(name = "team_id")
private Team team;
```

```java
Member member = memberRepository.findById(1L).orElseThrow();
// → Member 조회 시 Team도 함께 JOIN으로 즉시 조회
```

```sql
SELECT m.*, t.*
FROM member m
LEFT JOIN team t ON m.team_id = t.id
WHERE m.id = 1;
```

### 지연 로딩 (LAZY)

```java
@ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩
@JoinColumn(name = "team_id")
private Team team;
```

```java
Member member = memberRepository.findById(1L).orElseThrow();
// → Member만 조회. Team은 아직 조회 안 함.
// team 필드에는 Proxy 객체가 들어있음.

member.getTeam().getName();
// → 이 시점에 Team 조회 SQL 실행 (실제 사용 시점에 조회)
```

```sql
-- findById 시점
SELECT * FROM member WHERE id = 1;

-- getTeam().getName() 시점 (실제 접근 시)
SELECT * FROM team WHERE id = 1;
```

```
LAZY 로딩의 프록시:

member.getTeam() 반환 값:
  → Team$$HibernateProxy$$xxx (프록시 객체)
  → 실제 Team 데이터는 아직 없음
  → getName() 호출 시 프록시가 DB 조회 → 실제 데이터 채움
```

### 기본 전략

| 연관관계 | 기본 FetchType | 이유 |
|---------|---------------|------|
| **@ManyToOne** | EAGER | 단일 객체이므로 부담 적음 |
| **@OneToOne** | EAGER | 단일 객체이므로 부담 적음 |
| **@OneToMany** | LAZY | 컬렉션은 데이터 많을 수 있어 위험 |
| **@ManyToMany** | LAZY | 컬렉션은 데이터 많을 수 있어 위험 |

> **실무 원칙**: 모든 연관관계를 **LAZY로 설정**하고, 필요할 때 Fetch Join으로 가져온다.
> EAGER는 N+1 문제를 유발하기 쉽고, 예상치 못한 쿼리가 실행될 수 있다.

```java
// 실무 권장: 모든 @ManyToOne, @OneToOne에도 LAZY 적용
@ManyToOne(fetch = FetchType.LAZY)  // 명시적으로 LAZY 설정
@JoinColumn(name = "team_id")
private Team team;
```

---

## 연관관계 매핑

### @ManyToOne (다대일) - 가장 많이 사용

```java
// Member (N) → Team (1)
// FK를 가진 쪽이 연관관계의 주인

@Entity
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")  // FK 컬럼명
    private Team team;             // 연관관계의 주인
}
```

```
DB 테이블:
members                          teams
┌────┬──────┬─────────┐        ┌────┬──────┐
│ id │ name │ team_id │        │ id │ name │
├────┼──────┼─────────┤        ├────┼──────┤
│ 1  │ 홍길동│    1    │───→    │ 1  │ A팀  │
│ 2  │ 김철수│    1    │───→    │    │      │
│ 3  │ 이영희│    2    │───→    │ 2  │ B팀  │
└────┴──────┴─────────┘        └────┴──────┘
     FK(team_id)가 있는 쪽이 연관관계의 주인
```

### @OneToMany (일대다) - 양방향

```java
@Entity
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")  // 주인이 아님. Member.team 필드에 의해 매핑됨
    private List<Member> members = new ArrayList<>();
}
```

```
연관관계의 주인:
  - FK를 가진 테이블의 엔티티가 주인 (Member.team)
  - 주인만 DB 값을 변경할 수 있다
  - 주인이 아닌 쪽(Team.members)은 읽기만 가능

mappedBy:
  - "나는 주인이 아니다"를 선언
  - 값은 주인 쪽의 필드명 (Member의 "team" 필드)
```

### 양방향 매핑 시 주의사항

```java
// 연관관계 편의 메서드 (양쪽 모두 설정해야 안전)
@Entity
public class Team {
    // ...

    public void addMember(Member member) {
        this.members.add(member);
        member.setTeam(this);  // 양쪽 모두 설정!
    }
}
```

```java
// 잘못된 방법: 주인이 아닌 쪽만 설정
team.getMembers().add(member);  // DB에 반영 안 됨!

// 올바른 방법: 주인 쪽에 설정
member.setTeam(team);  // DB에 team_id가 설정됨

// 가장 좋은 방법: 양쪽 모두 설정 (편의 메서드)
team.addMember(member);  // 양쪽 모두 설정됨
```

> **실무 팁**: 가능하면 **단방향 매핑(@ManyToOne만)으로 설계**하고,
> 반대쪽에서 조회가 필요한 경우에만 양방향을 추가한다.
> 양방향 매핑이 많으면 객체 간 의존성이 복잡해진다.

---

## Spring Data JPA Repository 계층 구조

```
Repository (마커 인터페이스)
    │
CrudRepository
    │ - save(), findById(), findAll(), deleteById(), count(), existsById()
    │
ListCrudRepository
    │ - findAll()이 List 반환 (Java 17+)
    │
PagingAndSortingRepository
    │ - findAll(Sort), findAll(Pageable)
    │
JpaRepository  ← 실무에서 가장 많이 사용
    - flush(), saveAndFlush()
    - deleteAllInBatch()
    - findAll(Example)
```

### 기본 사용

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository 상속만으로 기본 CRUD 메서드 사용 가능!
}
```

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 기본 제공 메서드
    public User create(UserCreateRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return userRepository.save(user);           // INSERT
    }

    public User findById(Long id) {
        return userRepository.findById(id)           // SELECT by PK
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> findAll() {
        return userRepository.findAll();             // SELECT all
    }

    public void delete(Long id) {
        userRepository.deleteById(id);               // DELETE
    }
}
```

### 메서드 이름으로 쿼리 생성

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE name = ? AND age > ?
    List<User> findByNameAndAgeGreaterThan(String name, int age);

    // SELECT * FROM users WHERE name LIKE '%keyword%'
    List<User> findByNameContaining(String keyword);

    // SELECT * FROM users WHERE status = ? ORDER BY created_at DESC
    List<User> findByStatusOrderByCreatedAtDesc(String status);

    // SELECT COUNT(*) FROM users WHERE team_id = ?
    long countByTeamId(Long teamId);

    // SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    // FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
```

### @Query (JPQL / Native Query)

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // JPQL (엔티티 기준)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailJpql(@Param("email") String email);

    // JPQL + Fetch Join (N+1 해결)
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.team WHERE u.status = :status")
    List<User> findActiveUsersWithTeam(@Param("status") String status);

    // Native Query (SQL 직접 작성)
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);

    // 업데이트 쿼리 (벌크 연산)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = :status WHERE u.lastLoginAt < :date")
    int updateInactiveUsers(@Param("status") String status, @Param("date") LocalDateTime date);
}
```

### 페이징

```java
// Repository
Page<User> findByStatus(String status, Pageable pageable);

// Service
public Page<User> getActiveUsers(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userRepository.findByStatus("ACTIVE", pageable);
}

// Controller
@GetMapping("/api/users")
public Page<UserResponse> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return userService.getActiveUsers(page, size)
            .map(UserResponse::from);
}
```

```json
// Page 응답 형태
{
  "content": [
    {"id": 1, "name": "홍길동"},
    {"id": 2, "name": "김철수"}
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {"sorted": true, "direction": "DESC"}
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

---

## 실무 연결 / Spring Boot 연결

### Entity 설계 기본

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 스펙상 기본 생성자 필요
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)  // ORDINAL 사용 금지! (순서 변경 시 장애)
    @Column(nullable = false)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 생성자
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.status = UserStatus.ACTIVE;
    }

    // 비즈니스 메서드 (setter 대신)
    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = name;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}
```

### Spring Boot JPA 설정

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate          # 운영: validate, 개발: update
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100  # N+1 방지
    open-in-view: false           # OSIV 끄기 (권장)

  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password

logging:
  level:
    org.hibernate.SQL: debug                          # SQL 로그
    org.hibernate.orm.jdbc.bind: trace                # 파라미터 바인딩 로그
```

### ddl-auto 옵션

| 옵션 | 동작 | 사용 환경 |
|------|------|----------|
| **create** | 기존 테이블 삭제 후 재생성 | 초기 개발 |
| **create-drop** | 종료 시 테이블 삭제 | 테스트 |
| **update** | 변경분만 반영 (컬럼 추가 등) | 개발 |
| **validate** | 엔티티와 테이블 일치 여부만 검증 | **운영** |
| **none** | 아무것도 안 함 | 운영 |

> **운영에서는 반드시 validate 또는 none 사용.** update를 운영에서 쓰면 의도치 않은 DDL이 실행될 수 있다.

---

## 면접 핵심 정리

**Q: 영속성 컨텍스트란 무엇이고 어떤 이점이 있나요?**
> 영속성 컨텍스트는 엔티티를 관리하는 JPA의 논리적 영역입니다.
> 세 가지 핵심 이점이 있습니다.
> 첫째, 1차 캐시로 같은 트랜잭션 내 동일 엔티티 재조회 시 DB에 접근하지 않습니다.
> 둘째, 변경 감지(Dirty Checking)로 엔티티 수정 시 별도 save 없이 자동으로 UPDATE SQL이 실행됩니다.
> 셋째, 쓰기 지연으로 SQL을 모아서 커밋 시점에 한 번에 전송하여 효율적입니다.

**Q: N+1 문제가 무엇이고 어떻게 해결하나요?**
> 연관된 엔티티를 조회할 때, 1번의 쿼리로 N개를 가져온 후
> 각각의 연관 엔티티를 조회하기 위해 N번의 추가 쿼리가 실행되는 문제입니다.
> 해결 방법은 세 가지입니다.
> Fetch Join은 JPQL에서 JOIN FETCH로 한 번에 조회합니다.
> @EntityGraph는 어노테이션으로 Fetch Join과 같은 효과를 냅니다.
> @BatchSize는 IN 절을 사용하여 연관 엔티티를 한 번에 조회합니다.
> 실무에서는 글로벌로 default_batch_fetch_size를 설정하고,
> 특수한 경우에만 Fetch Join을 사용하는 것이 일반적입니다.

**Q: 즉시 로딩과 지연 로딩의 차이와 실무 전략은?**
> 즉시 로딩(EAGER)은 엔티티 조회 시 연관 엔티티도 함께 조회합니다.
> 지연 로딩(LAZY)은 연관 엔티티에 실제 접근할 때 조회합니다.
> 실무에서는 모든 연관관계를 LAZY로 설정하는 것이 원칙입니다.
> EAGER는 예상치 못한 쿼리가 실행되고 N+1 문제를 유발하기 쉽기 때문입니다.
> 필요한 경우 Fetch Join이나 EntityGraph로 한 번에 가져옵니다.

**Q: 변경 감지(Dirty Checking)는 어떻게 동작하나요?**
> 영속성 컨텍스트가 엔티티를 1차 캐시에 저장할 때 스냅샷을 함께 보관합니다.
> 트랜잭션 커밋 시점(flush)에 현재 엔티티와 스냅샷을 비교하여
> 변경된 필드가 있으면 UPDATE SQL을 자동으로 생성하여 실행합니다.
> 따라서 엔티티의 setter만 호출하면 별도의 save 없이도 DB에 반영됩니다.

**Q: 연관관계의 주인이란?**
> 양방향 매핑에서 외래 키(FK)를 실제로 관리하는 쪽을 연관관계의 주인이라 합니다.
> DB 테이블에서 FK가 있는 쪽, 즉 @ManyToOne이 있는 엔티티가 주인입니다.
> 주인만 외래 키 값을 등록/수정할 수 있고, 반대쪽은 mappedBy로 읽기만 가능합니다.
> 실무에서는 가능하면 단방향(@ManyToOne만)으로 설계하고, 필요 시 양방향을 추가합니다.
