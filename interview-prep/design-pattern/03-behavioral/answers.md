# 행위 패턴 (Behavioral Patterns) 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Strategy 패턴이란 무엇이며, 왜 사용하는지 설명해주세요.

> Strategy 패턴은 알고리즘(전략)을 인터페이스로 캡슐화하여 런타임에 교체 가능하게 하는 패턴입니다. 사용하는 이유는 첫째, if-else나 switch 문 없이 알고리즘을 교체할 수 있어 OCP(개방-폐쇄 원칙)를 지킬 수 있고, 둘째, 새로운 알고리즘 추가 시 기존 코드를 수정하지 않고 새 클래스만 추가하면 되며, 셋째, 각 알고리즘을 독립적으로 테스트할 수 있기 때문입니다. Java의 Comparator가 대표적인 예시로, 정렬 기준(전략)을 외부에서 주입하여 동일한 sort() 메서드로 다양한 정렬을 수행할 수 있습니다.

**Q2.** Observer 패턴이란 무엇이며, 발행-구독(Pub-Sub) 모델과의 관계를 설명해주세요.

> Observer 패턴은 한 객체(Subject)의 상태가 변경되면 의존하는 모든 객체(Observer)에 자동으로 알림을 보내는 패턴입니다. 발행-구독(Pub-Sub) 모델은 Observer 패턴의 변형으로, 순수 Observer 패턴에서는 Subject가 Observer를 직접 알고 있지만, Pub-Sub 모델에서는 중간에 이벤트 채널(브로커)이 있어 발행자와 구독자가 서로를 모릅니다. Spring의 ApplicationEvent 시스템은 Pub-Sub에 가깝습니다. EventPublisher가 이벤트를 발행하면 Spring이 중간에서 등록된 @EventListener들에게 전달합니다.

**Q3.** Template Method 패턴이란 무엇이고, 어떤 상황에서 사용하나요?

> Template Method 패턴은 알고리즘의 골격(전체 흐름)을 상위 클래스에서 정의하고, 세부 단계의 구현을 서브클래스에 위임하는 패턴입니다. 주로 여러 클래스에서 공통적인 알고리즘 구조는 같지만 세부 구현이 다른 경우에 사용합니다. 예를 들어, DB 작업의 "연결 → SQL 실행 → 결과 매핑 → 자원 정리"라는 골격은 같지만, SQL과 매핑 로직은 다릅니다. Spring의 JdbcTemplate이 대표적인 예시로, 반복적인 JDBC 코드(연결, 정리, 예외 처리)를 Template이 처리하고, 개발자는 SQL과 RowMapper만 작성하면 됩니다.

**Q4.** Iterator 패턴이란 무엇이며, Java에서 어떻게 구현되어 있나요?

> Iterator 패턴은 컬렉션의 내부 구조를 노출하지 않고 요소를 순차적으로 접근하는 통일된 방법을 제공하는 패턴입니다. Java에서는 Iterable 인터페이스가 iterator() 메서드를 정의하고, 모든 Collection이 이를 구현합니다. Iterator 인터페이스는 hasNext()와 next() 메서드를 제공합니다. ArrayList는 인덱스 기반 Iterator를, LinkedList는 노드 링크 기반 Iterator를, HashSet은 버킷 기반 Iterator를 내부적으로 구현합니다. for-each 문은 컴파일러에 의해 Iterator 사용 코드로 변환됩니다.

**Q5.** Spring의 @EventListener는 어떤 디자인 패턴을 구현한 것인가요? 동작 원리를 설명해주세요.

> @EventListener는 Observer 패턴(더 정확히는 Pub-Sub 패턴)의 구현입니다. 동작 원리는 다음과 같습니다. 먼저 ApplicationEventPublisher의 publishEvent() 메서드로 이벤트 객체를 발행합니다. Spring의 ApplicationEventMulticaster가 이벤트를 받아 해당 이벤트 타입을 파라미터로 받는 @EventListener 메서드들을 찾습니다. 찾은 모든 Listener 메서드를 호출하여 이벤트를 전달합니다. 이를 통해 이벤트 발행자는 구독자를 모르고, 구독자도 발행자를 모르는 느슨한 결합이 달성됩니다.

## 비교/구분 (6~9)

