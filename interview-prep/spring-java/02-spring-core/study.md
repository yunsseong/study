# 2. Spring Core (IoC, DI, AOP)

---

## Spring이란?

**Spring**: Java 기반의 엔터프라이즈 애플리케이션 개발 프레임워크.
핵심은 **IoC/DI 컨테이너**와 **AOP** 지원이다.

### Spring vs Spring Boot

| 항목 | Spring | Spring Boot |
|------|--------|-------------|
| 설정 | XML 또는 Java Config 직접 작성 | 자동 설정 (Auto Configuration) |
| 서버 | 외부 Tomcat에 war 배포 | 내장 Tomcat, `java -jar`로 실행 |
| 의존성 | 개별 라이브러리 버전 관리 | Starter로 묶어서 관리 |
| 설정 파일 | web.xml, applicationContext.xml | application.yml / properties |
| 목적 | 유연하고 세밀한 제어 | 빠른 개발, 최소 설정 |

```
Spring 설정 (과거):
  web.xml + applicationContext.xml + servlet-context.xml + ...
  + 외부 Tomcat 설치 + war 배포
  → 설정만 수백 줄

Spring Boot (현재):
  @SpringBootApplication + application.yml
  + java -jar app.jar
  → 바로 실행
```

> **면접 포인트**: "Spring Boot는 Spring을 쉽게 사용하기 위한 도구이지, 별개의 프레임워크가 아닙니다.
> 내부적으로 Spring의 IoC, DI, AOP가 그대로 동작합니다."

---

## IoC (Inversion of Control) - 제어의 역전

### 기존 방식: 개발자가 직접 제어

```java
// 개발자가 직접 객체를 생성하고 관리
public class OrderService {
    private OrderRepository orderRepository = new OrderRepository();  // 직접 생성
    private PaymentService paymentService = new PaymentService();      // 직접 생성
}
```

```
제어 흐름: 개발자 → 객체 생성 → 의존성 연결 → 생명주기 관리
```

### IoC 방식: 프레임워크가 제어

```java
// Spring이 객체를 생성하고 주입
@Service
public class OrderService {
    private final OrderRepository orderRepository;  // Spring이 주입
    private final PaymentService paymentService;     // Spring이 주입

    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}
```

```
제어 흐름: Spring 컨테이너 → 객체 생성 → 의존성 주입 → 생명주기 관리
          개발자는 "무엇을 사용할지"만 선언, "어떻게 만들지"는 Spring이 담당
```

**핵심**: 객체의 생성과 관리의 **제어권**이 개발자 → 프레임워크(Spring)로 **역전**됨.

```
기존:
  [개발자] ──생성──→ [OrderRepository]
  [개발자] ──생성──→ [PaymentService]
  [개발자] ──연결──→ [OrderService]

IoC:
  [Spring Container]
       │
       ├── 생성 → [OrderRepository Bean]
       ├── 생성 → [PaymentService Bean]
       └── 생성 + 주입 → [OrderService Bean]
                          (위 두 Bean을 자동 주입)
```

---

## DI (Dependency Injection) - 의존성 주입

IoC를 구현하는 방법 중 하나. 객체가 필요로 하는 의존성을 **외부에서 주입**받는 것.

### 1. 생성자 주입 (Constructor Injection) - 권장

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    // 생성자가 1개면 @Autowired 생략 가능
    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}
```

### 2. 세터 주입 (Setter Injection)

```java
@Service
public class OrderService {
    private OrderRepository orderRepository;

