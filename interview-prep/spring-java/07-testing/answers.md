# 테스트 (JUnit/Mockito) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** 테스트 코드를 작성하는 이유는 무엇인가요? 테스트 코드가 주는 이점을 설명해주세요.

> 테스트 코드를 작성하는 이유는 크게 네 가지입니다. 첫째, 버그를 예방합니다. 코드 변경 시 기존 기능이 깨지는 것을 즉시 감지할 수 있습니다. 둘째, 리팩토링의 안전망 역할을 합니다. 테스트가 있으면 코드를 자신 있게 개선할 수 있고, 리팩토링 후에도 기능이 정상 동작하는지 확인할 수 있습니다. 셋째, 살아있는 문서 역할을 합니다. 테스트 코드를 읽으면 "이 기능이 어떤 입력에 어떤 결과를 반환해야 하는지" 알 수 있어 별도의 문서 없이도 기능 명세를 파악할 수 있습니다. 넷째, 설계를 개선합니다. 테스트하기 어려운 코드는 의존성이 강하게 결합된 경우가 많으므로, 테스트를 작성하면서 자연스럽게 DI나 인터페이스 분리 등 좋은 설계를 유도하게 됩니다.

**Q2.** 단위 테스트, 통합 테스트, E2E 테스트의 차이를 설명하고, 테스트 피라미드에 대해 설명해주세요.

> 단위 테스트는 메서드나 클래스 단위로 외부 의존성을 Mock 처리하여 로직만 검증하는 테스트입니다. 매우 빠르고 독립적입니다. 통합 테스트는 여러 컴포넌트가 함께 동작하는지 검증하며, 실제 DB나 Spring Context를 사용합니다. E2E 테스트는 사용자 관점에서 전체 시스템을 테스트하며, 실제 HTTP 요청부터 DB까지 모든 것을 포함합니다.
>
> 테스트 피라미드는 단위 테스트를 가장 많이(70~80%), 통합 테스트를 적당히(15~20%), E2E 테스트를 가장 적게(5~10%) 작성해야 한다는 원칙입니다. 아래로 갈수록 빠르고 안정적이며 유지보수 비용이 낮고, 위로 갈수록 느리고 깨지기 쉽고 원인 파악이 어렵기 때문입니다. 따라서 핵심 비즈니스 로직은 단위 테스트로 철저히 검증하고, 컴포넌트 간 연동은 통합 테스트로, 핵심 사용자 시나리오만 E2E 테스트로 검증하는 것이 효율적입니다.

**Q3.** JUnit 5에서 @BeforeEach와 @BeforeAll의 차이를 설명해주세요. 각각 언제 사용하나요?

> `@BeforeEach`는 각 테스트 메서드가 실행되기 전에 매번 호출됩니다. 각 테스트가 독립적인 환경에서 실행되도록 테스트마다 새로운 객체를 생성하거나 초기 상태를 설정할 때 사용합니다. 예를 들어 `setUp()` 메서드에서 테스트 대상 객체를 새로 생성하는 용도로 가장 많이 사용됩니다.
>
> `@BeforeAll`은 테스트 클래스 전체에서 딱 한 번만 실행되며, static 메서드여야 합니다. 비용이 큰 초기화 작업(DB 연결, 외부 리소스 로딩 등)을 한 번만 수행할 때 사용합니다. 마찬가지로 `@AfterEach`는 각 테스트 후마다, `@AfterAll`은 전체 테스트가 끝난 뒤 한 번 실행됩니다. 실무에서는 `@BeforeEach`를 가장 많이 사용하는데, 테스트 간 상태 공유를 방지하여 독립성을 보장하기 위해서입니다.

**Q4.** Mockito에서 Mock 객체란 무엇이며, 왜 사용하나요?

