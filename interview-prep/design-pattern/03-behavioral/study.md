# 3. 행위 패턴 (Behavioral Patterns)

---

## 행위 패턴이란?

**행위 패턴**: 객체들 간의 **상호작용과 책임 분배** 방법을 다루는 패턴.
알고리즘, 이벤트 전달, 반복 등 객체들이 **어떻게 소통하는지**를 정의한다.

```
행위 패턴의 핵심:

  [객체 A] ──── 어떻게 소통? ────→ [객체 B]
                    ↑
          여기를 설계하는 것이 행위 패턴
          - Strategy: 알고리즘을 교체 가능하게
          - Observer: 이벤트를 구독/발행하게
          - Template Method: 골격은 고정, 세부 단계만 변경
          - Iterator: 컬렉션을 순회하는 통일된 방법
```

---

## Strategy 패턴

### 개념

**Strategy**: 알고리즘(전략)을 캡슐화하여, **런타임에 교체 가능**하게 만드는 패턴.
동일한 문제를 해결하는 여러 방법 중 하나를 선택하여 사용할 수 있다.

```
Strategy 패턴:

  [Context]  ←── 전략을 사용하는 주체
       │
       └── [Strategy 인터페이스]
                ↑
           ┌────┴─────────┐
      [StrategyA]    [StrategyB]
      (알고리즘 A)   (알고리즘 B)

  → Context는 Strategy 인터페이스에만 의존
  → 구체적인 알고리즘은 런타임에 교체 가능
```

### Java Comparator가 Strategy 패턴

```java
List<User> users = List.of(
    new User("홍길동", 30),
    new User("김철수", 25),
    new User("이영희", 35)
);

// Strategy 1: 이름순 정렬
users.sort(Comparator.comparing(User::getName));

// Strategy 2: 나이순 정렬
users.sort(Comparator.comparing(User::getAge));

// Strategy 3: 나이 역순 정렬
users.sort(Comparator.comparing(User::getAge).reversed());
```

```
Comparator = Strategy 인터페이스
Comparator.comparing(User::getName) = ConcreteStrategy A (이름순)
Comparator.comparing(User::getAge)  = ConcreteStrategy B (나이순)

List.sort() = Context (전략을 받아서 정렬 수행)
  → 정렬 알고리즘(전략)을 외부에서 주입받아 사용
  → 새로운 정렬 기준은 Comparator만 추가하면 됨
```

### 직접 구현 예시

```java
// Strategy 인터페이스
public interface PaymentStrategy {
    void pay(int amount);
}

// ConcreteStrategy A
public class CreditCardPayment implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("신용카드 결제: " + amount + "원");
    }
}

// ConcreteStrategy B
public class KakaoPayPayment implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("카카오페이 결제: " + amount + "원");
    }
}

// Context
public class PaymentService {
    private PaymentStrategy strategy;

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void processPayment(int amount) {
        strategy.pay(amount);
    }
}

// 사용: 런타임에 전략 교체
PaymentService service = new PaymentService();
service.setStrategy(new CreditCardPayment());  // 신용카드 결제
service.processPayment(10000);

service.setStrategy(new KakaoPayPayment());    // 카카오페이 결제
service.processPayment(5000);
```

### Spring에서의 Strategy 패턴

```java
// Spring DI를 활용한 Strategy 패턴
public interface DiscountPolicy {
    int discount(int price);
}

@Component("fixedDiscount")
public class FixedDiscountPolicy implements DiscountPolicy {
    @Override
    public int discount(int price) {
        return price - 1000;  // 1000원 할인
    }
}

@Component("rateDiscount")
public class RateDiscountPolicy implements DiscountPolicy {
    @Override
    public int discount(int price) {
        return (int) (price * 0.9);  // 10% 할인
    }
}

// 사용: @Qualifier로 전략 선택
@Service
public class OrderService {
    private final DiscountPolicy discountPolicy;

    public OrderService(@Qualifier("rateDiscount") DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }
}
```

