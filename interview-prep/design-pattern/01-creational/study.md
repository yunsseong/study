# 1. 생성 패턴 (Creational Patterns)

---

## 디자인 패턴이란?

**디자인 패턴**: 소프트웨어 설계에서 반복적으로 발생하는 문제에 대한 **재사용 가능한 해결 방법**.
GoF(Gang of Four)가 23가지 패턴을 **생성, 구조, 행위** 3가지로 분류했다.

```
GoF 디자인 패턴 분류:

생성 패턴 (Creational)     → 객체를 "어떻게 생성"할 것인가?
├── Singleton, Factory Method, Abstract Factory, Builder, Prototype

구조 패턴 (Structural)     → 객체를 "어떻게 조합"할 것인가?
├── Proxy, Adapter, Decorator, Facade, Composite, Bridge, Flyweight

행위 패턴 (Behavioral)     → 객체 간 "어떻게 소통"할 것인가?
├── Strategy, Observer, Template Method, Iterator, Command, State, ...
```

> **면접 포인트**: "디자인 패턴은 특정 기술이 아니라 설계 원칙입니다.
> Spring은 내부적으로 수많은 디자인 패턴을 활용하여 유연한 구조를 제공합니다."

---

## Singleton 패턴

### 개념

**Singleton**: 클래스의 인스턴스가 **오직 하나만** 존재하도록 보장하고, 전역 접근점을 제공하는 패턴.

```
Singleton 패턴:

  [Singleton 인스턴스 - 딱 1개]
       ↑        ↑        ↑
    Client A  Client B  Client C
    (모두 같은 인스턴스를 사용)
```

### Java 구현 방법

#### 1. Eager Initialization (즉시 초기화)

```java
public class Singleton {
    // 클래스 로딩 시 바로 생성 (thread-safe)
    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {}  // private 생성자 → 외부 생성 차단

    public static Singleton getInstance() {
        return INSTANCE;
    }
}
```

```
장점: 구현이 간단, thread-safe (클래스 로딩은 JVM이 보장)
단점: 사용하지 않아도 인스턴스가 생성됨 (메모리 낭비 가능)
```

#### 2. Lazy Initialization (지연 초기화)

```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {        // 처음 호출 시에만 생성
            instance = new Singleton();
        }
        return instance;
    }
}
```

```
장점: 필요할 때만 생성
단점: 멀티스레드 환경에서 안전하지 않음!

Thread A: if (instance == null) → true → 생성 시작...
Thread B: if (instance == null) → true → 또 생성!  ← 인스턴스 2개!
```

#### 3. Double-Checked Locking (DCL)

```java
public class Singleton {
    // volatile: 메모리 가시성 보장 (CPU 캐시가 아닌 메인 메모리에서 읽기)
    private static volatile Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {               // 1차 검사 (성능 최적화)
            synchronized (Singleton.class) {  // 락 획득
                if (instance == null) {       // 2차 검사 (동시성 보장)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

```
동작 흐름:

Thread A, B 동시에 getInstance() 호출
  │
  ├── 1차 검사: instance == null? → true
  │     ├── Thread A: synchronized 진입 → 2차 검사 → null → 생성
  │     └── Thread B: synchronized 대기...
  │                    → 2차 검사 → not null → 기존 인스턴스 반환
  │
  └── 이후 호출: 1차 검사에서 바로 반환 (synchronized 안 거침 → 빠름)
```

> **volatile이 필요한 이유**: `instance = new Singleton()`은 내부적으로
> (1) 메모리 할당 (2) 생성자 호출 (3) 참조 할당의 3단계인데, JVM이 (1)→(3)→(2)로
> 재배치(reordering)할 수 있다. volatile은 이 재배치를 방지한다.

#### 4. Enum Singleton (권장)

```java
public enum Singleton {
    INSTANCE;

    public void doSomething() {
        // 비즈니스 로직
    }
}

// 사용
Singleton.INSTANCE.doSomething();
```

```
장점:
  - Thread-safe (JVM이 보장)
  - 리플렉션으로도 새 인스턴스 생성 불가
  - 직렬화/역직렬화 시에도 Singleton 보장
  - 가장 간결한 구현

