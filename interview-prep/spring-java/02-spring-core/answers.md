# Spring Core (IoC, DI, AOP) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** IoC(제어의 역전)란 무엇이며, 기존 방식과 어떤 차이가 있는지 설명해주세요.

> IoC(Inversion of Control)는 객체의 생성과 생명주기 관리의 제어권이 개발자에서 프레임워크(Spring 컨테이너)로 역전되는 것입니다. 기존에는 개발자가 `new OrderRepository()`처럼 직접 객체를 생성하고 의존관계를 연결했지만, IoC 방식에서는 Spring 컨테이너가 객체를 생성하고 필요한 의존성을 주입합니다. 개발자는 "무엇을 사용할지"만 선언하고, "어떻게 만들지"는 Spring이 담당합니다. 이를 통해 느슨한 결합을 달성하고, 테스트 용이성과 유지보수성이 향상됩니다.

**Q2.** DI(의존성 주입)란 무엇이고, Spring에서 제공하는 DI 방식 3가지를 설명해주세요.

> DI(Dependency Injection)는 IoC를 구현하는 방법으로, 객체가 필요로 하는 의존성을 외부에서 주입받는 것입니다. Spring에서는 3가지 DI 방식을 제공합니다. 첫째, 생성자 주입은 생성자 파라미터로 의존성을 주입받는 방식으로, final 키워드를 사용할 수 있고 생성자가 1개면 @Autowired를 생략할 수 있습니다. 둘째, 세터 주입은 setter 메서드에 @Autowired를 붙여 주입받는 방식으로, 선택적 의존성에 사용합니다. 셋째, 필드 주입은 필드에 @Autowired를 직접 붙이는 방식으로, 간편하지만 테스트가 어렵고 Spring 컨테이너에 의존적이라 비권장됩니다.

**Q3.** 생성자 주입을 권장하는 이유를 설명해주세요.

> 생성자 주입을 권장하는 이유는 네 가지입니다. 첫째, final 키워드를 사용할 수 있어 불변성을 보장합니다. 한 번 주입된 의존성은 변경할 수 없어 안전합니다. 둘째, 의존성이 누락되면 컴파일 시점에 에러가 발생하여 안전합니다. 필드 주입이나 세터 주입은 null이 될 수 있어 런타임에야 문제를 발견합니다. 셋째, 순환 참조가 있으면 애플리케이션 시작 시 바로 감지됩니다. 필드 주입은 런타임에야 발견되어 더 위험합니다. 넷째, `new OrderService(mockRepo)`처럼 순수 Java 코드로 테스트할 수 있어 Spring 컨테이너 없이도 단위 테스트가 가능합니다.

**Q4.** Spring Bean이란 무엇이고, Bean의 생명주기를 설명해주세요.

> Spring Bean은 Spring IoC 컨테이너가 관리하는 객체를 말합니다. @Component, @Service, @Repository, @Controller 등의 어노테이션으로 등록합니다. Bean의 생명주기는 다음과 같습니다. 먼저 Spring 컨테이너가 시작되면 Bean 인스턴스를 생성(new)하고, 의존성을 주입(DI)합니다. 그 다음 초기화 콜백(@PostConstruct)이 호출되어 DB 연결 등 초기화 작업을 수행합니다. 이후 비즈니스 로직에서 사용되다가, Spring 컨테이너가 종료될 때 소멸 콜백(@PreDestroy)이 호출되어 리소스 해제 등 정리 작업을 수행합니다.

**Q5.** AOP(관점 지향 프로그래밍)란 무엇이며, 왜 필요한지 설명해주세요.

> AOP(Aspect Oriented Programming)는 핵심 비즈니스 로직과 공통 관심 사항(횡단 관심사)을 분리하는 프로그래밍 기법입니다. 로깅, 트랜잭션, 보안 같은 공통 코드가 여러 메서드에 반복되면 코드 중복이 발생하고 유지보수가 어려워집니다. AOP를 적용하면 이러한 횡단 관심사를 Aspect로 모듈화하여, 핵심 로직에는 비즈니스 코드만 남기고 공통 기능은 별도로 관리할 수 있습니다. 실무에서는 @Transactional(트랜잭션 관리), API 실행 시간 측정, 인증/인가 체크 등에 활용됩니다.