> Mock 객체는 실제 객체를 흉내 내는 가짜 객체입니다. 단위 테스트에서 테스트 대상 클래스가 의존하는 외부 컴포넌트(Repository, 외부 API 등)를 Mock으로 대체하여 사용합니다.
>
> Mock을 사용하는 이유는 첫째, 외부 의존성(DB, 네트워크, 파일 시스템) 없이 빠르게 테스트할 수 있습니다. 둘째, 테스트 대상 클래스의 로직만 격리하여 검증할 수 있습니다. 셋째, 네트워크 장애나 DB 상태 등 환경에 영향받지 않는 안정적인 테스트가 가능합니다. 예를 들어 OrderService를 테스트할 때 OrderRepository를 Mock으로 만들면, DB 연결 없이 Service의 비즈니스 로직만 검증할 수 있습니다. `when(repository.findById(1L)).thenReturn(order)`처럼 Mock의 행동을 정의하고, 테스트 대상이 올바르게 동작하는지 확인합니다.

**Q5.** Given-When-Then 패턴이란 무엇인가요? 예시를 들어 설명해주세요.

> Given-When-Then은 BDD(Behavior-Driven Development)에서 유래한 테스트 작성 패턴입니다. Given은 테스트에 필요한 데이터와 상태를 준비하는 단계, When은 테스트 대상 행위를 실행하는 단계, Then은 기대 결과를 검증하는 단계입니다.
>
> ```java
> @Test
> @DisplayName("회원은 10% 할인을 받는다")
> void memberDiscount() {
>     // Given - 준비
>     Order order = new Order("상품A", 10000);
>     User member = new User("홍길동", UserType.MEMBER);
>
>     // When - 실행
>     int finalPrice = orderService.calculatePrice(order, member);
>
>     // Then - 검증
>     assertThat(finalPrice).isEqualTo(9000);
> }
> ```
>
> 이 패턴을 사용하면 테스트의 의도가 명확해지고, 코드를 처음 읽는 사람도 테스트가 무엇을 검증하는지 쉽게 이해할 수 있습니다. AAA(Arrange-Act-Assert) 패턴과 구조가 동일하며, 이름만 다릅니다.

## 비교/구분 (6~9)

**Q6.** @Mock과 @MockBean의 차이를 설명해주세요. 각각 어떤 상황에서 사용하나요?

> `@Mock`은 순수 Mockito가 생성하는 Mock 객체로, Spring Context와 무관합니다. `@ExtendWith(MockitoExtension.class)`와 함께 사용하며, `@InjectMocks`를 통해 테스트 대상에 주입합니다. Spring Context를 로딩하지 않으므로 빠르고, Service 계층의 단위 테스트에서 주로 사용합니다.
>
> `@MockBean`은 Spring Boot Test에서 제공하는 어노테이션으로, Spring ApplicationContext에 Mock Bean을 등록하여 기존 Bean을 대체합니다. `@WebMvcTest`, `@SpringBootTest` 등 Spring Context가 필요한 테스트에서 사용합니다. 예를 들어 Controller 테스트에서 Service를 Mock 처리할 때 `@MockBean`을 사용합니다.
>
> | 구분 | @Mock | @MockBean |
> |------|-------|-----------|
> | 소속 | Mockito | Spring Boot Test |
> | Spring Context | 불필요 | 필요 |
> | Bean 등록 | X | ApplicationContext에 등록 |
> | 용도 | 단위 테스트 | Controller 테스트 등 |
> | 같이 쓰는 것 | @InjectMocks | @Autowired |

**Q7.** @WebMvcTest와 @SpringBootTest의 차이를 설명해주세요. Controller를 테스트할 때 어떤 것을 선택해야 하나요?

> `@SpringBootTest`는 전체 Spring ApplicationContext를 로딩하여 모든 Bean을 등록합니다. 실제 환경과 가장 유사하지만, 로딩 시간이 길고 무겁습니다. 전체 시스템의 통합 테스트에 적합합니다.
>
> `@WebMvcTest`는 Controller 계층과 관련된 Bean만 로딩합니다. MockMvc가 자동 구성되며, Service나 Repository는 로딩하지 않으므로 `@MockBean`으로 Mock 처리해야 합니다. Controller의 요청 매핑, 유효성 검증, 응답 형식 등을 빠르게 테스트할 수 있습니다.
>
> Controller를 테스트할 때는 **`@WebMvcTest`를 먼저 고려**하는 것이 좋습니다. Controller의 역할(요청 파싱, 응답 반환, 예외 처리)만 검증하는 것이 목적이라면 @WebMvcTest가 빠르고 효율적입니다. Controller에서 Service, Repository까지 실제로 연동되는 것을 확인하고 싶다면 @SpringBootTest를 사용합니다.

