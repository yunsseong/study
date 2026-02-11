# DB Replication/Sharding 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** DB Replication이란 무엇이고, 왜 필요한가요?

> Replication은 같은 데이터를 여러 DB 서버에 복제하여 유지하는 기술입니다. 필요한 이유는 세 가지입니다. 첫째, 읽기 부하 분산입니다. 대부분의 서비스는 읽기 비율이 70~90%인데, 여러 Slave에서 읽기를 분산 처리하면 Master의 부하를 줄일 수 있습니다. 둘째, 고가용성(High Availability)입니다. Master에 장애가 발생하면 Slave를 Master로 승격(Failover)하여 서비스 중단을 최소화할 수 있습니다. 셋째, 백업 편의성입니다. Slave에서 백업을 수행하면 Master의 성능에 영향을 주지 않습니다.

**Q2.** Master-Slave 구조에서 Write와 Read는 각각 어디서 처리되나요? 이렇게 분리하는 이유는 무엇인가요?

> Write(INSERT, UPDATE, DELETE)는 Master에서만 처리하고, Read(SELECT)는 Slave에서 처리합니다. 이렇게 분리하는 이유는 데이터 일관성을 유지하면서 읽기 성능을 높이기 위해서입니다. 쓰기를 Master 한 곳에서만 처리하면 데이터 충돌 없이 일관성을 보장할 수 있고, 읽기를 여러 Slave에 분산하면 병렬 처리로 읽기 처리량을 크게 늘릴 수 있습니다. 예를 들어 Slave를 3대 두면 읽기 처리량이 이론적으로 3배 증가합니다.

**Q3.** 동기 복제와 비동기 복제의 차이를 설명해주세요. 각각 어떤 상황에 적합한가요?

> 동기 복제는 Master가 Slave에 데이터를 전파하고 Slave의 확인(ACK)을 받은 후에야 클라이언트에 응답을 반환하는 방식입니다. 강한 일관성을 보장하지만 Slave의 응답을 기다려야 하므로 쓰기 성능이 저하됩니다. 금융 시스템이나 결제 시스템처럼 데이터 유실이 절대 허용되지 않는 경우에 적합합니다. 비동기 복제는 Master가 즉시 클라이언트에 응답하고 Slave에는 백그라운드로 복제하는 방식입니다. 쓰기 성능이 좋지만 Master 장애 시 아직 복제되지 않은 데이터가 유실될 수 있습니다. 일반적인 웹 서비스에서 가장 많이 사용합니다. 절충안으로 MySQL의 반동기(Semi-Synchronous) 복제가 있는데, 최소 1개의 Slave에만 동기 복제를 보장합니다.

**Q4.** Replication Lag이란 무엇이고, 어떤 문제를 일으킬 수 있나요?

> Replication Lag은 비동기 복제 환경에서 Master에 기록된 변경이 Slave에 반영되기까지의 시간차입니다. 예를 들어 사용자가 게시글을 작성(Master에 INSERT)한 직후 내 글 목록을 조회(Slave에서 SELECT)하면, Lag으로 인해 방금 쓴 글이 보이지 않는 현상이 발생합니다. 해결 방법으로는 쓰기 직후의 읽기는 Master에서 수행하는 방법이 가장 간단하고, MySQL의 GTID(Global Transaction ID)를 활용하여 Slave의 반영 상태를 확인하는 방법, 반동기 복제를 적용하는 방법이 있습니다.

**Q5.** Connection Pool이란 무엇이고, 왜 필요한가요? HikariCP의 주요 설정 항목을 설명해주세요.