    @Autowired
    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

### 3. 필드 주입 (Field Injection) - 비권장

```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;  // 필드에 직접 주입
}
```

### 3가지 방식 비교

| 항목 | 생성자 주입 | 세터 주입 | 필드 주입 |
|------|-----------|----------|----------|
| **불변성** | final 가능 (불변) | 변경 가능 | 변경 가능 |
| **필수 의존성** | 보장 (없으면 컴파일 에러) | 보장 안 됨 (null 가능) | 보장 안 됨 |
| **테스트** | new로 직접 주입 가능 | setter로 주입 | 리플렉션 필요 (어려움) |
| **순환 참조** | 컴파일 시점 감지 | 런타임 에러 | 런타임 에러 |
| **권장 여부** | **권장** | 선택적 의존성에 사용 | **비권장** |

### 생성자 주입을 권장하는 이유

```java
// 1. 불변 보장 (final)
@Service
public class OrderService {
    private final OrderRepository orderRepository;  // 한 번 주입 후 변경 불가

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// 2. 테스트 용이
@Test
void testOrder() {
    // Mock 객체를 직접 생성자로 주입 - 간단!
    OrderRepository mockRepo = mock(OrderRepository.class);
    OrderService service = new OrderService(mockRepo);
}

// 3. 순환 참조 방지
// A → B → A 순환 참조가 있으면 Spring 시작 시 바로 에러 발생
// 필드 주입은 런타임에야 발견 → 더 위험
```

```
필드 주입의 문제:
  @Autowired 없이는 객체를 만들 수 없음
  → 순수 Java 테스트가 어려움
  → Spring 컨테이너에 의존적

생성자 주입:
  new OrderService(mockRepo) 로 바로 테스트 가능
  → POJO 테스트 가능
  → Spring 없이도 동작
```

---

## Bean 생명주기

### Spring Bean이란?

**Spring IoC 컨테이너가 관리하는 객체**를 Bean이라 한다.

```java
@Component  // → Spring이 이 클래스를 Bean으로 등록
public class UserService { ... }

@Service    // @Component의 특수화
@Repository // @Component의 특수화
@Controller // @Component의 특수화
```

### Bean 생명주기 흐름

```
Spring 컨테이너 시작
        │
        ↓
  ① Bean 인스턴스 생성 (new)
        │
        ↓
  ② 의존성 주입 (DI)
        │
        ↓
  ③ 초기화 콜백
     - @PostConstruct
     - InitializingBean.afterPropertiesSet()
     - @Bean(initMethod = "init")
        │
        ↓
  ④ 사용 (비즈니스 로직 실행)
        │
        ↓
  ⑤ 소멸 콜백
     - @PreDestroy
     - DisposableBean.destroy()
     - @Bean(destroyMethod = "close")
        │
        ↓
  Spring 컨테이너 종료
```

```java
@Component
public class DatabaseConnector {

    private Connection connection;

    // 생성자 주입
    public DatabaseConnector(DataSource dataSource) {
        // ① 인스턴스 생성 + ② 의존성 주입
    }

    @PostConstruct  // ③ 초기화 콜백
    public void init() {
        this.connection = dataSource.getConnection();
        System.out.println("DB 연결 완료");
    }

    // ④ 비즈니스 로직에서 사용
    public void query(String sql) { ... }

    @PreDestroy  // ⑤ 소멸 콜백
    public void cleanup() {
        connection.close();
        System.out.println("DB 연결 해제");
    }
}
```

---

## Bean Scope

### Scope별 비교

| Scope | 설명 | 생성 시점 | 소멸 시점 |
|-------|------|----------|----------|
| **singleton** (기본) | 컨테이너에 1개만 존재 | 컨테이너 시작 시 | 컨테이너 종료 시 |
| **prototype** | 요청할 때마다 새로 생성 | 요청 시 | Spring이 관리 안 함 |
| **request** | HTTP 요청마다 1개 | 요청 시작 | 요청 종료 |
| **session** | HTTP 세션마다 1개 | 세션 시작 | 세션 종료 |

### Singleton (기본값)

```java
@Service  // 기본이 Singleton
public class UserService { ... }
```

```
Spring 컨테이너

[UserService Bean - 딱 1개]
     ↑        ↑        ↑
  요청1      요청2      요청3
  (같은 객체를 공유)
```

> **주의**: Singleton Bean은 여러 스레드가 공유하므로 **상태(state)를 가지면 안 된다**.

```java
@Service
public class UserService {
    // 절대 하면 안 되는 것!
    private int requestCount = 0;  // 여러 스레드가 동시에 접근 → 동시성 문제

    // 올바른 방식: 상태가 필요하면 지역 변수 또는 ThreadLocal 사용
    public void process() {
        int localCount = 0;  // 지역 변수는 Stack에 있으므로 스레드별 독립
    }
}
```

### Prototype

```java
@Component
@Scope("prototype")
public class PrototypeBean { ... }
```

```
Spring 컨테이너

요청1 → [PrototypeBean 1] (새로 생성)
요청2 → [PrototypeBean 2] (새로 생성)
요청3 → [PrototypeBean 3] (새로 생성)

주의: @PreDestroy가 호출되지 않음!
      Spring이 생성만 하고 관리하지 않음
```

---

## AOP (Aspect Oriented Programming)

### AOP란?

**관점 지향 프로그래밍**: 핵심 비즈니스 로직과 **공통 관심 사항(횡단 관심사)**을 분리하는 프로그래밍 기법.

```
AOP 없이: 각 메서드에 공통 코드 반복

┌──────────────────────────────────┐
│ OrderService.createOrder()       │
│   ├── 로깅 시작                   │  ← 반복
│   ├── 트랜잭션 시작               │  ← 반복
│   ├── 주문 생성 로직 (핵심)        │
│   ├── 트랜잭션 커밋               │  ← 반복
│   └── 로깅 종료                   │  ← 반복
└──────────────────────────────────┘
┌──────────────────────────────────┐
│ PaymentService.pay()             │
│   ├── 로깅 시작                   │  ← 반복
│   ├── 트랜잭션 시작               │  ← 반복
│   ├── 결제 처리 로직 (핵심)        │
│   ├── 트랜잭션 커밋               │  ← 반복
│   └── 로깅 종료                   │  ← 반복
└──────────────────────────────────┘
```

```
AOP 적용: 공통 관심사를 분리

      [로깅 Aspect]     [트랜잭션 Aspect]
           │                    │
           ↓                    ↓
  ┌────────────────────────────────────┐
  │ OrderService.createOrder()         │
  │   └── 주문 생성 로직만! (핵심)      │
  └────────────────────────────────────┘
  ┌────────────────────────────────────┐
  │ PaymentService.pay()               │
  │   └── 결제 처리 로직만! (핵심)      │
  └────────────────────────────────────┘
```

### AOP 핵심 용어

| 용어 | 설명 | 예시 |
|------|------|------|
| **Aspect** | 공통 관심사를 모듈화한 것 | 로깅, 트랜잭션, 보안 |
| **Advice** | 언제 무엇을 실행할지 | @Before, @After, @Around |
| **Pointcut** | 어디에 적용할지 (대상 지정) | `execution(* com.example.service.*.*(..))` |
| **JoinPoint** | Advice가 적용될 수 있는 지점 | 메서드 실행 시점 |
| **Target** | Aspect가 적용되는 대상 객체 | OrderService |
| **Proxy** | AOP가 적용된 대리 객체 | OrderService$$Proxy |

### AOP 동작 예시

```java
// Aspect 정의
@Aspect
@Component
public class LoggingAspect {

    // Pointcut: service 패키지의 모든 메서드에 적용
    @Around("execution(* com.example.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long end = System.currentTimeMillis();
        log.info("{} 실행 시간: {}ms",
            joinPoint.getSignature().getName(), (end - start));

        return result;
    }
}
```

```
호출 흐름:

클라이언트 → [Proxy 객체] → [LoggingAspect.logExecutionTime]
                                    │
                                    ├── 시작 시간 기록
                                    ├── joinPoint.proceed() → [실제 OrderService.createOrder()]
                                    ├── 종료 시간 기록
                                    └── 로그 출력

클라이언트는 Proxy인지 모름 (투명하게 동작)
```

### Advice 종류

```java
@Before("pointcut")    // 메서드 실행 전
@After("pointcut")     // 메서드 실행 후 (성공/실패 무관)
@AfterReturning        // 메서드 정상 반환 후
@AfterThrowing         // 메서드 예외 발생 후
@Around("pointcut")    // 메서드 실행 전후 모두 (가장 강력)
```

```
메서드 실행 흐름과 Advice:

@Before
   │
   ↓
[메서드 실행]
   │
   ├── 정상 → @AfterReturning
   └── 예외 → @AfterThrowing
   │
   ↓
@After (항상 실행)

@Around는 이 모든 것을 감싸서 제어 가능
```

---

## @Transactional이 AOP로 동작하는 원리

### 개념

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        paymentService.pay(order);
        // 예외 발생 시 → 자동 롤백
    }
}
```

실제로 Spring은 이 코드를 다음과 같이 처리한다:

```java
// Spring이 내부적으로 생성하는 프록시 (개념적 코드)
public class OrderService$$Proxy extends OrderService {

