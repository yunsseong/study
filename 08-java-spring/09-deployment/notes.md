# 배포 (Deployment)

## Docker

### Docker란?

```
애플리케이션과 실행 환경을 컨테이너로 패키징.
"내 PC에서는 되는데..." 문제 해결.

이미지: 실행 환경 + 앱을 담은 템플릿 (읽기 전용)
컨테이너: 이미지를 실행한 인스턴스 (프로세스)

이미지 = 클래스, 컨테이너 = 객체
```

### Dockerfile

```dockerfile
# 1단계: 빌드
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# 2단계: 실행 (멀티 스테이지 빌드 → 이미지 크기 최소화)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker 명령어

```bash
# 이미지 빌드
docker build -t myapp:1.0 .

# 컨테이너 실행
docker run -d -p 8080:8080 --name myapp myapp:1.0

# 컨테이너 확인
docker ps          # 실행 중
docker ps -a       # 전체

# 로그 확인
docker logs myapp
docker logs -f myapp  # 실시간

# 중지/삭제
docker stop myapp
docker rm myapp
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/mydb
    depends_on:
      - db
      - redis

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mydb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

```bash
# 전체 실행
docker-compose up -d

# 전체 중지
docker-compose down

# 로그 확인
docker-compose logs -f app
```

---

## CI/CD (GitHub Actions)

### 개념

```
CI (Continuous Integration): 코드 변경마다 자동 빌드 + 테스트
CD (Continuous Deployment): 자동 배포

PR → 빌드 → 테스트 → (머지 후) → Docker 이미지 빌드 → 서버 배포
```

### GitHub Actions 워크플로우

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: JDK 17 설정
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Gradle 캐시
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: gradle-${{ hashFiles('**/*.gradle*') }}

      - name: 빌드 및 테스트
        run: ./gradlew build

      - name: Docker 이미지 빌드 & 푸시
        if: github.ref == 'refs/heads/main'
        run: |
          docker build -t myapp:latest .
          # Docker Hub 또는 ECR에 푸시
```

---

## AWS 기본 배포

```
기본 구성:
  EC2: 서버 (Spring Boot 실행)
  RDS: MySQL 데이터베이스
  ElastiCache: Redis
  S3: 파일 저장
  ALB: 로드 밸런서

배포 방식:
1. EC2에 직접 배포 (초기)
   → SSH 접속 → jar 파일 업로드 → java -jar 실행

2. Docker 기반 배포
   → Docker 이미지 빌드 → ECR 푸시 → EC2에서 docker pull + run

3. ECS/EKS (컨테이너 오케스트레이션)
   → 자동 스케일링, 롤링 배포, 서비스 관리
```

---

## 모니터링

### Spring Boot Actuator

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always
```

```
GET /actuator/health    → 서버 상태
GET /actuator/metrics   → 메트릭 정보
GET /actuator/info      → 앱 정보
```

### 로깅 (SLF4J + Logback)

```java
@Slf4j  // Lombok
@Service
public class OrderService {

    public void createOrder(OrderDto dto) {
        log.info("주문 생성 시작: userId={}", dto.getUserId());

        try {
            // 비즈니스 로직
            log.debug("결제 처리 중: amount={}", dto.getAmount());
        } catch (Exception e) {
            log.error("주문 생성 실패: userId={}", dto.getUserId(), e);
            throw e;
        }

        log.info("주문 생성 완료: orderId={}", order.getId());
    }
}
```

```yaml
# 로그 레벨: TRACE < DEBUG < INFO < WARN < ERROR
logging:
  level:
    root: INFO
    com.example: DEBUG
  file:
    name: logs/app.log
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 배포 전 체크리스트

```
1. 환경 분리
   □ application-prod.yml 분리
   □ 비밀번호/키는 환경 변수로 관리
   □ ddl-auto: validate 또는 none

2. 성능
   □ DB 인덱스 확인
   □ N+1 문제 해결
   □ Redis 캐시 적용

3. 보안
   □ HTTPS 적용
   □ CORS 설정
   □ JWT Secret 환경 변수로 관리
   □ SQL Injection 방지 (JPA 사용 시 기본 방지)

4. 모니터링
   □ Actuator 설정
   □ 로그 설정
   □ 에러 알림 (Slack, 이메일)
```

---

## 면접 예상 질문

1. **Docker를 왜 사용하나요?**
   - 환경 일관성, 배포 편의성, 격리성, 확장성

2. **CI/CD란?**
   - CI: 코드 변경 시 자동 빌드/테스트 / CD: 자동 배포

3. **배포 시 환경별 설정은 어떻게 관리하나요?**
   - Spring Profile (application-{env}.yml) + 환경 변수

4. **로깅은 왜 중요한가요?**
   - 디버깅, 모니터링, 장애 추적, 감사(audit) 기록
