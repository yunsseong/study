# Spring Core (IoC, DI, AOP) 면접 질문

## 기본 개념 (1~5)

**Q1.** IoC(제어의 역전)란 무엇이며, 기존 방식과 어떤 차이가 있는지 설명해주세요.

**Q2.** DI(의존성 주입)란 무엇이고, Spring에서 제공하는 DI 방식 3가지를 설명해주세요.

**Q3.** 생성자 주입을 권장하는 이유를 설명해주세요.

**Q4.** Spring Bean이란 무엇이고, Bean의 생명주기를 설명해주세요.

**Q5.** AOP(관점 지향 프로그래밍)란 무엇이며, 왜 필요한지 설명해주세요.

## 비교/구분 (6~9)

**Q6.** Spring과 Spring Boot의 차이를 설명해주세요.

**Q7.** Bean Scope에서 Singleton과 Prototype의 차이를 설명해주세요.

**Q8.** JDK Dynamic Proxy와 CGLIB의 차이를 설명해주세요.

**Q9.** @Component, @Service, @Repository, @Controller의 차이를 설명해주세요.

## 심화/실무 (10~12)

**Q10.** @Transactional의 동작 원리를 설명해주세요. 같은 클래스 내부에서 @Transactional 메서드를 호출하면 어떤 문제가 발생하나요?

**Q11.** AOP의 핵심 용어(Aspect, Advice, Pointcut, JoinPoint, Proxy)를 설명하고, Spring AOP가 동작하는 흐름을 설명해주세요.

**Q12.** Singleton Bean에서 상태(state)를 가지면 안 되는 이유를 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** 순환 참조(Circular Dependency)란 무엇이며, 생성자 주입에서 어떻게 감지되나요?

**Q14.** 외부 라이브러리의 클래스를 Spring Bean으로 등록하려면 어떻게 해야 하나요?

**Q15.** @Around Advice에서 ProceedingJoinPoint의 역할은 무엇이며, proceed()를 호출하지 않으면 어떻게 되나요?
