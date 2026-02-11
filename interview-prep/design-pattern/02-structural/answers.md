# 구조 패턴 (Structural Patterns) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Proxy 패턴이란 무엇이며, 어떤 종류가 있는지 설명해주세요.

> Proxy 패턴은 실제 객체에 대한 대리 객체를 제공하여, 접근 제어나 부가 기능을 추가하는 패턴입니다. 클라이언트는 Proxy를 실제 객체처럼 사용하며 Proxy인지 인식하지 못합니다. 주요 종류로는 보호 프록시(접근 제어, Spring Security), 원격 프록시(원격 객체 접근, gRPC), 가상 프록시(지연 로딩, JPA Lazy Loading), 로깅/캐싱 프록시(부가 기능, Spring AOP)가 있습니다. Spring에서는 @Transactional, @Cacheable, @Async 등이 모두 Proxy 패턴으로 동작합니다.

**Q2.** Adapter 패턴이란 무엇이며, 왜 필요한지 실제 비유를 들어 설명해주세요.

> Adapter 패턴은 호환되지 않는 인터페이스를 가진 클래스들이 함께 동작할 수 있도록 변환하는 패턴입니다. 유럽 콘센트에 한국 플러그를 꽂기 위한 변환 어댑터와 같습니다. 기존 시스템(Adaptee)의 인터페이스를 변경하지 않으면서, 새로운 인터페이스(Target)에 맞게 변환합니다. 예를 들어, XML 기반의 레거시 시스템을 JSON 인터페이스를 사용하는 새 시스템에 연결할 때 Adapter를 사용합니다. Spring MVC의 HandlerAdapter가 대표적인 예시로, 다양한 형태의 Handler를 일관된 방식으로 처리합니다.

**Q3.** Decorator 패턴이란 무엇이며, 상속과 비교했을 때 어떤 장점이 있나요?

> Decorator 패턴은 객체에 동적으로 새로운 기능을 추가하는 패턴입니다. 대상 객체를 감싸는(wrapping) 방식으로, 여러 Decorator를 조합하여 다양한 기능 조합을 만들 수 있습니다. 상속에 비해 첫째, 런타임에 동적으로 기능을 추가/제거할 수 있고, 둘째, 조합의 수가 많아도 클래스 수가 폭발하지 않으며(상속은 조합마다 새 클래스 필요), 셋째, 단일 책임 원칙을 지킬 수 있습니다. Java I/O Stream이 대표적인 예시로, `new BufferedInputStream(new FileInputStream("file.txt"))` 형태로 기능을 조합합니다.

**Q4.** Facade 패턴이란 무엇이고, 어떤 상황에서 사용하나요?

> Facade 패턴은 복잡한 서브시스템에 대한 간단한 통합 인터페이스를 제공하는 패턴입니다. 호텔의 프론트 데스크처럼, 클라이언트가 복잡한 내부 구조를 몰라도 단일 창구를 통해 요청할 수 있게 합니다. 여러 서브시스템을 조합해야 하는 복잡한 비즈니스 로직이 있을 때, 클라이언트에게 간단한 메서드를 제공하고 싶을 때, 계층 간 결합도를 낮추고 싶을 때 사용합니다. Spring의 Service 계층이 대표적인 Facade로, Controller에게 단순한 인터페이스를 제공하면서 내부적으로 여러 Repository와 Service를 조합합니다.

**Q5.** Spring AOP가 Proxy 패턴으로 동작하는 원리를 설명해주세요.

> Spring은 AOP 적용 대상 Bean에 대해 Proxy 객체를 자동 생성합니다. Bean이 IoC 컨테이너에 등록될 때, AOP 대상이면 원본 객체 대신 Proxy 객체가 등록됩니다. 클라이언트가 해당 Bean을 호출하면 Proxy가 먼저 요청을 받아 부가 기능(트랜잭션 시작, 로깅 등)을 수행한 후, 실제 객체의 메서드를 호출합니다. Spring Boot는 기본적으로 CGLIB을 사용하여 대상 클래스를 상속한 Proxy를 생성합니다. @Transactional이면 Proxy가 트랜잭션 시작 후 메서드를 호출하고, 정상이면 commit, 예외면 rollback합니다.

