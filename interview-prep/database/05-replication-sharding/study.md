# 5. DB Replication & Sharding

---

## DB 스케일링이 필요한 이유

서비스 초기에는 단일 DB 서버로 충분하지만, 트래픽이 증가하면 한계에 부딪힌다.

```
서비스 성장에 따른 DB 부하 변화:

  초기 (일 1만 요청)          성장기 (일 100만 요청)       대규모 (일 1억 요청)
  +----------+               +----------+                +----------+
  |  App(1)  |               | App(1~5) |                | App(50+) |
  +----+-----+               +----+-----+                +----+-----+
       |                          |                           |
  +----+-----+               +----+-----+                +----+-----+
  | Single DB|               | Single DB| ← CPU 90%!     |    ???   |
  +----------+               +----------+   Slow Query!   +----------+
   문제없음                    병목 시작                    단일 DB 불가
```

**단일 DB의 한계점:**
- CPU/메모리 자원 한계로 쿼리 응답 지연
- 디스크 I/O 병목으로 처리량(TPS) 제한
- 단일 장애점(Single Point of Failure) - DB 다운 = 서비스 전체 중단
- 읽기/쓰기 경합으로 Lock 대기 시간 증가

---

## Scale Up vs Scale Out

| 구분 | Scale Up (수직 확장) | Scale Out (수평 확장) |
|------|---------------------|----------------------|
| 방법 | 서버 사양 증가 (CPU, RAM, SSD) | 서버 수를 늘림 |
| 비용 | 고사양으로 갈수록 기하급수적 증가 | 서버 추가 비용은 선형적 |
| 한계 | 물리적 하드웨어 한계 존재 | 이론적으로 무한 확장 가능 |
| 복잡도 | 낮음 (기존 구조 유지) | 높음 (분산 시스템 설계 필요) |
| 가용성 | 여전히 단일 장애점 | 일부 서버 장애에도 서비스 유지 |
| 적용 | DB 초기 최적화 단계 | 대규모 트래픽 처리 단계 |

```
Scale Up:                          Scale Out:
+------------------+               +--------+ +--------+ +--------+
|                  |               | DB #1  | | DB #2  | | DB #3  |
|  고사양 DB 서버   |               +--------+ +--------+ +--------+
|  CPU 64코어      |                    ↕          ↕          ↕
|  RAM 512GB       |               +-----------------------------------+
|  NVMe SSD 4TB    |               |        분산 처리 레이어           |
|                  |               +-----------------------------------+
+------------------+
  비용: $$$$$                        비용: $ + $ + $
  한계: 물리적 최대치                 한계: 이론적 무한
```

> **핵심**: 초기에는 Scale Up이 간단하지만, 일정 수준을 넘으면 반드시 Scale Out이 필요하다.

---

## Replication (복제)

Replication은 **같은 데이터를 여러 DB 서버에 복제하여 읽기 성능과 가용성을 높이는 기술**이다.

### Master-Slave 구조

가장 기본적인 Replication 구조이다. Write는 Master에서만, Read는 Slave에서 처리한다.

```
                    +-------------------+
                    |   Application     |
                    +--------+----------+
                       |Write     |Read
                       v          v
                 +---------+  +---------+
                 | Master  |  |  Slave  |
                 | (Write) |->| (Read)  |
                 +---------+  +---------+
                       |
                       v
                 +---------+
                 |  Slave  |
                 | (Read)  |
                 +---------+

  Write(INSERT/UPDATE/DELETE) → Master만 처리
  Read(SELECT)               → Slave들이 분산 처리
  복제(Replication)          → Master의 변경사항을 Slave에 전파
```

**장점:**
- 읽기 부하 분산 (대부분의 서비스는 읽기 비율이 70~90%)
- 고가용성 (Master 장애 시 Slave를 Master로 승격 = Failover)
- 백업 용이 (Slave에서 백업 수행 → Master 부하 없음)

---

### 동기 복제 vs 비동기 복제

| 구분 | 동기 복제 (Synchronous) | 비동기 복제 (Asynchronous) |
|------|------------------------|---------------------------|
| 동작 | Master가 Slave 확인 후 응답 반환 | Master가 즉시 응답, Slave는 나중에 반영 |
| 일관성 | 강한 일관성 보장 | 일시적 불일치 가능 (Eventual Consistency) |
| 성능 | 느림 (Slave 응답 대기) | 빠름 (Master만 처리하면 됨) |
| 안정성 | 데이터 유실 없음 | Master 장애 시 미반영 데이터 유실 가능 |
| 사용 | 금융, 결제 등 데이터 정합성 중요한 경우 | 일반적인 웹 서비스 (대부분 이 방식) |

