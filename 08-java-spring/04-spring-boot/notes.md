# Spring Boot

## Spring vs Spring Boot

```
Spring: 강력하지만 설정이 복잡 (XML, 수동 설정 많음)
Spring Boot: Spring을 쉽게 쓰게 해주는 도구

Spring Boot가 해주는 것:
├── 자동 설정 (Auto Configuration)
├── 내장 서버 (Tomcat)
├── 의존성 관리 (Starter)
├── 설정 파일 간소화 (application.yml)
└── 빌드/배포 간편화 (Jar 실행)
```

---

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── DemoApplication.java          // 메인 클래스
├── controller/                   // 웹 요청 처리
│   └── UserController.java
├── service/                      // 비즈니스 로직
│   └── UserService.java
├── repository/                   // 데이터 접근
│   └── UserRepository.java
├── entity/                       // DB 엔티티
│   └── User.java
├── dto/                          // 데이터 전송 객체
│   ├── UserRequestDto.java
│   └── UserResponseDto.java
├── exception/                    // 예외 처리
│   └── GlobalExceptionHandler.java
└── config/                       // 설정
    └── SecurityConfig.java

src/main/resources/
├── application.yml               // 설정 파일
└── static/                       // 정적 파일

src/test/java/                    // 테스트
```

---

## @SpringBootApplication

```java
@SpringBootApplication  // 아래 3개를 합친 것
// @SpringBootConfiguration: 설정 클래스
// @EnableAutoConfiguration: 자동 설정 활성화
// @ComponentScan: 빈 자동 스캔
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## application.yml

```yaml
# 서버 설정
server:
  port: 8080

# 데이터베이스
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: update  # create | create-drop | update | validate | none
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  # Redis
  data:
    redis:
      host: localhost
      port: 6379

# 로깅
logging:
  level:
    root: INFO
    com.example: DEBUG
    org.hibernate.SQL: DEBUG
```

### 프로파일 (환경별 설정)

```yaml
# application.yml (공통)
spring:
  profiles:
    active: dev  # 기본 프로파일

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb_dev

---
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://production-db:3306/mydb_prod

# 실행 시 프로파일 지정
# java -jar app.jar --spring.profiles.active=prod
```

---

## Gradle 의존성

```groovy
// build.gradle
dependencies {
    // Spring Boot 기본
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Security
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // DB
    runtimeOnly 'com.mysql:mysql-connector-j'

    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## Lombok

```java
// 반복 코드를 어노테이션으로 자동 생성

@Getter @Setter        // getter/setter 자동 생성
@NoArgsConstructor     // 기본 생성자
@AllArgsConstructor    // 모든 필드 생성자
@RequiredArgsConstructor // final 필드 생성자 (DI에 사용)
@Builder               // 빌더 패턴
@ToString              // toString()
@EqualsAndHashCode     // equals(), hashCode()
@Slf4j                 // Logger 자동 생성

// 실전 조합
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}

// DTO
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;

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

## 면접 예상 질문

1. **Spring과 Spring Boot의 차이는?**
   - Spring Boot: 자동 설정, 내장 서버, Starter 의존성 → 설정 간소화

2. **@SpringBootApplication이 하는 일은?**
   - Configuration + AutoConfiguration + ComponentScan을 합친 것

3. **ddl-auto 옵션의 차이는?**
   - create: 매번 재생성 / update: 변경분 반영 / validate: 검증만 / none: 아무것도 안 함

4. **프로파일이란?**
   - 환경별(dev, prod) 설정을 분리하여 관리
