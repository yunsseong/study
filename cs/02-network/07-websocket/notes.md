# 웹소켓 (WebSocket)

## 왜 필요한가?

HTTP는 **요청-응답** 모델 → 서버가 먼저 데이터를 보낼 수 없음.

실시간 통신이 필요한 경우:
- 채팅 앱
- 실시간 알림
- 주식 시세
- 온라인 게임
- 협업 도구 (Google Docs)

---

## HTTP 폴링 vs WebSocket

### 기존 방식들의 한계

#### 1. 폴링 (Polling)
클라이언트가 주기적으로 서버에 요청.

```
Client → "새 메시지 있어?" → Server: "없어"
(3초 후)
Client → "새 메시지 있어?" → Server: "없어"
(3초 후)
Client → "새 메시지 있어?" → Server: "1개 있어!"
```

- 단점: 불필요한 요청 많음, 서버 부하, 실시간성 부족

#### 2. 롱 폴링 (Long Polling)
서버가 데이터가 생길 때까지 응답을 대기.

```
Client → "새 메시지 있어?" → Server: (대기 중...)
... (30초 후 메시지 도착)
Server → "메시지 왔어!"
Client → "또 새 메시지 있어?" → Server: (대기 중...)
```

- 장점: 폴링보다 효율적
- 단점: 연결 재수립 반복, 완전한 실시간은 아님

#### 3. SSE (Server-Sent Events)
서버 → 클라이언트 단방향 실시간 스트리밍.

```
Client → 연결
Server → data: 메시지1
Server → data: 메시지2
Server → data: 메시지3
(서버가 일방적으로 전송)
```

- 장점: 간단, HTTP 기반
- 단점: **단방향** (서버→클라이언트만), 텍스트만 전송

---

## WebSocket

### 개념
- **양방향(Full-Duplex)** 실시간 통신 프로토콜
- HTTP로 연결을 시작한 후 **프로토콜 업그레이드**
- 한번 연결되면 **지속적 연결** 유지

### 연결 과정 (Handshake)

```
1. HTTP 업그레이드 요청
Client → Server:
  GET /chat HTTP/1.1
  Upgrade: websocket
  Connection: Upgrade
  Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==

2. 서버 승인
Server → Client:
  HTTP/1.1 101 Switching Protocols
  Upgrade: websocket
  Connection: Upgrade
  Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=

3. 이후 양방향 통신 (HTTP가 아닌 WebSocket 프로토콜)
Client ⇄ Server: 실시간 메시지 교환
```

### 동작 흐름

```
Client              Server
  |                    |
  |--- HTTP 업그레이드 →|   (HTTP → WebSocket 전환)
  |← 101 Switching ----|
  |                    |
  |===== WebSocket 연결 =====|
  |                    |
  |--- "안녕" --------→|   클라이언트 → 서버
  |←-- "반가워" -------|   서버 → 클라이언트
  |←-- "새 알림" ------|   서버가 먼저 전송 가능!
  |--- "확인" --------→|
  |                    |
  |--- close --------→ |   연결 종료
  |← close ------------|
```

---

## 비교 정리

| 방식 | 방향 | 실시간성 | 연결 | 오버헤드 |
|------|------|---------|------|---------|
| HTTP 폴링 | 단방향 | 낮음 | 매번 새 연결 | 높음 |
| 롱 폴링 | 단방향 | 중간 | 재연결 반복 | 중간 |
| SSE | 서버→클라이언트 | 높음 | 지속 연결 | 낮음 |
| **WebSocket** | **양방향** | **높음** | **지속 연결** | **낮음** |

---

## 실무 사용 예시

### Spring Boot WebSocket

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage send(ChatMessage message) {
        return message;
    }
}
```

### FastAPI WebSocket

```python
from fastapi import FastAPI, WebSocket, WebSocketDisconnect

app = FastAPI()

# 연결된 클라이언트 관리
clients: list[WebSocket] = []

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    clients.append(websocket)

    try:
        while True:
            data = await websocket.receive_text()
            # 모든 클라이언트에게 브로드캐스트
            for client in clients:
                await client.send_text(f"메시지: {data}")
    except WebSocketDisconnect:
        clients.remove(websocket)
```

---

## WebSocket 고려사항

### 연결 관리
- **하트비트(Heartbeat)**: 주기적 ping/pong으로 연결 상태 확인
- **재연결 로직**: 연결이 끊기면 자동으로 재연결 시도
- **연결 수 제한**: 서버 리소스 관리

### 확장성 (Scale-Out)

```
문제: 서버가 여러 대일 때, 클라이언트 A는 서버1에, B는 서버2에 연결
     → 서버1에서 보낸 메시지를 서버2의 클라이언트는 못 받음

해결:
- Redis Pub/Sub: 서버 간 메시지 브로드캐스트
- Kafka: 대규모 메시지 중계
- 전용 메시지 브로커: RabbitMQ
```

### 언제 WebSocket을 사용하나?

```
WebSocket 적합:
- 양방향 실시간 통신 (채팅, 게임)
- 서버에서 빈번하게 데이터 푸시
- 낮은 지연 시간(latency)이 중요

SSE 적합:
- 서버→클라이언트 단방향 (알림, 피드)
- 간단한 구현이 필요
- HTTP 인프라 그대로 활용

HTTP 폴링 적합:
- 실시간성이 낮아도 됨
- 간단한 상태 확인
- WebSocket 지원이 어려운 환경
```

---

## 면접 예상 질문

1. **WebSocket과 HTTP의 차이는?**
   - HTTP: 요청-응답, 비연결 / WebSocket: 양방향, 지속 연결

2. **WebSocket은 어떻게 연결되나요?**
   - HTTP 업그레이드 요청 → 101 Switching Protocols → WebSocket 프로토콜

3. **폴링, 롱 폴링, SSE, WebSocket의 차이는?**
   - 방향성, 실시간성, 오버헤드 비교

4. **WebSocket의 확장성 문제와 해결법은?**
   - 다중 서버 시 Redis Pub/Sub 또는 메시지 브로커로 해결

5. **언제 WebSocket 대신 SSE를 쓰나요?**
   - 서버→클라이언트 단방향이면 SSE가 간단하고 효율적
