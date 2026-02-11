# Cookie/Session/JWT 면접 질문

> 질문을 보고 직접 답변해보세요. 답변은 answers.md에서 확인할 수 있습니다.

---

## 기본 개념 (1~5)

**Q1.** HTTP가 Stateless하다는 것은 무슨 뜻이고, 왜 상태 유지가 필요한가요?

**Q2.** Cookie란 무엇이고, 어떻게 동작하나요? (Set-Cookie, Cookie 헤더)

**Q3.** Cookie의 HttpOnly, Secure, SameSite 속성은 각각 무엇을 방어하나요?

**Q4.** Session이란 무엇이고, 어떻게 동작하나요?

**Q5.** JWT(JSON Web Token)란 무엇이고, 구조(Header, Payload, Signature)를 설명해주세요.

---

## 비교/구분 (6~9)

**Q6.** Cookie와 Session의 차이는 무엇인가요?

**Q7.** Session 방식과 JWT 방식을 비교해주세요. 각각 언제 적합한가요?

**Q8.** JWT를 LocalStorage에 저장하는 것과 HttpOnly Cookie에 저장하는 것의 차이는 무엇인가요?

**Q9.** Access Token과 Refresh Token은 왜 분리하나요? 각각의 역할은 무엇인가요?

---

## 심화/실무 (10~12)

**Q10.** 서버가 여러 대일 때 Session을 어떻게 관리하나요? (Sticky Session, Session Clustering, Redis)

**Q11.** Spring Security + JWT 인증 흐름을 설명해주세요.

**Q12.** OAuth 2.0이란 무엇이고, 소셜 로그인(구글/카카오)은 어떤 흐름으로 동작하나요?

---

## 꼬리질문 대비 (13~15)

**Q13.** JWT의 Payload는 암호화되어 있나요? 민감 정보를 넣어도 되나요?

**Q14.** JWT를 서버에서 강제로 만료시킬 수 있나요? 어떻게 해야 하나요?

**Q15.** XSS와 CSRF 공격의 차이는 무엇이고, 각각 어떻게 방어하나요?
