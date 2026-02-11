# Spring Data JPA 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** JPA란 무엇이며, ORM이 필요한 이유를 설명해주세요.

> JPA(Java Persistence API)는 Java ORM의 표준 인터페이스(스펙)이며, 가장 많이 사용되는 구현체는 Hibernate입니다. ORM(Object-Relational Mapping)은 객체와 관계형 데이터베이스 테이블을 매핑하는 기술입니다. ORM이 없으면 SQL을 직접 작성하고 ResultSet에서 수동으로 객체에 매핑해야 하는 반복적이고 실수하기 쉬운 작업이 필요합니다. ORM을 사용하면 `entityManager.find(User.class, userId)`처럼 객체로 바로 조회할 수 있고, SQL은 JPA가 자동으로 생성합니다. Spring Data JPA는 JPA(Hibernate) 위에 Spring이 편리한 기능(Repository 인터페이스, 메서드 이름 쿼리 등)을 추가한 것입니다.

**Q2.** 영속성 컨텍스트(Persistence Context)란 무엇이며, 어떤 이점이 있나요?

> 영속성 컨텍스트는 엔티티를 관리하는 JPA의 논리적 영역으로, EntityManager를 통해 접근합니다. Spring에서는 기본적으로 트랜잭션 단위로 생성되고 소멸됩니다. 세 가지 핵심 이점이 있습니다. 첫째, 1차 캐시로 같은 트랜잭션 내에서 동일 엔티티를 여러 번 조회해도 DB 쿼리는 1번만 실행됩니다. 둘째, 변경 감지(Dirty Checking)로 엔티티의 필드를 수정하기만 하면 별도 save 없이 트랜잭션 커밋 시 자동으로 UPDATE SQL이 실행됩니다. 셋째, 쓰기 지연으로 SQL을 모아서 커밋 시점에 한 번에 전송하여 네트워크 효율을 높입니다.

**Q3.** 변경 감지(Dirty Checking)는 어떻게 동작하나요?

> 영속성 컨텍스트가 엔티티를 1차 캐시에 저장할 때 해당 시점의 상태를 스냅샷으로 함께 보관합니다. 이후 엔티티의 필드를 setter로 변경하면 1차 캐시의 엔티티는 변경되지만 스냅샷은 원래 상태를 유지합니다. 트랜잭션 커밋 시점에 flush()가 호출되면, 1차 캐시의 현재 엔티티와 스냅샷을 비교하여 변경된 필드가 있으면 자동으로 UPDATE SQL을 생성하여 실행합니다. 변경이 없으면 아무것도 실행하지 않습니다. 따라서 JPA에서는 엔티티 객체를 수정하기만 하면 별도의 save()를 호출하지 않아도 DB에 자동으로 반영됩니다.

**Q4.** N+1 문제란 무엇이며, 왜 발생하는지 설명해주세요.

> N+1 문제는 연관된 엔티티를 조회할 때, 1번의 쿼리로 N개의 엔티티를 가져온 후 각각의 연관 엔티티를 조회하기 위해 N번의 추가 쿼리가 실행되는 문제입니다. 예를 들어 Team 3개를 조회(1번 쿼리)하고, 각 Team의 Members를 접근하면 팀마다 멤버 조회 쿼리가 실행되어 총 1+3=4번의 쿼리가 발생합니다. 팀이 100개면 101번의 쿼리가 실행되어 성능이 크게 저하됩니다. 이 문제는 LAZY 로딩에서 연관 엔티티에 실제 접근할 때 발생하며, EAGER 로딩에서도 JPQL을 사용하면 동일하게 발생할 수 있습니다.

**Q5.** 엔티티의 생명주기(비영속, 영속, 준영속, 삭제) 4가지 상태를 설명해주세요.

> 비영속(New) 상태는 new로 객체를 생성만 한 순수 Java 객체 상태로, 영속성 컨텍스트와 무관합니다. 영속(Managed) 상태는 persist()나 find()를 통해 영속성 컨텍스트가 관리하는 상태로, 1차 캐시와 변경 감지의 대상이 됩니다. 준영속(Detached) 상태는 영속 상태였다가 detach()나 clear()로 영속성 컨텍스트에서 분리된 상태로, DB에는 데이터가 있지만 더 이상 관리되지 않아 변경 감지가 동작하지 않습니다. 삭제(Removed) 상태는 remove()로 삭제 요청된 상태로, 커밋 시 DELETE SQL이 실행됩니다.

## 비교/구분 (6~9)

**Q6.** 즉시 로딩(EAGER)과 지연 로딩(LAZY)의 차이를 설명해주세요. 실무에서는 어떤 전략을 사용하나요?