**Q6.** Strategy 패턴과 Template Method 패턴의 차이를 설명해주세요.

> 두 패턴 모두 알고리즘의 변경 가능성을 다루지만 접근 방식이 다릅니다. Strategy 패턴은 위임(delegation)을 사용합니다. 인터페이스를 구현한 별도 객체를 조합(has-a)하여 사용하므로, 런타임에 자유롭게 전략을 교체할 수 있습니다. Template Method 패턴은 상속(inheritance)을 사용합니다. 상위 클래스가 알고리즘 골격을 정의하고 서브클래스(is-a)가 세부를 구현하므로, 컴파일 시점에 구현이 결정됩니다. 일반적으로 Strategy가 더 유연하여 선호되며, Spring DI가 Strategy 패턴 기반입니다.

**Q7.** @EventListener와 @TransactionalEventListener의 차이를 설명해주세요.

> @EventListener는 이벤트가 발행되면 즉시 실행되며, 발행자의 트랜잭션 내에서 동작합니다. 따라서 Listener에서 예외가 발생하면 발행자의 트랜잭션도 롤백됩니다. @TransactionalEventListener는 트랜잭션의 특정 단계(기본값: AFTER_COMMIT)에서 실행됩니다. 트랜잭션이 성공적으로 커밋된 후 실행되므로, Listener 실패가 원래 트랜잭션에 영향을 주지 않습니다. 예를 들어 주문 생성 후 알림 발송은 @TransactionalEventListener가 적합합니다. 주문은 확실히 저장된 후에 알림을 보내야 하고, 알림 실패가 주문을 취소시키면 안 되기 때문입니다.

**Q8.** Observer 패턴에서 강한 결합과 느슨한 결합의 차이를 예시를 들어 설명해주세요.

> 강한 결합은 OrderService가 EmailService, PointService, PushService를 직접 의존하는 경우입니다. 새 기능(예: 쿠폰 발급) 추가 시 OrderService를 수정해야 하고, 각 서비스를 모두 Mock해야 테스트가 어렵습니다. 느슨한 결합은 OrderService가 이벤트만 발행하고, 각 기능이 @EventListener로 독립적으로 구독하는 경우입니다. 새 기능 추가 시 새 Listener만 만들면 되고, OrderService는 수정이 불필요합니다. 각 Listener도 독립적으로 테스트할 수 있습니다. 이것이 Observer 패턴이 결합도를 낮추는 방법입니다.

**Q9.** Strategy 패턴과 Factory Method 패턴의 차이를 설명해주세요.

> Strategy 패턴은 "어떤 알고리즘을 사용할 것인가"에 관한 패턴입니다. 클라이언트가 전략 객체를 주입받아 동일한 인터페이스로 다른 알고리즘을 실행합니다. Factory Method 패턴은 "어떤 객체를 생성할 것인가"에 관한 패턴입니다. 팩토리가 조건에 따라 적절한 구체 클래스의 인스턴스를 생성하여 반환합니다. Strategy는 행동(알고리즘)의 교체에 초점을 맞추고, Factory Method는 생성(인스턴스화)의 위임에 초점을 맞춥니다. 두 패턴을 결합하여, Factory가 적절한 Strategy 객체를 생성하여 반환하는 것도 일반적인 패턴입니다.

## 심화/실무 (10~12)

**Q10.** Spring에서 Strategy 패턴이 활용되는 대표적인 사례 3가지를 설명해주세요.

> 첫째, PlatformTransactionManager입니다. 트랜잭션 관리 전략을 인터페이스로 추상화하여, JDBC면 DataSourceTransactionManager, JPA면 JpaTransactionManager를 주입합니다. 기술 변경 시 구현체만 교체하면 됩니다. 둘째, ViewResolver입니다. 뷰 렌더링 전략을 추상화하여, JSP면 InternalResourceViewResolver, Thymeleaf면 ThymeleafViewResolver를 사용합니다. 셋째, HttpMessageConverter입니다. HTTP 메시지 변환 전략으로, JSON이면 MappingJackson2HttpMessageConverter, XML이면 Jaxb2RootElementHttpMessageConverter가 동작합니다. 모두 인터페이스에 의존하고 DI로 구현체를 주입받는 Strategy 패턴입니다.

