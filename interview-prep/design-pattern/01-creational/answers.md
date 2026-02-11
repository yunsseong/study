# 생성 패턴 (Creational Patterns) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Singleton 패턴이란 무엇이며, 왜 사용하는지 설명해주세요.

> Singleton 패턴은 클래스의 인스턴스가 오직 하나만 존재하도록 보장하고, 전역 접근점을 제공하는 디자인 패턴입니다. 주요 사용 이유는 첫째, 리소스를 공유해야 하는 경우(DB 커넥션 풀, 로깅 등)에 인스턴스를 하나만 만들어 메모리를 절약할 수 있고, 둘째, 설정 정보나 캐시처럼 여러 곳에서 동일한 상태를 참조해야 할 때 일관성을 보장할 수 있습니다. 다만 전역 상태를 만들 수 있어 테스트가 어려워질 수 있고, 결합도가 높아질 수 있다는 단점도 있습니다.

**Q2.** Java에서 Singleton을 구현하는 방법들을 설명하고, 각각의 장단점을 비교해주세요.

> 크게 4가지 방식이 있습니다. 첫째, Eager Initialization은 static final 필드로 클래스 로딩 시 바로 생성하며, 구현이 간단하고 thread-safe하지만 사용하지 않아도 메모리를 차지합니다. 둘째, Lazy Initialization은 getInstance() 호출 시 생성하여 메모리를 절약하지만 멀티스레드 환경에서 안전하지 않습니다. 셋째, Double-Checked Locking은 volatile + synchronized를 사용하여 thread-safe한 지연 초기화를 제공하지만 구현이 복잡합니다. 넷째, Enum Singleton은 JVM이 thread-safety와 직렬화 안전성을 모두 보장하므로 가장 권장되는 방식이지만, 상속이 불가능하고 지연 초기화를 할 수 없습니다.

**Q3.** Factory Method 패턴이란 무엇이며, 어떤 상황에서 사용하나요?

> Factory Method 패턴은 객체 생성 로직을 별도 메서드나 클래스로 분리하여, 생성할 객체의 구체적인 타입을 서브클래스나 팩토리가 결정하도록 하는 패턴입니다. 주로 사용하는 상황은 첫째, 어떤 구체 클래스를 생성할지 런타임에 결정해야 할 때(예: 알림 타입에 따라 Email, SMS 객체 생성), 둘째, 객체 생성 로직이 복잡하여 클라이언트 코드와 분리하고 싶을 때, 셋째, 새로운 타입 추가 시 기존 코드 수정 없이 확장하고 싶을 때(OCP 원칙) 사용합니다. Spring의 BeanFactory.getBean()이 대표적인 예시입니다.

**Q4.** Builder 패턴이란 무엇이고, 점층적 생성자 패턴 대비 어떤 장점이 있나요?

> Builder 패턴은 복잡한 객체의 생성 과정과 표현을 분리하여, 메서드 체이닝을 통해 단계별로 객체를 구성하는 패턴입니다. 점층적 생성자 패턴(매개변수 조합마다 생성자를 오버로딩)에 비해 여러 장점이 있습니다. 첫째, 각 매개변수에 이름을 부여하므로 가독성이 좋습니다(`.name("홍길동").age(25)`). 둘째, 선택적 매개변수에 null을 넘길 필요가 없습니다. 셋째, 불변 객체를 안전하게 생성할 수 있습니다. 넷째, 매개변수 순서를 실수할 위험이 없습니다. 실무에서는 Lombok의 @Builder를 사용하여 보일러플레이트 코드를 제거합니다.

**Q5.** Spring Bean의 기본 Scope이 Singleton인 이유를 설명해주세요.

> 웹 애플리케이션에서 매 요청마다 서비스, 리포지토리 등의 객체를 새로 생성하면 생성/GC 비용이 크고 메모리가 낭비됩니다. Spring의 Service, Repository 같은 Bean은 보통 상태를 가지지 않는(stateless) 객체이므로 여러 스레드가 안전하게 공유할 수 있습니다. 따라서 Bean을 1개만 생성하여 재사용하는 Singleton이 기본 Scope으로 적합합니다. 다만 Singleton Bean은 공유되므로 인스턴스 변수에 상태를 저장하면 동시성 문제가 발생할 수 있어, 반드시 무상태로 설계해야 합니다.

