# 테스트 (Testing)

## 테스트 종류

```
단위 테스트 (Unit Test):
  개별 메서드/클래스를 독립적으로 테스트
  → Service 로직, 유틸리티 메서드
  → 빠름, Mock 사용

통합 테스트 (Integration Test):
  여러 컴포넌트를 함께 테스트
  → Controller + Service + Repository + DB
  → 느리지만 실제 환경과 유사

슬라이스 테스트 (Slice Test):
  특정 계층만 테스트
  → @WebMvcTest (Controller만)
  → @DataJpaTest (Repository만)
```

---

## JUnit 5 기본

```java
import static org.assertj.core.api.Assertions.*;

class CalculatorTest {

    @Test
    @DisplayName("두 수를 더한다")
    void add() {
        Calculator calc = new Calculator();

        int result = calc.add(1, 2);

        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("0으로 나누면 예외 발생")
    void divideByZero() {
        Calculator calc = new Calculator();

        assertThatThrownBy(() -> calc.divide(10, 0))
            .isInstanceOf(ArithmeticException.class);
    }

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 실행
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후에 실행
    }
}
```

### AssertJ 주요 검증

```java
// 값 비교
assertThat(result).isEqualTo(3);
assertThat(name).isEqualTo("John");

// null 체크
assertThat(user).isNotNull();
assertThat(user).isNull();

// boolean
assertThat(user.isActive()).isTrue();

// 문자열
assertThat(email).contains("@");
assertThat(name).startsWith("J");

// 컬렉션
assertThat(list).hasSize(3);
assertThat(list).contains("a", "b");
assertThat(list).isEmpty();

// 예외
assertThatThrownBy(() -> service.getUser(999L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessageContaining("유저를 찾을 수 없습니다");
```

---

## Service 단위 테스트 (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock  // 가짜 객체
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks  // Mock을 주입받는 대상
    private UserService userService;

    @Test
    @DisplayName("유저 조회 성공")
    void getUser_success() {
        // given: 준비
        User user = User.builder()
            .id(1L)
            .name("John")
            .email("john@test.com")
            .build();

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        // when: 실행
        UserResponseDto result = userService.getUser(1L);

        // then: 검증
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@test.com");

        verify(userRepository, times(1)).findById(1L);  // 호출 횟수 검증
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외")
    void getUser_notFound() {
        // given
        when(userRepository.findById(999L))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(999L))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequestDto request = new SignupRequestDto("John", "john@test.com", "password123");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return saved;
        });

        // when
        userService.signup(request);

        // then
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("중복 이메일 회원가입 시 예외")
    void signup_duplicateEmail() {
        // given
        SignupRequestDto request = new SignupRequestDto("John", "john@test.com", "password123");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
            .isInstanceOf(DuplicateEmailException.class);
    }
}
```

---

## Controller 테스트 (@WebMvcTest + MockMvc)

```java
@WebMvcTest(UserController.class)  // Controller만 로드
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 제외 (선택)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // Spring Context의 Bean을 Mock으로 교체
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 조회 API")
    void getUser() throws Exception {
        // given
        UserResponseDto response = UserResponseDto.builder()
            .id(1L).name("John").email("john@test.com").build();

        when(userService.getUser(1L)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.email").value("john@test.com"))
            .andDo(print());  // 요청/응답 출력
    }

    @Test
    @DisplayName("유저 생성 API")
    void createUser() throws Exception {
        // given
        UserCreateRequestDto request = new UserCreateRequestDto(
            "John", "john@test.com", "password123");

        UserResponseDto response = UserResponseDto.builder()
            .id(1L).name("John").email("john@test.com").build();

        when(userService.createUser(any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andDo(print());
    }

    @Test
    @DisplayName("유효성 검증 실패")
    void createUser_validationFail() throws Exception {
        // given: 이름 없음
        String body = "{\"name\":\"\", \"email\":\"invalid\", \"password\":\"short\"}";

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
```

---

## Repository 테스트 (@DataJpaTest)

```java
@DataJpaTest  // JPA 관련 Bean만 로드, 내장 DB 사용
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 유저 조회")
    void findByEmail() {
        // given
        User user = User.builder()
            .name("John").email("john@test.com").password("pw").build();
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("john@test.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("이메일 중복 체크")
    void existsByEmail() {
        // given
        userRepository.save(User.builder()
            .name("John").email("john@test.com").password("pw").build());

        // when & then
        assertThat(userRepository.existsByEmail("john@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
    }
}
```

---

## 테스트 작성 원칙

```
Given - When - Then 패턴:
  Given: 테스트 데이터 준비
  When:  테스트 대상 실행
  Then:  결과 검증

테스트 이름: 한글로 명확하게 (@DisplayName)
독립적: 다른 테스트에 영향 없음
반복 가능: 몇 번을 실행해도 결과 동일

실무 테스트 비율:
  단위 테스트: 70%
  통합 테스트: 20%
  E2E 테스트:  10%
```

---

## 면접 예상 질문

1. **단위 테스트와 통합 테스트의 차이는?**
   - 단위: 개별 메서드 독립 테스트(Mock) / 통합: 여러 컴포넌트 함께 테스트

2. **@Mock과 @MockBean의 차이는?**
   - @Mock: Mockito 단위 테스트 / @MockBean: Spring Context의 Bean을 Mock 교체

3. **Given-When-Then 패턴이란?**
   - 준비-실행-검증으로 구조화한 테스트 작성 패턴

4. **테스트 코드를 왜 작성하나요?**
   - 버그 사전 방지, 리팩토링 안전성, 문서화 효과, 설계 개선
