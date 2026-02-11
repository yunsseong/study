# 2. 구조 패턴 (Structural Patterns)

---

## 구조 패턴이란?

**구조 패턴**: 클래스와 객체를 **더 큰 구조로 조합**하는 방법을 다루는 패턴.
기존 코드를 변경하지 않고 새로운 기능을 추가하거나, 호환되지 않는 인터페이스를 연결한다.

```
구조 패턴의 핵심:

  [클라이언트] → [???] → [실제 객체]
                  ↑
        여기에 들어가는 것이 구조 패턴
        - Proxy: 대리 객체 (접근 제어, 부가 기능)
        - Adapter: 변환기 (인터페이스 호환)
        - Decorator: 장식자 (기능 추가)
        - Facade: 창구 (복잡한 시스템 단순화)
```

---

## Proxy 패턴

### 개념

**Proxy**: 실제 객체에 대한 **대리 객체(대변인)**를 제공하여, 접근 제어나 부가 기능을 추가하는 패턴.
클라이언트는 Proxy를 실제 객체처럼 사용하며, Proxy인지 모른다.

```
Proxy 패턴:

  [클라이언트]
       │
       ↓
  [Proxy 객체]  ──── 같은 인터페이스 구현
       │
       ├── 접근 제어 (권한 체크)
       ├── 부가 기능 (로깅, 캐싱)
       ├── 실제 메서드 호출 위임
       │
       ↓
  [실제 객체 (Target)]
```

### Proxy의 종류

| 종류 | 목적 | 예시 |
|------|------|------|
| **보호 프록시** | 접근 제어 | Spring Security, @PreAuthorize |
| **원격 프록시** | 원격 객체 접근 | RMI, gRPC stub |
| **가상 프록시** | 지연 로딩 | JPA Lazy Loading |
| **로깅 프록시** | 부가 기능 추가 | Spring AOP |

### Spring AOP가 Proxy 패턴

```
Spring AOP 동작 원리:

  [Controller]
       │
       ↓
  [OrderService$$Proxy]  ← Spring이 자동 생성한 Proxy
       │
       ├── @Transactional → 트랜잭션 시작
       ├── @Cacheable → 캐시 확인
       ├── 로깅, 보안 체크 등
       │
       ↓
  [실제 OrderService]  ← 핵심 비즈니스 로직만 존재
       │
       ↓
  [OrderService$$Proxy]
       │
       ├── 트랜잭션 커밋/롤백
       └── 결과 반환
```

### JDK Dynamic Proxy vs CGLIB

```java
// 1. JDK Dynamic Proxy: 인터페이스 기반
public interface OrderService {
    void createOrder(OrderRequest request);
}

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public void createOrder(OrderRequest request) { ... }
}
```

```
JDK Dynamic Proxy:

  [OrderService 인터페이스]
       ↑                  ↑
  [OrderServiceImpl]   [JDK Proxy]
  (실제 구현체)         (인터페이스 기반 프록시)

  → 인터페이스가 반드시 있어야 함
  → 인터페이스 타입으로만 주입 가능
```

```java
// 2. CGLIB: 클래스 상속 기반
@Service
public class OrderService {  // 인터페이스 없음
    public void createOrder(OrderRequest request) { ... }
}
```

```
CGLIB Proxy:

  [OrderService]
       ↑
  [OrderService$$EnhancerByCGLIB]
  (클래스를 상속한 프록시)

  → 인터페이스 불필요
  → final 클래스/메서드는 프록시 불가 (상속이 안 되므로)
  → Spring Boot 2.0부터 기본값
```

| 항목 | JDK Dynamic Proxy | CGLIB |
|------|-------------------|-------|
| **조건** | 인터페이스 구현 필요 | 인터페이스 불필요 |
| **원리** | 인터페이스 기반 | 클래스 상속 기반 |
| **제약** | 인터페이스 타입으로만 주입 | final 클래스/메서드 불가 |
| **Spring Boot 기본** | - | **CGLIB (기본값)** |