## 비교/구분 (6~9)

**Q6.** GoF Singleton과 Spring Singleton의 차이를 설명해주세요.

> GoF Singleton은 클래스 자체에서 Singleton을 보장합니다. private 생성자와 static getInstance() 메서드를 통해 어떤 방법으로든 인스턴스가 1개만 생성됩니다. 반면 Spring Singleton은 IoC 컨테이너가 관리하는 Scope 개념입니다. 클래스 자체에 private 생성자가 없으므로 직접 new로 인스턴스를 생성할 수 있지만, Spring 컨테이너를 통해 가져오면 항상 같은 인스턴스를 반환합니다. 즉, GoF Singleton은 "클래스 레벨"에서, Spring Singleton은 "컨테이너 레벨"에서 인스턴스의 유일성을 보장합니다.

**Q7.** Factory Method 패턴과 Abstract Factory 패턴의 차이를 설명해주세요.

> Factory Method는 하나의 Product 객체를 생성하는 메서드를 서브클래스에 위임하는 패턴입니다. 예를 들어, 알림 타입(Email, SMS)에 따라 하나의 Notification 객체를 생성합니다. Abstract Factory는 관련된 여러 종류의 객체들을 하나의 일관된 세트(family)로 생성하는 패턴입니다. 예를 들어, 다크 테마의 Button, TextField, Checkbox를 한꺼번에 생성합니다. Factory Method가 "한 종류의 객체 생성"에 초점을 맞춘다면, Abstract Factory는 "관련 객체 그룹의 일관된 생성"에 초점을 맞춥니다.

**Q8.** Singleton의 Eager Initialization과 Lazy Initialization의 차이를 설명해주세요.

> Eager Initialization은 클래스 로딩 시점에 즉시 인스턴스를 생성합니다. `private static final Singleton INSTANCE = new Singleton()`으로 구현하며, JVM의 클래스 로딩 메커니즘에 의해 thread-safe하지만, 실제로 사용하지 않아도 메모리를 차지합니다. Lazy Initialization은 getInstance()가 최초 호출될 때 인스턴스를 생성합니다. 필요할 때만 생성하여 메모리를 절약하지만, 멀티스레드 환경에서는 두 스레드가 동시에 null 체크를 통과하여 인스턴스가 2개 생성될 수 있습니다. 이를 해결하려면 synchronized나 Double-Checked Locking이 필요합니다.

**Q9.** Builder 패턴과 정적 팩토리 메서드(static factory method)의 차이를 설명해주세요.

> 정적 팩토리 메서드는 `User.of(name, email)`처럼 이름을 가진 메서드로 객체를 생성하는 기법으로, 매개변수가 적고 의미가 명확할 때 적합합니다. Builder 패턴은 매개변수가 많거나 선택적 매개변수가 있을 때, `.name("홍길동").email("hong@email.com").age(25).build()`처럼 메서드 체이닝으로 단계별 구성하는 패턴입니다. 정적 팩토리 메서드는 간결함이 장점이고, Builder는 복잡한 객체의 가독성이 장점입니다. 실무에서는 매개변수가 3개 이하면 정적 팩토리 메서드, 4개 이상이면 Builder를 주로 사용합니다.

## 심화/실무 (10~12)

**Q10.** Double-Checked Locking에서 volatile 키워드가 필요한 이유를 설명해주세요.

> `instance = new Singleton()`은 내부적으로 (1) 메모리 할당, (2) 생성자 호출(초기화), (3) 참조 변수에 주소 할당의 3단계로 이루어집니다. JVM의 최적화(instruction reordering)로 인해 (1)→(3)→(2) 순서로 재배치될 수 있습니다. 이 경우 Thread A가 (1)→(3)까지 실행한 시점에 Thread B가 첫 번째 null 체크를 하면, instance가 null이 아니지만 아직 초기화가 완료되지 않은 객체를 사용하게 됩니다. volatile 키워드는 이러한 명령어 재배치를 방지(happens-before 관계 보장)하여 완전히 초기화된 객체만 다른 스레드에 보이도록 합니다.