단점:
  - 상속 불가 (enum은 다른 클래스 상속 불가)
  - 지연 초기화 불가
```

### Singleton 구현 방식 비교

| 방식 | Thread-Safe | Lazy Loading | 리플렉션 안전 | 복잡도 |
|------|------------|-------------|-------------|--------|
| Eager | O | X | X | 낮음 |
| Lazy | X | O | X | 낮음 |
| DCL | O | O | X | 중간 |
| Enum | O | X | **O** | 낮음 |

### Spring Bean이 Singleton인 이유

```
Spring Container:

  [UserService Bean - 1개]
       ↑        ↑        ↑
    요청1      요청2      요청3
    (같은 인스턴스 공유)

이유:
  1. 성능: 매 요청마다 new → 생성/GC 비용 → Singleton으로 재사용
  2. 메모리: 수백 개 Bean을 매번 생성하면 메모리 낭비
  3. 상태 관리: Service, Repository는 보통 stateless → 공유 가능
```

```java
// Spring의 Singleton은 GoF Singleton과 다르다!
// GoF: private 생성자 + static getInstance() → 클래스 자체가 Singleton 보장
// Spring: IoC 컨테이너가 Bean을 1개만 관리 → 컨테이너 레벨의 Singleton

@Service
public class UserService {
    // private 생성자가 아님! Spring 컨테이너가 관리하는 것
    // → 직접 new UserService()하면 새 인스턴스 생성 가능
    // → Spring을 통해서만 가져오면 항상 같은 인스턴스
}
```

### 멀티스레드 주의점

```java
@Service
public class UserService {
    // 절대 하면 안 되는 것! (Singleton Bean에 상태 저장)
    private User currentUser;       // 여러 스레드가 동시 접근 → 동시성 문제
    private int requestCount = 0;   // 레이스 컨디션 발생

    // 올바른 방식
    public void process(User user) {
        int localCount = 0;  // 지역 변수 → Stack에 저장 → 스레드별 독립
    }
}
```

```
위험한 상황:

Thread A: currentUser = userA → 처리 시작...
Thread B: currentUser = userB → 덮어씀!
Thread A: currentUser 사용 → userB 데이터로 처리! (버그)
```

---

## Factory Method 패턴

### 개념

**Factory Method**: 객체 생성을 서브클래스에 위임하는 패턴.
생성할 객체의 **구체적인 타입을 결정하는 것을 서브클래스가 담당**한다.

```
Factory Method 패턴:

  [Creator] ─── factoryMethod() ───→ [Product 인터페이스]
      │                                     ↑
      ↓                                     │
  [ConcreteCreatorA] → createProduct() → [ConcreteProductA]
  [ConcreteCreatorB] → createProduct() → [ConcreteProductB]
```

### Java 예시

```java
// Product 인터페이스
public interface Notification {
    void send(String message);
}

// 구체 Product
public class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("이메일 발송: " + message);
    }
}

public class SmsNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("SMS 발송: " + message);
    }
}

// Factory Method
public class NotificationFactory {
    public static Notification createNotification(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailNotification();
            case "SMS"   -> new SmsNotification();
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}

// 사용
Notification noti = NotificationFactory.createNotification("EMAIL");
noti.send("가입을 환영합니다!");
```

### Spring의 BeanFactory

```
Spring의 BeanFactory가 대표적인 Factory 패턴:

  [BeanFactory]
      │
      ├── getBean("userService") → [UserService 인스턴스]
      ├── getBean("orderService") → [OrderService 인스턴스]
      └── getBean(UserRepository.class) → [UserRepository 인스턴스]

  클라이언트는 어떤 구현체가 반환되는지 몰라도 됨
  → 인터페이스 타입으로 받으면 구현체 교체가 자유로움
```

```java
// Spring의 Factory 패턴 활용 예시
@Configuration
public class DataSourceConfig {

    @Bean
    @Profile("dev")
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }

    @Bean
    @Profile("prod")
    public DataSource mysqlDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://...");
        return ds;
    }
}