## 비교/구분 (6~9)

**Q6.** Proxy 패턴과 Decorator 패턴의 차이를 설명해주세요.

> 두 패턴 모두 대상 객체를 감싸는 구조이지만 목적과 사용 방식이 다릅니다. Proxy는 접근 제어가 주 목적으로, 클라이언트는 Proxy의 존재를 모르며, Proxy가 대상 객체를 내부에서 관리합니다. Spring AOP처럼 투명하게 동작합니다. Decorator는 기능 추가가 주 목적으로, 클라이언트가 직접 Decorator 조합을 결정하며, 여러 Decorator를 중첩하여 사용합니다. Java I/O Stream처럼 `new BufferedInputStream(new FileInputStream(...))`으로 조합합니다. 한 줄로 요약하면, Proxy는 "제어", Decorator는 "확장"입니다.

**Q7.** JDK Dynamic Proxy와 CGLIB의 차이를 설명해주세요.

> JDK Dynamic Proxy는 인터페이스를 구현하여 Proxy를 생성하므로 대상 클래스가 반드시 인터페이스를 구현해야 합니다. 인터페이스 타입으로만 주입할 수 있으며, 리플렉션 기반으로 동작합니다. CGLIB은 대상 클래스를 상속하여 Proxy를 생성하므로 인터페이스가 불필요하지만, final 클래스나 메서드에는 적용할 수 없습니다(상속 불가능). 바이트코드 생성 방식으로 약간 더 빠릅니다. Spring Boot 2.0부터 CGLIB이 기본값이며, 인터페이스 유무와 관계없이 CGLIB Proxy를 사용합니다.

**Q8.** Adapter 패턴과 Facade 패턴의 차이를 설명해주세요.

> Adapter는 호환되지 않는 인터페이스를 변환하여 기존 클래스를 새 인터페이스에 맞게 적응시키는 패턴입니다. 주로 하나의 클래스 인터페이스를 변환합니다. Facade는 복잡한 서브시스템에 간단한 통합 인터페이스를 제공하는 패턴입니다. 여러 서브시스템을 하나의 인터페이스 뒤에 숨깁니다. Adapter는 "인터페이스 변환"이 핵심이고, Facade는 "복잡성 숨기기"가 핵심입니다. Spring에서 HandlerAdapter는 Adapter 패턴, Service 계층은 Facade 패턴의 예시입니다.

**Q9.** Proxy 패턴과 Adapter 패턴의 차이를 설명해주세요.

> Proxy는 실제 객체와 같은 인터페이스를 제공하면서 접근을 제어하거나 부가 기능을 추가합니다. 클라이언트 입장에서 Proxy와 실제 객체의 인터페이스가 동일합니다. Adapter는 서로 다른 인터페이스를 가진 클래스들을 연결하기 위해 인터페이스를 변환합니다. Proxy는 "같은 인터페이스, 다른 동작(제어/부가기능)"이고, Adapter는 "다른 인터페이스, 같은 결과(호환성)"입니다. Spring AOP의 Proxy 객체는 원본과 같은 인터페이스를 가지고, HandlerAdapter는 다양한 Handler 인터페이스를 DispatcherServlet이 기대하는 인터페이스로 변환합니다.

## 심화/실무 (10~12)

**Q10.** @Transactional이 동작하지 않는 경우와 해결 방법을 설명해주세요. (Proxy 내부 호출 문제)

> @Transactional은 Proxy를 통해 동작하므로, 같은 클래스 내부에서 this로 @Transactional 메서드를 호출하면 Proxy를 거치지 않아 트랜잭션이 적용되지 않습니다. 예를 들어 OrderService의 createOrder()에서 this.sendNotification()을 호출하면, sendNotification()의 @Transactional은 무시됩니다. 이는 this가 Proxy가 아닌 실제 객체를 가리키기 때문입니다. 해결 방법은 첫째, 메서드를 별도 클래스(NotificationService)로 분리하는 것이 가장 권장됩니다. 둘째, self-injection으로 자기 자신의 Proxy를 주입받아 호출하는 방법도 있습니다.

**Q11.** Spring MVC에서 HandlerAdapter가 Adapter 패턴을 사용하는 이유와 동작 흐름을 설명해주세요.

