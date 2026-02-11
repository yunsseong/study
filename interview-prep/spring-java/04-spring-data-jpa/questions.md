# Spring Data JPA 면접 질문

## 기본 개념 (1~5)

**Q1.** JPA란 무엇이며, ORM이 필요한 이유를 설명해주세요.

**Q2.** 영속성 컨텍스트(Persistence Context)란 무엇이며, 어떤 이점이 있나요?

**Q3.** 변경 감지(Dirty Checking)는 어떻게 동작하나요?

**Q4.** N+1 문제란 무엇이며, 왜 발생하는지 설명해주세요.

**Q5.** 엔티티의 생명주기(비영속, 영속, 준영속, 삭제) 4가지 상태를 설명해주세요.

## 비교/구분 (6~9)

**Q6.** 즉시 로딩(EAGER)과 지연 로딩(LAZY)의 차이를 설명해주세요. 실무에서는 어떤 전략을 사용하나요?

**Q7.** Fetch Join과 @EntityGraph의 차이를 설명해주세요.

**Q8.** JPQL과 Native Query의 차이를 설명해주세요.

**Q9.** JPA의 save()를 호출할 때, INSERT와 UPDATE가 어떻게 구분되나요?

## 심화/실무 (10~12)

**Q10.** N+1 문제의 해결방법 3가지(Fetch Join, @EntityGraph, @BatchSize)를 비교하고, 실무에서 가장 권장되는 방법을 설명해주세요.

**Q11.** 연관관계의 주인이란 무엇이며, 양방향 매핑 시 주의사항을 설명해주세요.

**Q12.** ddl-auto 옵션의 종류와 환경별 권장 설정을 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** LAZY 로딩에서 프록시 객체란 무엇이며, LazyInitializationException은 왜 발생하나요?

**Q14.** Entity 설계 시 @Enumerated(EnumType.STRING)을 사용해야 하는 이유는 무엇인가요?

**Q15.** 쓰기 지연(Write-Behind)이란 무엇이며, 어떤 장점이 있나요?
