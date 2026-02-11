# 메시지 큐 (Message Queue)

## 개념

- 서비스 간 메시지를 **비동기적으로 전달**하는 미들웨어
- 생산자(Producer)가 메시지를 큐에 넣으면, 소비자(Consumer)가 꺼내서 처리

```
동기 방식:
  주문 → 결제 → 재고 감소 → 알림 → 응답 (3초)
  전체가 끝나야 응답 가능, 하나 실패하면 전부 실패

비동기 방식 (메시지 큐):
  주문 → 결제 → 응답 (0.5초)
         ↓ (큐에 메시지)
         재고 감소 (비동기)
         알림 발송 (비동기)
  핵심 로직만 동기, 나머지는 비동기
```

---

## 왜 필요한가?

### 1. 비동기 처리 (Asynchronous Processing)

```
이메일 발송, 알림, 로그 기록 등
→ 사용자 응답에 꼭 필요하지 않은 작업을 큐에 넣고 나중에 처리
→ 응답 시간 단축
```

### 2. 서비스 간 결합도 감소 (Decoupling)

```
직접 호출:
  주문서비스 → 재고서비스 (재고 서비스 죽으면 주문도 실패)

메시지 큐:
  주문서비스 → [큐] → 재고서비스
  재고 서비스가 잠시 죽어도 큐에 메시지 보관 → 복구 후 처리
```

### 3. 트래픽 완충 (Peak Shaving)

```
갑자기 10만 요청이 몰려도:
  요청 → [큐 (버퍼)] → 서버가 감당 가능한 속도로 처리

큐 없으면: 서버 과부하 → 장애
큐 있으면: 메시지 쌓아두고 순서대로 처리
```

---

## 메시지 큐 vs 이벤트 스트림

| 비교 | 메시지 큐 | 이벤트 스트림 |
|------|----------|-------------|
| **대표** | RabbitMQ, SQS | **Kafka**, Kinesis |
| **소비 방식** | 메시지 소비 후 삭제 | 메시지 유지 (재소비 가능) |
| **소비자 수** | 보통 1개 (경쟁 소비) | 여러 개 (각자 독립 소비) |
| **순서 보장** | 큐 단위 보장 | 파티션 단위 보장 |
| **적합** | 작업 분배, 비동기 처리 | 이벤트 소싱, 로그, 스트리밍 |

---

## Kafka

### 핵심 개념

```
Producer → [Topic] → Consumer

Topic: 메시지 카테고리 (예: "order-events", "user-events")
Partition: Topic을 나눈 단위 (병렬 처리의 핵심)
Offset: 파티션 내 메시지의 순번
Consumer Group: 소비자 그룹 (파티션을 나눠서 소비)
Broker: Kafka 서버 노드
```

```
Topic: order-events
  Partition 0: [msg0, msg1, msg2, msg3, ...]
  Partition 1: [msg0, msg1, msg2, ...]
  Partition 2: [msg0, msg1, ...]

Consumer Group A:
  Consumer 1 ← Partition 0
  Consumer 2 ← Partition 1, 2

Consumer Group B (독립):
  Consumer 3 ← Partition 0, 1, 2

→ 같은 그룹 내에서는 파티션을 나눠서 소비 (경쟁 소비)
→ 다른 그룹은 같은 메시지를 각각 소비 (팬아웃)
```

### Kafka 특징

```
장점:
├── 높은 처리량 (초당 수백만 메시지)
├── 메시지 영속화 (디스크 저장, 재소비 가능)
├── 수평 확장 (파티션 추가 → 처리량 증가)
├── 순서 보장 (파티션 내)
└── 이벤트 소싱, 로그 적합

단점:
├── 운영 복잡도 높음 (ZooKeeper/KRaft 필요)
├── 단순 작업 큐로는 과할 수 있음
└── 실시간 처리보다는 스트리밍에 강점
```

### Kafka 사용 예시

```python
# Producer (Python - kafka-python)
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# 주문 이벤트 발행
producer.send('order-events', {
    'orderId': 123,
    'userId': 1,
    'action': 'created',
    'amount': 50000
})
```

```python
# Consumer
from kafka import KafkaConsumer
import json

consumer = KafkaConsumer(
    'order-events',
    bootstrap_servers='localhost:9092',
    group_id='inventory-service',
    value_deserializer=lambda m: json.loads(m.decode('utf-8'))
)

for message in consumer:
    order = message.value
    print(f"재고 감소 처리: 주문 {order['orderId']}")
    # 재고 로직 처리
```

---

## RabbitMQ

### 핵심 개념

```
Producer → [Exchange] → [Queue] → Consumer

Exchange: 메시지 라우팅 규칙
  ├── Direct: 라우팅 키 정확히 일치
  ├── Fanout: 연결된 모든 큐에 전달 (브로드캐스트)
  ├── Topic: 패턴 매칭 (order.*, *.created)
  └── Headers: 헤더 값 기반

Queue: 메시지 저장소 (소비되면 삭제)
```

### RabbitMQ vs Kafka

| 비교 | RabbitMQ | Kafka |
|------|----------|-------|
| **모델** | 메시지 큐 (소비 후 삭제) | 이벤트 로그 (유지) |
| **처리량** | 만 단위/초 | 백만 단위/초 |
| **라우팅** | 풍부 (Exchange 타입) | 단순 (Topic/Partition) |
| **순서 보장** | 큐 단위 | 파티션 단위 |
| **재소비** | 불가 | 가능 |
| **적합** | 작업 큐, RPC, 복잡한 라우팅 | 로그, 이벤트 소싱, 대용량 |

### 언제 무엇을 쓸까?

```
Kafka:
├── 대용량 데이터 파이프라인
├── 이벤트 소싱, CQRS
├── 로그 수집 (ELK 스택)
├── 실시간 스트리밍 분석
└── MSA 간 이벤트 전달

RabbitMQ:
├── 비동기 작업 처리 (이메일, 알림)
├── 작업 분배 (워커 패턴)
├── 복잡한 라우팅 필요
├── RPC 패턴
└── 소규모~중규모 서비스
```

---

## 메시지 전달 보장

| 보장 수준 | 설명 | 사용 |
|----------|------|------|
| **At Most Once** | 최대 1번 (유실 가능) | 로그, 모니터링 |
| **At Least Once** | 최소 1번 (중복 가능) | 대부분의 서비스 |
| **Exactly Once** | 정확히 1번 | 결제 (구현 어려움) |

```
At Least Once + 멱등성(Idempotency) = 사실상 Exactly Once

멱등성: 같은 요청을 여러 번 보내도 결과가 같음

예: 주문 ID를 키로 중복 체크
  메시지: {orderId: 123, action: "pay"}
  → orderId=123 이미 처리됨? → 무시
  → 처리 안 됨? → 처리
```

---

## 면접 예상 질문

1. **메시지 큐를 왜 사용하나요?**
   - 비동기 처리, 서비스 간 결합도 감소, 트래픽 완충

2. **Kafka와 RabbitMQ의 차이는?**
   - Kafka: 이벤트 로그, 대용량, 재소비 가능 / RabbitMQ: 메시지 큐, 소비 후 삭제, 복잡한 라우팅

3. **Kafka의 파티션이란?**
   - Topic을 나눈 단위, 병렬 처리의 핵심, 파티션 내 순서 보장

4. **메시지 중복 처리는 어떻게 하나요?**
   - At Least Once + 멱등성(Idempotency)으로 해결

5. **Consumer Group이란?**
   - 파티션을 나눠서 소비하는 소비자 그룹, 그룹 내 경쟁 소비
