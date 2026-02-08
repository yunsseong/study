# 로드 밸런싱 (Load Balancing)

## 개념

- 들어오는 트래픽을 **여러 서버에 분산**하여 부하를 고르게 나누는 기술
- 서버 과부하 방지, 가용성(Availability) 향상, 수평 확장의 핵심

```
                    ┌─── 서버 1
클라이언트 → LB ────┼─── 서버 2
                    └─── 서버 3

하나의 서버가 죽어도 나머지가 처리 → 고가용성
```

---

## L4 vs L7 로드 밸런서

| 비교 | L4 (전송 계층) | L7 (응용 계층) |
|------|--------------|--------------|
| **기준** | IP, Port | URL, Header, Cookie |
| **속도** | 빠름 (패킷 레벨) | 상대적 느림 (내용 파악) |
| **기능** | 단순 분산 | 경로 기반 라우팅, SSL 종료 |
| **예시** | AWS NLB | AWS ALB, Nginx |
| **적합** | 단순 TCP 분산, 고성능 | API 라우팅, 마이크로서비스 |

```
L4 로드 밸런서:
  요청: 192.168.1.1:80 → 서버1 또는 서버2 (IP/Port만 봄)
  패킷 내용은 모름

L7 로드 밸런서:
  /api/users → 유저 서비스 서버
  /api/orders → 주문 서비스 서버
  /static/* → 정적 파일 서버
  URL, 헤더, 쿠키를 보고 라우팅
```

### Nginx L7 로드 밸런서 설정 예시

```nginx
upstream backend {
    server 10.0.0.1:8080;
    server 10.0.0.2:8080;
    server 10.0.0.3:8080;
}

server {
    listen 80;

    location /api/ {
        proxy_pass http://backend;
    }

    location /static/ {
        root /var/www/static;
    }
}
```

---

## 로드 밸런싱 알고리즘

### 1. 라운드 로빈 (Round Robin)

```
순서대로 돌아가며 분배. 가장 단순.

요청1 → 서버1
요청2 → 서버2
요청3 → 서버3
요청4 → 서버1 (다시 처음부터)

장점: 구현 단순, 균등 분배
단점: 서버 성능 차이를 고려하지 않음
```

### 2. 가중 라운드 로빈 (Weighted Round Robin)

```
서버 성능에 따라 가중치 부여.

서버1 (weight=3): 요청 3개 처리
서버2 (weight=1): 요청 1개 처리
서버3 (weight=2): 요청 2개 처리

사용: 서버 스펙이 다를 때
```

### 3. 최소 연결 (Least Connections)

```
현재 연결이 가장 적은 서버에 전달.

서버1: 현재 100개 연결
서버2: 현재 30개 연결  ← 여기로!
서버3: 현재 80개 연결

사용: 요청 처리 시간이 다양할 때 (긴 요청이 섞여있을 때)
```

### 4. IP 해시 (IP Hash)

```
클라이언트 IP를 해싱하여 항상 같은 서버로.

hash(192.168.1.1) % 3 = 1 → 항상 서버1
hash(192.168.1.2) % 3 = 0 → 항상 서버0

사용: 세션 고정(Sticky Session) 필요 시
단점: 서버 추가/제거 시 매핑 변경
```

### 5. 일관된 해싱 (Consistent Hashing)

```
해시 링에 서버를 배치, 요청 키의 해시값에서 시계방향으로 가장 가까운 서버.

서버 추가/제거 시 최소한의 키만 재배치 → 캐시 적중률 유지

사용: 캐시 서버 분산 (Redis 클러스터, CDN)
```

### 알고리즘 선택 가이드

```
일반적인 웹 서비스       → 라운드 로빈
서버 스펙이 다름         → 가중 라운드 로빈
요청 처리 시간 불균등     → 최소 연결
세션 유지 필요           → IP 해시
캐시 서버 분산           → 일관된 해싱
```

---

## 헬스 체크 (Health Check)

서버 상태를 주기적으로 확인하여 장애 서버를 제외.

```
종류:
1. Active 헬스 체크: LB가 주기적으로 서버에 요청 (GET /health)
2. Passive 헬스 체크: 실제 요청의 응답을 관찰하여 판단

/health 엔드포인트 응답:
  200 OK → 정상
  503 Service Unavailable → 비정상 → LB에서 제외
```

```python
# FastAPI 헬스 체크 엔드포인트
@app.get("/health")
def health_check():
    # DB 연결 확인
    try:
        db.execute("SELECT 1")
    except:
        raise HTTPException(status_code=503)

    # Redis 연결 확인
    try:
        redis.ping()
    except:
        raise HTTPException(status_code=503)

    return {"status": "healthy"}
```

```java
// Spring Boot Actuator (자동 헬스 체크 제공)
// GET /actuator/health → {"status": "UP"}

// build.gradle
// implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

---

## SSL 종료 (SSL Termination)

```
클라이언트 ──HTTPS──→ LB ──HTTP──→ 서버1, 서버2, 서버3

LB에서 SSL 암호화/복호화를 처리
→ 내부 서버는 HTTP로 통신 (서버 부하 감소)
→ SSL 인증서를 LB에서만 관리 (관리 편의성)
```

---

## 세션 관리와 로드 밸런싱

```
문제: 서버가 여러 대일 때 세션 공유는?

방법 1: Sticky Session (세션 고정)
  같은 클라이언트 → 항상 같은 서버
  단점: 서버 장애 시 세션 유실, 부하 불균등

방법 2: 세션 스토어 분리 (권장)
  Redis에 세션 저장 → 어떤 서버에서든 접근 가능

  클라이언트 → LB → 서버1 ─┐
                    서버2 ─┼── Redis (세션)
                    서버3 ─┘

방법 3: Stateless (JWT)
  서버에 세션 저장 안 함 → 토큰에 정보 포함
  LB 신경 안 써도 됨
```

---

## 면접 예상 질문

1. **L4 vs L7 로드 밸런서의 차이는?**
   - L4: IP/Port 기반, 빠름 / L7: URL/Header 기반, 기능 풍부

2. **로드 밸런싱 알고리즘을 아는 대로 설명해주세요**
   - 라운드 로빈, 가중 라운드 로빈, 최소 연결, IP 해시, 일관된 해싱

3. **서버가 여러 대일 때 세션 관리는 어떻게 하나요?**
   - Redis 세션 스토어, Sticky Session, JWT

4. **헬스 체크란?**
   - LB가 서버 상태를 주기적으로 확인하여 장애 서버를 제외
