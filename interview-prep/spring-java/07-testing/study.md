# 7. 테스트 (JUnit / Mockito)

---

## 테스트를 왜 하는가?

테스트 코드는 "귀찮은 추가 작업"이 아니라 **소프트웨어 품질을 보장하는 핵심 도구**다.

| 목적 | 설명 | 예시 |
|------|------|------|
| **버그 예방** | 코드 변경 시 기존 기능이 깨지는 것을 즉시 감지 | 할인 로직 변경 후 기존 주문 계산이 틀어지는 것 방지 |
| **리팩토링 안전망** | 테스트가 있으면 자신 있게 코드를 개선할 수 있다 | 메서드 추출, 클래스 분리 후 테스트 통과 확인 |
| **살아있는 문서** | 테스트 코드가 "이 기능은 이렇게 동작해야 한다"를 보여준다 | `shouldReturnDiscountedPrice()` 테스트 자체가 스펙 |
| **설계 개선** | 테스트하기 어려운 코드 = 설계가 나쁜 코드 | 의존성이 강한 코드를 테스트하면서 DI로 개선하게 됨 |

> **면접 포인트**: "테스트 코드를 작성하는 이유는 버그를 예방하고, 리팩토링의 안전망 역할을 하며,
> 코드의 동작을 문서화하는 효과가 있기 때문입니다."

---

## 테스트 종류 비교

### 단위 테스트 (Unit Test)

가장 작은 단위(메서드, 클래스)를 **외부 의존성 없이** 테스트한다.

```java
// Service 로직만 단독으로 테스트
@Test
void calculateDiscount() {
    OrderService service = new OrderService(mockRepository);
    int result = service.calculateDiscount(10000, 0.1);
    assertEquals(9000, result);
}
```

### 통합 테스트 (Integration Test)

여러 컴포넌트가 **함께 동작**하는지 테스트한다. DB, Spring Context 등 실제 환경을 사용.

```java
// Controller → Service → Repository 전체 흐름 테스트
@SpringBootTest
@Transactional
void createOrderIntegration() {
    OrderRequest request = new OrderRequest("상품A", 2);
    Order order = orderService.createOrder(request);
    assertNotNull(orderRepository.findById(order.getId()));
}
```

### E2E 테스트 (End-to-End Test)

사용자 관점에서 **전체 시스템**을 테스트한다. 실제 HTTP 요청부터 DB까지 전부.

```java
// 실제 서버를 띄우고 HTTP 요청
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
void createOrderE2E() {
    ResponseEntity<Order> response = restTemplate
        .postForEntity("/api/orders", request, Order.class);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
}
```

### 비교 정리

| 구분 | 단위 테스트 | 통합 테스트 | E2E 테스트 |
|------|-----------|-----------|-----------|
| **범위** | 메서드/클래스 | 여러 컴포넌트 | 전체 시스템 |
| **속도** | 매우 빠름 (ms) | 보통 (초) | 느림 (수초~분) |
| **외부 의존성** | Mock 처리 | 실제 사용 (DB 등) | 모두 실제 |
| **Spring Context** | 불필요 | 필요 | 필요 |
| **목적** | 로직 정확성 | 연동 정확성 | 시나리오 검증 |
| **유지보수 비용** | 낮음 | 보통 | 높음 |
| **신뢰도** | 로직 수준 | 연동 수준 | 가장 높음 |

---

## 테스트 피라미드

```
          /\
         /  \         E2E 테스트 (적게)
        / E2E\        - 느리고 비용 높음
       /------\       - 핵심 시나리오만
      /        \
     /Integration\    통합 테스트 (적당히)
    /   Test      \   - Spring Context, DB 연동
   /--------------\
  /                \
 /    Unit Test     \  단위 테스트 (가장 많이)
/                    \ - 빠르고 안정적
/--------------------\
```

**핵심 원칙**:
- **단위 테스트를 가장 많이** 작성한다 (70~80%)
- 통합 테스트로 컴포넌트 간 연동을 확인한다 (15~20%)
- E2E 테스트는 핵심 시나리오만 최소한으로 작성한다 (5~10%)

> **이유**: 단위 테스트는 빠르고 안정적이며 유지보수 비용이 낮다.
> 위로 갈수록 느리고, 깨지기 쉽고, 원인 파악이 어렵다.

---

## JUnit 5 기본

