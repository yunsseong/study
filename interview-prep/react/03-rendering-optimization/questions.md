# React 렌더링 & 최적화 면접 질문

## 기본 개념 (1~5)

**Q1.** Virtual DOM이란 무엇이며, React가 Virtual DOM을 사용하는 이유는 무엇인가요?

**Q2.** React의 Reconciliation(재조정) 알고리즘에 대해 설명해주세요.

**Q3.** React의 Diffing 알고리즘은 어떻게 동작하나요? O(n^3) 복잡도를 O(n)으로 줄일 수 있는 이유는 무엇인가요?

**Q4.** React 컴포넌트가 리렌더링되는 조건을 모두 설명해주세요.

**Q5.** key prop이 리스트 렌더링 성능에 미치는 영향과 올바른 사용법을 설명해주세요.

## 비교/구분 (6~9)

**Q6.** React.memo와 useMemo의 차이점은 무엇인가요? 각각 언제 사용해야 하나요?

**Q7.** React.memo의 동작 원리와 얕은 비교(shallow comparison)에 대해 설명해주세요.

**Q8.** Fiber 아키텍처가 기존 Stack Reconciler와 다른 점은 무엇인가요?

**Q9.** React.lazy와 Suspense를 사용한 Code Splitting의 장점과 동작 원리를 설명해주세요.

## 심화/실무 (10~12)

**Q10.** 불필요한 리렌더링을 방지하는 방법들을 구체적인 예시와 함께 설명해주세요.

**Q11.** React DevTools Profiler를 사용하여 성능 문제를 진단하고 해결한 경험이 있나요?

**Q12.** Concurrent Mode(현재 Concurrent Features)의 등장 배경과 주요 기능(useTransition, useDeferredValue)을 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** index를 key로 사용하면 안 되는 이유를 구체적인 시나리오와 함께 설명해주세요.

**Q14.** React 18의 자동 배치(Automatic Batching)는 무엇이며, 이전 버전과 어떤 차이가 있나요?

**Q15.** 대규모 리스트를 렌더링할 때 성능을 최적화하는 방법은 무엇인가요? (Virtual Scrolling, Windowing 등)
