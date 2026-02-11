# TCP Handshake 면접 질문

> 질문을 보고 직접 답변해보세요. 답변은 answers.md에서 확인할 수 있습니다.

---

## 기본 개념

**Q1.** TCP 3-way handshake 과정을 설명해주세요.

**Q2.** 3-way handshake에서 SYN, ACK가 각각 무엇인가요?

**Q3.** TCP 4-way handshake 과정을 설명해주세요.

**Q4.** 3-way handshake 각 단계에서 클라이언트와 서버의 상태 변화를 설명해주세요.

**Q5.** 4-way handshake 각 단계에서 클라이언트와 서버의 상태 변화를 설명해주세요.

---

## 비교/구분

**Q6.** 왜 연결은 3-way인데 종료는 4-way인가요?

**Q7.** 왜 2-way가 아니라 3-way handshake인가요?

**Q8.** Half-Close란 무엇인가요?

---

## 심화/실무

**Q9.** TIME_WAIT 상태는 무엇이고, 왜 필요한가요?

**Q10.** 서버에 TIME_WAIT이 대량으로 쌓이면 어떤 문제가 생기나요? 어떻게 해결하나요?

**Q11.** SYN Flooding 공격은 무엇이고, 어떻게 방어하나요?

**Q12.** CLOSE_WAIT 상태가 서버에 쌓이면 어떤 문제가 있나요? 원인은 무엇인가요?

---

## 꼬리질문

**Q13.** 시퀀스 번호는 왜 0이 아니라 랜덤 값에서 시작하나요?

**Q14.** HTTP Keep-Alive는 handshake와 어떤 관계가 있나요?

**Q15.** Spring Boot 서버에 사용자가 접속할 때 3-way handshake는 누가 처리하나요?