### 핵심 어노테이션

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeAll  // 클래스 전체에서 1번만 실행 (static 필수)
    static void beforeAll() {
        System.out.println("테스트 클래스 시작");
    }

    @BeforeEach  // 각 테스트 메서드 실행 전마다 실행
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    @DisplayName("정상 주문 시 총액이 올바르게 계산된다")
    void calculateTotalAmount() {
        // given
        int price = 10000;
        int quantity = 3;

        // when
        int total = orderService.calculateTotal(price, quantity);

        // then
        assertEquals(30000, total);
    }

    @AfterEach  // 각 테스트 메서드 실행 후마다 실행
    void tearDown() {
        System.out.println("테스트 종료");
    }

    @AfterAll  // 클래스 전체에서 1번만 실행 (static 필수)
    static void afterAll() {
        System.out.println("테스트 클래스 종료");
    }
}
```

### 생명주기 실행 순서

```
@BeforeAll (1번)
  │
  ├─ @BeforeEach → @Test (test1) → @AfterEach
  ├─ @BeforeEach → @Test (test2) → @AfterEach
  └─ @BeforeEach → @Test (test3) → @AfterEach
  │
@AfterAll (1번)
```

> **@BeforeEach**를 가장 많이 사용한다. 각 테스트가 독립적으로 실행되도록
> 테스트마다 새로운 객체를 생성하는 것이 일반적이다.

### Assertions (검증)

```java
// 값 비교
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);

// null 검증
assertNull(value);
assertNotNull(value);

// boolean 검증
assertTrue(condition);
assertFalse(condition);

// 예외 검증 (가장 자주 사용되는 패턴 중 하나)
assertThrows(IllegalArgumentException.class, () -> {
    orderService.createOrder(null);
});

// 예외 메시지까지 검증
IllegalArgumentException exception = assertThrows(
    IllegalArgumentException.class,
    () -> orderService.createOrder(null)
);
assertEquals("주문 정보가 없습니다", exception.getMessage());

// 여러 검증을 한 번에 (하나 실패해도 나머지 모두 실행)
assertAll(
    () -> assertEquals("홍길동", user.getName()),
    () -> assertEquals(25, user.getAge()),
    () -> assertNotNull(user.getEmail())
);
```

### AssertJ (더 읽기 좋은 검증)

실무에서는 JUnit의 기본 Assertions보다 **AssertJ**를 더 많이 사용한다.

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 값 검증 - 메서드 체이닝으로 가독성 좋음
assertThat(actual).isEqualTo(expected);
assertThat(name).isNotEmpty();
assertThat(age).isGreaterThan(0).isLessThan(200);

// 컬렉션 검증
assertThat(list).hasSize(3);
assertThat(list).contains("A", "B");
assertThat(list).containsExactly("A", "B", "C");  // 순서까지 검증

// 예외 검증
assertThatThrownBy(() -> orderService.createOrder(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("주문 정보가 없습니다");
```

> **JUnit assertEquals vs AssertJ assertThat**:
> `assertEquals(expected, actual)` - 매개변수 순서가 헷갈림
> `assertThat(actual).isEqualTo(expected)` - 자연어처럼 읽힘

### @ParameterizedTest (매개변수화 테스트)

같은 로직을 **여러 입력값**으로 반복 테스트할 때 사용한다.

```java
@ParameterizedTest
@DisplayName("할인율에 따른 가격 계산")
@CsvSource({
    "10000, 0.1, 9000",    // 10% 할인
    "20000, 0.2, 16000",   // 20% 할인
    "5000,  0.0, 5000",    // 할인 없음
    "10000, 1.0, 0"        // 100% 할인
})
void calculateDiscountedPrice(int price, double rate, int expected) {
    int result = calculator.discount(price, rate);
    assertEquals(expected, result);
}

// 여러 문자열 값으로 테스트
@ParameterizedTest
@ValueSource(strings = {"", " ", "  "})
void shouldRejectBlankName(String name) {
    assertThrows(IllegalArgumentException.class,
        () -> new User(name));
}

// null과 빈 문자열 테스트
@ParameterizedTest
@NullAndEmptySource
void shouldRejectNullOrEmptyName(String name) {
    assertThrows(IllegalArgumentException.class,
        () -> new User(name));
}

// Enum 값으로 테스트
@ParameterizedTest
@EnumSource(OrderStatus.class)
void allStatusShouldHaveDescription(OrderStatus status) {
    assertNotNull(status.getDescription());
}
```

---

## Mockito 기본

