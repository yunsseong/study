# Java Spring Backend 로드맵

> Java + Spring Boot 기반 백엔드 개발자 학습 로드맵

## 학습 순서

```
주 1-2   : Java 기초 문법 + OOP
주 3-4   : Spring Core (IoC, DI, AOP)
주 5-6   : Spring Boot + Spring MVC (REST API)
주 7-8   : Spring Data JPA + 데이터베이스
주 9-10  : Spring Security (인증/인가)
주 11    : 테스트 (JUnit, MockMvc)
주 12    : 배포 (Docker, CI/CD)
```

---

## Phase 1: Java 기초

### 1. [Java 기초 문법](./01-java-basics/)

| 주제 | 핵심 |
|------|------|
| 변수, 타입, 연산자 | 기본형 vs 참조형, 형변환 |
| 제어문 | if, switch, for, while |
| 배열, 컬렉션 | List, Map, Set, Stream API |
| 예외 처리 | try-catch, checked vs unchecked |
| 제네릭 | 타입 안전성, 와일드카드 |
| 람다, Stream | 함수형 프로그래밍, map/filter/reduce |
| 멀티스레드 기초 | Thread, Runnable, synchronized |

### 2. [OOP (객체지향)](./02-oop/)

| 주제 | 핵심 |
|------|------|
| 클래스, 객체 | 생성자, 접근 제어자 |
| 상속, 다형성 | 오버라이딩, 업캐스팅 |
| 추상 클래스, 인터페이스 | 차이점, 언제 어떤 걸 쓰는지 |
| SOLID 원칙 | 실제 코드 예시와 함께 |
| 디자인 패턴 | 싱글톤, 팩토리, 빌더, 전략 패턴 |

---

## Phase 2: Spring 핵심

### 3. [Spring Core](./03-spring-core/)

| 주제 | 핵심 |
|------|------|
| IoC (제어의 역전) | 컨테이너가 객체 생명주기 관리 |
| DI (의존성 주입) | 생성자 주입, 필드 주입, setter 주입 |
| Bean | 스코프 (singleton, prototype), 생명주기 |
| AOP | 관점 지향 프로그래밍, 횡단 관심사 분리 |
| Component Scan | @Component, @Service, @Repository, @Controller |

### 4. [Spring Boot](./04-spring-boot/)

| 주제 | 핵심 |
|------|------|
| 프로젝트 생성 | Spring Initializr, Gradle vs Maven |
| 자동 설정 | @SpringBootApplication, auto-configuration |
| application.yml | 프로파일, 환경별 설정 |
| 내장 서버 | Tomcat, 포트 설정 |
| Lombok | @Getter, @Builder, @RequiredArgsConstructor |

---

## Phase 3: 웹 개발

### 5. [Spring MVC (REST API)](./05-spring-mvc/)

| 주제 | 핵심 |
|------|------|
| @RestController | @GetMapping, @PostMapping, @PutMapping, @DeleteMapping |
| 요청/응답 처리 | @RequestBody, @PathVariable, @RequestParam |
| DTO 패턴 | 요청 DTO, 응답 DTO, 변환 |
| 유효성 검증 | @Valid, @NotBlank, @Size, BindingResult |
| 예외 처리 | @ExceptionHandler, @ControllerAdvice, 커스텀 예외 |
| API 문서화 | Swagger / SpringDoc OpenAPI |
| ResponseEntity | 상태 코드, 헤더, 응답 본문 제어 |

### 6. [Spring Data JPA](./06-spring-data-jpa/)

| 주제 | 핵심 |
|------|------|
| JPA 기초 | 엔티티, 영속성 컨텍스트, 생명주기 |
| 엔티티 매핑 | @Entity, @Id, @Column, @Table |
| 연관 관계 | @OneToMany, @ManyToOne, @ManyToMany |
| Repository | JpaRepository, 쿼리 메서드, @Query (JPQL) |
| 페이징, 정렬 | Pageable, Sort |
| N+1 문제 | fetch join, @EntityGraph, 해결 전략 |
| 트랜잭션 | @Transactional, 전파 속성, 격리 수준 |
| QueryDSL | 타입 안전한 동적 쿼리 (선택) |

---

## Phase 4: 보안 & 품질

### 7. [Spring Security](./07-spring-security/)

| 주제 | 핵심 |
|------|------|
| 인증 vs 인가 | Authentication vs Authorization |
| SecurityFilterChain | 필터 체인 구조, 요청 흐름 |
| 폼 로그인 | UserDetailsService, PasswordEncoder |
| JWT 인증 | 토큰 생성, 검증, 필터 구현 |
| OAuth 2.0 | 소셜 로그인 (Google, Kakao) |
| 권한 관리 | @PreAuthorize, 역할 기반 접근 제어 |

### 8. [테스트](./08-testing/)

| 주제 | 핵심 |
|------|------|
| JUnit 5 | @Test, @BeforeEach, 단언문 |
| Mockito | @Mock, @InjectMocks, when/then |
| 슬라이스 테스트 | @WebMvcTest, @DataJpaTest |
| 통합 테스트 | @SpringBootTest, TestRestTemplate |
| MockMvc | API 엔드포인트 테스트 |

### 9. [배포](./09-deployment/)

| 주제 | 핵심 |
|------|------|
| Docker | Dockerfile, docker-compose |
| CI/CD | GitHub Actions |
| 클라우드 | AWS EC2, RDS 기본 |
| 모니터링 | Actuator, 로깅 (SLF4J, Logback) |

---

## 실습 프로젝트 (포트폴리오)

### 프로젝트 추천 순서

```
1단계: 게시판 CRUD API
   - Spring Boot + JPA + MySQL
   - REST API 설계, DTO 패턴, 예외 처리

2단계: 회원 인증/인가 추가
   - Spring Security + JWT
   - 로그인, 회원가입, 권한 관리

3단계: 고도화
   - 페이징, 검색, 파일 업로드
   - Redis 캐싱, 테스트 코드
   - Docker 배포
```

---

## 면접 빈출 질문

### Spring 핵심
1. IoC와 DI란 무엇인가요?
2. Bean의 스코프 종류와 차이점은?
3. @Component, @Service, @Repository의 차이는?
4. AOP란 무엇이고 어디에 사용하나요?
5. 생성자 주입을 권장하는 이유는?

### JPA
6. 영속성 컨텍스트란?
7. N+1 문제란? 해결 방법은?
8. @Transactional의 전파 속성이란?
9. 즉시 로딩 vs 지연 로딩 차이는?

### 보안 & 설계
10. JWT의 동작 원리와 장단점은?
11. REST API 설계 원칙이란?
12. 계층형 아키텍처 (Controller → Service → Repository)를 사용하는 이유는?

---

## 진행 상황

- [ ] Java 기초 문법
- [ ] OOP (객체지향)
- [ ] Spring Core
- [ ] Spring Boot
- [ ] Spring MVC (REST API)
- [ ] Spring Data JPA
- [ ] Spring Security
- [ ] 테스트
- [ ] 배포
- [ ] 실습 프로젝트