```
Spring의 다양한 Strategy 사용 사례:

  1. PlatformTransactionManager
     → DataSourceTransactionManager (JDBC)
     → JpaTransactionManager (JPA)
     → 트랜잭션 관리 전략을 교체 가능

  2. HandlerMethodArgumentResolver
     → @RequestBody, @PathVariable 등 파라미터 해석 전략

  3. ViewResolver
     → InternalResourceViewResolver (JSP)
     → ThymeleafViewResolver (Thymeleaf)
     → 뷰 렌더링 전략을 교체 가능

  4. RestTemplate / WebClient
     → HttpMessageConverter
     → JSON, XML 등 메시지 변환 전략
```

> **면접 포인트**: "Strategy 패턴은 알고리즘을 인터페이스로 추상화하여 런타임에 교체 가능하게 합니다.
> Spring은 DI를 통해 Strategy 패턴을 자연스럽게 구현합니다.
> 인터페이스에 의존하고 구현체를 주입받으므로, 구현체 교체가 자유롭습니다."

---

## Observer 패턴

### 개념

**Observer**: 한 객체의 상태가 변경되면, 그 객체에 **의존하는 모든 객체에 자동으로 알림**을 보내는 패턴.
발행-구독(Publish-Subscribe) 모델이라고도 한다.

```
Observer 패턴:

  [Subject (발행자)]
       │
       ├── notify() → [Observer A (구독자)]
       ├── notify() → [Observer B (구독자)]
       └── notify() → [Observer C (구독자)]

  → Subject의 상태가 변경되면 등록된 모든 Observer에게 알림
  → Observer는 Subject를 직접 polling하지 않음 (push 방식)
```

### 직접 구현 예시

```java
// Observer 인터페이스
public interface OrderEventListener {
    void onOrderCreated(Order order);
}

// ConcreteObserver A
public class EmailNotifier implements OrderEventListener {
    @Override
    public void onOrderCreated(Order order) {
        System.out.println("주문 확인 이메일 발송: " + order.getId());
    }
}

// ConcreteObserver B
public class InventoryManager implements OrderEventListener {
    @Override
    public void onOrderCreated(Order order) {
        System.out.println("재고 차감: " + order.getProductId());
    }
}

// Subject
public class OrderService {
    private final List<OrderEventListener> listeners = new ArrayList<>();

    public void addListener(OrderEventListener listener) {
        listeners.add(listener);
    }

    public void createOrder(Order order) {
        // 주문 생성 로직
        orderRepository.save(order);

        // 모든 Observer에게 알림
        for (OrderEventListener listener : listeners) {
            listener.onOrderCreated(order);
        }
    }
}
```

### Spring ApplicationEvent / EventListener

```java
// 1. 이벤트 정의
public class OrderCreatedEvent {
    private final Long orderId;
    private final Long userId;
    private final int totalPrice;

    public OrderCreatedEvent(Long orderId, Long userId, int totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPrice = totalPrice;
    }

    // Getter 생략
}

// 2. 이벤트 발행 (Publisher)
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createOrder(OrderRequest request) {
        Order order = Order.create(request);
        orderRepository.save(order);

        // 이벤트 발행 → 구독자들이 자동으로 처리
        eventPublisher.publishEvent(
            new OrderCreatedEvent(order.getId(), request.getUserId(), order.getTotalPrice())
        );
    }
}

// 3. 이벤트 구독 (Listener/Observer)
@Component
public class OrderEventHandler {

    @EventListener
    public void sendConfirmationEmail(OrderCreatedEvent event) {
        // 주문 확인 이메일 발송
        System.out.println("이메일 발송: 주문 " + event.getOrderId());
    }

    @EventListener
    public void addPoints(OrderCreatedEvent event) {
        // 포인트 적립
        System.out.println("포인트 적립: " + event.getTotalPrice() * 0.01);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPushNotification(OrderCreatedEvent event) {
        // 트랜잭션 커밋 후 푸시 알림 발송
        System.out.println("푸시 알림 발송: 주문 " + event.getOrderId());
    }
}
```

