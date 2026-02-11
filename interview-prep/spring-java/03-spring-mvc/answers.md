# Spring MVC 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Spring MVC의 요청 처리 흐름을 DispatcherServlet 중심으로 설명해주세요.

> 모든 HTTP 요청은 먼저 DispatcherServlet이 받습니다. DispatcherServlet은 HandlerMapping을 통해 요청 URL에 맞는 Controller 메서드를 찾고, HandlerAdapter를 통해 해당 메서드를 실행합니다. Controller에서 비즈니스 로직(Service -> Repository -> DB)을 처리한 후 결과를 반환합니다. @RestController의 경우 HttpMessageConverter(Jackson)가 반환 객체를 JSON으로 변환하여 응답하고, @Controller의 경우 ViewResolver가 View를 찾아 HTML을 렌더링합니다. 이처럼 DispatcherServlet이 중앙에서 요청을 분배하는 것이 Front Controller 패턴입니다.

**Q2.** DispatcherServlet이란 무엇이며, Front Controller 패턴이 왜 필요한지 설명해주세요.

> DispatcherServlet은 Spring MVC에서 모든 HTTP 요청의 진입점 역할을 하는 서블릿입니다. HttpServlet을 상속받아 구현되어 있습니다. Front Controller 패턴이 없으면 URL마다 별도의 서블릿을 만들어야 하고, 인코딩, 인증 등 공통 로직이 각 서블릿에 반복됩니다. Front Controller 패턴을 적용하면 모든 요청이 DispatcherServlet 하나를 거치므로 공통 로직을 한 곳에서 처리할 수 있고, 핸들러 매핑, 예외 처리, 응답 변환 등을 일관되게 관리할 수 있습니다. Spring Boot에서는 별도의 web.xml 없이 자동으로 DispatcherServlet이 등록됩니다.

**Q3.** @RestController와 @Controller의 차이를 설명해주세요.

> @Controller는 View 이름(문자열)을 반환하여 ViewResolver가 해당 이름의 HTML 템플릿(Thymeleaf 등)을 찾아 렌더링합니다. JSON 데이터를 반환하려면 메서드마다 @ResponseBody를 붙여야 합니다. @RestController는 @Controller + @ResponseBody의 조합으로, 클래스의 모든 메서드에 @ResponseBody가 자동 적용됩니다. 반환 객체를 HttpMessageConverter가 JSON으로 직접 변환하여 응답합니다. 현재 프론트엔드(React, Vue)와 백엔드(Spring Boot)를 분리하는 구조에서는 @RestController를 주로 사용합니다.

**Q4.** Spring Boot에서 예외 처리는 어떻게 하나요? @ControllerAdvice를 사용하는 방법을 설명해주세요.

> @RestControllerAdvice와 @ExceptionHandler를 사용하여 전역 예외 처리를 합니다. @RestControllerAdvice 클래스를 만들고, 그 안에 @ExceptionHandler를 붙인 메서드를 정의합니다. 각 메서드는 특정 예외 타입을 파라미터로 받아 적절한 HTTP 상태 코드와 에러 응답 DTO를 반환합니다. 예를 들어 ResourceNotFoundException은 404, MethodArgumentNotValidException(입력값 검증 실패)은 400을 반환합니다. 최하위에 Exception.class 핸들러를 두어 예상치 못한 에러에도 500 응답을 반환하도록 하면, 모든 예외를 일관된 형식으로 처리할 수 있습니다.

**Q5.** REST API 설계 시 URL 설계 원칙을 설명해주세요.

> REST API는 리소스 중심으로 URL을 설계합니다. URL에는 동사가 아닌 명사(리소스)를 사용하고, HTTP 메서드(GET, POST, PUT, DELETE)로 행위를 표현합니다. 예를 들어 사용자 목록 조회는 `GET /api/users`, 생성은 `POST /api/users`, 특정 사용자 조회는 `GET /api/users/{id}`, 수정은 `PUT /api/users/{id}`, 삭제는 `DELETE /api/users/{id}`로 설계합니다. `GET /api/getUser`처럼 동사를 URL에 넣는 것은 나쁜 설계입니다. 계층적 관계가 있으면 `GET /api/users/1/orders`처럼 표현합니다. 적절한 HTTP 상태 코드(200, 201, 204, 400, 404 등)도 함께 사용해야 합니다.