// 사용하는 쪽은 DataSource 인터페이스만 알면 됨
@Repository
public class UserRepository {
    private final DataSource dataSource;  // dev면 H2, prod면 MySQL
}
```

> **면접 포인트**: "Spring의 BeanFactory는 Bean 이름이나 타입을 기반으로
> 적절한 객체를 생성/반환하는 Factory 패턴을 사용합니다.
> 이를 통해 클라이언트 코드와 구체적인 구현체 사이의 결합도를 낮춥니다."

---

## Abstract Factory 패턴

### 개념

**Abstract Factory**: **관련된 객체들의 가족(family)**을 생성하는 인터페이스를 제공하는 패턴.
구체적인 클래스를 지정하지 않고 관련 객체 그룹을 함께 생성한다.

```
Abstract Factory 패턴:

  [AbstractFactory]
      │
      ├── createButton()
      └── createTextField()

  [WindowsFactory]              [MacFactory]
      ├── createButton()            ├── createButton()
      │   → WindowsButton           │   → MacButton
      └── createTextField()         └── createTextField()
          → WindowsTextField            → MacTextField

  → 관련된 객체들(Button, TextField)을 일관된 세트로 생성
```

### Factory Method vs Abstract Factory

| 항목 | Factory Method | Abstract Factory |
|------|---------------|-----------------|
| **목적** | 하나의 객체 생성을 서브클래스에 위임 | 관련 객체 **그룹**을 함께 생성 |
| **생성 객체** | 1종류의 Product | 여러 종류의 Product (가족) |
| **구조** | 메서드 1개 | 팩토리 인터페이스 + 여러 메서드 |
| **사용 시점** | 어떤 객체를 만들지 서브클래스가 결정 | 관련 객체를 일관되게 만들어야 할 때 |
| **Spring 예시** | BeanFactory.getBean() | FactoryBean 인터페이스 |

```java
// Abstract Factory 예시
public interface UIFactory {
    Button createButton();
    TextField createTextField();
}

public class DarkThemeFactory implements UIFactory {
    @Override
    public Button createButton() { return new DarkButton(); }
    @Override
    public TextField createTextField() { return new DarkTextField(); }
}

public class LightThemeFactory implements UIFactory {
    @Override
    public Button createButton() { return new LightButton(); }
    @Override
    public TextField createTextField() { return new LightTextField(); }
}

// 사용: 테마 변경 시 Factory만 교체하면 모든 UI가 일관되게 변경
UIFactory factory = new DarkThemeFactory();  // 또는 LightThemeFactory
Button button = factory.createButton();
TextField textField = factory.createTextField();
```

---

## Builder 패턴

### 개념

**Builder**: 복잡한 객체의 **생성 과정과 표현을 분리**하여, 단계별로 객체를 구성하는 패턴.
특히 생성자 매개변수가 많거나, 선택적 매개변수가 있을 때 유용하다.

### 문제: 점층적 생성자 패턴

```java
// 매개변수가 많으면 가독성이 매우 떨어진다
User user = new User("홍길동", "hong@email.com", 25, "서울", "010-1234-5678", null, null);
//                    이름     이메일            나이  주소    전화번호       ...
// → 7번째 null이 뭔지 알 수 없음
```

### Builder 패턴 직접 구현

```java
public class User {
    private final String name;       // 필수
    private final String email;      // 필수
    private final int age;           // 선택
    private final String address;    // 선택
    private final String phone;      // 선택

    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.address = builder.address;
        this.phone = builder.phone;
    }

    public static class Builder {
        // 필수 매개변수
        private final String name;
        private final String email;

        // 선택 매개변수 (기본값)
        private int age = 0;
        private String address = "";
        private String phone = "";

        public Builder(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public Builder age(int age) { this.age = age; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }

        public User build() {
            return new User(this);
        }
    }
}

// 사용: 가독성이 매우 좋음
User user = new User.Builder("홍길동", "hong@email.com")
        .age(25)
        .address("서울")
        .phone("010-1234-5678")
        .build();
```

### Lombok @Builder

```java
// Lombok으로 한 줄이면 Builder 패턴 완성
@Builder
public class UserCreateRequest {
    private String name;
    private String email;
    private int age;
    private String address;
}

