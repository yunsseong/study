# 3. Spring MVC

---

## Spring MVC 요청 처리 흐름

Spring MVC의 핵심은 **DispatcherServlet**이 모든 요청을 받아서 적절한 곳으로 분배하는 것이다.

```
클라이언트 요청: GET /api/users/1

┌─────────────────────────────────────────────────────────────────┐
│                        Spring MVC 흐름                          │
│                                                                 │
│  [클라이언트]                                                    │
│      │ ① HTTP 요청                                              │
│      ↓                                                          │
│  [DispatcherServlet]  ← 모든 요청의 진입점 (Front Controller)    │
│      │                                                          │
│      │ ② 핸들러 검색                                             │
│      ↓                                                          │
│  [HandlerMapping]  → "이 URL은 어떤 Controller가 처리하지?"       │
│      │               → UserController.getUser() 찾음             │
│      │                                                          │
│      │ ③ 핸들러 어댑터 조회                                       │
│      ↓                                                          │
│  [HandlerAdapter]  → "이 핸들러를 어떻게 실행하지?"               │
│      │               → RequestMappingHandlerAdapter 사용          │
│      │                                                          │
│      │ ④ 핸들러(컨트롤러) 실행                                    │
│      ↓                                                          │
│  [Controller]  → UserController.getUser(1) 실행                  │
│      │           → Service → Repository → DB                     │
│      │                                                          │
│      │ ⑤ 응답 반환                                               │
│      ↓                                                          │
│  [ViewResolver]  → JSON 응답이면 MessageConverter가 처리          │
│      │             → HTML이면 ViewResolver가 View를 찾아 렌더링    │
│      │                                                          │
│      │ ⑥ 응답 전송                                               │
│      ↓                                                          │
│  [클라이언트]  ← HTTP 응답 수신                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### REST API 기준 상세 흐름

```
① 요청 수신
   GET /api/users/1
   Accept: application/json

② HandlerMapping
   @GetMapping("/api/users/{id}") 매핑 찾음
   → UserController.getUser() 메서드 결정

③ HandlerAdapter
   RequestMappingHandlerAdapter 선택
   → @PathVariable, @RequestBody 등 파라미터 바인딩 준비

④ Controller 실행
   UserController.getUser(1L) 호출
   → UserService.findById(1L)
   → UserRepository.findById(1L)
   → User 객체 반환

⑤ 응답 변환
   @RestController이므로 HttpMessageConverter 동작
   → MappingJackson2HttpMessageConverter
   → User 객체 → JSON 문자열 변환
   → {"id": 1, "name": "홍길동", "email": "hong@example.com"}

⑥ 응답 전송
   HTTP/1.1 200 OK
   Content-Type: application/json
   {"id": 1, "name": "홍길동", "email": "hong@example.com"}
```

---

## DispatcherServlet 역할

### Front Controller 패턴

```
Front Controller 패턴 적용 전:
  /users   → UserServlet
  /orders  → OrderServlet
  /products → ProductServlet
  (서블릿마다 공통 로직 반복)

Front Controller 패턴 적용 후:
  모든 요청 → DispatcherServlet → 적절한 Controller로 분배
  (공통 로직은 DispatcherServlet에서 한 번만 처리)
```

```
DispatcherServlet의 상속 구조:

HttpServlet (Java EE 표준)
    └── HttpServletBean
         └── FrameworkServlet
              └── DispatcherServlet (Spring)
```

### DispatcherServlet 핵심 역할

| 역할 | 설명 |
|------|------|
| **요청 접수** | 모든 HTTP 요청을 최초로 받음 |
| **핸들러 조회** | HandlerMapping으로 적절한 Controller 메서드 찾기 |
| **핸들러 실행** | HandlerAdapter로 Controller 메서드 실행 |
| **예외 처리** | HandlerExceptionResolver로 예외 처리 |
| **응답 생성** | ViewResolver 또는 MessageConverter로 응답 생성 |

```
Spring Boot에서의 DispatcherServlet:

# 자동 설정됨 (별도 web.xml 불필요)
# URL 패턴: "/" (모든 요청)
# 내장 Tomcat 위에서 동작
```

---

## Filter vs Interceptor vs AOP 비교

### 전체 실행 순서

```
[클라이언트 요청]
       │
       ↓
┌─────────────────┐
│   Filter        │ ← 서블릿 컨테이너(Tomcat) 레벨
│  (doFilter)     │   Spring 밖에서 동작
└───────┬─────────┘
        │
        ↓
┌─────────────────┐
│ DispatcherServlet│
└───────┬─────────┘
        │
        ↓