### @Transactional이 Proxy로 동작하는 원리

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        paymentService.pay(order);
    }
}
```

```
실제 동작:

  [Controller]
       │
       ↓
  [OrderService Proxy]        ← Spring이 생성
       │
       ├── TransactionManager.getTransaction()  ← 트랜잭션 시작
       ├── 실제 OrderService.createOrder() 호출
       │     ├── save() 실행
       │     └── pay() 실행
       ├── 정상 → commit()
       └── 예외 → rollback()
```

```java
// Proxy의 내부 동작 (개념적 코드)
public class OrderServiceProxy extends OrderService {

    @Override
    public void createOrder(OrderRequest request) {
        TransactionStatus tx = transactionManager.getTransaction();
        try {
            super.createOrder(request);   // 실제 메서드 호출
            transactionManager.commit(tx);
        } catch (RuntimeException e) {
            transactionManager.rollback(tx);
            throw e;
        }
    }
}
```

### 프록시 주의사항: 내부 호출 문제

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        orderRepository.save(order);
        this.sendNotification(order);   // 내부 호출 → Proxy를 거치지 않음!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Order order) {
        // 이 @Transactional은 동작하지 않는다!
    }
}
```

```
외부 호출 (AOP 적용됨):
  [Controller] → [Proxy] → [실제 OrderService]
                   ↑ Proxy를 거침

내부 호출 (AOP 미적용):
  [실제 OrderService.createOrder()]
       │
       └── this.sendNotification()
            ↑ this = 실제 객체 (Proxy가 아님!)
```

```
해결 방법:
  1. 별도 클래스로 분리 (권장)
     → NotificationService로 분리하면 Proxy를 거침
  2. Self-injection
     → 자기 자신의 Proxy를 주입받아 호출
```

> **면접 포인트**: "Spring AOP는 Proxy 패턴으로 동작하며, @Transactional, @Cacheable, @Async 등이
> 모두 Proxy를 통해 부가 기능을 수행합니다. 같은 클래스 내부에서 this로 호출하면
> Proxy를 거치지 않으므로 AOP가 적용되지 않는 점을 주의해야 합니다."

---

## Adapter 패턴

### 개념

**Adapter**: 호환되지 않는 인터페이스를 가진 클래스들을 **함께 동작할 수 있도록 변환**하는 패턴.
기존 코드를 수정하지 않고 새로운 인터페이스에 맞게 적응(adapt)시킨다.

```
Adapter 패턴:

  [클라이언트] → [Target 인터페이스]
                       ↑
                   [Adapter]  ← 인터페이스를 변환
                       │
                       ↓
                   [Adaptee]  ← 기존 클래스 (호환 안 되는 인터페이스)

  비유: 유럽 콘센트(Adaptee)에 한국 플러그(Client)를 꽂기 위한 변환 어댑터
```

### Java 예시

```java
// 기존 시스템: XML 형식으로 데이터 제공
public class LegacyXmlParser {
    public String parseXml(String xml) {
        // XML 파싱 로직
        return "<data>result</data>";
    }
}

// 새 시스템: JSON 인터페이스를 기대
public interface JsonParser {
    String parseJson(String json);
}

// Adapter: XML 파서를 JSON 인터페이스에 맞게 변환
public class XmlToJsonAdapter implements JsonParser {
    private final LegacyXmlParser xmlParser;  // 기존 시스템 포함

    public XmlToJsonAdapter(LegacyXmlParser xmlParser) {
        this.xmlParser = xmlParser;
    }

    @Override
    public String parseJson(String json) {
        // JSON → XML 변환 후 기존 파서 사용
        String xml = convertJsonToXml(json);
        String result = xmlParser.parseXml(xml);
        return convertXmlToJson(result);  // 결과를 다시 JSON으로 변환
    }
}
```

### Spring MVC의 HandlerAdapter