> Connection Pool은 DB 연결을 미리 여러 개 만들어 놓고 재사용하는 기술입니다. 매 요청마다 Connection을 생성하면 TCP 3-way handshake, DB 인증 등으로 약 50ms의 오버헤드가 발생하는데, Pool에서 가져오면 0.1ms 수준으로 줄어듭니다. Spring Boot 2.0부터 기본 Connection Pool인 HikariCP의 주요 설정은 다음과 같습니다. maximumPoolSize(기본 10)는 최대 Connection 수로, 너무 크면 DB 자원이 낭비되고 너무 작으면 대기가 발생합니다. minimumIdle은 유휴 상태로 유지할 최소 Connection 수인데, HikariCP는 maximumPoolSize와 같게 설정하는 고정 크기 풀을 권장합니다. connectionTimeout(기본 30초)은 Connection 획득 대기 최대 시간입니다. 일반적으로 CPU 코어 수 x 2 + 디스크 수를 기준으로 Pool 크기를 설정합니다.

## 비교/구분 (6~9)

**Q6.** Scale Up과 Scale Out의 차이를 설명해주세요. DB 확장에서는 어떤 방식이 더 적합한가요?

> Scale Up은 기존 서버의 사양(CPU, RAM, SSD)을 높이는 수직 확장이고, Scale Out은 서버 수를 늘리는 수평 확장입니다. Scale Up은 구현이 간단하고 기존 구조를 유지할 수 있지만, 고사양 장비의 가격이 기하급수적으로 증가하고 물리적 한계가 있으며 여전히 단일 장애점입니다. Scale Out은 비용이 선형적이고 이론적으로 무한 확장이 가능하며 가용성도 높아지지만, 분산 시스템 설계의 복잡도가 추가됩니다. DB 확장에서는 초기에 Scale Up으로 빠르게 대응하고, 트래픽이 일정 수준을 넘으면 Replication이나 Sharding 같은 Scale Out 방식으로 전환하는 것이 일반적입니다.

**Q7.** Partitioning과 Sharding의 차이는 무엇인가요?

> Partitioning은 하나의 DB 서버 내에서 큰 테이블을 여러 조각으로 나누는 것이고, Sharding은 여러 개의 독립된 DB 서버에 데이터를 분산하는 것입니다. Partitioning은 같은 서버의 자원(CPU, 메모리)을 공유하므로 서버 스펙에 한정되지만, 쿼리 시 필요한 파티션만 스캔(Partition Pruning)하여 성능을 높일 수 있습니다. DB 레벨 설정만으로 가능합니다. Sharding은 각 Shard가 독립 서버이므로 자원이 별도이고 서버 추가로 무한 확장이 가능하지만, Cross-shard JOIN 불가, 글로벌 유니크 ID 관리, 분산 트랜잭션 등의 복잡도가 추가되며, 애플리케이션 레벨에서 라우팅 로직이 필요합니다.

**Q8.** 수평 파티셔닝과 수직 파티셔닝의 차이를 설명하고, 각각의 사용 사례를 들어주세요.

> 수평 파티셔닝(Horizontal Partitioning)은 행(Row) 단위로 테이블을 분할하는 것입니다. 예를 들어 주문 테이블을 날짜 기준으로 2023년 데이터, 2024년 데이터로 나누는 것입니다. 데이터가 매우 많은 테이블에서 특정 범위만 조회하는 경우에 적합합니다. 수직 파티셔닝(Vertical Partitioning)은 열(Column) 단위로 테이블을 분할하는 것입니다. 예를 들어 회원 테이블에서 자주 조회하는 이름, 이메일과 가끔 조회하는 대용량 프로필 이미지, 자기소개를 별도 테이블로 분리하는 것입니다. 컬럼별 접근 빈도가 크게 다르거나 BLOB 같은 대용량 컬럼이 있는 경우에 적합합니다.

**Q9.** Range Sharding과 Hash Sharding의 장단점을 비교해주세요.