┌─────────────────┐
│  Interceptor     │ ← Spring MVC 레벨
│  (preHandle)     │   Spring 안에서 동작
└───────┬─────────┘
        │
        ↓
┌─────────────────┐
│  AOP             │ ← Spring Bean 레벨
│  (@Around)       │   메서드 실행 전후
└───────┬─────────┘
        │
        ↓
┌─────────────────┐
│  Controller      │ ← 실제 비즈니스 로직
│  (핸들러 메서드)  │
└───────┬─────────┘
        │
        ↓ (응답 방향 - 역순)
```

### 비교표

| 항목 | Filter | Interceptor | AOP |
|------|--------|-------------|-----|
| **관리 주체** | 서블릿 컨테이너 (Tomcat) | Spring MVC | Spring |
| **실행 시점** | DispatcherServlet 전/후 | Controller 전/후 | 메서드 전/후 |
| **접근 가능 정보** | ServletRequest/Response | HttpServletRequest + Handler 정보 | JoinPoint (메서드 인자 등) |
| **Spring Bean 접근** | 직접 불가 (DelegatingFilterProxy 필요) | 가능 | 가능 |
| **용도** | 인코딩, CORS, 보안 헤더 | 인증/인가, 로깅 | 트랜잭션, 로깅, 권한 |
| **예외 처리** | try-catch 직접 | afterCompletion에서 처리 | @ControllerAdvice |

### Filter 구현

```java
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info("[Filter] {} {}", req.getMethod(), req.getRequestURI());

        chain.doFilter(request, response);  // 다음 Filter 또는 DispatcherServlet으로

        log.info("[Filter] 응답 완료");
    }
}
```

### Interceptor 구현

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null) {
            response.setStatus(401);
            return false;  // Controller까지 가지 않음
        }
        return true;  // 계속 진행
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        // Controller 실행 후, 응답 전
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 응답 완료 후 (예외 발생해도 실행)
    }
}

// Interceptor 등록
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")           // 적용할 경로
                .excludePathPatterns("/api/auth/**");  // 제외할 경로
    }
}
```

### 사용 가이드

```
언제 무엇을 쓸까?

Filter:
  - 모든 요청에 대한 전처리 (인코딩, CORS)
  - Spring과 무관한 처리
  - 요청/응답 자체를 변경해야 할 때

Interceptor:
  - Spring MVC에 특화된 전후 처리
  - Controller 정보(Handler)가 필요할 때
  - 인증/인가 체크

AOP:
  - 비즈니스 로직 레벨의 공통 처리
  - 트랜잭션, 메서드 실행 시간 측정
  - 특정 어노테이션이 붙은 메서드에 적용
```

---

## @Controller vs @RestController

### @Controller

```java
@Controller
public class PageController {

    @GetMapping("/users")
    public String userListPage(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";  // → ViewResolver가 user/list.html 찾아서 렌더링
    }

    @GetMapping("/api/users")
    @ResponseBody  // JSON으로 반환하려면 이것 필요
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

### @RestController

```java
@RestController  // = @Controller + @ResponseBody
public class UserApiController {

    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userService.findAll();  // 자동으로 JSON 변환
    }

    @GetMapping("/api/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);  // 자동으로 JSON 변환
    }
}
```

| 항목 | @Controller | @RestController |
|------|-------------|-----------------|
| **반환** | View 이름 (HTML) | 데이터 (JSON/XML) |
| **@ResponseBody** | 메서드마다 필요 | 클래스에 자동 적용 |
| **용도** | SSR (서버 사이드 렌더링) | REST API |
| **View 필요** | 필요 (Thymeleaf 등) | 불필요 |

> **현재 트렌드**: 프론트엔드(React, Vue)와 백엔드(Spring Boot)를 분리하는 구조에서는
> @RestController를 주로 사용한다.

---

## 어노테이션

### 요청 매핑 어노테이션

```java
@RestController
@RequestMapping("/api/users")  // 공통 경로
public class UserController {

    @GetMapping            // GET /api/users
    public List<User> getAll() { ... }

    @GetMapping("/{id}")   // GET /api/users/1
    public User getById(@PathVariable Long id) { ... }

    @PostMapping           // POST /api/users
    public User create(@RequestBody @Valid UserCreateRequest request) { ... }

    @PutMapping("/{id}")   // PUT /api/users/1
    public User update(@PathVariable Long id,
                        @RequestBody @Valid UserUpdateRequest request) { ... }

    @DeleteMapping("/{id}")  // DELETE /api/users/1
    public void delete(@PathVariable Long id) { ... }
}
```

### 파라미터 바인딩 어노테이션

```java
// 1. @PathVariable - URL 경로의 변수
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) { ... }
// GET /users/1 → id = 1