```
Spring Event 동작 흐름:

  [OrderService]
       │
       ├── orderRepository.save(order)
       │
       └── eventPublisher.publishEvent(OrderCreatedEvent)
                │
                ↓
           [Spring EventMulticaster]
                │
                ├── @EventListener sendConfirmationEmail()  → 이메일 발송
                ├── @EventListener addPoints()              → 포인트 적립
                └── @TransactionalEventListener
                     sendPushNotification()                 → 푸시 알림
```

### @EventListener vs @TransactionalEventListener

| 항목 | @EventListener | @TransactionalEventListener |
|------|---------------|---------------------------|
| **실행 시점** | 이벤트 발행 즉시 | 트랜잭션 커밋/롤백 후 |
| **트랜잭션** | 발행자의 트랜잭션 내 실행 | 별도 트랜잭션 |
| **실패 시** | 발행자 트랜잭션에 영향 | 발행자에 영향 없음 |
| **용도** | 동일 트랜잭션 내 처리 | 외부 알림, 비동기 처리 |

```
@EventListener:
  [트랜잭션 시작]
     ├── save()
     ├── publishEvent() → listener 실행 (같은 트랜잭션)
     │                     └── 여기서 예외 → 전체 롤백!
     └── [커밋]

@TransactionalEventListener(AFTER_COMMIT):
  [트랜잭션 시작]
     ├── save()
     ├── publishEvent() → 이벤트 등록만 (실행은 나중에)
     └── [커밋] → 커밋 후 listener 실행
                    └── 여기서 예외 → 주문 트랜잭션에 영향 없음
```

### Observer 패턴의 장점 (느슨한 결합)

```
Observer 적용 전 (강한 결합):
  [OrderService]
       ├── emailService.send()          ← 직접 의존
       ├── pointService.addPoints()     ← 직접 의존
       ├── pushService.sendPush()       ← 직접 의존
       └── inventoryService.decrease()  ← 직접 의존
  → 새 기능 추가 시 OrderService 수정 필요

Observer 적용 후 (느슨한 결합):
  [OrderService]
       └── eventPublisher.publishEvent()  ← 이벤트만 발행

  [Handler A] @EventListener → 이메일 발송
  [Handler B] @EventListener → 포인트 적립
  [Handler C] @EventListener → 푸시 알림     ← 새로 추가해도 OrderService 수정 불필요!
```

> **면접 포인트**: "Spring의 ApplicationEvent와 @EventListener는 Observer 패턴의 구현입니다.
> 이벤트 기반으로 서비스 간 결합도를 낮추고, 새 기능 추가 시 기존 코드 수정 없이
> EventListener만 추가하면 됩니다. @TransactionalEventListener로 트랜잭션과 연계할 수도 있습니다."

---

## Template Method 패턴

### 개념

**Template Method**: 알고리즘의 **골격(구조)을 상위 클래스에서 정의**하고,
세부 단계를 서브클래스에서 구현하게 하는 패턴.
"전체 흐름은 고정, 세부 구현만 변경"이 핵심이다.

```
Template Method 패턴:

  [AbstractClass]
       │
       └── templateMethod()  ← 전체 흐름 정의 (final)
            │
            ├── step1()      ← 공통 로직 (구현 완료)
            ├── step2()      ← 추상 메서드 (서브클래스가 구현)
            └── step3()      ← 추상 메서드 (서브클래스가 구현)

  [ConcreteClassA]             [ConcreteClassB]
       ├── step2() 구현 A          ├── step2() 구현 B
       └── step3() 구현 A          └── step3() 구현 B
```

### 직접 구현 예시