**Q11.** JdbcTemplate이 Template Method 패턴을 사용하는 것의 장점을 구체적으로 설명해주세요.

> 첫째, 반복 코드 제거입니다. Connection 획득, PreparedStatement 생성, ResultSet 처리, 자원 close, 예외 처리 등의 JDBC boilerplate를 Template이 모두 처리합니다. 둘째, 자원 누수 방지입니다. try-finally에서 자원을 닫는 것을 개발자가 실수할 수 있지만, Template이 자동으로 처리합니다. 셋째, 예외 변환입니다. JDBC의 checked exception인 SQLException을 Spring의 unchecked DataAccessException 계층으로 변환하여 일관된 예외 처리가 가능합니다. 넷째, 개발자는 핵심 로직(SQL, RowMapper)에만 집중할 수 있어 생산성이 향상됩니다.

**Q12.** Spring Event를 활용하여 서비스 간 결합도를 낮추는 방법을 구체적인 시나리오로 설명해주세요.

> 주문 완료 시 이메일 발송, 포인트 적립, 재고 차감, 알림 발송이 필요한 시나리오를 가정합니다. Event 없이 OrderService가 4개의 서비스를 직접 호출하면 강하게 결합됩니다. Event를 사용하면 OrderService는 주문 저장 후 OrderCreatedEvent만 발행합니다. 이메일, 포인트, 재고, 알림 각각의 @EventListener가 독립적으로 이벤트를 처리합니다. 새로운 요구사항(예: 쿠폰 발급)이 추가되면 새 Listener만 만들면 됩니다. 외부 알림은 @TransactionalEventListener(AFTER_COMMIT)로 트랜잭션 커밋 후에만 발송하여 데이터 정합성도 보장할 수 있습니다.

## 꼬리질문 대비 (13~15)

**Q13.** Spring에서 같은 이벤트를 여러 Listener가 처리할 때, 실행 순서를 제어할 수 있나요?

> 네, @Order 어노테이션을 사용하여 Listener의 실행 순서를 제어할 수 있습니다. @Order(1)이 @Order(2)보다 먼저 실행됩니다. 또는 Ordered 인터페이스를 구현하여 getOrder() 메서드에서 순서 값을 반환할 수 있습니다. 다만, 기본적으로 @EventListener는 동기적으로 순차 실행되므로, 순서에 의존적인 설계보다는 각 Listener가 독립적으로 동작하도록 설계하는 것이 더 좋습니다. 비동기 처리가 필요하면 @Async와 함께 사용할 수 있지만, 이 경우 실행 순서를 보장할 수 없습니다.

**Q14.** Template Method 패턴의 단점은 무엇이며, 이를 어떻게 보완할 수 있나요?

> Template Method 패턴의 단점은 첫째, 상속 기반이므로 Java의 단일 상속 제약을 받습니다. 이미 다른 클래스를 상속하고 있으면 적용할 수 없습니다. 둘째, 상위 클래스의 변경이 모든 서브클래스에 영향을 미칩니다(취약한 기반 클래스 문제). 셋째, 서브클래스가 많아지면 클래스 계층이 복잡해집니다. 이를 보완하는 방법으로 Strategy 패턴을 사용할 수 있습니다. 상속 대신 조합(composition)을 사용하여 알고리즘의 일부를 인터페이스로 주입받으면 더 유연합니다. 실제로 JdbcTemplate은 RowMapper를 콜백으로 받아 Strategy 패턴 방식으로도 활용합니다.

**Q15.** Java Stream API와 Iterator 패턴의 관계를 설명해주세요.

> Stream API는 Iterator 패턴의 발전된 형태로 볼 수 있습니다. 기존 Iterator는 외부 반복(external iteration)으로, 개발자가 while 루프로 hasNext()/next()를 직접 호출합니다. Stream은 내부 반복(internal iteration)으로, 반복 로직을 라이브러리가 처리하고 개발자는 수행할 작업(filter, map, collect)만 선언합니다. 또한 Stream은 지연 평가(lazy evaluation)로 중간 연산은 최종 연산이 호출될 때 실행되어 효율적입니다. Collection의 stream() 메서드는 내부적으로 Iterator 기반의 Spliterator를 사용하여 Stream을 생성합니다.