## 비교/구분 (6~9)

**Q6.** Spring과 Spring Boot의 차이를 설명해주세요.

> Spring Boot는 Spring을 쉽게 사용하기 위한 도구이지, 별개의 프레임워크가 아닙니다. 내부적으로 Spring의 IoC, DI, AOP가 그대로 동작합니다. 차이점으로는, Spring은 XML 또는 Java Config를 직접 작성해야 하지만 Spring Boot는 자동 설정(Auto Configuration)을 제공합니다. Spring은 외부 Tomcat에 war를 배포해야 하지만, Spring Boot는 내장 Tomcat으로 `java -jar`로 바로 실행할 수 있습니다. Spring은 개별 라이브러리 버전을 관리해야 하지만, Spring Boot는 Starter로 의존성을 묶어서 관리합니다.

**Q7.** Bean Scope에서 Singleton과 Prototype의 차이를 설명해주세요.

> Singleton은 기본 Scope로, 컨테이너에 해당 Bean이 1개만 존재합니다. 컨테이너 시작 시 생성되고 종료 시 소멸되며, 여러 요청이 같은 객체를 공유합니다. Prototype은 요청할 때마다 새로운 객체를 생성합니다. Spring이 생성만 하고 이후 관리하지 않으므로, @PreDestroy가 호출되지 않습니다. Singleton Bean은 여러 스레드가 공유하므로 인스턴스 변수로 상태를 가지면 동시성 문제가 발생할 수 있어, 무상태(stateless)로 설계해야 합니다.

**Q8.** JDK Dynamic Proxy와 CGLIB의 차이를 설명해주세요.

> JDK Dynamic Proxy는 대상 클래스가 인터페이스를 구현한 경우에 사용되며, 인터페이스를 기반으로 프록시를 생성합니다. 따라서 인터페이스 타입으로만 주입 가능합니다. CGLIB(Code Generation Library)는 인터페이스가 없는 구체 클래스에 사용되며, 대상 클래스를 상속받아 프록시를 생성합니다. 따라서 final 클래스나 메서드에는 적용할 수 없습니다. Spring Boot 2.0부터는 CGLIB이 기본값이며, 인터페이스 유무와 관계없이 CGLIB 프록시를 사용하므로 구체 클래스 타입으로 주입해도 문제없습니다.

**Q9.** @Component, @Service, @Repository, @Controller의 차이를 설명해주세요.

> 네 가지 모두 Spring Bean을 등록하는 어노테이션입니다. @Component는 일반적인 컴포넌트에 사용하는 기본 어노테이션이고, 나머지 세 가지는 @Component의 특수화(specialization)입니다. @Service는 비즈니스 로직을 담당하는 서비스 계층에, @Repository는 데이터 액세스 계층에, @Controller는 웹 요청을 처리하는 프레젠테이션 계층에 사용합니다. 기능적으로는 모두 Bean 등록 역할을 하지만, @Repository는 추가로 데이터 접근 예외를 Spring의 DataAccessException으로 변환해주는 기능이 있고, 계층별 어노테이션을 사용하면 코드의 가독성과 역할이 명확해집니다.

## 심화/실무 (10~12)

**Q10.** @Transactional의 동작 원리를 설명해주세요. 같은 클래스 내부에서 @Transactional 메서드를 호출하면 어떤 문제가 발생하나요?

> @Transactional은 Spring AOP를 통해 동작합니다. Spring이 대상 클래스의 프록시 객체를 생성하여, 메서드 호출 시 프록시가 트랜잭션을 시작하고 정상 완료 시 커밋, 예외 발생 시 롤백을 자동 처리합니다. 문제는 같은 클래스 내부에서 this로 다른 @Transactional 메서드를 호출할 때 발생합니다. 외부에서 호출하면 프록시를 거치므로 AOP가 적용되지만, 내부에서 this로 호출하면 프록시가 아닌 실제 객체를 직접 호출하므로 @Transactional이 적용되지 않습니다. 해결 방법은 해당 메서드를 별도 클래스(Bean)로 분리하여 외부 Bean을 통해 호출하는 것이 권장됩니다.

**Q11.** AOP의 핵심 용어(Aspect, Advice, Pointcut, JoinPoint, Proxy)를 설명하고, Spring AOP가 동작하는 흐름을 설명해주세요.