// 사용
UserCreateRequest request = UserCreateRequest.builder()
        .name("홍길동")
        .email("hong@email.com")
        .age(25)
        .build();
```

### 실무 활용: DTO 생성

```java
// 1. Response DTO 생성
@Builder
@Getter
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    // Entity → Response DTO 변환
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

// 2. 테스트 데이터 생성
@Test
void testCreateUser() {
    UserCreateRequest request = UserCreateRequest.builder()
            .name("테스트")
            .email("test@email.com")
            .age(20)
            .build();

    // 필요한 필드만 설정 → 가독성 높음
}
```

```
Builder 패턴이 유용한 상황:
  1. 매개변수 4개 이상 → 가독성 향상
  2. 선택적 매개변수 존재 → 불필요한 null 전달 제거
  3. 불변 객체(immutable) 생성 → private 생성자 + Builder
  4. 테스트 데이터 생성 → 필요한 것만 설정
```

> **면접 포인트**: "Builder 패턴은 복잡한 객체 생성의 가독성과 안전성을 높입니다.
> 실무에서는 Lombok의 @Builder를 사용하여 DTO, Response 객체, 테스트 데이터 생성에 활용합니다."

---

## 면접 핵심 정리

**Q: Singleton 패턴이란 무엇이고, Spring에서 어떻게 사용되나요?**
> Singleton 패턴은 클래스의 인스턴스가 오직 하나만 존재하도록 보장하는 패턴입니다.
> Spring Bean은 기본 Scope이 Singleton이라서 컨테이너에 Bean이 1개만 존재합니다.
> 다만 GoF의 Singleton은 클래스 자체에서 보장(private 생성자 + static)하고,
> Spring의 Singleton은 IoC 컨테이너가 관리하는 방식이라는 차이가 있습니다.
> Singleton Bean은 멀티스레드 환경에서 공유되므로 반드시 무상태(stateless)로 설계해야 합니다.

**Q: Factory Method 패턴을 설명하고, Spring에서의 예시를 들어주세요.**
> Factory Method는 객체 생성 로직을 별도 메서드로 분리하여, 어떤 구체 클래스를 생성할지를
> 서브클래스나 팩토리가 결정하는 패턴입니다. Spring의 BeanFactory가 대표적인 예로,
> getBean() 메서드를 통해 Bean 이름이나 타입에 따라 적절한 객체를 반환합니다.
> 이를 통해 클라이언트 코드는 구체적인 구현 클래스를 몰라도 인터페이스만으로 사용할 수 있습니다.

**Q: Builder 패턴은 언제 사용하나요?**
> 생성자 매개변수가 많거나(4개 이상), 선택적 매개변수가 있을 때 사용합니다.
> 점층적 생성자 패턴의 가독성 문제를 해결하고, 불변 객체를 안전하게 생성할 수 있습니다.
> 실무에서는 Lombok의 @Builder 어노테이션을 주로 사용하며,
> DTO 생성, Response 객체 변환, 테스트 데이터 생성 등에 널리 활용됩니다.

**Q: Singleton을 멀티스레드 환경에서 안전하게 구현하는 방법은?**
> 가장 권장되는 방법은 Enum Singleton입니다. JVM이 thread-safety를 보장하고,
> 리플렉션이나 직렬화에도 안전합니다. 그 외에는 Double-Checked Locking이 있는데,
> volatile 키워드와 synchronized를 함께 사용하여 최초 생성 시에만 동기화하고,
> 이후에는 성능 저하 없이 인스턴스를 반환합니다.

**Q: Factory Method와 Abstract Factory의 차이는?**
> Factory Method는 하나의 객체 생성을 서브클래스에 위임하는 패턴이고,
> Abstract Factory는 관련된 객체들의 그룹(가족)을 일관되게 생성하는 패턴입니다.
> 예를 들어, Factory Method는 "알림 객체 1개를 생성"하는 것이고,
> Abstract Factory는 "다크 테마의 버튼, 텍스트필드, 체크박스를 한 세트로 생성"하는 것입니다.
