# Spring Data JPA

## JPA란?

```
JPA (Java Persistence API):
  자바 ORM 표준 인터페이스.
  객체와 테이블을 매핑하여 SQL 없이 데이터 조작.

Hibernate:
  JPA의 대표 구현체.

Spring Data JPA:
  JPA를 더 쉽게 사용하게 해주는 Spring의 모듈.
  → Repository 인터페이스만 만들면 기본 CRUD 자동 제공.
```

---

## 엔티티 (Entity)

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto Increment
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)  // ENUM은 STRING으로 저장
    private Role role;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 메서드 (엔티티에 로직 포함 = 도메인 모델 패턴)
    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }
}

public enum Role {
    USER, ADMIN
}
```

### BaseEntity (공통 필드 분리)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

// 사용
@Entity
public class User extends BaseEntity {
    // createdAt, updatedAt 자동 관리
}
```

---

## 연관 관계 매핑

### @ManyToOne / @OneToMany (가장 흔함)

```java
// 게시글 - 작성자 (N:1)
@Entity
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩 (권장)
    @JoinColumn(name = "user_id")
    private User author;
}

// 유저 - 게시글 (1:N)
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "author")  // Post.author가 주인
    private List<Post> posts = new ArrayList<>();
}

// mappedBy: 연관 관계의 주인이 아닌 쪽에 설정
// 주인: FK를 가진 쪽 (Post.author)
// 주인만 DB에 영향을 줌 (INSERT, UPDATE)
```

### 즉시 로딩 vs 지연 로딩 (면접 빈출)

```java
// EAGER (즉시 로딩): 연관 엔티티를 바로 조회
@ManyToOne(fetch = FetchType.EAGER)
// SELECT * FROM post JOIN user ON ... (한 번에 가져옴)

// LAZY (지연 로딩): 실제 사용할 때 조회 (권장!)
@ManyToOne(fetch = FetchType.LAZY)
// SELECT * FROM post (Post만 가져옴)
// post.getAuthor().getName()  ← 이때 SELECT * FROM user

// 규칙: 모든 연관 관계는 LAZY로 설정!
// 필요할 때만 fetch join으로 한 번에 조회
```

---

## Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // 쿼리 메서드: 메서드 이름으로 쿼리 자동 생성
    Optional<User> findByEmail(String email);
    List<User> findByNameContaining(String keyword);
    boolean existsByEmail(String email);

    // JPQL (@Query)
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") Role role);

    // Native Query
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmailNative(String email);
}

// JpaRepository가 기본 제공하는 메서드:
// save(), findById(), findAll(), delete(), count(), existsById()
```

### 페이징

```java
// Repository
Page<Post> findByAuthorId(Long authorId, Pageable pageable);

// Service
public Page<PostResponseDto> getPostsByAuthor(Long authorId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> posts = postRepository.findByAuthorId(authorId, pageable);
    return posts.map(PostResponseDto::from);
}

// Controller
@GetMapping
public Page<PostResponseDto> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return postService.getPosts(page, size);
}
```

---

## 영속성 컨텍스트 (Persistence Context)

```
엔티티를 관리하는 가상의 공간. EntityManager가 관리.

상태:
  비영속(new):     new User() → 영속성 컨텍스트와 무관
  영속(managed):   em.persist(user) 또는 조회 → 관리 중
  준영속(detached): em.detach(user) → 관리 해제
  삭제(removed):   em.remove(user) → 삭제 예정

핵심 기능:
  1. 1차 캐시: 같은 엔티티 조회 시 DB 안 감 → 캐시에서 반환
  2. 동일성 보장: 같은 ID 조회 시 같은 객체 (==)
  3. 변경 감지(Dirty Checking): 엔티티 수정 시 자동 UPDATE
  4. 쓰기 지연: 트랜잭션 커밋 시점에 SQL 일괄 실행
```

### 변경 감지 (Dirty Checking)

```java
@Transactional
public void updateUser(Long id, String name) {
    User user = userRepository.findById(id).orElseThrow();
    user.updateProfile(name, user.getEmail());
    // save() 호출하지 않아도 자동 UPDATE!
    // 트랜잭션 커밋 시 변경 감지 → UPDATE SQL 실행
}
```

---

## N+1 문제 (면접 핵심)

### 문제

```java
List<Post> posts = postRepository.findAll();  // 쿼리 1번

for (Post post : posts) {
    post.getAuthor().getName();  // 각 Post마다 Author 조회 → N번
}
// 총 1 + N번 쿼리 실행!
```

### 해결 1: Fetch Join

```java
@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthor();
// SELECT p.*, u.* FROM post p JOIN user u ON p.user_id = u.id
// 1번의 쿼리로 해결!
```

### 해결 2: @EntityGraph

```java
@EntityGraph(attributePaths = {"author"})
List<Post> findAll();
// 자동으로 LEFT JOIN 생성
```

### 해결 3: @BatchSize

```java
@Entity
public class User {
    @OneToMany(mappedBy = "author")
    @BatchSize(size = 100)  // IN 절로 묶어서 조회
    private List<Post> posts;
}
// WHERE user_id IN (1, 2, 3, ..., 100)  → N번 대신 몇 번만
```

---

## @Transactional

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    @Transactional  // 쓰기 작업
    public void createOrder(OrderCreateDto dto) {
        // 트랜잭션 시작
        Order order = dto.toEntity();
        orderRepository.save(order);
        // 예외 발생 시 자동 롤백
        // 정상 종료 시 자동 커밋
    }

    @Transactional(readOnly = true)  // 읽기 전용 (성능 최적화)
    public OrderResponseDto getOrder(Long id) {
        // 변경 감지 비활성화 → 성능 향상
        // Slave DB로 라우팅 가능
        return OrderResponseDto.from(
            orderRepository.findById(id).orElseThrow()
        );
    }
}

// 주의: 같은 클래스 내부 호출 시 @Transactional 안 먹힘 (프록시 문제)
```

---

## 면접 예상 질문

1. **영속성 컨텍스트란?**
   - 엔티티를 관리하는 가상 공간. 1차 캐시, 변경 감지, 쓰기 지연 제공

2. **N+1 문제란? 해결 방법은?**
   - 연관 엔티티를 개별 쿼리로 조회 → Fetch Join, @EntityGraph, @BatchSize

3. **즉시 로딩 vs 지연 로딩?**
   - EAGER: 바로 조회 / LAZY: 사용 시 조회 → 모든 연관 관계 LAZY 권장

4. **변경 감지(Dirty Checking)란?**
   - 영속 상태 엔티티 변경 시 트랜잭션 커밋 때 자동 UPDATE

5. **@Transactional(readOnly = true)의 장점은?**
   - 변경 감지 비활성화 → 성능 향상, Slave DB 라우팅 가능
