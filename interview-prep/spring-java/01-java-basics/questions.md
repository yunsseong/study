# Java 기초 & JVM 면접 질문

## 기본 개념 (1~5)

**Q1.** JVM이란 무엇이며, Java가 "Write Once, Run Anywhere"를 실현할 수 있는 이유를 설명해주세요.

**Q2.** JVM의 메모리 구조를 설명해주세요. 각 영역이 어떤 데이터를 저장하는지, 스레드 간 공유 여부를 포함하여 답변해주세요.

**Q3.** Garbage Collection(GC)이란 무엇이며, 기본적인 동작 원리(Mark & Sweep)를 설명해주세요.

**Q4.** Java 컬렉션 프레임워크에서 List, Set, Map의 차이점을 설명해주세요.

**Q5.** HashMap의 내부 동작 원리를 설명해주세요. key를 통해 value를 조회하는 과정을 포함해주세요.

## 비교/구분 (6~9)

**Q6.** Stack 영역과 Heap 영역의 차이를 설명하고, 참조 타입 변수가 어떻게 저장되는지 설명해주세요.

**Q7.** ArrayList와 LinkedList의 차이점을 설명해주세요. 실무에서는 어떤 것을 주로 사용하나요?

**Q8.** Minor GC와 Major GC(Full GC)의 차이를 설명해주세요.

**Q9.** HashSet, LinkedHashSet, TreeSet의 차이를 설명해주세요.

## 심화/실무 (10~12)

**Q10.** equals()와 hashCode()를 왜 함께 재정의해야 하나요? 둘 중 하나만 재정의하면 어떤 문제가 발생하는지 구체적으로 설명해주세요.

**Q11.** G1 GC의 특징과 기존 GC와의 차이점을 설명해주세요.

**Q12.** HashMap에서 해시 충돌이 발생하면 어떻게 처리하나요? Java 8에서 개선된 점은 무엇인가요?

## 꼬리질문 대비 (13~15)

**Q13.** Spring Boot 애플리케이션에서 OutOfMemoryError가 발생했을 때, 어떤 원인을 의심하고 어떻게 대응하시겠습니까?

**Q14.** JIT Compiler란 무엇이며, Interpreter와 어떻게 협력하여 동작하나요?

**Q15.** HashMap의 초기 용량(capacity)과 Load Factor는 무엇이며, 왜 적절하게 설정해야 하나요?