    @Override
    public void createOrder(OrderRequest request) {
        TransactionStatus tx = transactionManager.getTransaction();
        try {
            super.createOrder(request);  // 실제 메서드 호출
            transactionManager.commit(tx);
        } catch (RuntimeException e) {
            transactionManager.rollback(tx);
            throw e;
        }
    }
}
```

```
@Transactional 동작 흐름:

[Controller] → [OrderService Proxy] → [실제 OrderService]
                      │
                      ├── 트랜잭션 시작
                      ├── super.createOrder() 호출
                      │     ├── save() 실행
                      │     └── pay() 실행
                      ├── 성공 → commit
                      └── 예외 → rollback
```

### @Transactional 주의사항

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        this.sendNotification(order);  // 같은 클래스 내부 호출
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Order order) {
        // 이 @Transactional은 동작하지 않는다!
    }
}
```

```
왜 동작하지 않을까?

외부 호출: [Controller] → [Proxy] → [실제 객체]
                          ↑ Proxy를 거치므로 AOP 적용됨

내부 호출: [실제 객체의 createOrder()] → this.sendNotification()
           ↑ this는 Proxy가 아니라 실제 객체이므로 AOP 미적용!
```

**해결 방법**:

```java
// 1. 별도 클래스로 분리 (권장)
@Service
public class OrderService {
    private final NotificationService notificationService;  // 별도 Bean

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        notificationService.sendNotification(order);  // 외부 Bean 호출 → Proxy 거침
    }
}

// 2. self-injection (차선)
@Service
public class OrderService {
    @Lazy
    @Autowired
    private OrderService self;  // 자기 자신의 Proxy를 주입

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        self.sendNotification(order);  // Proxy를 통한 호출 → AOP 적용
    }
}
```