> 즉시 로딩(EAGER)은 엔티티를 조회할 때 연관 엔티티도 JOIN으로 함께 즉시 조회합니다. 지연 로딩(LAZY)은 연관 엔티티에 실제 접근할 때까지 조회를 지연하며, 프록시 객체를 넣어두었다가 접근 시점에 DB 쿼리를 실행합니다. @ManyToOne, @OneToOne의 기본값은 EAGER이고, @OneToMany, @ManyToMany는 LAZY입니다. 실무에서는 모든 연관관계를 LAZY로 설정하는 것이 원칙입니다. EAGER는 예상치 못한 쿼리가 실행되고 N+1 문제를 유발하기 쉽기 때문입니다. 필요한 경우 Fetch Join이나 EntityGraph로 한 번에 가져옵니다.

**Q7.** Fetch Join과 @EntityGraph의 차이를 설명해주세요.

> Fetch Join은 JPQL에서 `JOIN FETCH` 키워드를 사용하여 연관 엔티티를 한 번의 쿼리로 함께 조회합니다. `@Query` 어노테이션에 직접 JPQL을 작성해야 하며, 복잡한 조건이나 여러 연관관계를 세밀하게 제어할 수 있습니다. @EntityGraph는 어노테이션의 attributePaths에 함께 조회할 연관 엔티티를 지정하여 Fetch Join과 같은 효과를 냅니다. JPQL을 직접 작성하지 않아도 되어 간편하고, 메서드 이름 쿼리에도 적용 가능합니다. 두 방법 모두 컬렉션 연관관계에서 페이징이 불가하다는 제약이 동일합니다. 간단한 경우 @EntityGraph, 복잡한 조건이 필요하면 Fetch Join을 사용합니다.

**Q8.** JPQL과 Native Query의 차이를 설명해주세요.

> JPQL(Java Persistence Query Language)은 엔티티 객체를 대상으로 쿼리하는 언어입니다. 테이블명 대신 엔티티명, 컬럼명 대신 필드명을 사용하며, DB에 독립적입니다. 예를 들어 `SELECT u FROM User u WHERE u.email = :email`처럼 작성합니다. Native Query는 실제 SQL을 직접 작성하는 방식으로, `@Query(nativeQuery = true)`로 사용합니다. DB에 종속적이지만 JPA로 표현하기 어려운 복잡한 쿼리나 DB 특화 기능을 사용할 수 있습니다. 일반적으로는 JPQL을 사용하고, 성능 최적화나 복잡한 집계 쿼리 등 특수한 경우에만 Native Query를 사용합니다.

**Q9.** JPA의 save()를 호출할 때, INSERT와 UPDATE가 어떻게 구분되나요?

> Spring Data JPA의 save() 메서드는 엔티티의 식별자(ID) 값을 기준으로 구분합니다. ID가 null이면 새로운 엔티티로 판단하여 persist()(INSERT)를 호출하고, ID가 이미 존재하면 기존 엔티티로 판단하여 merge()(UPDATE)를 호출합니다. 다만 변경 감지(Dirty Checking)가 있으므로, 영속 상태의 엔티티를 수정할 때는 save()를 호출할 필요 없이 엔티티의 필드만 변경하면 트랜잭션 커밋 시 자동으로 UPDATE가 실행됩니다. 새로운 엔티티를 생성할 때만 save()를 호출하면 됩니다. @GeneratedValue로 ID를 자동 생성하면 persist 시점에 ID가 할당됩니다.

## 심화/실무 (10~12)

**Q10.** N+1 문제의 해결방법 3가지(Fetch Join, @EntityGraph, @BatchSize)를 비교하고, 실무에서 가장 권장되는 방법을 설명해주세요.

> Fetch Join은 JPQL에서 `JOIN FETCH`로 한 번의 쿼리에 연관 엔티티를 모두 조회합니다. 쿼리 1번으로 해결되지만, 컬렉션 Fetch Join 시 페이징이 불가능하고 중복 데이터가 발생할 수 있어 DISTINCT가 필요합니다. @EntityGraph도 쿼리 1번으로 해결되며 어노테이션으로 간편하지만 같은 페이징 제약이 있습니다. @BatchSize는 IN 절을 사용하여 연관 엔티티를 한 번에 조회하므로 2번의 쿼리가 필요하지만, 페이징이 가능합니다. 실무에서는 글로벌로 `default_batch_fetch_size: 100`을 설정하여 기본적으로 N+1을 방지하고, 특수한 경우에만 Fetch Join을 사용하는 것이 관리하기 편합니다.

**Q11.** 연관관계의 주인이란 무엇이며, 양방향 매핑 시 주의사항을 설명해주세요.

