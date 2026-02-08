# Spring MVC (REST API)

## 계층형 아키텍처

```
Client
  ↓ HTTP 요청
Controller  (요청/응답 처리, 유효성 검증)
  ↓ DTO
Service     (비즈니스 로직)
  ↓ Entity
Repository  (데이터 접근, DB)
  ↓ SQL
Database

규칙:
- Controller → Service → Repository 방향으로만 호출
- 역방향 호출 금지
- 각 계층은 자신의 역할만 담당
```

---

## Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    // 생성
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 요청 파라미터 받기

```java
// 1. @PathVariable: URL 경로 변수
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) { }
// GET /api/users/1

// 2. @RequestParam: 쿼리 파라미터
@GetMapping
public List<User> searchUsers(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page) { }
// GET /api/users?keyword=john&page=0

// 3. @RequestBody: JSON 본문
@PostMapping
public User createUser(@RequestBody UserCreateRequestDto request) { }
// POST /api/users  {"name": "john", "email": "john@test.com"}
```

---

## DTO 패턴

```java
// Entity를 직접 반환하지 않고 DTO로 변환하여 반환

// 왜 DTO를 쓰나?
// 1. 엔티티 내부 구조 노출 방지 (비밀번호 등)
// 2. API 스펙과 DB 스키마 분리
// 3. 요청/응답별 다른 필드 구성

// 요청 DTO
@Getter
@NoArgsConstructor
public class UserCreateRequestDto {
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다")
    @NotBlank
    private String email;

    @Size(min = 8, message = "비밀번호는 8자 이상")
    private String password;

    public User toEntity(String encodedPassword) {
        return User.builder()
            .name(name)
            .email(email)
            .password(encodedPassword)
            .build();
    }
}

// 응답 DTO
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    // password는 포함하지 않음!

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }
}
```

---

## 유효성 검증 (Validation)

```java
// spring-boot-starter-validation 필요

// 주요 어노테이션
@NotBlank    // null, "", " " 불가
@NotNull     // null 불가
@NotEmpty    // null, "" 불가 (공백은 허용)
@Email       // 이메일 형식
@Size(min=2, max=20)  // 길이 제한
@Min(0) @Max(100)     // 숫자 범위
@Pattern(regexp = "^010-\\d{4}-\\d{4}$")  // 정규식

// Controller에서 @Valid 사용
@PostMapping
public ResponseEntity<UserResponseDto> createUser(
        @Valid @RequestBody UserCreateRequestDto request) {
    // 유효성 실패 시 MethodArgumentNotValidException 발생
}
```

---

## 예외 처리 (Global Exception Handler)

```java
// 커스텀 예외
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("유저를 찾을 수 없습니다. ID: " + id);
    }
}

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("이미 존재하는 이메일입니다: " + email);
    }
}

// 에러 응답 DTO
@Getter @Builder
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
}

// 전역 예외 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .status(404)
                .code("USER_NOT_FOUND")
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.builder()
                .status(409)
                .code("DUPLICATE_EMAIL")
                .message(e.getMessage())
                .build());
    }

    // 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .status(400)
                .code("VALIDATION_ERROR")
                .message(message)
                .build());
    }
}
```

---

## ResponseEntity

```java
// HTTP 상태 코드 + 헤더 + 본문을 직접 제어

// 200 OK
return ResponseEntity.ok(data);

// 201 Created
return ResponseEntity.status(HttpStatus.CREATED).body(data);

// 204 No Content
return ResponseEntity.noContent().build();

// 커스텀 헤더
return ResponseEntity.ok()
    .header("X-Custom-Header", "value")
    .body(data);
```

---

## 면접 예상 질문

1. **@Controller와 @RestController의 차이는?**
   - @Controller: 뷰 반환 / @RestController: JSON 반환 (@ResponseBody 포함)

2. **DTO를 왜 사용하나요?**
   - 엔티티 노출 방지, API 스펙과 DB 분리, 요청/응답별 필드 분리

3. **@Valid는 어떻게 동작하나요?**
   - 요청 DTO의 제약 조건 검증, 실패 시 MethodArgumentNotValidException

4. **@ControllerAdvice란?**
   - 전역 예외 처리, 모든 Controller에서 발생하는 예외를 한 곳에서 처리

5. **계층형 아키텍처를 사용하는 이유는?**
   - 관심사 분리, 유지보수성, 테스트 용이성, 각 계층 독립적 변경 가능