---

## Spring 프록시 패턴

Spring AOP는 **프록시 객체**를 생성하여 동작한다. 2가지 방식이 있다.

### JDK Dynamic Proxy

```
조건: 대상 클래스가 인터페이스를 구현한 경우

[인터페이스]
    │
    ├── [실제 구현체]
    └── [JDK Dynamic Proxy] ← 인터페이스 기반으로 프록시 생성
```

```java
// 인터페이스
public interface OrderService {
    void createOrder(OrderRequest request);
}

// 구현체
@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public void createOrder(OrderRequest request) { ... }
}

// Spring이 생성하는 Proxy는 OrderService 인터페이스를 구현
// → OrderService 타입으로만 주입 가능
// → OrderServiceImpl 타입으로 주입하면 에러!
```

### CGLIB (Code Generation Library)

```
조건: 인터페이스가 없는 구체 클래스

[구체 클래스]
    │
    └── [CGLIB Proxy] ← 클래스를 상속받아 프록시 생성 (서브클래스)
```

```java
// 인터페이스 없이 바로 클래스
@Service
public class OrderService {
    public void createOrder(OrderRequest request) { ... }
}

// Spring이 생성하는 Proxy는 OrderService를 상속
// → OrderService$$EnhancerBySpringCGLIB$$xxx
// → final 클래스/메서드는 프록시 불가 (상속 불가능하므로)
```

### JDK Dynamic Proxy vs CGLIB 비교

| 항목 | JDK Dynamic Proxy | CGLIB |
|------|-------------------|-------|
| **조건** | 인터페이스 구현 필요 | 인터페이스 불필요 |
| **원리** | 인터페이스 기반 프록시 | 클래스 상속 기반 프록시 |
| **제약** | 인터페이스 타입으로만 주입 | final 클래스/메서드 불가 |
| **성능** | 리플렉션 사용 (약간 느림) | 바이트코드 생성 (약간 빠름) |
| **Spring Boot 기본** | - | **CGLIB** (기본값) |

```
Spring Boot 기본 설정:

spring.aop.proxy-target-class=true  (기본값)
→ CGLIB 사용 (인터페이스가 있어도 CGLIB)

spring.aop.proxy-target-class=false
→ JDK Dynamic Proxy 사용 (인터페이스가 있을 때)
```

> **Spring Boot 2.0부터 CGLIB가 기본값**이다. 인터페이스 유무와 관계없이 CGLIB를 사용하므로,
> 구체 클래스 타입으로 주입해도 문제없다.