```
Spring MVC에서 DispatcherServlet이 다양한 형태의 Handler를 처리하는 방법:

  [DispatcherServlet]
       │
       ↓
  [HandlerAdapter]  ← Adapter 패턴!
       │
       ├── RequestMappingHandlerAdapter → @Controller 메서드 처리
       ├── HttpRequestHandlerAdapter    → HttpRequestHandler 처리
       └── SimpleControllerHandlerAdapter → Controller 인터페이스 처리

  → DispatcherServlet은 어떤 Handler든 HandlerAdapter를 통해 일관되게 호출
  → 새로운 Handler 타입이 추가되어도 새 Adapter만 만들면 됨
```

```java
// HandlerAdapter 인터페이스 (간략화)
public interface HandlerAdapter {
    boolean supports(Object handler);           // 이 핸들러를 처리할 수 있는지?
    ModelAndView handle(HttpServletRequest request,
                       HttpServletResponse response,
                       Object handler);         // 핸들러 실행
}
```

```
요청 처리 흐름:

  HTTP 요청 → DispatcherServlet
                    │
                    ├── HandlerMapping: 어떤 Handler가 처리할지 결정
                    │
                    ├── HandlerAdapter: 해당 Handler를 실행할 Adapter 선택
                    │     └── adapter.supports(handler) == true인 것
                    │
                    ├── adapter.handle(request, response, handler)
                    │     └── 실제 Controller 메서드 호출
                    │
                    └── ViewResolver: 결과 반환
```

> **면접 포인트**: "Spring MVC의 HandlerAdapter는 Adapter 패턴의 대표적인 예시입니다.
> DispatcherServlet은 다양한 형태의 Handler(Controller)를 직접 알 필요 없이,
> HandlerAdapter를 통해 일관된 방식으로 처리할 수 있습니다."

---

## Decorator 패턴

### 개념

**Decorator**: 객체에 **동적으로 새로운 기능을 추가**하는 패턴.
상속 없이 기능을 확장할 수 있으며, 여러 Decorator를 조합하여 다양한 기능 조합을 만든다.

```
Decorator 패턴:

  [Component 인터페이스]
       ↑            ↑
  [ConcreteComponent]  [Decorator]
                          ↑
                     ┌────┴────┐
              [DecoratorA] [DecoratorB]

  → Decorator는 Component를 감싸서(wrap) 기능을 추가
  → 여러 Decorator를 겹겹이 감쌀 수 있음 (러시아 인형처럼)
```

### Java I/O Stream이 Decorator 패턴

```java
// Java I/O는 대표적인 Decorator 패턴!
InputStream is = new FileInputStream("data.txt");         // 기본 스트림
InputStream buffered = new BufferedInputStream(is);       // + 버퍼링 기능
InputStream data = new DataInputStream(buffered);         // + 데이터 타입 읽기

// 한 줄로 작성
DataInputStream dis = new DataInputStream(
    new BufferedInputStream(
        new FileInputStream("data.txt")
    )
);
```

```
Java I/O Decorator 구조:

  [InputStream]  ← Component (추상 클래스)
       ↑
  ┌────┴──────────────┐
  │                   │
  [FileInputStream]   [FilterInputStream]  ← Decorator (추상)
  (ConcreteComponent)       ↑
                       ┌────┴────────────┐
                [BufferedInputStream] [DataInputStream]
                (ConcreteDecorator)   (ConcreteDecorator)

감싸는 순서:
  FileInputStream → BufferedInputStream으로 감쌈 → DataInputStream으로 감쌈
  [파일 읽기]     → [+ 버퍼링]                 → [+ 데이터 타입 변환]
```

### 직접 구현 예시