**Q8.** @Mock과 @Spy의 차이를 설명해주세요.

> `@Mock`은 완전한 가짜 객체를 생성합니다. 모든 메서드의 기본 반환값은 null(참조 타입), 0(숫자), false(boolean)이며, Stubbing(`when().thenReturn()`)으로 행동을 정의하지 않으면 아무 동작도 하지 않습니다.
>
> `@Spy`는 실제 객체를 감싸는 부분적 Mock입니다. Stubbing하지 않은 메서드는 실제 구현 코드가 실행되고, 특정 메서드만 선택적으로 행동을 재정의할 수 있습니다. 주로 실제 로직은 유지하면서 일부 메서드만 다르게 동작시키고 싶을 때 사용합니다.
>
> ```java
> @Mock
> List<String> mockList;
> mockList.size();  // → 0 (Stubbing 안 했으므로 기본값)
>
> @Spy
> List<String> spyList = new ArrayList<>();
> spyList.add("A");
> spyList.size();   // → 1 (실제 ArrayList의 동작)
> doReturn(100).when(spyList).size();
> spyList.size();   // → 100 (Stubbing으로 재정의)
> ```
>
> 실무에서는 `@Mock`을 훨씬 더 자주 사용합니다. `@Spy`는 레거시 코드를 테스트하거나, 실제 동작을 대부분 유지하면서 특정 부분만 제어해야 할 때 제한적으로 사용합니다.

**Q9.** MockMvc와 TestRestTemplate의 차이를 설명해주세요.

> MockMvc는 서블릿 컨테이너를 실제로 띄우지 않고 Controller를 테스트하는 도구입니다. HTTP 요청을 시뮬레이션하며, `@WebMvcTest`와 함께 사용합니다. 서버를 띄우지 않으므로 빠르고, Controller 계층의 단위 테스트에 적합합니다.
>
> TestRestTemplate는 실제 내장 서버(Tomcat)를 띄우고 실제 HTTP 요청을 보내는 도구입니다. `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`와 함께 사용합니다. 실제 네트워크를 통한 요청이므로 더 현실적인 테스트가 가능하지만, 서버를 띄우는 만큼 느립니다.
>
> | 구분 | MockMvc | TestRestTemplate |
> |------|---------|-----------------|
> | 서버 | 띄우지 않음 | 실제 내장 서버 |
> | 요청 | 시뮬레이션 | 실제 HTTP |
> | 속도 | 빠름 | 느림 |
> | 용도 | Controller 단위 테스트 | 통합/E2E 테스트 |
> | 어노테이션 | @WebMvcTest | @SpringBootTest |
>
> Controller의 요청/응답 매핑만 빠르게 검증하려면 MockMvc를, 서블릿 필터나 인터셉터를 포함한 전체 HTTP 흐름을 확인하려면 TestRestTemplate를 사용합니다.

## 심화/실무 (10~12)

**Q10.** @WebMvcTest에서 MockMvc를 사용하여 POST 요청을 테스트하는 코드를 작성해주세요.

> ```java
> @WebMvcTest(OrderController.class)
> class OrderControllerTest {
>
>     @Autowired
>     private MockMvc mockMvc;
>
>     @Autowired
>     private ObjectMapper objectMapper;
>
>     @MockBean
>     private OrderService orderService;
>
>     @Test
>     @DisplayName("POST /api/orders - 주문 생성 성공")
>     void createOrder() throws Exception {
>         // given
>         OrderRequest request = new OrderRequest("상품A", 2, 10000);
>         given(orderService.createOrder(any(OrderRequest.class)))
>             .willReturn(1L);
>
>         // when & then
>         mockMvc.perform(post("/api/orders")
>                 .contentType(MediaType.APPLICATION_JSON)
>                 .content(objectMapper.writeValueAsString(request)))
>             .andExpect(status().isCreated())
>             .andExpect(jsonPath("$.orderId").value(1))
>             .andDo(print());
>     }
> }
> ```
>
> 핵심 포인트는 다섯 가지입니다. 첫째, `@WebMvcTest(OrderController.class)`로 Controller만 로딩합니다. 둘째, Service는 `@MockBean`으로 Mock 처리합니다. 셋째, `ObjectMapper`로 요청 객체를 JSON 문자열로 변환합니다. 넷째, `perform(post(...))`으로 POST 요청을 시뮬레이션합니다. 다섯째, `andExpect()`로 상태 코드와 응답 본문을 검증합니다.

