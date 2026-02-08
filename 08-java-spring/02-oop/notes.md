# OOP (객체지향 프로그래밍)

## 4대 특성

### 1. 캡슐화 (Encapsulation)

```java
// 데이터와 메서드를 하나로 묶고, 외부 접근 제한

public class Account {
    private int balance;  // 외부 직접 접근 불가

    public void deposit(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("금액은 양수");
        this.balance += amount;
    }

    public void withdraw(int amount) {
        if (amount > balance) throw new IllegalStateException("잔액 부족");
        this.balance -= amount;
    }

    public int getBalance() {
        return balance;
    }
}

// account.balance = -1000;  ❌ 컴파일 에러
// account.withdraw(1000);   ✅ 검증 로직을 거침
```

### 접근 제어자

```
public:    어디서든 접근
protected: 같은 패키지 + 자식 클래스
default:   같은 패키지 (아무것도 안 쓰면)
private:   같은 클래스만
```

### 2. 상속 (Inheritance)

```java
public class Animal {
    protected String name;

    public void eat() {
        System.out.println(name + " 먹는 중");
    }
}

public class Dog extends Animal {
    public void bark() {
        System.out.println(name + " 멍멍!");
    }
}

Dog dog = new Dog();
dog.name = "뽀삐";
dog.eat();   // Animal의 메서드 사용 가능
dog.bark();  // Dog 고유 메서드
```

### 3. 다형성 (Polymorphism)

```java
// 같은 타입, 다른 동작
public class Animal {
    public void sound() {
        System.out.println("...");
    }
}

public class Dog extends Animal {
    @Override
    public void sound() {
        System.out.println("멍멍");
    }
}

public class Cat extends Animal {
    @Override
    public void sound() {
        System.out.println("야옹");
    }
}

// 부모 타입으로 다양한 자식 객체 사용 (업캐스팅)
Animal animal1 = new Dog();
Animal animal2 = new Cat();
animal1.sound();  // "멍멍"
animal2.sound();  // "야옹"

// 실전: 인터페이스로 다형성 활용
List<Animal> animals = List.of(new Dog(), new Cat());
for (Animal a : animals) {
    a.sound();  // 각자의 구현이 호출됨
}
```

### 4. 추상화 (Abstraction)

```java
// 핵심만 노출하고 상세 구현은 숨김

// 추상 클래스: 일부 구현 + 일부 추상
public abstract class NotificationSender {
    // 공통 로직
    public void send(String to, String message) {
        validate(to);
        doSend(to, message);
        log(to, message);
    }

    // 자식이 구현해야 함
    protected abstract void doSend(String to, String message);

    private void validate(String to) { /* ... */ }
    private void log(String to, String msg) { /* ... */ }
}

// 인터페이스: 계약만 정의
public interface PaymentGateway {
    PaymentResult pay(int amount);
    void refund(String paymentId);
}
```

---

## 추상 클래스 vs 인터페이스

| 비교 | 추상 클래스 | 인터페이스 |
|------|-----------|-----------|
| 키워드 | abstract class | interface |
| 구현 | 일부 메서드 구현 가능 | Java 8+ default 메서드 가능 |
| 상속 | 단일 상속만 | 다중 구현 가능 |
| 변수 | 인스턴스 변수 가능 | 상수(public static final)만 |
| 목적 | "is-a" 관계, 공통 구현 공유 | "can-do" 관계, 계약 정의 |

```java
// 인터페이스: "할 수 있는 것" 정의
public interface Cacheable {
    String getCacheKey();
    int getTtl();
}

public interface Searchable {
    List<String> getSearchKeywords();
}

// 다중 구현
public class Product implements Cacheable, Searchable {
    @Override
    public String getCacheKey() { return "product:" + id; }

    @Override
    public int getTtl() { return 3600; }

    @Override
    public List<String> getSearchKeywords() {
        return List.of(name, category);
    }
}
```

---

## SOLID 원칙

### S - 단일 책임 원칙 (Single Responsibility)

```java
// ❌ 하나의 클래스가 여러 역할
public class UserService {
    public void register(User user) { /* 유저 등록 */ }
    public void sendEmail(User user) { /* 이메일 전송 */ }
    public String exportToCsv(List<User> users) { /* CSV 변환 */ }
}

// ✅ 역할별로 분리
public class UserService {
    public void register(User user) { /* 유저 등록 */ }
}
public class EmailService {
    public void sendEmail(User user) { /* 이메일 전송 */ }
}
public class UserExporter {
    public String exportToCsv(List<User> users) { /* CSV 변환 */ }
}
```

### O - 개방-폐쇄 원칙 (Open/Closed)