### Mock이란? 왜 사용하는가?

**Mock**: 실제 객체를 흉내 내는 가짜 객체.

```
단위 테스트에서의 문제:

OrderService를 테스트하고 싶다
  → OrderService는 OrderRepository에 의존
    → OrderRepository는 DB에 의존
      → DB 연결이 필요?  ← 단위 테스트가 아니게 됨!

해결: OrderRepository를 Mock으로 대체

OrderService  →  Mock(OrderRepository)  ← DB 연결 불필요!
                 "findById(1L) 호출하면
                  Order 객체를 반환해줘"
```

**Mock을 사용하는 이유**:
- 외부 의존성(DB, API, 파일) 없이 **빠르게** 테스트
- 테스트 대상 클래스의 **로직만** 검증 가능
- 네트워크 장애 등 **환경에 영향받지 않는** 테스트

### @Mock, @InjectMocks, @Spy 차이

```java
@ExtendWith(MockitoExtension.class)  // Mockito 활성화
class OrderServiceTest {

    @Mock  // 가짜 객체 생성 (모든 메서드가 기본값 반환)
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks  // @Mock으로 만든 객체를 자동 주입
    private OrderService orderService;

    @Spy  // 실제 객체를 감싸서 일부 메서드만 Stubbing 가능
    private PriceCalculator priceCalculator = new PriceCalculator();
}
```

| 어노테이션 | 역할 | 특징 |
|-----------|------|------|
| **@Mock** | 가짜 객체 생성 | 모든 메서드가 null/0/false 반환 |
| **@InjectMocks** | Mock을 주입받는 테스트 대상 | 생성자/세터로 Mock 자동 주입 |
| **@Spy** | 실제 객체를 감싼 부분 Mock | Stubbing하지 않은 메서드는 실제 동작 |

```
@Mock: 완전한 가짜. 모든 행동을 정의해야 함
  → orderRepository.findById(1L) → null (기본)
  → when(orderRepository.findById(1L)).thenReturn(order); → order 반환

@Spy: 실제 객체 + 일부 가짜
  → priceCalculator.calculate(100) → 실제 계산 결과 반환
  → doReturn(0).when(priceCalculator).calculate(100); → 0 반환 (오버라이드)
```

### when().thenReturn() 사용법

```java
@Test
@DisplayName("주문 조회 - 정상 케이스")
void findOrder() {
    // given - Mock 행동 정의 (Stubbing)
    Order order = new Order(1L, "상품A", 10000);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    // when - 테스트 대상 실행
    Order result = orderService.findOrder(1L);

    // then - 검증
    assertEquals("상품A", result.getProductName());
    assertEquals(10000, result.getPrice());
}

@Test
@DisplayName("존재하지 않는 주문 조회 시 예외 발생")
void findOrder_NotFound() {
    // given
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    // when & then
    assertThrows(OrderNotFoundException.class,
        () -> orderService.findOrder(99L));
}

// 예외를 던지도록 Stubbing
@Test
void shouldThrowWhenPaymentFails() {
    when(paymentService.pay(any()))
        .thenThrow(new PaymentException("결제 실패"));

    assertThrows(PaymentException.class,
        () -> orderService.createOrder(request));
}
```

### verify() - 호출 검증

```java
@Test
void createOrder() {
    // given
    when(orderRepository.save(any(Order.class)))
        .thenReturn(new Order(1L, "상품A", 10000));

    // when
    orderService.createOrder(request);

    // then - Mock의 메서드가 호출되었는지 검증
    verify(orderRepository).save(any(Order.class));       // 1번 호출 확인
    verify(orderRepository, times(1)).save(any());        // 정확히 1번
    verify(orderRepository, never()).deleteById(any());   // 호출 안 됨 확인
    verify(paymentService, atLeastOnce()).pay(any());     // 최소 1번 호출
}
```

### BDDMockito (BDD 스타일)

**BDD(Behavior-Driven Development)** 스타일로 가독성을 높인 Mockito 래퍼.

```java
import static org.mockito.BDDMockito.*;

@Test
@DisplayName("BDD 스타일 - 주문 조회")
void findOrderBDD() {
    // given - when 대신 given 사용
    Order order = new Order(1L, "상품A", 10000);
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));

    // when
    Order result = orderService.findOrder(1L);

    // then - verify 대신 then 사용
    then(orderRepository).should().findById(1L);
    assertThat(result.getProductName()).isEqualTo("상품A");
}
```