**Q11.** TDD(Test-Driven Development)란 무엇이며, 그 사이클을 설명해주세요. 장단점은 무엇인가요?

> TDD는 테스트를 먼저 작성하고, 테스트를 통과하는 코드를 구현한 뒤, 리팩토링하는 개발 방법론입니다. Red-Green-Refactor 사이클을 반복합니다.
>
> Red 단계에서 아직 구현하지 않은 기능에 대한 실패하는 테스트를 작성합니다. Green 단계에서 테스트를 통과하는 최소한의 코드를 작성합니다. Refactor 단계에서 테스트가 계속 통과하는 상태를 유지하면서 코드를 개선합니다.
>
> **장점**:
> - 테스트를 먼저 작성하므로 자연스럽게 테스트하기 좋은 구조의 코드가 나옵니다
> - 요구사항을 테스트로 먼저 정의하므로 기능 명세가 명확해집니다
> - 코드 변경 시 기존 테스트가 안전망 역할을 하여 리팩토링이 쉽습니다
> - 불필요한 코드를 작성하지 않게 됩니다 (YAGNI 원칙과 일치)
>
> **단점**:
> - 초기 개발 속도가 느려질 수 있습니다
> - 요구사항이 빈번하게 변경되는 상황에서 테스트도 함께 수정해야 합니다
> - 팀 전체가 TDD에 익숙하지 않으면 적용하기 어렵습니다
> - 모든 상황에 적합하지는 않습니다 (프로토타이핑, UI 등)

**Q12.** 테스트 커버리지에서 라인 커버리지와 브랜치 커버리지의 차이를 설명해주세요. 커버리지가 높으면 품질이 보장되나요?

> 라인 커버리지는 전체 코드 라인 중 테스트가 실행한 라인의 비율입니다. 브랜치 커버리지는 조건문(if, switch 등)의 모든 분기(true/false)를 실행한 비율입니다. 브랜치 커버리지가 더 엄격합니다.
>
> 예를 들어 `if (isMember) { discount = 10%; }`에서 `isMember = true`로만 테스트하면, 라인 커버리지는 모든 라인이 실행되어 100%이지만, 브랜치 커버리지는 false 분기를 실행하지 않았으므로 50%입니다.
>
> 커버리지가 높다고 품질이 보장되지는 않습니다. 커버리지 100%가 버그 0%를 의미하지 않습니다. 커버리지는 "테스트되지 않은 코드"를 찾는 도구이지, "충분히 테스트된 코드"를 보장하는 지표가 아닙니다. 코드가 실행되었다고 해서 모든 경계값, 예외 상황, 비즈니스 규칙이 검증된 것은 아닙니다. 따라서 커버리지는 참고 지표로 활용하되, 의미 있는 테스트를 작성하는 것이 더 중요합니다. 실무에서는 라인 커버리지 80% 이상을 목표로 하는 경우가 많습니다.

## 꼬리질문 대비 (13~15)

**Q13.** Service 계층의 단위 테스트에서 Repository를 Mock 처리했을 때, 실제 DB와 동작이 다를 수 있는 문제는 어떻게 해결하나요?