```java
// Component
public interface Coffee {
    String getDescription();
    int getCost();
}

// ConcreteComponent
public class BasicCoffee implements Coffee {
    @Override
    public String getDescription() { return "기본 커피"; }
    @Override
    public int getCost() { return 3000; }
}

// Decorator (추상)
public abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee;  // 감싸는 대상

    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}

// ConcreteDecorator
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }

    @Override
    public String getDescription() { return coffee.getDescription() + " + 우유"; }
    @Override
    public int getCost() { return coffee.getCost() + 500; }
}

public class ShotDecorator extends CoffeeDecorator {
    public ShotDecorator(Coffee coffee) { super(coffee); }

    @Override
    public String getDescription() { return coffee.getDescription() + " + 샷추가"; }
    @Override
    public int getCost() { return coffee.getCost() + 300; }
}

// 사용: 동적으로 기능 조합
Coffee coffee = new BasicCoffee();                    // 기본 커피: 3000원
coffee = new MilkDecorator(coffee);                   // + 우유: 3500원
coffee = new ShotDecorator(coffee);                   // + 샷: 3800원
System.out.println(coffee.getDescription());          // "기본 커피 + 우유 + 샷추가"
System.out.println(coffee.getCost());                 // 3800
```

### Proxy vs Decorator 차이

| 항목 | Proxy | Decorator |
|------|-------|-----------|
| **목적** | 접근 제어, 부가 기능 위임 | 기능 추가, 확장 |
| **대상 객체** | 클라이언트가 모름 (투명) | 클라이언트가 조합을 결정 |
| **관계** | Proxy가 대상 객체를 내부에서 생성/관리 | 클라이언트가 Decorator 조합 |
| **개수** | 보통 1개 | 여러 개 중첩 가능 |
| **Spring 예시** | AOP Proxy, @Transactional | Java I/O Stream |
| **핵심 차이** | **제어** (접근을 통제) | **기능 추가** (새 행동 부여) |

```
Proxy:
  [클라이언트] → [Proxy (몰래 끼어듦)] → [실제 객체]
                  ↑ 클라이언트는 Proxy인지 모름

Decorator:
  [클라이언트] → [Decorator A → Decorator B → 실제 객체]
                  ↑ 클라이언트가 직접 조합을 결정
```

---

## Facade 패턴

### 개념

**Facade**: 복잡한 서브시스템에 대한 **간단한 통합 인터페이스**를 제공하는 패턴.
클라이언트가 복잡한 내부 구조를 알 필요 없이, Facade를 통해 간편하게 사용한다.

```
Facade 패턴:

  [클라이언트]
       │
       ↓
  [Facade] ← 단순한 인터페이스 제공
       │
       ├── [서브시스템 A]
       ├── [서브시스템 B]
       └── [서브시스템 C]

  비유: 호텔 프론트 데스크
        → 고객은 프론트에만 요청
        → 프론트가 청소부, 벨보이, 셰프 등을 조율
```

### Service 계층이 Facade 역할

```
Spring 계층 구조에서 Service가 Facade:

  [Controller]
       │
       ↓
  [OrderService]  ← Facade (복잡한 내부를 숨김)
       │
       ├── [OrderRepository]     주문 저장
       ├── [PaymentService]      결제 처리
       ├── [InventoryService]    재고 차감
       ├── [NotificationService] 알림 발송
       └── [PointService]        포인트 적립

  Controller는 OrderService의 createOrder()만 호출
  → 내부의 5가지 서브시스템의 복잡한 조합을 몰라도 됨
```

```java
@Service
@RequiredArgsConstructor
public class OrderService {   // Facade 역할

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final PointService pointService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. 재고 확인 및 차감
        inventoryService.decreaseStock(request.getProductId(), request.getQuantity());

        // 2. 주문 생성
        Order order = Order.create(request);
        orderRepository.save(order);

        // 3. 결제 처리
        paymentService.pay(order);

        // 4. 포인트 적립
        pointService.addPoints(request.getUserId(), order.getTotalPrice());

        // 5. 알림 발송
        notificationService.sendOrderConfirmation(order);

        return OrderResponse.from(order);
    }
}

// Controller는 단순하게 호출만
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
}
```

### Facade 패턴의 장점