> Range Sharding은 Shard Key의 값 범위로 분할하는 방식입니다. 예를 들어 user_id 1~100만은 Shard 1, 100만~200만은 Shard 2처럼 나눕니다. 범위 조회가 효율적이고 구현이 간단하지만, 최근 가입자가 마지막 Shard에 집중되는 Hotspot 문제가 발생할 수 있습니다. Hash Sharding은 hash(key) % shard_count로 분할하는 방식입니다. 데이터가 균등하게 분배되어 Hotspot이 발생하지 않지만, 범위 조회 시 모든 Shard를 조회해야 하고, Shard 수가 변경되면 대부분의 데이터 재배치(리밸런싱)가 필요합니다. 이를 해결하기 위해 Consistent Hashing을 적용할 수 있습니다.

## 심화/실무 (10~12)

**Q10.** Spring Boot에서 Read/Write 분리를 어떻게 구현하나요? AbstractRoutingDataSource의 동작 원리를 설명해주세요.

> Spring Boot에서는 AbstractRoutingDataSource를 상속하여 Read/Write를 분리합니다. 이 클래스의 determineCurrentLookupKey() 메서드를 오버라이드하여, TransactionSynchronizationManager.isCurrentTransactionReadOnly()로 현재 트랜잭션이 readOnly인지 확인합니다. readOnly가 true면 "slave" 키를, false면 "master" 키를 반환하고, 이 키에 매핑된 DataSource로 연결됩니다. 설정 시 Master와 Slave의 DataSource를 각각 Bean으로 등록하고, RoutingDataSource에 targetDataSources Map으로 등록합니다. Service 계층에서 @Transactional(readOnly = true)를 붙이면 자동으로 Slave로 라우팅되고, @Transactional만 붙이면 Master로 라우팅됩니다. 주의할 점은 LazyConnectionDataSourceProxy를 함께 사용해야 실제 쿼리 실행 시점에 DataSource가 결정된다는 것입니다.

**Q11.** Sharding 도입 시 발생하는 문제점(Cross-shard JOIN, 글로벌 유니크 ID 등)과 해결 방안을 설명해주세요.

> Sharding 도입 시 주요 문제점과 해결 방안은 다음과 같습니다. 첫째, Cross-shard JOIN 불가 문제입니다. 다른 Shard에 있는 테이블 간 JOIN이 불가능하므로, 애플리케이션 레벨에서 각 Shard를 별도로 조회 후 병합하거나, 자주 JOIN하는 데이터를 반정규화하여 같은 Shard에 둡니다. 둘째, 글로벌 유니크 ID 문제입니다. 각 Shard의 AUTO_INCREMENT가 독립적이므로 ID가 중복될 수 있습니다. Twitter의 Snowflake처럼 타임스탬프 + 머신ID + 시퀀스를 조합한 분산 ID 생성기를 사용하거나, UUID를 사용합니다. 셋째, 리밸런싱 문제입니다. Shard 추가 시 데이터 재분배가 필요한데, Consistent Hashing을 적용하면 최소한의 데이터만 이동시킬 수 있습니다. 넷째, 분산 트랜잭션 문제입니다. 여러 Shard에 걸친 트랜잭션이 어려우므로 Saga Pattern이나 2PC(Two-Phase Commit)를 활용합니다.

**Q12.** 서비스가 성장할 때 DB 확장은 어떤 순서로 진행해야 하나요? 각 단계별 이유를 설명해주세요.

> DB 확장은 비용 대비 효과가 큰 순서로 점진적으로 진행해야 합니다. 1단계는 인덱스 최적화와 쿼리 튜닝입니다. 비용이 가장 낮고 효과가 크며, 슬로우 쿼리를 분석하여 적절한 인덱스를 추가하고 쿼리를 개선합니다. 2단계는 캐시 도입(Redis)입니다. 자주 조회되는 데이터를 캐싱하면 DB 읽기 부하를 70% 이상 줄일 수 있습니다. 3단계는 Replication입니다. Master-Slave 구조로 읽기를 분산하고, Master 장애 시 Failover로 가용성을 확보합니다. 이 단계까지 대부분의 서비스를 커버할 수 있습니다. 4단계는 Sharding입니다. 쓰기 트래픽도 한계에 도달하면 데이터를 여러 서버에 분산합니다. 운영 복잡도가 크게 증가하므로 정말 필요한 시점에 도입해야 합니다. 처음부터 Sharding을 적용하는 것은 과도한 설계(Over-Engineering)입니다.