| Mockito 기본 | BDDMockito |
|-------------|------------|
| `when(mock.method()).thenReturn(value)` | `given(mock.method()).willReturn(value)` |
| `when(mock.method()).thenThrow(ex)` | `given(mock.method()).willThrow(ex)` |
| `verify(mock).method()` | `then(mock).should().method()` |

> **BDDMockito를 추천하는 이유**: Given-When-Then 패턴과 자연스럽게 어울리며,
> `when`이 Mockito의 `when`인지 테스트 단계의 "when"인지 헷갈리지 않는다.

---

## Spring Boot 테스트

### @SpringBootTest (통합 테스트)

**전체 Spring Context**를 로딩하여 테스트한다.

```java
@SpringBootTest  // 모든 Bean을 로딩
@Transactional   // 테스트 후 롤백
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 생성 통합 테스트")
    void createOrder() {
        // given
        OrderRequest request = new OrderRequest("상품A", 2, 10000);

        // when
        Long orderId = orderService.createOrder(request);

        // then
        Order saved = orderRepository.findById(orderId).orElseThrow();
        assertThat(saved.getProductName()).isEqualTo("상품A");
        assertThat(saved.getTotalPrice()).isEqualTo(20000);
    }
}
```

### @WebMvcTest (Controller 테스트)

**Controller 레이어만** 로딩한다. Service, Repository는 로딩하지 않는다.

```java
@WebMvcTest(OrderController.class)  // Controller만 로딩
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;  // HTTP 요청을 시뮬레이션

    @MockBean  // Spring Context에 Mock Bean 등록
    private OrderService orderService;

    @Test
    @DisplayName("GET /api/orders/{id} - 주문 조회 성공")
    void getOrder() throws Exception {
        // given
        OrderResponse response = new OrderResponse(1L, "상품A", 10000);
        given(orderService.findOrder(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/orders/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.productName").value("상품A"))
            .andExpect(jsonPath("$.price").value(10000))
            .andDo(print());  // 요청/응답 출력
    }

    @Test
    @DisplayName("POST /api/orders - 주문 생성 성공")
    void createOrder() throws Exception {
        // given
        OrderRequest request = new OrderRequest("상품A", 2, 10000);
        given(orderService.createOrder(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    @DisplayName("GET /api/orders/999 - 존재하지 않는 주문")
    void getOrder_NotFound() throws Exception {
        // given
        given(orderService.findOrder(999L))
            .willThrow(new OrderNotFoundException("주문을 찾을 수 없습니다"));

        // when & then
        mockMvc.perform(get("/api/orders/{id}", 999L))
            .andExpect(status().isNotFound());
    }
}
```

### @DataJpaTest (Repository 테스트)

**JPA 관련 Bean만** 로딩한다. 내장 DB(H2)를 자동으로 사용.

```java
@DataJpaTest  // JPA 관련 Bean만 로딩 + 내장 DB
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("상품명으로 주문 검색")
    void findByProductName() {
        // given
        Order order = Order.builder()
            .productName("상품A")
            .quantity(2)
            .price(10000)
            .build();
        entityManager.persist(order);
        entityManager.flush();

        // when
        List<Order> orders = orderRepository.findByProductName("상품A");

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getProductName()).isEqualTo("상품A");
    }
}
```

### @MockBean vs @Mock 차이

```
@Mock (Mockito)
  - 순수 Mockito가 생성하는 Mock 객체
  - Spring Context와 무관
  - 단위 테스트에서 사용
  - @ExtendWith(MockitoExtension.class) 필요

@MockBean (Spring Boot Test)
  - Spring ApplicationContext에 Mock Bean을 등록
  - 기존 Bean을 Mock으로 대체
  - @WebMvcTest, @SpringBootTest 등 Spring 테스트에서 사용
  - Spring Context가 필요한 테스트에서 사용
```

| 구분 | @Mock | @MockBean |
|------|-------|-----------|
| **소속** | Mockito | Spring Boot Test |
| **Spring Context** | 불필요 | 필요 |
| **Bean 등록** | 안 함 | Spring Context에 등록 |
| **용도** | 단위 테스트 | 통합 테스트 (Controller 등) |
| **속도** | 빠름 | 상대적으로 느림 (Context 로딩) |
| **같이 쓰는 것** | @InjectMocks | @Autowired |