```
Facade 적용 전:
  [Controller]
       ├── inventoryService.check()
       ├── orderRepository.save()
       ├── paymentService.pay()
       ├── pointService.add()
       └── notificationService.send()
  → Controller가 비즈니스 로직을 알아야 함
  → 순서 변경 시 Controller 수정 필요

Facade 적용 후:
  [Controller]
       └── orderService.createOrder()
  → Controller는 "주문 생성"만 요청
  → 내부 로직 변경 시 Service만 수정
  → 테스트도 OrderService만 집중 테스트
```

> **면접 포인트**: "Spring의 Service 계층은 Facade 패턴 역할을 합니다.
> Controller에게 단순한 인터페이스를 제공하고, 내부적으로 여러 Repository와
> 다른 Service들의 복잡한 비즈니스 로직을 조합합니다.
> 이를 통해 Controller는 얇게(thin) 유지하고, 비즈니스 로직은 Service에 집중합니다."

---

## 면접 핵심 정리

**Q: Proxy 패턴이란 무엇이고, Spring에서 어떻게 활용되나요?**
> Proxy 패턴은 실제 객체 대신 대리 객체를 두어 접근 제어나 부가 기능을 추가하는 패턴입니다.
> Spring AOP가 대표적으로, @Transactional, @Cacheable, @Async 등이 모두 Proxy를 통해 동작합니다.
> Spring Boot는 기본적으로 CGLIB를 사용하여 대상 클래스를 상속한 Proxy를 생성합니다.
> 주의할 점은 같은 클래스 내부에서 this로 호출하면 Proxy를 거치지 않아 AOP가 적용되지 않는다는 것입니다.

**Q: Adapter 패턴을 설명하고, Spring에서의 예시를 들어주세요.**
> Adapter 패턴은 호환되지 않는 인터페이스를 가진 클래스들이 함께 동작할 수 있도록 변환하는 패턴입니다.
> Spring MVC의 HandlerAdapter가 대표적인 예시입니다. DispatcherServlet은 다양한 형태의 Handler를
> 직접 호출하는 대신, HandlerAdapter를 통해 일관된 방식으로 처리합니다.
> 새로운 Handler 타입이 추가되어도 해당 Adapter만 구현하면 되므로 확장에 유리합니다.

**Q: Proxy 패턴과 Decorator 패턴의 차이는?**
> 두 패턴 모두 대상 객체를 감싸는(wrapping) 구조이지만 목적이 다릅니다.
> Proxy는 접근 제어와 부가 기능 위임이 목적이며, 클라이언트는 Proxy인지 모릅니다.
> Decorator는 기능 추가가 목적이며, 클라이언트가 직접 조합을 결정합니다.
> Spring AOP의 @Transactional은 Proxy 패턴, Java I/O Stream은 Decorator 패턴의 예시입니다.

**Q: Facade 패턴이란 무엇이고, Spring에서 어떤 역할을 하나요?**
> Facade 패턴은 복잡한 서브시스템에 대해 간단한 통합 인터페이스를 제공하는 패턴입니다.
> Spring에서 Service 계층이 Facade 역할을 합니다. Controller에게는 단순한 메서드를 제공하고,
> 내부적으로 여러 Repository와 Service들의 복잡한 비즈니스 로직을 조합합니다.
> 이를 통해 관심사를 분리하고, Controller는 요청 처리에만 집중할 수 있습니다.

**Q: Spring Boot에서 CGLIB이 기본 Proxy 방식인 이유는?**
> Spring Boot 2.0부터 CGLIB이 기본값입니다. JDK Dynamic Proxy는 인터페이스가 반드시 있어야 하고,
> 인터페이스 타입으로만 주입할 수 있다는 제약이 있습니다. CGLIB은 인터페이스 없이도
> 클래스를 상속하여 Proxy를 생성하므로 더 유연합니다. 다만 final 클래스/메서드에는
> 적용할 수 없다는 제약이 있습니다. 대부분의 경우 CGLIB이 더 편리하므로 기본값으로 채택되었습니다.