> DispatcherServlet은 HTTP 요청을 처리할 Handler를 찾아 실행해야 하는데, Handler의 형태가 다양합니다(@Controller 메서드, HttpRequestHandler, Controller 인터페이스 등). 각 Handler 타입을 직접 처리하면 DispatcherServlet이 복잡해지고, 새 Handler 타입 추가 시 DispatcherServlet을 수정해야 합니다. HandlerAdapter를 통해 이 문제를 해결합니다. 요청이 오면 HandlerMapping이 적절한 Handler를 찾고, 등록된 HandlerAdapter들 중 supports(handler)가 true인 Adapter를 선택한 후, adapter.handle()로 Handler를 실행합니다. 새 Handler 타입이 추가되면 해당 Adapter만 구현하면 됩니다.

**Q12.** Service 계층이 Facade 역할을 한다는 것은 무슨 의미이며, 이렇게 설계하는 이유는 무엇인가요?

> Service 계층이 Facade라는 것은, Controller에게는 단순한 메서드(`createOrder()`)만 노출하고, 내부적으로 여러 Repository와 다른 Service들의 복잡한 비즈니스 로직(재고 확인, 주문 생성, 결제, 포인트 적립, 알림 발송 등)을 조합한다는 의미입니다. 이렇게 설계하는 이유는 첫째, Controller를 얇게(thin) 유지하여 요청 처리와 비즈니스 로직을 분리할 수 있고, 둘째, 비즈니스 로직 변경 시 Service만 수정하면 Controller에 영향이 없으며, 셋째, Service 단위로 트랜잭션을 관리할 수 있고, 넷째, Service를 단위 테스트하기 용이합니다.

## 꼬리질문 대비 (13~15)

**Q13.** CGLIB Proxy가 final 클래스/메서드에 적용할 수 없는 이유는 무엇인가요?

> CGLIB은 대상 클래스를 상속하여 Proxy 서브클래스를 생성하는 방식으로 동작합니다. Java에서 final 클래스는 상속이 불가능하므로, CGLIB이 서브클래스를 만들 수 없어 Proxy를 생성할 수 없습니다. 마찬가지로 final 메서드는 오버라이드가 불가능하므로, Proxy가 해당 메서드를 가로채서 부가 기능을 추가할 수 없습니다. 따라서 Spring AOP를 적용할 Bean 클래스와 AOP 대상 메서드에는 final을 붙이지 않아야 합니다.

**Q14.** Java I/O Stream이 Decorator 패턴이라면, BufferedInputStream은 어떤 역할을 하나요?

> BufferedInputStream은 ConcreteDecorator 역할을 합니다. 내부에 다른 InputStream(Component)을 감싸고, 원래의 읽기 기능에 "버퍼링"이라는 새로운 기능을 추가합니다. 데이터를 한 바이트씩 읽는 대신, 내부 버퍼(기본 8KB)에 미리 읽어두고 버퍼에서 제공하므로 I/O 횟수가 줄어 성능이 향상됩니다. `new BufferedInputStream(new FileInputStream("file.txt"))`처럼 FileInputStream을 감싸서 버퍼링 기능을 동적으로 추가하며, 추가로 DataInputStream으로 감싸면 데이터 타입 읽기 기능도 추가할 수 있습니다.

**Q15.** Spring에서 Proxy 방식을 JDK Dynamic Proxy로 변경하려면 어떻게 설정하나요? 언제 변경하는 것이 유리한가요?

> application.properties에서 `spring.aop.proxy-target-class=false`로 설정하면 인터페이스가 있는 경우 JDK Dynamic Proxy를 사용합니다. JDK Dynamic Proxy가 유리한 경우는, 첫째, 인터페이스 기반 프로그래밍을 철저히 하여 구현체 교체가 빈번한 프로젝트에서 인터페이스 타입으로만 주입하는 것을 강제하고 싶을 때, 둘째, CGLIB의 클래스 상속에 의한 제약(final 문제 등)을 피하고 싶을 때입니다. 다만 대부분의 Spring Boot 프로젝트에서는 CGLIB이 기본값으로 충분하며, 인터페이스 없이도 Proxy를 생성할 수 있어 더 편리합니다.