```
동기 복제:
  Client → Master: INSERT
  Master → Slave: 복제 요청
  Slave → Master: 복제 완료 (ACK)       ← 이 단계를 기다림
  Master → Client: INSERT 성공

비동기 복제:
  Client → Master: INSERT
  Master → Client: INSERT 성공           ← 즉시 응답
  Master → Slave: 복제 요청 (비동기)     ← 백그라운드 처리
  Slave: 복제 완료
```

**반동기 복제 (Semi-Synchronous):** 최소 1개의 Slave에만 동기 복제를 보장하는 절충안이다. MySQL에서 지원한다.

---

### Replication Lag 문제와 해결

비동기 복제에서 **Master에 쓴 데이터가 Slave에 아직 반영되지 않은 시간차**를 Replication Lag이라 한다.

```
시간 흐름 →

Master:  [INSERT 완료] ─────────────────────────────→
Slave:   [아직 미반영] ──── [Lag] ──── [반영 완료] ──→
                             ↑
                        이 구간에서 Slave 읽으면
                        방금 쓴 데이터가 안 보임!
```

**문제 시나리오:**
1. 사용자가 글을 작성 (Master에 INSERT)
2. 즉시 내 글 목록 조회 (Slave에서 SELECT)
3. 방금 쓴 글이 안 보임 (Lag 때문)

**해결 방법:**

| 방법 | 설명 | 적용 |
|------|------|------|
| Write 후 Read는 Master | 쓰기 직후 조회는 Master에서 | 가장 간단하고 확실 |
| 지연 시간 대기 | 쓰기 후 일정 시간 후 Slave 조회 | 단순하지만 UX 저하 |
| GTID 기반 확인 | Slave가 특정 트랜잭션까지 반영됐는지 확인 | MySQL GTID 활용 |
| 반동기 복제 | 최소 1개 Slave 동기 보장 | MySQL Semi-Sync |

---

### Spring Boot에서 Read/Write 분리

Spring Boot에서는 `@Transactional(readOnly = true)`를 활용하여 읽기 쿼리를 Slave로 라우팅한다.

```java
// 1. DataSource 설정 (application.yml)
// spring:
//   datasource:
//     master:
//       url: jdbc:mysql://master-host:3306/mydb
//       username: root
//       password: password
//     slave:
//       url: jdbc:mysql://slave-host:3306/mydb
//       username: readonly
//       password: password

// 2. DataSource Config
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {

        RoutingDataSource routing = new RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        targetDataSources.put("slave", slave);

        routing.setTargetDataSources(targetDataSources);
        routing.setDefaultTargetDataSource(master);
        return routing;
    }
}
```

### AbstractRoutingDataSource 구현

```java
// 3. 현재 트랜잭션이 readOnly인지 판별하여 DataSource 분기
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // readOnly 트랜잭션이면 slave, 아니면 master
        boolean isReadOnly = TransactionSynchronizationManager
                .isCurrentTransactionReadOnly();
        return isReadOnly ? "slave" : "master";
    }
}

// 4. Service 계층에서 사용
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 쓰기 → Master로 라우팅
    @Transactional
    public Product createProduct(ProductRequest request) {
        return productRepository.save(request.toEntity());
    }

    // 읽기 → Slave로 라우팅
    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상품 없음"));
    }

    // 읽기 → Slave로 라우팅
    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAll();
    }
}
```

```
요청 흐름:

  @Transactional                    @Transactional(readOnly=true)
       |                                    |
       v                                    v
  RoutingDataSource                 RoutingDataSource
  determineCurrentLookupKey()       determineCurrentLookupKey()
       |                                    |
  isReadOnly = false                isReadOnly = true
       |                                    |
       v                                    v
   "master" → Master DB             "slave" → Slave DB
```

> **핵심**: `@Transactional(readOnly = true)`만 붙이면 자동으로 Slave로 라우팅되도록 인프라를 구성한다.

---

## Partitioning (파티셔닝)

파티셔닝은 **하나의 큰 테이블을 물리적으로 여러 조각으로 나누는 기술**이다. 하나의 DB 서버 안에서 이루어진다.

### 수평 파티셔닝 vs 수직 파티셔닝