**Q11.** Singleton Bean에서 상태(state)를 가지면 어떤 문제가 발생하나요? 구체적인 예시를 들어주세요.

> Singleton Bean은 여러 스레드가 동시에 공유하므로, 인스턴스 변수에 상태를 저장하면 레이스 컨디션이 발생합니다. 예를 들어 OrderService에 `private User currentUser` 필드가 있다고 가정하면, Thread A가 currentUser를 userA로 설정한 후 처리하는 도중 Thread B가 currentUser를 userB로 덮어쓰면, Thread A는 userB의 데이터로 주문을 처리하게 됩니다. 해결 방법은 첫째, Bean을 무상태로 설계하고 필요한 데이터는 메서드 매개변수나 지역 변수로 전달하는 것이고, 둘째, 스레드별 독립 저장이 필요하면 ThreadLocal을 사용하는 것입니다.

**Q12.** 실무에서 Lombok @Builder를 활용하는 대표적인 사례를 설명해주세요.

> 첫째, DTO/Response 객체 생성에 사용합니다. `UserResponse.builder().id(1L).name("홍길동").build()`처럼 Entity에서 Response DTO로 변환할 때 가독성이 좋습니다. 둘째, 테스트 데이터 생성에 활용합니다. 테스트에서 필요한 필드만 설정하고 나머지는 기본값을 사용할 수 있어 테스트 코드가 간결해집니다. 셋째, 복잡한 설정 객체를 생성할 때 사용합니다. 다만, @Builder를 Entity 클래스에 직접 사용하면 protected/private 생성자 없이 어디서든 객체를 생성할 수 있으므로, Entity는 정적 팩토리 메서드나 별도의 Builder를 구성하는 것이 더 안전합니다.

## 꼬리질문 대비 (13~15)

**Q13.** Enum Singleton이 리플렉션과 직렬화에 안전한 이유는 무엇인가요?

> 리플렉션 안전성: Java의 리플렉션 API는 enum 타입의 newInstance() 호출을 명시적으로 막습니다. Constructor.newInstance()를 시도하면 IllegalArgumentException이 발생합니다. 이는 JVM 스펙에 정의된 동작입니다. 직렬화 안전성: 일반 Singleton은 역직렬화 시 readObject()가 새 인스턴스를 생성하여 Singleton이 깨질 수 있어 readResolve() 메서드를 별도로 구현해야 합니다. 반면 enum은 JVM이 직렬화/역직렬화를 특별하게 처리하여 항상 동일한 인스턴스를 반환합니다.

**Q14.** Spring에서 BeanFactory와 ApplicationContext의 차이를 설명해주세요. 둘 다 Factory 패턴과 관련이 있나요?

> BeanFactory는 Spring IoC 컨테이너의 최상위 인터페이스로, Bean의 생성과 의존성 주입을 담당하는 기본 Factory입니다. Lazy Loading으로 getBean() 호출 시 Bean을 생성합니다. ApplicationContext는 BeanFactory를 상속하면서 추가 기능(이벤트 발행, 메시지 국제화, AOP 연동, 환경 변수 처리 등)을 제공합니다. Eager Loading으로 컨테이너 시작 시 모든 Singleton Bean을 미리 생성합니다. 둘 다 Factory 패턴을 기반으로 Bean 이름/타입에 따라 적절한 객체를 생성하고 반환합니다. 실무에서는 ApplicationContext를 사용합니다.

**Q15.** Singleton 패턴의 단점은 무엇이며, Spring은 이를 어떻게 보완하나요?

> GoF Singleton의 단점은 첫째, private 생성자로 인해 상속이 어렵고, 둘째, 전역 상태를 만들어 테스트가 어렵고(Mock 교체가 힘듦), 셋째, 클래스 간 결합도가 높아진다는 것입니다. Spring은 이를 IoC 컨테이너를 통해 보완합니다. Bean 클래스 자체는 일반 클래스이므로 상속이 가능하고, DI를 통해 인터페이스 기반으로 주입받으므로 테스트 시 Mock 객체로 쉽게 교체할 수 있습니다. 또한 @Scope("prototype")으로 Scope을 변경할 수도 있어, GoF Singleton의 경직성 없이 Singleton의 장점(성능, 메모리)을 누릴 수 있습니다.