> Aspect는 공통 관심사를 모듈화한 것(예: 로깅, 트랜잭션)이고, Advice는 언제 무엇을 실행할지(@Before, @After, @Around 등)를 정의합니다. Pointcut은 어디에 적용할지 대상을 지정하는 표현식이고, JoinPoint는 Advice가 적용될 수 있는 메서드 실행 지점입니다. Proxy는 AOP가 적용된 대리 객체입니다. 동작 흐름은, 클라이언트가 Bean의 메서드를 호출하면 실제 객체가 아닌 Proxy 객체가 호출을 받습니다. Proxy는 Pointcut에 매칭되면 Advice를 실행하고, proceed()로 실제 메서드를 호출합니다. 클라이언트는 Proxy인지 모르고 투명하게 동작합니다.

**Q12.** Singleton Bean에서 상태(state)를 가지면 안 되는 이유를 설명해주세요.

> Singleton Bean은 Spring 컨테이너에 1개만 존재하고 여러 스레드가 공유합니다. 만약 Singleton Bean에 인스턴스 변수로 상태를 저장하면, 여러 스레드가 동시에 해당 변수에 접근하면서 동시성 문제(race condition)가 발생합니다. 예를 들어 `private int requestCount = 0;`처럼 인스턴스 변수를 두면, 여러 요청이 동시에 이 값을 수정하여 데이터가 꼬일 수 있습니다. 따라서 Singleton Bean은 무상태(stateless)로 설계해야 하며, 상태가 필요하면 메서드의 지역 변수(Stack에 저장되어 스레드별 독립)를 사용하거나 ThreadLocal을 활용해야 합니다.

## 꼬리질문 대비 (13~15)

**Q13.** 순환 참조(Circular Dependency)란 무엇이며, 생성자 주입에서 어떻게 감지되나요?

> 순환 참조는 A가 B에 의존하고, B가 다시 A에 의존하는 구조입니다. 생성자 주입에서는 A를 생성하려면 B가 필요하고, B를 생성하려면 A가 필요하여 무한 루프에 빠집니다. Spring은 이를 애플리케이션 시작 시점에 감지하여 즉시 에러를 발생시킵니다. 이는 생성자 주입의 장점인데, 필드 주입이나 세터 주입은 객체 생성 후에 의존성을 주입하므로 순환 참조를 시작 시점에 감지하지 못하고 런타임에야 문제가 드러납니다. 순환 참조가 발생하면 설계를 재검토하여 의존관계를 분리하는 것이 근본적인 해결 방법입니다.

**Q14.** 외부 라이브러리의 클래스를 Spring Bean으로 등록하려면 어떻게 해야 하나요?

> 외부 라이브러리의 클래스는 소스코드를 수정할 수 없으므로 @Component를 붙일 수 없습니다. 이때는 @Configuration 클래스에서 @Bean 어노테이션을 사용하여 직접 Bean을 등록합니다. 예를 들어 Jackson의 ObjectMapper를 커스터마이징하여 Bean으로 등록하려면, @Configuration 클래스 안에 @Bean 메서드를 만들어 new ObjectMapper()를 반환하면 됩니다. 추가로 @ConditionalOnProperty 같은 조건부 어노테이션을 활용하면, 특정 설정값에 따라 Bean을 선택적으로 등록할 수도 있습니다.

**Q15.** @Around Advice에서 ProceedingJoinPoint의 역할은 무엇이며, proceed()를 호출하지 않으면 어떻게 되나요?

> ProceedingJoinPoint는 @Around Advice에서만 사용되며, 실제 대상 메서드의 실행을 제어하는 역할을 합니다. proceed()를 호출하면 실제 메서드가 실행되고, 그 반환값을 받아 후처리할 수 있습니다. proceed()를 호출하지 않으면 실제 메서드가 실행되지 않습니다. 이 특성을 활용하면 특정 조건에서 메서드 실행을 차단하거나, 캐시된 결과를 반환하는 등의 로직을 구현할 수 있습니다. @Around는 메서드 실행 전후 모두를 제어할 수 있어 가장 강력한 Advice이며, @Before, @After, @AfterReturning, @AfterThrowing의 기능을 모두 포함합니다.