```java
// Template (추상 클래스)
public abstract class DataProcessor {

    // Template Method: 전체 흐름 정의 (final로 변경 방지)
    public final void process() {
        readData();       // 1. 데이터 읽기 (서브클래스가 구현)
        processData();    // 2. 데이터 가공 (서브클래스가 구현)
        saveData();       // 3. 데이터 저장 (공통)
    }

    protected abstract void readData();      // 서브클래스가 구현
    protected abstract void processData();   // 서브클래스가 구현

    private void saveData() {                // 공통 로직
        System.out.println("DB에 저장 완료");
    }
}

// ConcreteClass A: CSV 처리
public class CsvProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("CSV 파일 읽기");
    }

    @Override
    protected void processData() {
        System.out.println("CSV 데이터 파싱");
    }
}

// ConcreteClass B: JSON 처리
public class JsonProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("JSON 파일 읽기");
    }

    @Override
    protected void processData() {
        System.out.println("JSON 데이터 파싱");
    }
}

// 사용
DataProcessor processor = new CsvProcessor();
processor.process();
// 출력: CSV 파일 읽기 → CSV 데이터 파싱 → DB에 저장 완료
```

### Spring의 Template Method: JdbcTemplate

```java
// JdbcTemplate이 Template Method 패턴
// "DB 연결 → SQL 실행 → 결과 매핑 → 자원 정리" 흐름은 JdbcTemplate이 담당
// 개발자는 "SQL"과 "결과 매핑" 부분만 구현

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public User findById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",    // SQL (개발자가 작성)
            (rs, rowNum) -> new User(               // RowMapper (개발자가 작성)
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email")
            ),
            id
        );
    }
}
```

```
JdbcTemplate 내부 동작 (Template Method):

  jdbcTemplate.queryForObject()
       │
       ├── ① Connection 획득        ← JdbcTemplate이 처리 (고정)
       ├── ② PreparedStatement 생성  ← JdbcTemplate이 처리 (고정)
       ├── ③ SQL 실행               ← 개발자가 전달한 SQL
       ├── ④ ResultSet 매핑         ← 개발자가 전달한 RowMapper
       ├── ⑤ 자원 정리 (close)       ← JdbcTemplate이 처리 (고정)
       └── ⑥ 예외 변환              ← JdbcTemplate이 처리 (고정)

  → 개발자는 반복적인 DB 코드(연결, 정리, 예외) 없이
    SQL과 매핑 로직만 작성하면 됨
```

### Spring의 다른 Template들

```
Spring이 제공하는 Template Method 패턴 클래스들:

  JdbcTemplate        → JDBC 코드의 반복 제거
  RestTemplate        → HTTP 통신 코드의 반복 제거
  TransactionTemplate → 트랜잭션 관리 코드의 반복 제거
  RedisTemplate       → Redis 코드의 반복 제거
  JmsTemplate         → JMS 메시지 코드의 반복 제거

  공통점:
    - "연결 → 실행 → 정리 → 예외 처리"의 골격을 제공
    - 개발자는 핵심 로직(SQL, URL, 메시지)만 전달
    - 반복적인 boilerplate 코드를 제거
```

```java
// RestTemplate 예시
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final RestTemplate restTemplate;

    public UserDto getUser(Long id) {
        // RestTemplate이 HTTP 연결/요청/응답/에러처리를 모두 담당
        // 개발자는 URL과 응답 타입만 지정
        return restTemplate.getForObject(
            "https://api.example.com/users/{id}",
            UserDto.class,
            id
        );
    }
}
```

> **면접 포인트**: "Template Method 패턴은 알고리즘의 골격을 정의하고 세부 구현을 위임하는 패턴입니다.
> Spring의 JdbcTemplate은 DB 연결, 자원 정리, 예외 처리 등의 반복 코드를 처리하고,
> 개발자는 SQL과 결과 매핑만 작성하면 됩니다. 이를 통해 boilerplate 코드를 제거합니다."

---

## Iterator 패턴

### 개념

**Iterator**: 컬렉션의 내부 구조를 노출하지 않고, **요소를 순차적으로 접근**할 수 있는
통일된 방법을 제공하는 패턴.