```
수평 파티셔닝 (Horizontal Partitioning):
행(Row) 단위로 분할

  원본 테이블 (주문):
  +----+--------+--------+------------+
  | id | user_id| amount | created_at |
  +----+--------+--------+------------+
  |  1 |  100   | 50000  | 2024-01    |
  |  2 |  200   | 30000  | 2024-06    |
  |  3 |  300   | 70000  | 2025-01    |
  |  4 |  400   | 20000  | 2025-07    |
  +----+--------+--------+------------+

  → 파티션 1 (2024년):         → 파티션 2 (2025년):
  +----+--------+--------+    +----+--------+--------+
  |  1 |  100   | 50000  |    |  3 |  300   | 70000  |
  |  2 |  200   | 30000  |    |  4 |  400   | 20000  |
  +----+--------+--------+    +----+--------+--------+


수직 파티셔닝 (Vertical Partitioning):
열(Column) 단위로 분할

  원본 테이블 (회원):
  +----+------+-------+--------+-----------+
  | id | name | email | avatar | biography |
  +----+------+-------+--------+-----------+

  → 자주 조회:                  → 가끔 조회 (대용량):
  +----+------+-------+        +----+--------+-----------+
  | id | name | email |        | id | avatar | biography |
  +----+------+-------+        +----+--------+-----------+
```

### 파티셔닝 전략

| 전략 | 설명 | 예시 | 장점 | 단점 |
|------|------|------|------|------|
| Range | 범위 기준 분할 | 날짜별, ID 범위별 | 범위 조회 빠름 | 데이터 편향 가능 |
| Hash | 해시 함수로 분할 | hash(user_id) % 4 | 균등 분배 | 범위 조회 어려움 |
| List | 특정 값 목록 기준 | 지역별(서울, 부산) | 논리적 그룹핑 | 목록 관리 필요 |

```sql
-- MySQL Range Partitioning 예시
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2),
    created_at DATE NOT NULL,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION pmax  VALUES LESS THAN MAXVALUE
);

-- 2024년 데이터 조회 → p2024 파티션만 스캔 (Partition Pruning)
SELECT * FROM orders WHERE created_at BETWEEN '2024-01-01' AND '2024-12-31';
```

---

## Sharding (샤딩)

### Sharding이란?

Sharding은 **데이터를 여러 개의 독립된 DB 서버(Shard)에 분산 저장하는 기술**이다.
수평 파티셔닝을 여러 서버에 걸쳐 수행하는 것이다.

```
Partitioning (단일 서버 내):        Sharding (여러 서버에 분산):

  +---------------------------+     +----------+  +----------+  +----------+
  |       단일 DB 서버         |     | Shard #1 |  | Shard #2 |  | Shard #3 |
  | +--------+ +--------+    |     | Server A |  | Server B |  | Server C |
  | | Part 1 | | Part 2 |    |     +----------+  +----------+  +----------+
  | +--------+ +--------+    |     | user 1~  |  | user     |  | user     |
  | +--------+ +--------+    |     | 1000만   |  | 1000만~  |  | 2000만~  |
  | | Part 3 | | Part 4 |    |     |          |  | 2000만   |  | 3000만   |
  | +--------+ +--------+    |     +----------+  +----------+  +----------+
  +---------------------------+       ↑ 각각 독립된 서버, CPU/메모리 별도
    ↑ 같은 서버, 같은 자원 공유
```

| 구분 | Partitioning | Sharding |
|------|-------------|----------|
| 위치 | 단일 서버 내부 | 여러 서버에 분산 |
| 자원 | 같은 서버 자원 공유 | 각 서버가 독립 자원 |
| 확장성 | 서버 스펙에 한정 | 서버 추가로 무한 확장 |
| 복잡도 | DB 레벨 설정 | 애플리케이션 레벨 로직 필요 |

---

### Shard Key 선택의 중요성

Shard Key는 데이터를 어느 Shard에 저장할지 결정하는 기준 컬럼이다.

**좋은 Shard Key 조건:**
- **높은 카디널리티**: 값의 종류가 많아 균등 분배 가능
- **균등한 분포**: 특정 Shard에 데이터가 몰리지 않음 (Hotspot 방지)
- **쿼리 패턴 일치**: 자주 사용하는 조회 조건과 일치

```
나쁜 Shard Key (성별):                좋은 Shard Key (user_id):

  Shard 1 (남성): 60% 데이터          Shard 1: ~33% 데이터
  Shard 2 (여성): 40% 데이터          Shard 2: ~33% 데이터
  → 불균등! Shard 1에 부하 집중        Shard 3: ~33% 데이터
                                       → 균등 분배!
```

---

### 샤딩 전략

**1) Range Sharding**
```
Shard Key: user_id

  Shard 1: user_id 1 ~ 1,000,000
  Shard 2: user_id 1,000,001 ~ 2,000,000
  Shard 3: user_id 2,000,001 ~ 3,000,000

  장점: 구현 간단, 범위 조회 용이
  단점: 신규 가입자가 마지막 Shard에 집중 (Hotspot)
```

