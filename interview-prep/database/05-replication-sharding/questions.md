# DB Replication/Sharding 면접 질문

## 기본 개념 (1~5)

**Q1.** DB Replication이란 무엇이고, 왜 필요한가요?

**Q2.** Master-Slave 구조에서 Write와 Read는 각각 어디서 처리되나요? 이렇게 분리하는 이유는 무엇인가요?

**Q3.** 동기 복제와 비동기 복제의 차이를 설명해주세요. 각각 어떤 상황에 적합한가요?

**Q4.** Replication Lag이란 무엇이고, 어떤 문제를 일으킬 수 있나요?

**Q5.** Connection Pool이란 무엇이고, 왜 필요한가요? HikariCP의 주요 설정 항목을 설명해주세요.

## 비교/구분 (6~9)

**Q6.** Scale Up과 Scale Out의 차이를 설명해주세요. DB 확장에서는 어떤 방식이 더 적합한가요?

**Q7.** Partitioning과 Sharding의 차이는 무엇인가요?

**Q8.** 수평 파티셔닝과 수직 파티셔닝의 차이를 설명하고, 각각의 사용 사례를 들어주세요.

**Q9.** Range Sharding과 Hash Sharding의 장단점을 비교해주세요.

## 심화/실무 (10~12)

**Q10.** Spring Boot에서 Read/Write 분리를 어떻게 구현하나요? AbstractRoutingDataSource의 동작 원리를 설명해주세요.

**Q11.** Sharding 도입 시 발생하는 문제점(Cross-shard JOIN, 글로벌 유니크 ID 등)과 해결 방안을 설명해주세요.

**Q12.** 서비스가 성장할 때 DB 확장은 어떤 순서로 진행해야 하나요? 각 단계별 이유를 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** Consistent Hashing이란 무엇이고, 일반 Hash Sharding 대비 어떤 장점이 있나요?

**Q14.** Shard Key를 잘못 선택하면 어떤 문제가 발생하나요? 좋은 Shard Key의 조건은 무엇인가요?

**Q15.** Replication 환경에서 Master 장애가 발생하면 어떻게 대응하나요? Failover 과정을 설명해주세요.
