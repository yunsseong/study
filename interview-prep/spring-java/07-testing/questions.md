# 테스트 (JUnit/Mockito) 면접 질문

## 기본 개념 (1~5)

**Q1.** 테스트 코드를 작성하는 이유는 무엇인가요? 테스트 코드가 주는 이점을 설명해주세요.

**Q2.** 단위 테스트, 통합 테스트, E2E 테스트의 차이를 설명하고, 테스트 피라미드에 대해 설명해주세요.

**Q3.** JUnit 5에서 @BeforeEach와 @BeforeAll의 차이를 설명해주세요. 각각 언제 사용하나요?

**Q4.** Mockito에서 Mock 객체란 무엇이며, 왜 사용하나요?

**Q5.** Given-When-Then 패턴이란 무엇인가요? 예시를 들어 설명해주세요.

## 비교/구분 (6~9)

**Q6.** @Mock과 @MockBean의 차이를 설명해주세요. 각각 어떤 상황에서 사용하나요?

**Q7.** @WebMvcTest와 @SpringBootTest의 차이를 설명해주세요. Controller를 테스트할 때 어떤 것을 선택해야 하나요?

**Q8.** @Mock과 @Spy의 차이를 설명해주세요.

**Q9.** MockMvc와 TestRestTemplate의 차이를 설명해주세요.

## 심화/실무 (10~12)

**Q10.** @WebMvcTest에서 MockMvc를 사용하여 POST 요청을 테스트하는 코드를 작성해주세요.

**Q11.** TDD(Test-Driven Development)란 무엇이며, 그 사이클을 설명해주세요. 장단점은 무엇인가요?

**Q12.** 테스트 커버리지에서 라인 커버리지와 브랜치 커버리지의 차이를 설명해주세요. 커버리지가 높으면 품질이 보장되나요?

## 꼬리질문 대비 (13~15)

**Q13.** Service 계층의 단위 테스트에서 Repository를 Mock 처리했을 때, 실제 DB와 동작이 다를 수 있는 문제는 어떻게 해결하나요?

**Q14.** @Transactional을 테스트에 붙이면 어떻게 동작하나요? 테스트에서 @Transactional 사용 시 주의할 점은 무엇인가요?

**Q15.** BDDMockito의 given().willReturn()과 Mockito의 when().thenReturn()은 무엇이 다른가요? 왜 BDDMockito를 사용하나요?
