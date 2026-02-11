# 시스템 설계 면접 실전

## 면접 접근법 (4단계)

### 1단계: 요구사항 명확화 (3~5분)

```
바로 설계하지 말고, 질문으로 범위를 좁힌다.

확인할 것:
├── 핵심 기능은 무엇인가? (MVP)
├── 사용자 규모는? (DAU)
├── 읽기/쓰기 비율은?
├── 데이터 보관 기간은?
├── 특별한 요구사항은? (실시간, 순서 보장, 정합성)
└── 기존 인프라 제약은?

예: "URL 단축기를 설계해주세요"
→ "DAU는 어느 정도인가요?"
→ "단축 URL의 길이 제한이 있나요?"
→ "커스텀 URL을 지원해야 하나요?"
→ "만료 기능이 필요한가요?"
```

### 2단계: 대략적 설계 (10~15분)

```
큰 그림을 그린다. 핵심 컴포넌트와 데이터 흐름.

컴포넌트:
├── 클라이언트 (Web/App)
├── 로드 밸런서
├── 애플리케이션 서버
├── 데이터베이스
├── 캐시 (Redis)
├── 메시지 큐 (필요 시)
└── CDN (정적 파일)

그림으로 표현:
클라이언트 → LB → App Server → DB
                            → Cache
```

### 3단계: 상세 설계 (10~15분)

```
면접관이 관심 있는 부분을 깊이 설계.

보통 물어보는 것:
├── 데이터 모델 (스키마, 인덱스)
├── API 설계 (엔드포인트, 요청/응답)
├── 확장 전략 (샤딩, 캐시, 레플리카)
├── 핵심 알고리즘 (해시, 매칭 등)
└── 엣지 케이스 처리
```

### 4단계: 마무리 (3~5분)

```
트레이드오프 논의, 확장 가능성 언급.

├── 현재 설계의 병목점은 어디인가?
├── 10배 확장 시 어떻게 변경해야 하나?
├── 단일 장애점(SPOF)은 없는가?
└── 모니터링은 어떻게 할 것인가?
```

---

## 실전 예제 1: URL 단축기 (TinyURL)

### 요구사항

```
기능:
- 긴 URL → 짧은 URL 생성
- 짧은 URL → 원래 URL로 리다이렉트
- 만료 기능 (선택)

비기능:
- DAU 100만
- 읽기:쓰기 = 100:1
- URL 길이: 7자리
- 짧은 지연 시간
```

### 용량 추정 (Back-of-the-envelope)

```
쓰기: 100만 DAU × 0.1 (10%가 생성) = 10만/일 ≈ 1.2/초
읽기: 10만 × 100 = 1000만/일 ≈ 116/초

저장:
- URL 평균 100bytes × 10만/일 × 365일 × 5년
- ≈ 18GB (크지 않음)

7자리 URL 가능한 조합:
- [a-zA-Z0-9] = 62자
- 62^7 ≈ 3.5조 → 충분
```

### 핵심 설계

```
생성 흐름:
1. POST /api/shorten {longUrl: "https://very-long-url.com/..."}
2. 해시 생성: Base62(hash(longUrl)) → "aB3x9Kz"
3. DB 저장: shortUrl → longUrl 매핑
4. 응답: "https://tiny.url/aB3x9Kz"

리다이렉트 흐름:
1. GET /aB3x9Kz
2. Redis 캐시 확인 → Hit면 바로 리다이렉트
3. Cache Miss → DB 조회 → 캐시 저장
4. 301 Redirect → 원래 URL

아키텍처:
클라이언트 → LB → App Server → Redis (캐시)
                              → DB (MySQL)
```

### 데이터 모델

```sql
CREATE TABLE urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(7) UNIQUE NOT NULL,
    long_url VARCHAR(2048) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NULL,
    INDEX idx_short_code (short_code)
);
```

### ID 생성 전략

```
방법 1: 해시 (MD5/SHA-256) + Base62
  hash("https://long-url.com") → "5d41402a..." → Base62 앞 7자리
  충돌 시: 뒤에 랜덤 문자 추가 후 재해시

방법 2: Auto Increment + Base62
  id=1 → Base62(1) = "1"
  id=1000000 → Base62(1000000) = "4C92"
  순서 예측 가능 → 보안 이슈 (랜덤 시드 추가)

방법 3: Snowflake ID + Base62
  분산 환경에서 유니크 보장
```

### 확장

```
캐시: 자주 접근하는 URL을 Redis에 (80/20 법칙)
DB: 읽기 레플리카 추가 (읽기 100배 많으므로)
샤딩: short_code 해시 기반 샤딩 (규모가 클 때)
Rate Limiting: 악용 방지
Analytics: 클릭 수, 지역, 시간대 통계
```