```java
// @Mock 사용 - 단위 테스트
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;
}

// @MockBean 사용 - Spring 통합 테스트
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @MockBean
    private OrderService orderService;
    @Autowired
    private MockMvc mockMvc;
}
```

### MockMvc 주요 메서드 정리

```java
// 요청 메서드
mockMvc.perform(get("/api/orders"))       // GET 요청
mockMvc.perform(post("/api/orders"))      // POST 요청
mockMvc.perform(put("/api/orders/1"))     // PUT 요청
mockMvc.perform(delete("/api/orders/1"))  // DELETE 요청

// 요청 설정
.contentType(MediaType.APPLICATION_JSON)  // Content-Type 헤더
.content("{\"name\":\"상품A\"}")           // 요청 본문
.header("Authorization", "Bearer token")  // 커스텀 헤더
.param("page", "0")                       // 쿼리 파라미터

// 응답 검증
.andExpect(status().isOk())               // 200
.andExpect(status().isCreated())          // 201
.andExpect(status().isNotFound())         // 404
.andExpect(status().isBadRequest())       // 400

// JSON 응답 검증
.andExpect(jsonPath("$.id").value(1))              // 단일 필드
.andExpect(jsonPath("$.name").value("상품A"))       // 문자열
.andExpect(jsonPath("$.items").isArray())           // 배열 확인
.andExpect(jsonPath("$.items.length()").value(3))   // 배열 크기

// 디버깅
.andDo(print())  // 요청/응답 전체 출력
```

### TestRestTemplate / WebTestClient

```java
// TestRestTemplate - 실제 HTTP 요청 (동기)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getOrder() {
        ResponseEntity<OrderResponse> response = restTemplate
            .getForEntity("/api/orders/1", OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getProductName()).isEqualTo("상품A");
    }
}

// WebTestClient - 비동기/반응형 지원 (WebFlux)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getOrder() {
        webTestClient.get().uri("/api/orders/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.productName").isEqualTo("상품A");
    }
}
```

| 구분 | MockMvc | TestRestTemplate | WebTestClient |
|------|---------|-----------------|---------------|
| **HTTP 요청** | 시뮬레이션 (서블릿 컨테이너 X) | 실제 HTTP | 실제 HTTP |
| **서버** | 서버 없이 동작 | 내장 서버 필요 | 내장 서버 필요 |
| **속도** | 빠름 | 느림 | 느림 |
| **용도** | Controller 단위 테스트 | 통합/E2E 테스트 | WebFlux/통합 테스트 |
| **비동기** | X | X | O |

---

## 테스트 작성 패턴

### Given-When-Then 패턴

BDD(Behavior-Driven Development)에서 유래한 패턴. **가장 널리 사용되는 테스트 구조**.

```java
@Test
@DisplayName("10% 할인 쿠폰 적용 시 가격이 정확히 계산된다")
void applyCoupon() {
    // Given (준비) - 테스트에 필요한 데이터와 상태 설정
    Order order = new Order("상품A", 10000);
    Coupon coupon = new Coupon("DISCOUNT10", 0.1);

    // When (실행) - 테스트 대상 행위 실행
    order.applyCoupon(coupon);

    // Then (검증) - 기대 결과 확인
    assertThat(order.getFinalPrice()).isEqualTo(9000);
}
```

### AAA 패턴 (Arrange-Act-Assert)

Given-When-Then과 사실상 같은 구조. **이름만 다르다**.

```java
@Test
void calculateTotal() {
    // Arrange (준비) = Given
    Cart cart = new Cart();
    cart.addItem(new Item("A", 1000));
    cart.addItem(new Item("B", 2000));

    // Act (실행) = When
    int total = cart.calculateTotal();

    // Assert (검증) = Then
    assertEquals(3000, total);
}
```

### 좋은 테스트의 조건 (FIRST 원칙)

| 원칙 | 의미 | 설명 |
|------|------|------|
| **F**ast | 빠르게 | 테스트가 느리면 자주 실행하지 않게 된다 |
| **I**ndependent | 독립적으로 | 테스트 간 순서나 상태에 의존하면 안 된다 |
| **R**epeatable | 반복 가능하게 | 언제, 어디서 실행해도 같은 결과여야 한다 |
| **S**elf-validating | 자가 검증 | 수동 확인 없이 성공/실패가 자동으로 판단되어야 한다 |
| **T**imely | 적시에 | 코드 작성과 동시에(또는 먼저) 테스트를 작성한다 |