**2) Hash Sharding**
```
Shard Key: user_id
Shard 수: 3
공식: shard_number = hash(user_id) % 3

  user_id=100 → hash(100) % 3 = 1 → Shard 1
  user_id=201 → hash(201) % 3 = 0 → Shard 0
  user_id=305 → hash(305) % 3 = 2 → Shard 2

  장점: 균등 분배
  단점: 범위 조회 어려움, Shard 추가 시 리밸런싱 필요
```

**3) Directory Sharding**
```
+---------------------+
| Lookup Table        |
+----------+----------+
| Key 범위  | Shard   |
+----------+----------+
| VIP 고객  | Shard 1 |   ← 전용 고성능 서버
| 한국 고객  | Shard 2 |
| 일본 고객  | Shard 3 |
| 기타      | Shard 4 |
+----------+----------+

  장점: 유연한 라우팅 가능
  단점: Lookup Table이 단일 장애점, 추가 조회 비용
```

---

### 샤딩의 문제점

| 문제 | 설명 | 해결 방안 |
|------|------|----------|
| Cross-shard JOIN | 다른 Shard에 있는 데이터 JOIN 불가 | 애플리케이션 레벨 JOIN, 반정규화 |
| 리밸런싱 | Shard 추가/제거 시 데이터 재분배 필요 | Consistent Hashing 적용 |
| 글로벌 유니크 ID | AUTO_INCREMENT가 Shard별로 독립 | Snowflake ID, UUID, 별도 ID 서버 |
| 분산 트랜잭션 | 여러 Shard에 걸친 트랜잭션 어려움 | Saga Pattern, 2PC |
| 운영 복잡도 | 스키마 변경, 백업 등 관리 대상 증가 | 자동화 도구 활용 |

**글로벌 유니크 ID 생성 전략:**

```
1) UUID: 128비트 랜덤 (인덱스 성능 저하 우려)
   예: "550e8400-e29b-41d4-a716-446655440000"

2) Snowflake ID (Twitter 방식): 64비트 = 타임스탬프 + 머신ID + 시퀀스
   예: 1382971839180800001
   장점: 시간순 정렬 가능, 인덱스 친화적

3) 별도 ID 서버 (Ticket Server):
   중앙 서버에서 ID 발급 → 단일 장애점 주의
```

---

### Consistent Hashing 기본 개념

일반 Hash Sharding에서 Shard 수가 변경되면 거의 모든 데이터의 재배치가 필요하다.
Consistent Hashing은 **Shard 추가/제거 시 최소한의 데이터만 이동**하도록 한다.

```
일반 Hash Sharding (Shard 3 → 4로 변경):

  hash(key) % 3 = ?  →  hash(key) % 4 = ?
  대부분의 key가 다른 Shard로 이동해야 함! (약 75% 재배치)


Consistent Hashing (링 구조):

              Shard A
               /    \
          key1/      \key4
             /        \
       Shard D -------- Shard B
             \        /
          key3\      /key2
               \    /
              Shard C

  - 각 Shard와 Key를 해시 링 위에 배치
  - Key는 시계 방향으로 가장 가까운 Shard에 저장
  - Shard 추가 시: 인접한 Key만 이동 (약 1/N만 재배치)
  - Virtual Node로 균등 분배 보장
```

---

## Connection Pool (HikariCP)

### Connection Pool이란?

DB 연결(Connection)을 미리 만들어 놓고 재사용하는 기술이다.
매 요청마다 Connection을 생성/종료하면 TCP 3-way handshake + 인증 등의 비용이 크다.

```
Connection Pool 없이:                Connection Pool 사용:

  요청1 → DB 연결 생성 (50ms)         요청1 → Pool에서 꺼냄 (0.1ms)
  요청1 → 쿼리 실행                   요청1 → 쿼리 실행
  요청1 → DB 연결 종료                 요청1 → Pool에 반환

  요청2 → DB 연결 생성 (50ms)         요청2 → Pool에서 꺼냄 (0.1ms)
  요청2 → 쿼리 실행                   요청2 → 쿼리 실행
  요청2 → DB 연결 종료                 요청2 → Pool에 반환

  매번 50ms 오버헤드!                  Connection 재사용으로 고속!
```

### HikariCP

Spring Boot 2.0부터 기본 Connection Pool이다. 가장 빠르고 가벼운 JDBC Connection Pool 라이브러리이다.

**주요 설정:**