---

## 실전 예제 2: 채팅 시스템

### 요구사항

```
기능:
- 1:1 채팅
- 그룹 채팅 (최대 100명)
- 온라인 상태 표시
- 읽음 표시
- 메시지 저장 (30일)

비기능:
- DAU 500만
- 실시간 메시지 전달
- 메시지 순서 보장
```

### 핵심 설계

```
실시간 통신: WebSocket

클라이언트 A ──WebSocket──→ [Chat Server] ──WebSocket──→ 클라이언트 B
                              ↓
                         [Message Queue]
                              ↓
                         [DB 저장]

1:1 채팅:
  A가 메시지 전송 → Chat Server → B에게 즉시 전달
  B가 오프라인이면 → 큐에 저장 → 접속 시 전달

그룹 채팅:
  A가 메시지 전송 → Chat Server → Fanout (그룹 멤버 전원에게)
```

### 데이터 모델

```sql
-- 메시지 (NoSQL이 더 적합할 수 있음)
CREATE TABLE messages (
    id BIGINT PRIMARY KEY,  -- Snowflake ID (시간순 보장)
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_room_created (chat_room_id, created_at)
);

-- 채팅방
CREATE TABLE chat_rooms (
    id BIGINT PRIMARY KEY,
    type ENUM('DIRECT', 'GROUP'),
    created_at DATETIME
);

-- 채팅방 멤버
CREATE TABLE chat_members (
    chat_room_id BIGINT,
    user_id BIGINT,
    last_read_message_id BIGINT,  -- 읽음 표시
    PRIMARY KEY (chat_room_id, user_id)
);
```

### 읽음 표시

```
1. 사용자가 메시지를 읽음
2. last_read_message_id 업데이트
3. 상대방에게 읽음 이벤트 전송

안 읽은 메시지 수:
SELECT COUNT(*) FROM messages
WHERE chat_room_id = ?
  AND id > (SELECT last_read_message_id FROM chat_members WHERE ...)
```

### 온라인 상태

```
방법 1: 하트비트 (Heartbeat)
  클라이언트 → 30초마다 서버에 "나 살아있어"
  서버: 마지막 하트비트가 60초 전이면 → 오프라인 처리

방법 2: WebSocket 연결 상태
  WebSocket 연결 중 → 온라인
  연결 끊김 → 오프라인

저장: Redis에 온라인 상태 캐싱
  SETEX online:user:1 60 "1"  → 60초 TTL
```

### 확장

```
WebSocket 서버 확장:
  여러 서버에 접속한 유저 간 메시지 전달 → Redis Pub/Sub 활용

  User A → WS Server 1 → Redis Pub/Sub → WS Server 2 → User B

메시지 저장:
  최근 메시지: Redis (빠른 접근)
  전체 메시지: Cassandra/MongoDB (대용량)

Push 알림:
  오프라인 유저 → FCM/APNs로 Push
```

---

## 용량 추정 팁 (Back-of-the-envelope)

```
알아두면 유용한 숫자:
├── 1일 = 86,400초 ≈ 10만 초
├── 1년 ≈ 3천만 초
├── QPS 100 = 하루 약 860만 요청
├── 1KB × 100만 = 1GB
├── 1MB × 100만 = 1TB
└── 2^10 = 1K, 2^20 = 1M, 2^30 = 1G

지연 시간 기준:
├── L1 캐시: 0.5ns
├── 메모리: 100ns
├── SSD 읽기: 150μs
├── HDD 읽기: 10ms
├── 같은 데이터센터 네트워크: 0.5ms
├── 다른 데이터센터: 150ms
└── Redis 조회: ~0.5ms, DB 조회: ~10ms
```

---

## 면접 예상 질문

1. **시스템 설계 면접에서 가장 중요한 것은?**
   - 요구사항 명확화, 트레이드오프 논의, 큰 그림 먼저

2. **URL 단축기를 설계해주세요**
   - 해시/Base62로 단축 코드 생성, Redis 캐시, 301 리다이렉트

3. **채팅 시스템을 설계해주세요**
   - WebSocket 실시간 통신, 메시지 큐, Snowflake ID로 순서 보장

4. **용량 추정은 왜 하나요?**
   - 필요한 서버 수, 저장소 크기, 네트워크 대역폭을 예측하여 적절한 아키텍처 결정

5. **시스템 설계에서 트레이드오프 예시를 들어주세요**
   - 일관성 vs 가용성 (CAP), 성능 vs 비용, 단순함 vs 확장성