## 비교/구분 (6~9)

**Q6.** Filter와 Interceptor의 차이를 설명해주세요.

> Filter는 서블릿 컨테이너(Tomcat) 레벨에서 동작하며, DispatcherServlet 전후에 실행됩니다. ServletRequest/Response에 접근할 수 있고, Spring Bean에 직접 접근하기 어렵습니다(DelegatingFilterProxy 필요). 인코딩, CORS, 보안 헤더 같은 저수준 처리에 적합합니다. Interceptor는 Spring MVC 레벨에서 동작하며, Controller 전후에 실행됩니다. HttpServletRequest에 더해 Handler(Controller) 정보에 접근할 수 있고, Spring Bean을 자유롭게 사용할 수 있습니다. 인증/인가 체크, 로깅 같은 비즈니스 관련 처리에 적합합니다.

**Q7.** @PathVariable, @RequestParam, @RequestBody의 차이를 설명해주세요.

> @PathVariable은 URL 경로의 변수를 바인딩합니다. 예를 들어 `@GetMapping("/users/{id}")`에서 id 값을 추출합니다. @RequestParam은 쿼리 파라미터를 바인딩합니다. `GET /users?name=홍길동&page=0`에서 name과 page 값을 추출하며, defaultValue를 설정할 수 있습니다. @RequestBody는 HTTP 요청 본문(body)의 JSON을 Java 객체로 변환합니다. 주로 POST, PUT 요청에서 사용하며, Jackson의 HttpMessageConverter가 JSON을 역직렬화합니다. 정리하면, @PathVariable은 경로, @RequestParam은 쿼리스트링, @RequestBody는 요청 본문에서 데이터를 추출합니다.

**Q8.** Filter, Interceptor, AOP의 실행 순서와 각각의 적절한 사용 시점을 설명해주세요.

> 실행 순서는 Filter -> DispatcherServlet -> Interceptor(preHandle) -> AOP -> Controller 순이며, 응답 시에는 역순으로 실행됩니다. Filter는 모든 요청에 대한 전처리(인코딩, CORS)나 요청/응답 자체를 변경해야 할 때 사용합니다. Interceptor는 Spring MVC에 특화된 전후 처리로, Controller 정보(Handler)가 필요하거나 인증/인가 체크에 사용합니다. AOP는 비즈니스 로직 레벨의 공통 처리로, 트랜잭션 관리, 메서드 실행 시간 측정, 특정 어노테이션이 붙은 메서드에 적용할 때 사용합니다.

**Q9.** ResponseEntity를 사용하는 것과 직접 객체를 반환하는 것의 차이를 설명해주세요.

> 직접 객체를 반환하면 항상 HTTP 200 OK 상태 코드와 함께 JSON으로 변환됩니다. ResponseEntity를 사용하면 HTTP 상태 코드, 헤더, 본문을 세밀하게 제어할 수 있습니다. 예를 들어, 리소스 생성 시 `ResponseEntity.created(location).body(user)`로 201 Created와 Location 헤더를 함께 반환하거나, 삭제 시 `ResponseEntity.noContent().build()`로 204 No Content를 반환할 수 있습니다. REST API의 적절한 HTTP 상태 코드 사용을 위해 ResponseEntity를 사용하는 것이 권장됩니다.

## 심화/실무 (10~13)

**Q10.** Spring Boot 내장 Tomcat의 스레드 풀은 어떻게 동작하며, 주요 설정 옵션은 무엇인가요?

> Spring Boot 내장 Tomcat은 기본 200개의 워커 스레드 풀을 관리합니다. 요청이 들어오면 Acceptor Thread가 연결을 수락하고, 최대 커넥션 수(기본 8192)를 확인합니다. 커넥션이 수락되면 Worker Thread Pool에서 스레드를 할당하여 요청을 처리합니다. 모든 스레드가 사용 중이면 대기열(accept-count, 기본 100)에서 대기하고, 대기열도 초과하면 연결을 거부합니다. 주요 설정으로는 `server.tomcat.threads.max`(최대 스레드 수), `server.tomcat.threads.min-spare`(최소 유지 스레드 수), `server.tomcat.max-connections`(최대 커넥션 수), `server.tomcat.accept-count`(대기열 크기)가 있습니다.