> 양방향 매핑에서 외래 키(FK)를 실제로 관리하는 쪽을 연관관계의 주인이라 합니다. DB 테이블에서 FK가 있는 쪽, 즉 @ManyToOne이 있는 엔티티가 주인이며, 주인만 외래 키 값을 등록/수정할 수 있습니다. 반대쪽은 `mappedBy`로 읽기만 가능함을 선언합니다. 주의사항으로, 주인이 아닌 쪽만 설정하면(`team.getMembers().add(member)`) DB에 반영되지 않고, 주인 쪽에 설정해야(`member.setTeam(team)`) 합니다. 안전하게 양쪽 모두 설정하는 편의 메서드를 만드는 것이 좋습니다. 실무에서는 가능하면 단방향(@ManyToOne만)으로 설계하고, 필요 시 양방향을 추가합니다.

**Q12.** ddl-auto 옵션의 종류와 환경별 권장 설정을 설명해주세요.

> ddl-auto는 JPA가 엔티티 기반으로 DDL을 어떻게 처리할지 결정하는 옵션입니다. create는 기존 테이블을 삭제하고 재생성하여 초기 개발에 사용합니다. create-drop은 애플리케이션 종료 시 테이블을 삭제하여 테스트에 사용합니다. update는 변경분만 반영(컬럼 추가 등)하여 개발 단계에 사용합니다. validate는 엔티티와 테이블의 일치 여부만 검증하고 DDL을 실행하지 않아 운영 환경에 권장됩니다. none은 아무것도 하지 않습니다. 운영에서는 반드시 validate 또는 none을 사용해야 합니다. update를 운영에서 사용하면 의도치 않은 DDL이 실행되어 장애가 발생할 수 있습니다.

## 꼬리질문 대비 (13~15)

**Q13.** LAZY 로딩에서 프록시 객체란 무엇이며, LazyInitializationException은 왜 발생하나요?

> LAZY 로딩 시 연관 엔티티에는 실제 객체 대신 Hibernate가 생성한 프록시 객체(`Team$$HibernateProxy$$xxx`)가 들어갑니다. 프록시는 실제 데이터 없이 껍데기만 있다가, getName() 같은 메서드가 호출되면 그 시점에 DB 쿼리를 실행하여 실제 데이터를 채웁니다. LazyInitializationException은 영속성 컨텍스트가 종료된(트랜잭션이 끝난) 후에 프록시 객체에 접근할 때 발생합니다. 영속성 컨텍스트가 없으면 DB 조회를 할 수 없기 때문입니다. 해결하려면 트랜잭션 내에서 필요한 데이터를 미리 로드하거나, Fetch Join/EntityGraph를 사용합니다. OSIV(Open Session In View)를 true로 설정하면 해결되지만, 성능 문제가 있어 실무에서는 false로 설정하는 것이 권장됩니다.

**Q14.** Entity 설계 시 @Enumerated(EnumType.STRING)을 사용해야 하는 이유는 무엇인가요?

> @Enumerated에는 ORDINAL(기본값)과 STRING 두 가지 옵션이 있습니다. ORDINAL은 enum의 순서(0, 1, 2...)를 DB에 저장합니다. 문제는 enum에 새로운 값을 추가하거나 순서를 변경하면, 기존에 저장된 숫자의 의미가 달라져 심각한 데이터 오류가 발생한다는 것입니다. 예를 들어 ACTIVE(0), INACTIVE(1) 사이에 PENDING을 추가하면 기존의 INACTIVE(1)가 PENDING으로 해석됩니다. STRING은 enum의 이름("ACTIVE", "INACTIVE")을 문자열로 저장하므로, 순서 변경이나 값 추가에도 기존 데이터에 영향이 없습니다. 따라서 실무에서는 반드시 EnumType.STRING을 사용해야 합니다.

**Q15.** 쓰기 지연(Write-Behind)이란 무엇이며, 어떤 장점이 있나요?

> 쓰기 지연은 persist()를 호출해도 즉시 INSERT SQL을 DB에 전송하지 않고, 영속성 컨텍스트의 쓰기 지연 SQL 저장소에 모아두었다가 트랜잭션 커밋 시점(flush)에 한꺼번에 전송하는 메커니즘입니다. 장점은 세 가지입니다. 첫째, SQL을 모아서 한 번에 전송하므로 네트워크 왕복 횟수가 줄어 효율적입니다. 둘째, 커밋 전까지 SQL을 최적화할 수 있습니다. 셋째, `hibernate.jdbc.batch_size` 설정을 통해 JDBC 배치 기능을 활용하면 다수의 INSERT를 하나의 네트워크 요청으로 묶어 전송할 수 있어 대량 데이터 저장 시 성능이 크게 향상됩니다.