```java
// 나쁜 테스트 - 독립적이지 않음 (I 위반)
static int counter = 0;

@Test
void test1() { counter++; assertEquals(1, counter); }  // test2가 먼저 실행되면 실패

@Test
void test2() { counter++; assertEquals(1, counter); }

// 좋은 테스트 - 독립적
@BeforeEach
void setUp() { counter = 0; }  // 매 테스트 전 초기화
```

```java
// 나쁜 테스트 - 반복 불가능 (R 위반)
@Test
void checkTodayOrder() {
    assertTrue(order.getDate().equals(LocalDate.of(2024, 1, 15)));
    // 날짜가 바뀌면 실패!
}

// 좋은 테스트 - 반복 가능
@Test
void checkTodayOrder() {
    assertTrue(order.getDate().equals(LocalDate.now()));
    // 또는 Clock을 주입하여 시간을 제어
}
```

---

## TDD (Test-Driven Development)

### Red -> Green -> Refactor 사이클

```
┌─────────┐     ┌──────────┐     ┌───────────┐
│   RED    │────→│  GREEN   │────→│ REFACTOR  │
│ 실패하는  │     │ 통과하는  │     │ 코드 개선  │
│ 테스트 작성│     │ 최소 코드 │     │ 테스트 유지 │
└─────────┘     └──────────┘     └─────┬─────┘
      ↑                                 │
      └─────────────────────────────────┘
              다음 기능으로 반복
```

**1단계: RED - 실패하는 테스트 작성**
```java
@Test
void addItem() {
    Cart cart = new Cart();
    cart.addItem(new Item("A", 1000));
    assertEquals(1, cart.getItemCount());  // 컴파일 에러 또는 실패
}
```

**2단계: GREEN - 테스트를 통과하는 최소한의 코드 작성**
```java
public class Cart {
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public int getItemCount() {
        return items.size();
    }
}
```

**3단계: REFACTOR - 코드 개선 (테스트는 계속 통과해야 함)**
```java
public class Cart {
    private final List<Item> items = new ArrayList<>();  // final 추가

    public void addItem(Item item) {
        Objects.requireNonNull(item, "item은 null일 수 없습니다");  // 검증 추가
        items.add(item);
    }

    public int getItemCount() {
        return items.size();
    }
}
```

> **TDD의 핵심**: "테스트를 먼저 작성하면, 자연스럽게 테스트하기 좋은(= 설계가 좋은) 코드가 나온다."

---

## 테스트 커버리지

### 라인 커버리지 (Line Coverage)

**전체 코드 라인 중 테스트가 실행한 라인의 비율.**

```java
public int calculate(int price, boolean isMember) {
    int discount = 0;                    // 1번 라인
    if (isMember) {                      // 2번 라인
        discount = price / 10;           // 3번 라인
    }                                    //
    return price - discount;             // 4번 라인
}
```

```java
// 테스트: isMember = true로만 테스트
@Test
void memberDiscount() {
    assertEquals(9000, calculate(10000, true));
}
// 실행된 라인: 1, 2, 3, 4 → 라인 커버리지: 4/4 = 100%
// 하지만 isMember = false 케이스는 테스트 안 됨!
```

### 브랜치 커버리지 (Branch Coverage)

**조건문의 모든 분기(true/false)를 실행한 비율.** 라인 커버리지보다 더 엄격.

```java
// 위 코드의 브랜치: if (isMember) → true / false 2개

// isMember = true만 테스트
// → 브랜치 커버리지: 1/2 = 50% (false 분기 미실행)

// isMember = true AND false 모두 테스트
// → 브랜치 커버리지: 2/2 = 100%
```

| 커버리지 종류 | 측정 대상 | 의미 |
|-------------|----------|------|
| **라인 커버리지** | 실행된 코드 라인 수 | 기본적인 커버리지 |
| **브랜치 커버리지** | 조건문의 분기 경로 | 더 정밀한 커버리지 |

> **주의**: 커버리지 100%가 버그 0%를 의미하지는 않는다.
> 커버리지는 "테스트되지 않은 코드"를 찾는 도구이지, "충분히 테스트된 코드"를 보장하지 않는다.
> 실무에서는 **라인 커버리지 80% 이상, 브랜치 커버리지 70% 이상**을 목표로 하는 경우가 많다.

### JaCoCo (커버리지 도구)

```groovy
// build.gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    reports {
        html.required = true
        xml.required = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 최소 80% 커버리지
            }
        }
    }
}
```