// 2. @RequestParam - 쿼리 파라미터
@GetMapping("/users")
public List<User> search(@RequestParam String name,
                          @RequestParam(defaultValue = "0") int page) { ... }
// GET /users?name=홍길동&page=0

// 3. @RequestBody - 요청 본문 (JSON → 객체)
@PostMapping("/users")
public User create(@RequestBody UserCreateRequest request) { ... }
// { "name": "홍길동", "email": "hong@example.com" }

// 4. @RequestHeader - HTTP 헤더
@GetMapping("/users/me")
public User getMe(@RequestHeader("Authorization") String token) { ... }
```

---

## 예외 처리

### @ExceptionHandler

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);  // 없으면 예외 발생
    }

    // 이 Controller 안에서 발생한 예외만 처리
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException e) {
        return new ErrorResponse("USER_NOT_FOUND", e.getMessage());
    }
}
```

### @ControllerAdvice - 전역 예외 처리

```java
@RestControllerAdvice  // 모든 Controller의 예외를 처리
public class GlobalExceptionHandler {

    // 404 - 리소스 없음
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException e) {
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    // 400 - 입력값 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", message);
    }

    // 500 - 서버 에러 (최후의 방어선)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception e) {
        log.error("서버 에러 발생", e);
        return new ErrorResponse("INTERNAL_ERROR", "서버 에러가 발생했습니다.");
    }
}
```

```java
// 공통 에러 응답 DTO
public record ErrorResponse(String code, String message) {}
```

```
예외 처리 우선순위:

1. Controller 내 @ExceptionHandler (해당 Controller 범위)
2. @ControllerAdvice 내 @ExceptionHandler (글로벌 범위)
3. Spring 기본 예외 처리 (BasicErrorController)

→ 실무에서는 @RestControllerAdvice로 전역 처리하는 것이 일반적
```

---

## HTTP 상태 코드와 REST API 설계

### 주요 상태 코드

| 코드 | 의미 | 사용 시점 |
|------|------|----------|
| **200** | OK | 조회 성공, 수정 성공 |
| **201** | Created | 리소스 생성 성공 |
| **204** | No Content | 삭제 성공 (응답 본문 없음) |
| **400** | Bad Request | 입력값 검증 실패 |
| **401** | Unauthorized | 인증 실패 (로그인 필요) |
| **403** | Forbidden | 인가 실패 (권한 없음) |
| **404** | Not Found | 리소스 없음 |
| **409** | Conflict | 중복 등 충돌 |
| **500** | Internal Server Error | 서버 에러 |

### REST API 설계 원칙

```
리소스 중심 URL 설계:

좋은 예:
  GET    /api/users          ← 사용자 목록 조회
  GET    /api/users/1        ← 사용자 1번 조회
  POST   /api/users          ← 사용자 생성
  PUT    /api/users/1        ← 사용자 1번 전체 수정
  PATCH  /api/users/1        ← 사용자 1번 부분 수정
  DELETE /api/users/1        ← 사용자 1번 삭제

나쁜 예:
  GET    /api/getUser?id=1   ← 동사 사용 X
  POST   /api/createUser     ← 동사 사용 X
  POST   /api/deleteUser/1   ← HTTP 메서드 의미 무시
```

```
계층적 리소스 설계:

GET  /api/users/1/orders        ← 사용자 1의 주문 목록
GET  /api/users/1/orders/5      ← 사용자 1의 주문 5번
POST /api/users/1/orders        ← 사용자 1의 주문 생성
```

### 표준 응답 형식

```java
// 성공 응답
@GetMapping("/api/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok(user);
    // 200 OK + User JSON
}

@PostMapping("/api/users")
public ResponseEntity<User> createUser(@RequestBody @Valid UserCreateRequest request) {
    User user = userService.create(request);
    URI location = URI.create("/api/users/" + user.getId());
    return ResponseEntity.created(location).body(user);
    // 201 Created + Location 헤더 + User JSON
}

@DeleteMapping("/api/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
    // 204 No Content
}
```

---

## Spring Boot 내장 Tomcat 동작

### 전통적 방식 vs Spring Boot

```
전통적 방식:
  1. Tomcat 별도 설치
  2. war 파일 빌드
  3. Tomcat의 webapps/에 war 배포
  4. Tomcat 시작

Spring Boot 방식:
  1. spring-boot-starter-web 의존성 추가 (내장 Tomcat 포함)
  2. jar 파일 빌드
  3. java -jar app.jar (Tomcat이 앱 안에서 시작)
```

### 내장 Tomcat 시작 과정

