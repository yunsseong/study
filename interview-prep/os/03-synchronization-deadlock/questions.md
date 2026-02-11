# 동기화와 데드락 면접 질문

> 질문을 보고 직접 답변해보세요. 답변은 answers.md에서 확인할 수 있습니다.

---

## 기본 개념

**Q1.** Race Condition(경쟁 상태)이 무엇이며, 왜 발생하나요?

**Q2.** 임계 영역(Critical Section)이 무엇이고, 임계 영역 문제를 해결하기 위한 3가지 조건을 설명해주세요.

**Q3.** Mutex와 Semaphore의 차이점을 설명해주세요.

**Q4.** Monitor가 무엇이며, Mutex/Semaphore와 어떻게 다른가요?

**Q5.** 데드락(Deadlock)의 정의와 발생 조건 4가지를 설명해주세요.

---

## 비교/구분

**Q6.** Mutex, Binary Semaphore, Counting Semaphore를 비교 설명해주세요.

**Q7.** Spinlock과 일반 Lock(Sleep Lock)의 차이와 각각의 사용 시나리오를 설명해주세요.

**Q8.** 데드락 처리 방법 4가지(예방, 회피, 탐지, 무시)를 설명하고, 각각의 장단점을 비교해주세요.

**Q9.** Java의 synchronized 키워드와 ReentrantLock의 차이점은 무엇인가요?

---

## 심화/실무

**Q10.** 은행원 알고리즘(Banker's Algorithm)이 무엇이며, 어떻게 데드락을 회피하나요?

**Q11.** 데이터베이스에서 데드락이 발생하는 상황과 해결 방법을 설명해주세요.

**Q12.** Spring Boot 애플리케이션에서 동시성 문제가 발생할 수 있는 상황과 해결 방법을 설명해주세요.

**Q13.** Reader-Writer Problem과 그 해결 방법을 설명해주세요.

---

## 꼬리질문

**Q14.** 데드락과 기아(Starvation), 라이브락(Livelock)의 차이는 무엇인가요?

**Q15.** Java에서 synchronized 블록과 synchronized 메서드의 차이는 무엇인가요?