> 이것은 단위 테스트의 본질적인 한계입니다. Mock은 개발자가 정의한 대로만 동작하므로, 실제 DB의 쿼리 결과나 제약 조건(unique, foreign key 등)과 다를 수 있습니다. 이 문제를 해결하기 위해 테스트 피라미드에서 여러 레이어의 테스트를 함께 작성합니다.
>
> 첫째, Service의 비즈니스 로직은 `@Mock`을 사용한 단위 테스트로 빠르게 검증합니다. 둘째, Repository의 쿼리 동작은 `@DataJpaTest`로 내장 DB(H2)를 사용하여 검증합니다. 특히 커스텀 쿼리(@Query)나 복잡한 JPQL은 반드시 Repository 테스트를 작성해야 합니다. 셋째, Service에서 Repository까지 연동이 올바르게 동작하는지는 `@SpringBootTest`로 통합 테스트를 작성하여 확인합니다.
>
> 즉, 각 레이어의 테스트가 보완적으로 동작하여 단위 테스트의 한계를 커버하는 것입니다. Mock 단위 테스트만으로 모든 것을 검증하려 하지 않고, 적절한 수준의 통합 테스트를 병행하는 것이 핵심입니다.

**Q14.** @Transactional을 테스트에 붙이면 어떻게 동작하나요? 테스트에서 @Transactional 사용 시 주의할 점은 무엇인가요?

> 테스트 클래스나 메서드에 `@Transactional`을 붙이면, 각 테스트 메서드가 하나의 트랜잭션 안에서 실행되고, 테스트가 끝나면 자동으로 **롤백**됩니다. 이를 통해 테스트가 DB 상태를 변경하지 않으므로 테스트 간 데이터가 격리되어 독립성이 보장됩니다. `@DataJpaTest`에는 `@Transactional`이 기본으로 포함되어 있습니다.
>
> 주의할 점이 있습니다. 첫째, 테스트에서는 롤백되기 때문에 `@Transactional`의 실제 커밋/롤백 동작은 검증되지 않습니다. 예를 들어 Service에서 트랜잭션 전파 속성을 잘못 설정해도 테스트에서는 발견하지 못할 수 있습니다. 둘째, 테스트의 `@Transactional`이 프로덕션 코드의 `@Transactional`과 합쳐져서 하나의 트랜잭션에서 실행됩니다. 프로덕션에서는 Service 메서드 종료 시 flush가 일어나지만, 테스트에서는 영속성 컨텍스트가 테스트 끝까지 유지되어 LazyLoading이 테스트에서만 성공하는 경우(N+1 문제를 못 잡는 경우)가 발생할 수 있습니다. 이런 이유로 E2E/통합 테스트에서는 `@Transactional` 없이 실제 커밋 동작을 확인하는 것도 필요합니다.

**Q15.** BDDMockito의 given().willReturn()과 Mockito의 when().thenReturn()은 무엇이 다른가요? 왜 BDDMockito를 사용하나요?

> 기능적으로는 동일합니다. `when(mock.method()).thenReturn(value)`와 `given(mock.method()).willReturn(value)`는 같은 동작을 합니다. 차이는 이름과 가독성에 있습니다.
>
> BDDMockito를 사용하는 이유는 Given-When-Then 패턴과의 일관성 때문입니다. 테스트를 Given-When-Then으로 작성할 때, "Given" 단계에서 Mockito의 `when()`을 사용하면 테스트 단계의 "When"과 이름이 혼동됩니다. BDDMockito의 `given()`을 사용하면 "Given 단계에서 given()으로 Stubbing"이 되어 자연스럽게 읽힙니다.
>
> ```java
> // Mockito 기본 - "when"이 두 번 등장하여 혼동
> // given (준비)
> when(repository.findById(1L)).thenReturn(Optional.of(order));
> // when (실행)
> Order result = service.findOrder(1L);
>
> // BDDMockito - 단계별로 깔끔하게 분리
> // given (준비)
> given(repository.findById(1L)).willReturn(Optional.of(order));
> // when (실행)
> Order result = service.findOrder(1L);
> // then (검증)
> then(repository).should().findById(1L);
> ```
>
> 검증도 `verify(mock).method()` 대신 `then(mock).should().method()`를 사용하여 Then 단계와 자연스럽게 어울립니다. 팀 코딩 컨벤션으로 BDDMockito를 표준으로 사용하는 경우가 많습니다.