```
java -jar app.jar 실행

① SpringApplication.run() 호출
     │
② ApplicationContext 생성
     │
③ Auto Configuration 동작
   → ServletWebServerFactoryAutoConfiguration
   → TomcatServletWebServerFactory 생성
     │
④ 내장 Tomcat 인스턴스 생성 및 시작
   → 포트 8080 (기본) 리스닝
     │
⑤ DispatcherServlet 등록
   → URL 패턴 "/" 매핑
     │
⑥ 요청 수신 대기
```

### Tomcat 설정

```yaml
# application.yml
server:
  port: 8080                          # 포트
  tomcat:
    threads:
      max: 200                        # 최대 스레드 수
      min-spare: 10                   # 최소 유지 스레드 수
    max-connections: 8192             # 최대 커넥션 수
    accept-count: 100                 # 대기열 크기
    connection-timeout: 20000         # 커넥션 타임아웃 (ms)
```

```
요청 처리 흐름:

[클라이언트 요청] → [Acceptor Thread] → [Connection Queue]
                                              │
                                    max-connections(8192) 초과?
                                    ├── YES → accept-count(100) 대기열
                                    │         대기열도 초과? → 연결 거부
                                    └── NO  → [Worker Thread Pool]
                                                    │
                                           max-threads(200) 모두 사용 중?
                                           ├── YES → 대기열에서 대기
                                           └── NO  → 스레드 할당 → 요청 처리
```

---

## 실무 연결 / Spring Boot 연결

### API 응답 표준화

```java
// 표준 응답 래퍼
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorInfo error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message));
    }
}

public record ErrorInfo(String code, String message) {}

// 사용
@GetMapping("/{id}")
public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
    return ApiResponse.ok(userService.findById(id));
}
```

```json
// 성공 응답
{
  "success": true,
  "data": { "id": 1, "name": "홍길동" },
  "error": null
}

// 실패 응답
{
  "success": false,
  "data": null,
  "error": { "code": "NOT_FOUND", "message": "사용자를 찾을 수 없습니다" }
}
```

### CORS 설정

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

---

## 면접 핵심 정리

**Q: Spring MVC의 요청 처리 흐름을 설명해주세요**
> 모든 요청은 DispatcherServlet이 받습니다.
> HandlerMapping으로 요청 URL에 맞는 Controller 메서드를 찾고,
> HandlerAdapter를 통해 해당 메서드를 실행합니다.
> @RestController의 경우 반환값을 HttpMessageConverter(Jackson)가 JSON으로 변환하여 응답합니다.
> DispatcherServlet이 중앙에서 요청을 분배하는 Front Controller 패턴입니다.

**Q: Filter와 Interceptor의 차이는?**
> Filter는 서블릿 컨테이너(Tomcat) 레벨에서 동작하며 DispatcherServlet 전후에 실행됩니다.
> Interceptor는 Spring MVC 레벨에서 동작하며 Controller 전후에 실행됩니다.
> Filter는 ServletRequest/Response에 접근하고,
> Interceptor는 추가로 Handler(Controller) 정보에 접근할 수 있습니다.
> 인코딩, CORS 같은 저수준 처리는 Filter,
> 인증/인가 같은 비즈니스 관련 처리는 Interceptor를 사용합니다.

**Q: @Controller와 @RestController의 차이는?**
> @Controller는 View 이름을 반환하여 ViewResolver가 HTML을 렌더링합니다.
> @RestController는 @Controller + @ResponseBody로,
> 반환 객체를 HttpMessageConverter가 JSON으로 직접 변환하여 응답합니다.
> 현재 프론트엔드와 백엔드를 분리하는 구조에서는 @RestController를 주로 사용합니다.

**Q: Spring Boot에서 예외 처리는 어떻게 하나요?**
> @RestControllerAdvice와 @ExceptionHandler를 사용하여 전역 예외 처리를 합니다.
> 커스텀 예외별로 적절한 HTTP 상태 코드와 에러 응답을 반환합니다.
> 예를 들어 ResourceNotFoundException은 404, MethodArgumentNotValidException은 400을 반환합니다.
> 최하위에 Exception.class 핸들러를 두어 예상치 못한 에러도 처리합니다.

**Q: 내장 Tomcat의 스레드 풀은 어떻게 동작하나요?**
> Spring Boot 내장 Tomcat은 기본 200개의 워커 스레드 풀을 관리합니다.
> 요청이 들어오면 스레드 풀에서 스레드를 할당하여 처리하고, 완료 후 반환합니다.
> 모든 스레드가 사용 중이면 대기열(accept-count)에서 대기하며,
> 대기열도 초과하면 연결을 거부합니다.
> server.tomcat.threads.max로 최대 스레드 수를 조정할 수 있습니다.