```java
// 확장에 열려있고, 수정에 닫혀있어야 한다

// ❌ 새 할인 타입마다 코드 수정 필요
public double calculateDiscount(String type, double price) {
    if (type.equals("VIP")) return price * 0.2;
    if (type.equals("COUPON")) return price * 0.1;
    // 새 타입 추가마다 여기를 수정...
}

// ✅ 인터페이스로 확장
public interface DiscountPolicy {
    double calculate(double price);
}

public class VipDiscount implements DiscountPolicy {
    public double calculate(double price) { return price * 0.2; }
}

public class CouponDiscount implements DiscountPolicy {
    public double calculate(double price) { return price * 0.1; }
}

// 새 할인 추가 시 클래스만 추가, 기존 코드 수정 없음
```

### L - 리스코프 치환 원칙 (Liskov Substitution)

```java
// 자식 클래스는 부모를 대체할 수 있어야 한다

// ❌ 위반: 자식이 부모의 동작을 깨뜨림
public class Bird {
    public void fly() { /* 날기 */ }
}
public class Penguin extends Bird {
    @Override
    public void fly() { throw new UnsupportedOperationException(); }
    // 펭귄은 못 나는데 Bird를 상속받음 → 위반
}

// ✅ 올바른 설계
public abstract class Bird { }
public interface Flyable { void fly(); }

public class Sparrow extends Bird implements Flyable {
    public void fly() { /* 날기 */ }
}
public class Penguin extends Bird {
    // Flyable 구현 안 함
}
```

### I - 인터페이스 분리 원칙 (Interface Segregation)

```java
// ❌ 너무 큰 인터페이스
public interface Worker {
    void work();
    void eat();
    void sleep();
}
// 로봇은 eat(), sleep()이 필요 없는데 구현해야 함

// ✅ 역할별로 분리
public interface Workable { void work(); }
public interface Eatable { void eat(); }
public interface Sleepable { void sleep(); }

public class Human implements Workable, Eatable, Sleepable { ... }
public class Robot implements Workable { ... }  // 필요한 것만
```

### D - 의존성 역전 원칙 (Dependency Inversion)

```java
// 고수준 모듈이 저수준 모듈에 의존하지 않고, 추상화에 의존

// ❌ 구체 클래스에 의존
public class OrderService {
    private MySqlOrderRepository repository = new MySqlOrderRepository();
}

// ✅ 인터페이스(추상화)에 의존
public class OrderService {
    private final OrderRepository repository;  // 인터페이스

    public OrderService(OrderRepository repository) {
        this.repository = repository;  // 생성자 주입 → Spring DI!
    }
}

// MySQL이든 MongoDB든 인터페이스만 구현하면 교체 가능
```

---

## 디자인 패턴 (면접 빈출)

### 싱글톤 (Singleton)

```java
// 인스턴스가 1개만 존재하도록 보장
// Spring의 Bean이 기본적으로 싱글톤

public class DatabaseConnection {
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private DatabaseConnection() {}  // 외부 생성 방지

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }
}
```

### 빌더 (Builder)

```java
// 복잡한 객체 생성을 단계별로
// Lombok @Builder로 자동 생성 가능

User user = User.builder()
    .name("John")
    .email("john@test.com")
    .age(25)
    .build();
```

### 전략 (Strategy)

```java
// 알고리즘을 캡슐화하여 교체 가능하게
// Spring의 DI가 전략 패턴의 구현

public interface SortStrategy {
    void sort(List<Integer> list);
}

public class QuickSort implements SortStrategy { ... }
public class MergeSort implements SortStrategy { ... }

public class Sorter {
    private final SortStrategy strategy;

    public Sorter(SortStrategy strategy) {  // 주입
        this.strategy = strategy;
    }

    public void sort(List<Integer> list) {
        strategy.sort(list);  // 어떤 전략이든 동작
    }
}
```

---

## 면접 예상 질문

1. **OOP의 4대 특성을 설명해주세요**
   - 캡슐화, 상속, 다형성, 추상화

2. **추상 클래스와 인터페이스의 차이는?**
   - 추상 클래스: 단일 상속, 공통 구현 공유 / 인터페이스: 다중 구현, 계약 정의

3. **SOLID 원칙을 설명해주세요**
   - S: 단일 책임 / O: 개방-폐쇄 / L: 리스코프 치환 / I: 인터페이스 분리 / D: 의존성 역전

4. **다형성이란? Spring에서 어떻게 활용되나요?**
   - 같은 인터페이스, 다른 구현. Spring DI로 구현체 교체 가능

5. **싱글톤 패턴이란? Spring에서 왜 쓰나요?**
   - 인스턴스 1개 보장. Spring Bean은 기본 싱글톤 → 메모리 절약