```bash
# 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 커버리지 검증 (80% 미만이면 빌드 실패)
./gradlew jacocoTestCoverageVerification
```

---

## Spring Boot 테스트 어노테이션 정리

```
┌────────────────────────────────────────────────────────┐
│              @SpringBootTest                            │
│  모든 Bean 로딩 (전체 통합 테스트)                        │
│  가장 느림, 하지만 가장 실제 환경에 가까움                  │
└────────────────────────────────────────────────────────┘

┌─────────────────┐ ┌─────────────────┐ ┌───────────────┐
│  @WebMvcTest     │ │  @DataJpaTest   │ │ @RestClient-  │
│  Controller 계층 │ │  Repository 계층│ │   Test        │
│  MockMvc 자동구성│ │  내장 DB 사용    │ │ REST 클라이언트│
│  Service Mock   │ │  @Transactional │ │               │
└─────────────────┘ └─────────────────┘ └───────────────┘

┌─────────────────┐ ┌─────────────────┐
│  @JsonTest       │ │  @WebFluxTest   │
│  JSON 직렬화     │ │  WebFlux 컨트롤러│
│  /역직렬화 테스트 │ │  WebTestClient  │
└─────────────────┘ └─────────────────┘
```

| 어노테이션 | 로딩 범위 | 주요 용도 | 속도 |
|-----------|----------|----------|------|
| `@SpringBootTest` | 전체 Bean | 통합 테스트 | 느림 |
| `@WebMvcTest` | Controller + 관련 Bean | Controller 테스트 | 빠름 |
| `@DataJpaTest` | JPA + Repository Bean | Repository 테스트 | 보통 |
| `@JsonTest` | JSON 관련 Bean | 직렬화/역직렬화 | 빠름 |

---

## 면접 핵심 정리

**Q: 단위 테스트와 통합 테스트의 차이는?**
> 단위 테스트는 메서드/클래스 단위로 외부 의존성을 Mock 처리하여 로직만 검증합니다.
> 빠르고 독립적이며, 테스트 피라미드에서 가장 많이 작성해야 합니다.
> 통합 테스트는 여러 컴포넌트가 함께 동작하는지 실제 DB, Spring Context 등을 사용하여 검증합니다.
> 더 현실적이지만 느리고 유지보수 비용이 높습니다.

**Q: @Mock과 @MockBean의 차이는?**
> @Mock은 순수 Mockito가 생성하는 Mock 객체로 Spring Context와 무관합니다. @InjectMocks와 함께 사용하여 단위 테스트에서 활용합니다.
> @MockBean은 Spring ApplicationContext에 Mock Bean을 등록하여 기존 Bean을 대체합니다. @WebMvcTest 같은 Spring 테스트에서 사용하며, @Autowired로 주입받습니다.

**Q: MockMvc란 무엇인가요?**
> MockMvc는 실제 서버를 띄우지 않고 Controller 계층을 테스트할 수 있는 Spring 테스트 도구입니다.
> HTTP 요청을 시뮬레이션하여 perform()으로 요청을 보내고, andExpect()로 상태 코드, 응답 본문 등을 검증합니다.
> @WebMvcTest와 함께 사용하면 Controller만 빠르게 테스트할 수 있습니다.

**Q: TDD란 무엇인가요?**
> TDD(Test-Driven Development)는 테스트를 먼저 작성하고, 테스트를 통과하는 최소한의 코드를 구현한 뒤, 리팩토링하는 개발 방법론입니다.
> Red(실패) -> Green(통과) -> Refactor(개선) 사이클을 반복합니다.
> 테스트를 먼저 작성하면 자연스럽게 테스트하기 좋은 구조의 코드가 나오는 장점이 있습니다.

**Q: 테스트 커버리지가 높으면 품질이 보장되나요?**
> 아닙니다. 커버리지 100%가 버그 0%를 의미하지는 않습니다.
> 커버리지는 "테스트되지 않은 코드"를 찾는 도구이며, 모든 코드 라인이 실행되었다고 해서 모든 경우의 수가 검증된 것은 아닙니다.
> 예를 들어 라인 커버리지 100%여도 브랜치 커버리지가 50%일 수 있고, 경계값 테스트나 예외 상황 테스트가 빠져 있을 수 있습니다.
> 커버리지는 참고 지표이며, 의미 있는 테스트를 작성하는 것이 더 중요합니다.