| 설정 | 기본값 | 설명 |
|------|--------|------|
| `maximumPoolSize` | 10 | 풀에서 유지하는 최대 Connection 수 |
| `minimumIdle` | maximumPoolSize와 동일 | 유휴 상태로 유지할 최소 Connection 수 |
| `connectionTimeout` | 30000 (30초) | Connection 획득 대기 최대 시간 (ms) |
| `idleTimeout` | 600000 (10분) | 유휴 Connection 유지 시간 |
| `maxLifetime` | 1800000 (30분) | Connection 최대 수명 |

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 10        # 고정 크기 풀 권장 (HikariCP 공식 권장)
      connection-timeout: 3000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: MyHikariPool
```

**maximumPoolSize 산정 공식 (참고):**

```
Pool Size = Tn x (Cm - 1) + 1

  Tn = 전체 스레드 수
  Cm = 하나의 요청에서 동시에 필요한 최대 Connection 수

  예) 스레드 10개, 요청당 Connection 1개:
      Pool Size = 10 x (1 - 1) + 1 = 1  (최소값)
      실무에서는 보통 스레드 수와 비슷하게 설정

  일반 권장: CPU 코어 수 x 2 + 디스크 수
  예) 4코어 서버: 4 x 2 + 1 = 9 ~ 10개
```

> **핵심**: Pool 크기를 너무 크게 잡으면 DB 측 Connection 자원 낭비, 너무 작으면 대기 시간 증가. CPU 코어 수 기반으로 적절히 설정한다.

---

## 실무 시나리오: 서비스 성장에 따른 DB 확장 단계

```
단계 1: 인덱스 최적화
+----------+      슬로우 쿼리 발견 → 인덱스 추가 → 쿼리 튜닝
| Single DB| ←    비용: 낮음 / 효과: 높음
+----------+

    ↓ 그래도 느리면

단계 2: 캐시 도입
+----------+     +-------+
| Single DB| ←── | Redis | ←── 자주 조회되는 데이터 캐싱
+----------+     +-------+     읽기 부하 70% 이상 감소

    ↓ 그래도 부족하면

단계 3: Replication (Read/Write 분리)
+---------+     +---------+
| Master  |────>| Slave 1 |     읽기 트래픽 분산
| (Write) |────>| Slave 2 |     가용성 확보
+---------+     +---------+

    ↓ 쓰기도 한계에 도달하면

단계 4: Sharding (데이터 분산)
+----------+  +----------+  +----------+
| Shard 1  |  | Shard 2  |  | Shard 3  |
| (각각    |  | (각각    |  | (각각    |
| Master+  |  | Master+  |  | Master+  |
| Slave)   |  | Slave)   |  | Slave)   |
+----------+  +----------+  +----------+
  쓰기/읽기 모두 분산, 최종 아키텍처
```

> **핵심**: 처음부터 Sharding을 하지 않는다. 각 단계별 비용 대비 효과를 따져 점진적으로 확장한다.

---

## 면접 핵심 정리 Q&A

**Q1: Replication은 무엇이고 왜 사용하나요?**
> 같은 데이터를 여러 DB 서버에 복제하는 기술이다. 읽기 부하 분산, 고가용성(Failover), 백업 편의를 위해 사용한다. Master에 쓰고 Slave에서 읽는 구조가 가장 기본이다.

**Q2: Replication Lag이 뭔가요? 어떻게 해결하나요?**
> 비동기 복제에서 Master와 Slave 간 데이터 반영 시간차이다. 쓰기 직후 읽기는 Master에서 하거나, 반동기 복제를 적용하거나, GTID 기반으로 Slave 반영 상태를 확인하여 해결한다.

**Q3: Sharding과 Partitioning의 차이는?**
> Partitioning은 단일 서버 내에서 테이블을 분할하는 것이고, Sharding은 여러 서버에 데이터를 분산하는 것이다. Sharding은 서버 추가로 확장 가능하지만 Cross-shard JOIN 불가, 글로벌 유니크 ID 관리 등의 복잡도가 추가된다.

**Q4: Spring Boot에서 Read/Write 분리를 어떻게 구현하나요?**
> AbstractRoutingDataSource를 상속하여 determineCurrentLookupKey()에서 TransactionSynchronizationManager.isCurrentTransactionReadOnly()를 확인한다. readOnly=true면 Slave, 아니면 Master DataSource를 반환한다.

**Q5: HikariCP의 maximumPoolSize는 어떻게 결정하나요?**
> 일반적으로 CPU 코어 수 x 2 + 디스크 수를 기준으로 설정한다. 너무 크면 DB 측 Connection 자원이 낭비되고 컨텍스트 스위칭이 증가하며, 너무 작으면 Connection 획득 대기가 발생한다. 부하 테스트를 통해 최적값을 찾는 것이 가장 좋다.