---

## 실무 연결 / Spring Boot 연결

### Bean 등록 방법

```java
// 1. 컴포넌트 스캔 (가장 일반적)
@Component / @Service / @Repository / @Controller
public class UserService { ... }

// 2. Java Config (외부 라이브러리 Bean 등록 시)
@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}

// 3. 조건부 Bean 등록
@Configuration
public class CacheConfig {
    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
```

### AOP 실무 활용

```java
// 1. API 실행 시간 측정
@Aspect
@Component
public class ApiTimerAspect {
    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch sw = new StopWatch();
        sw.start();
        Object result = joinPoint.proceed();
        sw.stop();
        log.info("[{}] {}ms", joinPoint.getSignature().toShortString(), sw.getTotalTimeMillis());
        return result;
    }
}

// 2. 커스텀 어노테이션 + AOP로 인증 체크
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {}

@Aspect
@Component
public class AuthAspect {
    @Before("@annotation(LoginRequired)")
    public void checkLogin(JoinPoint joinPoint) {
        // 현재 요청에서 인증 정보 확인
        // 미인증 시 UnauthorizedException throw
    }
}
```

---

## 면접 핵심 정리

**Q: IoC와 DI를 설명해주세요**
> IoC(제어의 역전)는 객체의 생성과 생명주기 관리를 개발자가 아닌 프레임워크(Spring 컨테이너)가 담당하는 것입니다.
> DI(의존성 주입)는 IoC를 구현하는 방법으로, 객체가 필요로 하는 의존성을 외부에서 주입받는 것입니다.
> 이를 통해 느슨한 결합을 달성하고, 테스트 용이성과 유지보수성이 향상됩니다.

**Q: 생성자 주입을 권장하는 이유는?**
> 첫째, final 키워드를 사용할 수 있어 불변성을 보장합니다.
> 둘째, 의존성이 누락되면 컴파일 시점에 에러가 발생하여 안전합니다.
> 셋째, 순환 참조가 있으면 애플리케이션 시작 시 바로 감지됩니다.
> 넷째, new 키워드로 직접 객체를 생성할 수 있어 순수 Java 테스트가 가능합니다.

**Q: Bean Scope의 종류와 Singleton에서 주의할 점은?**
> Bean Scope에는 Singleton(기본), Prototype, Request, Session 등이 있습니다.
> Singleton은 컨테이너에 하나만 존재하므로 여러 스레드가 공유합니다.
> 따라서 Bean에 상태(인스턴스 변수)를 두면 동시성 문제가 발생할 수 있어,
> Singleton Bean은 무상태(stateless)로 설계해야 합니다.

**Q: AOP가 무엇이고 Spring에서 어떻게 동작하나요?**
> AOP는 로깅, 트랜잭션 등 횡단 관심사를 핵심 로직에서 분리하는 프로그래밍 기법입니다.
> Spring AOP는 프록시 패턴으로 동작합니다.
> Spring Boot는 기본적으로 CGLIB를 사용하여 대상 클래스를 상속한 프록시 객체를 생성하고,
> 이 프록시가 Advice(부가 기능)를 실행한 뒤 실제 메서드를 호출합니다.

**Q: @Transactional의 동작 원리와 주의사항은?**
> @Transactional은 Spring AOP를 통해 동작합니다.
> Spring이 프록시 객체를 생성하여 메서드 호출을 가로채고,
> 트랜잭션 시작/커밋/롤백을 자동으로 처리합니다.
> 주의할 점은 같은 클래스 내부에서 this로 호출하면 프록시를 거치지 않으므로
> @Transactional이 적용되지 않습니다. 이를 해결하려면 별도 클래스로 분리해야 합니다.

**Q: JDK Dynamic Proxy와 CGLIB의 차이는?**
> JDK Dynamic Proxy는 인터페이스를 구현하여 프록시를 생성하므로 인터페이스가 필수입니다.
> CGLIB은 대상 클래스를 상속하여 프록시를 생성하므로 인터페이스가 불필요하지만,
> final 클래스나 메서드에는 적용할 수 없습니다.
> Spring Boot 2.0부터는 CGLIB이 기본값이며,
> 인터페이스 유무와 관계없이 CGLIB 프록시를 사용합니다.