## 꼬리질문 대비 (13~15)

**Q13.** Consistent Hashing이란 무엇이고, 일반 Hash Sharding 대비 어떤 장점이 있나요?

> Consistent Hashing은 해시 공간을 링(Ring) 형태로 구성하여 노드(Shard)와 키를 같은 해시 함수로 링 위에 배치하고, 각 키를 시계 방향으로 가장 가까운 노드에 할당하는 방식입니다. 일반 Hash Sharding에서 Shard를 추가하면 hash(key) % N에서 N이 변경되어 거의 모든 데이터(약 75% 이상)의 재배치가 필요합니다. 반면 Consistent Hashing에서는 Shard 추가 시 인접한 키만 이동하면 되므로 약 1/N의 데이터만 재배치됩니다. 추가로 Virtual Node를 사용하여 각 물리 노드를 여러 가상 노드로 링에 배치하면 데이터 분포의 균등성을 높일 수 있습니다. Amazon DynamoDB, Apache Cassandra 등에서 이 방식을 사용합니다.

**Q14.** Shard Key를 잘못 선택하면 어떤 문제가 발생하나요? 좋은 Shard Key의 조건은 무엇인가요?

> Shard Key를 잘못 선택하면 데이터 편향(Hotspot)이 발생합니다. 예를 들어 성별을 Shard Key로 사용하면 남/여 두 Shard에만 나뉘어 확장성이 없고, 가입일을 Shard Key로 쓰면 최근 데이터가 하나의 Shard에 집중됩니다. 특정 Shard에 데이터가 몰리면 해당 서버의 CPU, 메모리, 디스크 I/O가 과부하되어 Sharding의 의미가 없어집니다. 좋은 Shard Key의 조건은 세 가지입니다. 첫째, 높은 카디널리티(값의 종류가 많음)로 균등 분배가 가능해야 합니다. 둘째, 데이터 분포가 균등하여 특정 Shard에 몰리지 않아야 합니다. 셋째, 주요 쿼리 패턴과 일치하여 Cross-shard 조회를 최소화해야 합니다. user_id는 카디널리티가 높고 균등 분배가 가능하며, 대부분의 조회가 사용자 기반이므로 좋은 Shard Key 후보입니다.

**Q15.** Replication 환경에서 Master 장애가 발생하면 어떻게 대응하나요? Failover 과정을 설명해주세요.

> Master 장애 발생 시 Failover를 통해 Slave 중 하나를 새 Master로 승격시킵니다. 과정은 다음과 같습니다. 첫째, 장애 감지입니다. Health Check(Heartbeat)를 통해 Master 무응답을 탐지합니다. 일반적으로 여러 번 연속 실패해야 장애로 판단합니다(오탐 방지). 둘째, Slave 선택입니다. 가장 최신 데이터를 가진 Slave(Replication Lag이 가장 적은)를 새 Master 후보로 선정합니다. 셋째, Slave를 Master로 승격합니다. 선택된 Slave의 읽기 전용 모드를 해제하고 쓰기를 허용합니다. 넷째, 나머지 Slave를 새 Master에 연결합니다. 다른 Slave들이 새 Master를 복제 원본으로 바라보도록 변경합니다. 다섯째, 애플리케이션의 DB 연결 정보를 갱신합니다. 이 과정을 수동으로 하면 시간이 오래 걸리므로, MySQL MHA(Master High Availability)나 Orchestrator 같은 자동 Failover 도구를 사용합니다. 자동 Failover 시 주의할 점은 Split-Brain(두 노드가 동시에 Master 역할)을 방지해야 한다는 것입니다.