```
Iterator 패턴:

  [Aggregate (컬렉션)]
       │
       └── createIterator() → [Iterator]
                                  │
                                  ├── hasNext()  → 다음 요소 존재 여부
                                  └── next()     → 다음 요소 반환

  → 컬렉션이 배열이든, 연결 리스트든, 트리든
    동일한 인터페이스(hasNext, next)로 순회 가능
```

### Java Collection의 Iterator

```java
// Java의 모든 Collection은 Iterator를 제공
List<String> list = List.of("A", "B", "C");
Set<String> set = Set.of("X", "Y", "Z");
Map<String, Integer> map = Map.of("a", 1, "b", 2);

// Iterator 직접 사용
Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String element = iterator.next();
    System.out.println(element);
}

// Enhanced for-loop (내부적으로 Iterator 사용)
for (String element : list) {
    System.out.println(element);
}

// Stream도 Iterator 기반
list.stream()
    .filter(s -> s.length() > 1)
    .forEach(System.out::println);
```

```
Java Collection과 Iterator 관계:

  [Iterable 인터페이스]  ← iterator() 메서드 정의
       ↑
  [Collection 인터페이스]
       ↑
  ┌────┴─────────┐
  [List]        [Set]
  ↑               ↑
  ArrayList     HashSet
  LinkedList    TreeSet

  각 구현체는 자신만의 Iterator를 제공:
  - ArrayList  → ArrayList$Itr (인덱스 기반 순회)
  - LinkedList → LinkedList$ListItr (노드 링크 기반 순회)
  - HashSet    → HashMap$KeyIterator (버킷 기반 순회)
  - TreeSet    → TreeMap$KeyIterator (트리 순회)

  → 사용하는 쪽은 Iterator 인터페이스만 알면 됨
  → 내부 구조(배열, 링크, 해시, 트리)를 몰라도 동일하게 순회
```

### for-each 문의 동작 원리

```java
// 이 코드는
for (String s : list) {
    System.out.println(s);
}

// 컴파일러에 의해 이렇게 변환된다
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    System.out.println(s);
}
```

```
for-each가 동작하려면:
  1. Iterable 인터페이스를 구현해야 함
  2. iterator() 메서드가 Iterator를 반환해야 함
  3. Iterator는 hasNext()와 next()를 구현해야 함

  → 배열은 Iterable이 아니지만, 컴파일러가 인덱스 기반 for문으로 변환
```

### 커스텀 Iterator 구현

```java
// 역순 Iterator 구현 예시
public class ReverseList<T> implements Iterable<T> {
    private final List<T> list;

    public ReverseList(List<T> list) {
        this.list = list;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int index = list.size() - 1;

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public T next() {
                return list.get(index--);
            }
        };
    }
}

// 사용
ReverseList<String> reverse = new ReverseList<>(List.of("A", "B", "C"));
for (String s : reverse) {
    System.out.println(s);  // C, B, A
}
```

> **면접 포인트**: "Iterator 패턴은 컬렉션의 내부 구조를 노출하지 않고 통일된 순회 방법을 제공합니다.
> Java의 모든 Collection은 Iterable 인터페이스를 구현하여 Iterator를 제공하며,
> for-each 문이 내부적으로 Iterator를 사용합니다.
> ArrayList, LinkedList, HashSet 등 내부 구조가 달라도 동일한 방식으로 순회할 수 있습니다."

---

## 패턴들의 Spring 활용 요약

