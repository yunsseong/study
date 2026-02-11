# Spring MVC 면접 질문

## 기본 개념 (1~5)

**Q1.** Spring MVC의 요청 처리 흐름을 DispatcherServlet 중심으로 설명해주세요.

**Q2.** DispatcherServlet이란 무엇이며, Front Controller 패턴이 왜 필요한지 설명해주세요.

**Q3.** @RestController와 @Controller의 차이를 설명해주세요.

**Q4.** Spring Boot에서 예외 처리는 어떻게 하나요? @ControllerAdvice를 사용하는 방법을 설명해주세요.

**Q5.** REST API 설계 시 URL 설계 원칙을 설명해주세요.

## 비교/구분 (6~9)

**Q6.** Filter와 Interceptor의 차이를 설명해주세요.

**Q7.** @PathVariable, @RequestParam, @RequestBody의 차이를 설명해주세요.

**Q8.** Filter, Interceptor, AOP의 실행 순서와 각각의 적절한 사용 시점을 설명해주세요.

**Q9.** ResponseEntity를 사용하는 것과 직접 객체를 반환하는 것의 차이를 설명해주세요.

## 심화/실무 (10~13)

**Q10.** Spring Boot 내장 Tomcat의 스레드 풀은 어떻게 동작하며, 주요 설정 옵션은 무엇인가요?

**Q11.** API 응답을 표준화하려면 어떻게 설계하시겠습니까?

**Q12.** HttpMessageConverter의 역할은 무엇이며, @RestController에서 JSON 응답이 만들어지는 과정을 설명해주세요.

**Q13.** CORS란 무엇이며, Spring Boot에서 어떻게 설정하나요?

## 꼬리질문 대비 (14~15)

**Q14.** @ExceptionHandler의 우선순위는 어떻게 되나요? Controller 내 핸들러와 @ControllerAdvice 핸들러가 동시에 존재하면 어떻게 되나요?

**Q15.** Interceptor의 preHandle에서 false를 반환하면 어떻게 되며, afterCompletion은 언제 호출되나요?