**Q11.** API 응답을 표준화하려면 어떻게 설계하시겠습니까?

> 표준 응답 래퍼 클래스를 만들어 모든 API 응답을 통일합니다. 성공 시에는 `{"success": true, "data": {...}, "error": null}` 형태로, 실패 시에는 `{"success": false, "data": null, "error": {"code": "NOT_FOUND", "message": "사용자를 찾을 수 없습니다"}}` 형태로 응답합니다. Java의 record를 활용하여 `ApiResponse<T>` 클래스를 만들고, `ApiResponse.ok(data)`와 `ApiResponse.error(code, message)` 같은 정적 팩토리 메서드를 제공합니다. @ControllerAdvice의 예외 처리에서도 동일한 형식을 사용하면, 프론트엔드에서 일관된 응답 파싱이 가능해집니다.

**Q12.** HttpMessageConverter의 역할은 무엇이며, @RestController에서 JSON 응답이 만들어지는 과정을 설명해주세요.

> HttpMessageConverter는 HTTP 요청 본문을 Java 객체로 변환하거나(역직렬화), Java 객체를 HTTP 응답 본문으로 변환(직렬화)하는 역할을 합니다. @RestController에서 메서드가 객체를 반환하면, Spring은 Accept 헤더와 반환 타입을 보고 적절한 MessageConverter를 선택합니다. JSON의 경우 MappingJackson2HttpMessageConverter가 선택되어 Jackson 라이브러리로 Java 객체를 JSON 문자열로 변환합니다. 이것이 @ResponseBody(또는 @RestController)가 붙었을 때 자동으로 JSON 응답이 만들어지는 과정입니다.

**Q13.** CORS란 무엇이며, Spring Boot에서 어떻게 설정하나요?

> CORS(Cross-Origin Resource Sharing)는 브라우저가 다른 출처(도메인, 포트)의 리소스에 접근할 수 있도록 허용하는 메커니즘입니다. 예를 들어 프론트엔드가 localhost:3000에서 실행되고 백엔드가 localhost:8080에서 실행되면, 브라우저의 동일 출처 정책(Same-Origin Policy)에 의해 요청이 차단됩니다. Spring Boot에서는 WebMvcConfigurer를 구현하여 addCorsMappings 메서드에서 설정합니다. allowedOrigins로 허용할 출처, allowedMethods로 허용할 HTTP 메서드, allowedHeaders로 허용할 헤더를 지정합니다. 또는 @CrossOrigin 어노테이션을 Controller나 메서드에 직접 붙일 수도 있습니다.

## 꼬리질문 대비 (14~15)

**Q14.** @ExceptionHandler의 우선순위는 어떻게 되나요? Controller 내 핸들러와 @ControllerAdvice 핸들러가 동시에 존재하면 어떻게 되나요?

> 예외 처리 우선순위는 Controller 내 @ExceptionHandler가 가장 높고, 그 다음이 @ControllerAdvice 내 @ExceptionHandler, 마지막이 Spring 기본 예외 처리(BasicErrorController)입니다. 따라서 동일한 예외 타입에 대해 Controller 내 핸들러와 @ControllerAdvice 핸들러가 동시에 존재하면, Controller 내 핸들러가 먼저 처리합니다. 실무에서는 @RestControllerAdvice로 전역 예외 처리를 하고, 특정 Controller에서만 다르게 처리해야 할 때 Controller 내 @ExceptionHandler를 추가하는 방식이 일반적입니다.

**Q15.** Interceptor의 preHandle에서 false를 반환하면 어떻게 되며, afterCompletion은 언제 호출되나요?

> preHandle에서 false를 반환하면 이후 Interceptor의 preHandle과 Controller 실행이 모두 중단됩니다. 보통 인증 실패 시 response에 401 상태 코드를 설정하고 false를 반환하여 요청을 차단합니다. postHandle은 Controller가 정상적으로 실행된 후, 응답이 클라이언트에 전달되기 전에 호출됩니다. afterCompletion은 요청 처리가 완전히 완료된 후에 호출되며, 예외가 발생해도 반드시 실행됩니다. 따라서 리소스 정리나 로깅 같은 작업에 적합합니다. afterCompletion의 Exception 파라미터로 발생한 예외 정보에 접근할 수 있습니다.