```
┌─────────────────────┬──────────────────────────────────────────┐
│ 디자인 패턴          │ Spring 활용 예시                          │
├─────────────────────┼──────────────────────────────────────────┤
│ Strategy            │ DI (인터페이스 + 구현체 교체)               │
│                     │ PlatformTransactionManager               │
│                     │ ViewResolver, HttpMessageConverter       │
├─────────────────────┼──────────────────────────────────────────┤
│ Observer            │ ApplicationEvent + @EventListener         │
│                     │ @TransactionalEventListener              │
├─────────────────────┼──────────────────────────────────────────┤
│ Template Method     │ JdbcTemplate, RestTemplate               │
│                     │ TransactionTemplate, RedisTemplate       │
├─────────────────────┼──────────────────────────────────────────┤
│ Iterator            │ Java Collection의 Iterator               │
│                     │ for-each 문, Stream API                  │
├─────────────────────┼──────────────────────────────────────────┤
│ Singleton           │ Spring Bean 기본 Scope                    │
├─────────────────────┼──────────────────────────────────────────┤
│ Factory Method      │ BeanFactory, FactoryBean                 │
├─────────────────────┼──────────────────────────────────────────┤
│ Proxy               │ Spring AOP, @Transactional, @Cacheable   │
├─────────────────────┼──────────────────────────────────────────┤
│ Adapter             │ HandlerAdapter (Spring MVC)              │
├─────────────────────┼──────────────────────────────────────────┤
│ Facade              │ Service 계층                              │
└─────────────────────┴──────────────────────────────────────────┘
```

---

## 면접 핵심 정리

**Q: Strategy 패턴이란 무엇이고, Spring에서 어떻게 활용되나요?**
> Strategy 패턴은 알고리즘을 인터페이스로 캡슐화하여 런타임에 교체 가능하게 하는 패턴입니다.
> Spring의 DI 자체가 Strategy 패턴의 구현으로 볼 수 있습니다. 인터페이스에 의존하고
> 구현체를 주입받으므로, 설정만으로 전략(구현체)을 교체할 수 있습니다.
> PlatformTransactionManager, ViewResolver 등이 대표적인 예시입니다.

**Q: Observer 패턴이란 무엇이고, Spring Event와의 관계를 설명해주세요.**
> Observer 패턴은 한 객체의 상태 변경 시 의존하는 모든 객체에 자동으로 알림을 보내는 패턴입니다.
> Spring의 ApplicationEventPublisher로 이벤트를 발행하고, @EventListener로 구독합니다.
> 이를 통해 서비스 간 결합도를 낮추고, 새 기능 추가 시 EventListener만 추가하면 됩니다.
> @TransactionalEventListener를 사용하면 트랜잭션 커밋 후 이벤트를 처리할 수도 있습니다.

**Q: Template Method 패턴을 설명하고, Spring에서의 예시를 들어주세요.**
> Template Method 패턴은 알고리즘의 골격을 상위 클래스에서 정의하고,
> 세부 단계를 서브클래스에서 구현하게 하는 패턴입니다.
> Spring의 JdbcTemplate이 대표적인 예시로, DB 연결/자원 정리/예외 처리의 골격을 제공하고,
> 개발자는 SQL과 결과 매핑(RowMapper)만 작성하면 됩니다.
> 이를 통해 반복적인 boilerplate 코드를 제거할 수 있습니다.

**Q: Iterator 패턴을 설명하고, Java에서 어떻게 구현되어 있나요?**
> Iterator 패턴은 컬렉션의 내부 구조를 노출하지 않고 요소를 순차적으로 접근하는
> 통일된 방법을 제공하는 패턴입니다. Java의 모든 Collection은 Iterable 인터페이스를 구현하여
> iterator() 메서드로 Iterator를 제공합니다. for-each 문(`for (T item : collection)`)은
> 컴파일러에 의해 Iterator의 hasNext()와 next() 호출로 변환됩니다.
> ArrayList, LinkedList, HashSet 등 내부 구조가 달라도 동일한 방식으로 순회할 수 있습니다.

**Q: Strategy 패턴과 Template Method 패턴의 차이는?**
> Strategy 패턴은 **위임(delegation)**을 통해 알고리즘을 교체합니다.
> 인터페이스를 구현한 별도 객체를 주입받아 사용하므로 런타임에 자유롭게 교체 가능합니다.
> Template Method 패턴은 **상속(inheritance)**을 통해 알고리즘의 일부를 변경합니다.
> 상위 클래스가 골격을 정의하고, 서브클래스가 세부 단계를 구현합니다.
> Strategy는 "has-a(조합)" 관계, Template Method는 "is-a(상속)" 관계입니다.
