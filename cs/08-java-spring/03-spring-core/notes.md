# Spring Core (IoC, DI, AOP)

## IoC (제어의 역전, Inversion of Control)

### 개념

```
기존: 개발자가 직접 객체를 생성하고 관리
  OrderService service = new OrderService(new OrderRepository());

IoC: 프레임워크(Spring 컨테이너)가 객체를 생성하고 관리
  → 개발자는 "무엇을"에 집중, "어떻게"는 Spring이 처리

"제어권"이 개발자 → Spring 컨테이너로 역전
```

### IoC 컨테이너

```
Spring IoC 컨테이너 (= ApplicationContext)
├── Bean 정의를 읽음 (@Component, @Configuration 등)
├── Bean 객체를 생성
├── 의존성을 주입 (DI)
├── 생명주기 관리 (초기화 → 사용 → 소멸)
└── 싱글톤으로 관리 (기본)
```

---

## DI (의존성 주입, Dependency Injection)

IoC를 구현하는 방법. 외부에서 의존 객체를 넣어준다.

### 주입 방식 3가지

#### 1. 생성자 주입 (권장)

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    // 생성자 주입: final 가능, 불변 보장
    // @Autowired 생략 가능 (생성자가 1개일 때)
    public OrderService(OrderRepository orderRepository,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}

// Lombok으로 간소화
@Service
@RequiredArgsConstructor  // final 필드의 생성자 자동 생성
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
}
```

#### 2. 필드 주입 (비권장)

```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    // 문제: final 불가, 테스트에서 Mock 주입 어려움
}
```

#### 3. Setter 주입

```java
@Service
public class OrderService {
    private OrderRepository orderRepository;

    @Autowired
    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    // 선택적 의존성에 사용 (거의 안 씀)
}
```

### 왜 생성자 주입을 권장하나? (면접 빈출)

```
1. 불변성: final로 선언 → 변경 불가, 안전
2. 필수 의존성 보장: 생성자에서 null 체크 가능
3. 테스트 용이: new OrderService(mockRepo, mockPay) 가능
4. 순환 참조 방지: 컴파일 타임에 순환 의존 감지
5. DI 컨테이너 없이도 동작: 순수 Java로 테스트 가능
```

---

## Bean

### Bean이란?

```
Spring IoC 컨테이너가 관리하는 객체.

@Component, @Service, @Repository, @Controller로 등록
→ Spring이 자동으로 객체 생성 + 관리
```

### Component Scan과 스테레오 타입

```java
@Component      // 일반 컴포넌트
@Service        // 비즈니스 로직 계층
@Repository     // 데이터 접근 계층 (예외 변환 기능 추가)
@Controller     // 웹 요청 처리 계층
@RestController // @Controller + @ResponseBody

// 기능적 차이는 거의 없지만, 역할을 명확히 표현
// @Repository만 DB 예외를 Spring 예외로 변환하는 추가 기능
```

### Bean 스코프

```java
@Component
@Scope("singleton")  // 기본값: 컨테이너에 1개만 존재
public class SingletonBean { }

@Component
@Scope("prototype")  // 요청할 때마다 새 객체 생성
public class PrototypeBean { }

// 웹 스코프
@Scope("request")   // HTTP 요청마다 1개
@Scope("session")   // HTTP 세션마다 1개

// 실무: 99% singleton 사용
```

### Bean 생명주기

```java
@Component
public class MyBean {

    @PostConstruct  // Bean 생성 + 의존성 주입 후 호출
    public void init() {
        System.out.println("초기화 작업");
    }

    @PreDestroy  // Bean 소멸 전 호출
    public void destroy() {
        System.out.println("정리 작업");
    }
}

// 생명주기: 생성 → 의존성 주입 → @PostConstruct → 사용 → @PreDestroy → 소멸
```

---

## AOP (관점 지향 프로그래밍)

### 개념

```
횡단 관심사(Cross-Cutting Concern)를 분리하여 모듈화.

횡단 관심사란?
  여러 계층에 걸쳐 반복되는 공통 로직:
  ├── 로깅
  ├── 트랜잭션 관리
  ├── 보안 검사
  ├── 성능 측정
  └── 예외 처리

비즈니스 로직과 공통 로직을 분리
→ 코드 중복 제거, 유지보수성 향상
```

### AOP 용어

```
Aspect:     횡단 관심사를 모듈화한 클래스 (@Aspect)
Advice:     언제 실행할지 (Before, After, Around)
JoinPoint:  Advice를 적용할 수 있는 지점 (메서드 실행)
Pointcut:   어디에 적용할지 (대상 메서드 지정)
```

### AOP 실전 예시

```java
// 실행 시간 측정 AOP
@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("execution(* com.example.service..*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long end = System.currentTimeMillis();
        System.out.println(joinPoint.getSignature() + " 실행 시간: " + (end - start) + "ms");

        return result;
    }
}

// 로깅 AOP
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("execution(* com.example.controller..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("호출: {} | 인자: {}",
            joinPoint.getSignature().getName(),
            Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "execution(* com.example.service..*(..))",
                    returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        log.info("완료: {} | 결과: {}",
            joinPoint.getSignature().getName(), result);
    }
}
```

### @Transactional도 AOP

```java
// Spring의 @Transactional은 AOP로 구현됨
// 프록시 객체가 메서드 전후에 트랜잭션 시작/커밋/롤백 처리

@Transactional  // AOP 프록시가 이 메서드를 감싸서 트랜잭션 관리
public void transfer(Long from, Long to, int amount) {
    // 비즈니스 로직만 작성
    accountRepository.withdraw(from, amount);
    accountRepository.deposit(to, amount);
    // 예외 발생 시 → AOP가 자동 롤백
}
```

---

## @Configuration과 수동 Bean 등록

```java
// 외부 라이브러리나 복잡한 초기화가 필요한 Bean

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

---

## 면접 예상 질문

1. **IoC와 DI란?**
   - IoC: 객체 생성/관리 권한이 Spring으로 역전 / DI: 외부에서 의존 객체를 주입

2. **생성자 주입을 권장하는 이유는?**
   - 불변성(final), 테스트 용이, 순환 참조 방지, 필수 의존성 보장

3. **Bean 스코프 종류는?**
   - singleton(기본), prototype, request, session

4. **AOP란? 어디에 사용하나요?**
   - 횡단 관심사 분리. 로깅, 트랜잭션, 보안, 성능 측정

5. **@Component와 @Bean의 차이는?**
   - @Component: 클래스에 붙여 자동 등록 / @Bean: 메서드에 붙여 수동 등록
