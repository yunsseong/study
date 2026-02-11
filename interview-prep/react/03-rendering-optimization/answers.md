# React 렌더링 & 최적화 면접 질문 + 답변

## 기본 개념

**Q1.** Virtual DOM이란 무엇이며, React가 Virtual DOM을 사용하는 이유는 무엇인가요?

> **Virtual DOM이란:**
> - 실제 DOM의 가벼운 JavaScript 객체 표현
> - 메모리 상에 존재하는 UI의 이상적인 또는 "가상" 표현
> - React 엘리먼트는 불변(immutable) 객체로 Virtual DOM을 구성
>
> **사용 이유:**
> 1. **성능 최적화**: 실제 DOM 조작은 비용이 큼 (reflow, repaint)
> 2. **효율적인 업데이트**: 변경된 부분만 실제 DOM에 반영
> 3. **배치 업데이트**: 여러 변경사항을 한 번에 처리
> 4. **선언적 프로그래밍**: 개발자는 "무엇을" 렌더링할지만 작성
>
> **동작 과정:**
> ```
> State 변경 → 새 Virtual DOM 생성 → Diffing (이전 Virtual DOM과 비교)
>   → 변경된 부분만 계산 → 실제 DOM에 최소한의 변경 적용
> ```
>
> 실제 DOM 조작을 최소화하여 애플리케이션의 성능을 향상시킵니다.

---

**Q2.** React의 Reconciliation(재조정) 알고리즘에 대해 설명해주세요.

> **Reconciliation이란:**
> - React가 변경된 부분을 찾아 실제 DOM에 반영하는 과정
> - Virtual DOM의 이전 버전과 새 버전을 비교하는 알고리즘
>
> **두 가지 핵심 가정:**
> 1. **서로 다른 타입의 엘리먼트는 다른 트리를 생성**
>    - 타입이 다르면 기존 트리를 버리고 새로 구축
>    ```jsx
>    // div → span: 전체 트리 재생성
>    <div>Hello</div> → <span>Hello</span>
>    ```
>
> 2. **key prop을 통해 여러 렌더링에서 어떤 자식이 동일한지 표시**
>    - 리스트 렌더링 시 엘리먼트의 고유성 보장
>
> **Diffing 전략:**
> - **엘리먼트 타입이 다를 때**: 이전 트리를 제거하고 새 트리 구축
> - **엘리먼트 타입이 같을 때**: 속성만 비교하여 변경된 속성만 업데이트
> - **자식 엘리먼트**: key를 사용하여 효율적으로 비교
>
> 이 휴리스틱을 통해 O(n) 시간 복잡도로 트리를 비교합니다.

---

**Q3.** React의 Diffing 알고리즘은 어떻게 동작하나요? O(n^3) 복잡도를 O(n)으로 줄일 수 있는 이유는 무엇인가요?

> **일반적인 트리 비교 알고리즘:**
> - 두 트리의 차이를 찾는 최소 연산 횟수: O(n^3)
> - 1000개 노드 비교 시 10억 번의 비교 필요
>
> **React의 O(n) 달성 방법:**
>
> 1. **같은 레벨끼리만 비교 (Level-by-Level)**
>    ```
>    이전:        새로운:
>      A            A
>     / \          / \
>    B   C        B   D
>
>    → A의 자식들만 비교 (B와 B, C와 D)
>    → 다른 레벨 간 비교 X
>    ```
>
> 2. **타입이 다르면 전체 재생성**
>    ```jsx
>    // div → span: 하위 트리 전체 교체
>    <div><Child /></div> → <span><Child /></span>
>    // Child 컴포넌트도 언마운트 후 재마운트
>    ```
>
> 3. **key를 통한 엘리먼트 식별**
>    ```jsx
>    // key 없을 때: 모든 항목 재렌더링
>    [A, B, C] → [A, D, B, C]
>
>    // key 있을 때: D만 삽입, 나머지 재사용
>    [<li key="a">A</li>, <li key="b">B</li>, <li key="c">C</li>]
>    → [<li key="a">A</li>, <li key="d">D</li>, <li key="b">B</li>, <li key="c">C</li>]
>    ```
>
> **제한사항:**
> - 컴포넌트가 다른 위치로 이동하는 경우 감지 불가
> - 휴리스틱이므로 항상 최적은 아니지만 실용적으로 충분히 빠름

---

**Q4.** React 컴포넌트가 리렌더링되는 조건을 모두 설명해주세요.

> **리렌더링 발생 조건:**
>
> | 조건 | 설명 | 예시 |
> |------|------|------|
> | **1. State 변경** | useState, useReducer로 관리하는 상태가 변경될 때 | `setState(newValue)` |
> | **2. Props 변경** | 부모로부터 받은 props가 변경될 때 | `<Child name={name} />` |
> | **3. 부모 컴포넌트 리렌더링** | 부모가 리렌더링되면 자식도 기본적으로 리렌더링 | 최적화 없으면 자동 리렌더링 |
> | **4. Context 값 변경** | Context.Provider의 value가 변경될 때 | `<MyContext.Provider value={...}>` |
> | **5. 강제 업데이트** | forceUpdate() 호출 시 (클래스 컴포넌트) | 권장하지 않음 |
>
> **중요한 포인트:**
> ```jsx
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   return (
>     <div>
>       <button onClick={() => setCount(count + 1)}>
>         Count: {count}
>       </button>
>       {/* count가 변경되면 Child도 리렌더링됨 (props가 안 바뀌어도!) */}
>       <Child name="React" />
>     </div>
>   );
> }
> ```
>
> **리렌더링이 발생하지 않는 경우:**
> - State를 같은 값으로 업데이트 (React.memo 없어도)
> - 참조가 같은 객체/배열로 업데이트
>   ```jsx
>   const [user, setUser] = useState({ name: 'Lee' });
>   setUser(user); // 리렌더링 안 됨 (같은 참조)
>   ```

---

**Q5.** key prop이 리스트 렌더링 성능에 미치는 영향과 올바른 사용법을 설명해주세요.

> **key의 역할:**
> - React가 어떤 항목이 변경/추가/삭제되었는지 식별하는 힌트
> - 형제 엘리먼트 사이에서 고유해야 함 (전역적으로 고유할 필요는 없음)
>
> **key가 없을 때의 문제:**
> ```jsx
> // key 없이 렌더링
> const items = ['A', 'B', 'C'];
> items.map(item => <li>{item}</li>);
>
> // 'D'를 맨 앞에 추가
> ['D', 'A', 'B', 'C']
>
> // React의 동작:
> // 이전:  <li>A</li> <li>B</li> <li>C</li>
> // 새로운: <li>D</li> <li>A</li> <li>B</li> <li>C</li>
> // → 모든 li를 업데이트 (A→D, B→A, C→B, C 추가)
> ```
>
> **key가 있을 때:**
> ```jsx
> items.map(item => <li key={item.id}>{item}</li>);
>
> // React의 동작:
> // key로 식별 → D만 추가, A/B/C는 재사용
> // → 성능 향상!
> ```
>
> **올바른 사용법:**
> ```jsx
> // ✅ GOOD: 안정적이고 예측 가능한 고유 ID
> {users.map(user => <User key={user.id} {...user} />)}
>
> // ✅ GOOD: 항목이 재정렬되지 않는 정적 리스트
> {staticList.map((item, index) => <Item key={index} {...item} />)}
>
> // ❌ BAD: 재정렬되는 리스트에 index 사용
> {sortedList.map((item, index) => <Item key={index} {...item} />)}
>
> // ❌ BAD: 랜덤 값 (매 렌더링마다 변경됨)
> {items.map(item => <Item key={Math.random()} {...item} />)}
> ```
>
> **index를 key로 사용하면 안 되는 경우:**
> - 항목의 순서가 바뀔 수 있을 때
> - 항목이 추가/삭제될 수 있을 때
> - 리스트가 필터링될 때
>
> 이런 경우 컴포넌트 state가 잘못된 항목과 매칭될 수 있습니다.

## 비교/구분

**Q6.** React.memo와 useMemo의 차이점은 무엇인가요? 각각 언제 사용해야 하나요?

> **React.memo:**
> - **목적**: 컴포넌트 메모이제이션 (컴포넌트 자체를 감싸는 HOC)
> - **비교 대상**: props
> - **반환**: 메모이제이션된 컴포넌트
>
> ```jsx
> // 사용법
> const MemoizedComponent = React.memo(function MyComponent(props) {
>   return <div>{props.name}</div>;
> });
>
> // props가 변경되지 않으면 리렌더링 스킵
> <MemoizedComponent name="React" />
> ```
>
> **useMemo:**
> - **목적**: 계산 비용이 큰 값의 메모이제이션 (Hook)
> - **비교 대상**: 의존성 배열
> - **반환**: 메모이제이션된 값
>
> ```jsx
> // 사용법
> function Component({ items }) {
>   // items가 변경될 때만 재계산
>   const expensiveValue = useMemo(() => {
>     return items.reduce((acc, item) => acc + item.value, 0);
>   }, [items]);
>
>   return <div>{expensiveValue}</div>;
> }
> ```
>
> **비교표:**
>
> | 구분 | React.memo | useMemo |
> |------|-----------|---------|
> | 타입 | HOC | Hook |
> | 메모이제이션 대상 | 컴포넌트 | 값 |
> | 사용 위치 | 컴포넌트 외부 | 컴포넌트 내부 |
> | 비교 기준 | props | 의존성 배열 |
> | 방지하는 것 | 리렌더링 | 값 재계산 |
>
> **언제 사용하나:**
> ```jsx
> // React.memo: 부모가 자주 리렌더링되지만 props는 거의 안 바뀌는 경우
> const HeavyChild = React.memo(({ data }) => {
>   // 복잡한 렌더링 로직
>   return <ExpensiveVisualization data={data} />;
> });
>
> // useMemo: 매 렌더링마다 계산하기엔 비용이 큰 값
> function Dashboard({ logs }) {
>   const statistics = useMemo(() => {
>     // 수천 개의 로그를 분석하는 무거운 연산
>     return analyzeLogs(logs);
>   }, [logs]);
>
>   return <StatsDisplay stats={statistics} />;
> }
> ```

---

**Q7.** React.memo의 동작 원리와 얕은 비교(shallow comparison)에 대해 설명해주세요.

> **React.memo 동작 원리:**
> ```jsx
> const MemoizedComponent = React.memo(Component);
>
> // React가 내부적으로 하는 일:
> // 1. 이전 props와 새로운 props를 얕은 비교
> // 2. 모든 props가 같으면 이전 렌더링 결과 재사용
> // 3. 하나라도 다르면 리렌더링
> ```
>
> **얕은 비교 (Shallow Comparison):**
> - 객체의 첫 번째 레벨만 비교
> - `Object.is()` 알고리즘 사용 (대부분 `===`와 동일)
>
> ```javascript
> // 얕은 비교 예시
> const prevProps = { name: 'Lee', age: 30 };
> const nextProps = { name: 'Lee', age: 30 };
>
> // 얕은 비교 결과:
> Object.is(prevProps.name, nextProps.name); // true
> Object.is(prevProps.age, nextProps.age);   // true
> // → 리렌더링 안 함
> ```
>
> **문제가 되는 경우:**
> ```jsx
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   // ❌ 매번 새로운 객체 생성 → 얕은 비교 실패
>   const user = { name: 'Lee', age: 30 };
>
>   // ❌ 매번 새로운 함수 생성
>   const handleClick = () => console.log('clicked');
>
>   return <MemoizedChild user={user} onClick={handleClick} />;
> }
>
> // Parent가 리렌더링될 때마다 MemoizedChild도 리렌더링됨
> // (user와 handleClick이 매번 새로 생성되므로)
> ```
>
> **해결 방법:**
> ```jsx
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   // ✅ useMemo로 객체 메모이제이션
>   const user = useMemo(() => ({ name: 'Lee', age: 30 }), []);
>
>   // ✅ useCallback으로 함수 메모이제이션
>   const handleClick = useCallback(() => {
>     console.log('clicked');
>   }, []);
>
>   return <MemoizedChild user={user} onClick={handleClick} />;
> }
> ```
>
> **커스텀 비교 함수:**
> ```jsx
> const MemoizedComponent = React.memo(Component, (prevProps, nextProps) => {
>   // true를 반환하면 리렌더링 스킵 (같다고 판단)
>   // false를 반환하면 리렌더링 수행 (다르다고 판단)
>   return prevProps.id === nextProps.id;
> });
> ```

---

**Q8.** Fiber 아키텍처가 기존 Stack Reconciler와 다른 점은 무엇인가요?

> **Stack Reconciler (React 15 이전):**
> - 재귀적으로 동기 방식으로 트리를 순회
> - 작업을 시작하면 중단할 수 없음
> - 거대한 컴포넌트 트리 업데이트 시 메인 스레드 블로킹
> - 16ms(60fps) 안에 완료하지 못하면 화면이 끊김
>
> ```
> Stack Reconciler:
> [업데이트 시작] ────────────────────> [업데이트 완료]
>                (중단 불가)
>              [메인 스레드 블로킹]
> ```
>
> **Fiber 아키텍처 (React 16+):**
> - 작업을 작은 단위(fiber)로 분할
> - 작업에 우선순위 부여 가능
> - 작업을 중단하고 나중에 재개 가능
> - 브라우저가 긴급한 작업(애니메이션, 사용자 입력) 우선 처리 가능
>
> ```
> Fiber:
> [업데이트 시작] ─> [일시중지] ─> [사용자 입력 처리] ─> [재개] ─> [완료]
>                      ↓
>                 [브라우저가 다른 작업 수행 가능]
> ```
>
> **주요 차이점:**
>
> | 구분 | Stack Reconciler | Fiber |
> |------|-----------------|-------|
> | 실행 방식 | 동기 (synchronous) | 비동기 (asynchronous) |
> | 작업 중단 | 불가능 | 가능 |
> | 우선순위 | 없음 | 있음 (urgent vs normal) |
> | 증분 렌더링 | 불가능 | 가능 |
> | Time Slicing | 지원 안 함 | 지원 |
>
> **Fiber의 핵심 기능:**
>
> 1. **작업 우선순위:**
> ```jsx
> // 높은 우선순위: 사용자 입력, 애니메이션
> // 낮은 우선순위: 데이터 페칭, 분석
>
> // React 18의 useTransition 활용
> const [isPending, startTransition] = useTransition();
>
> startTransition(() => {
>   // 이 업데이트는 낮은 우선순위로 처리
>   setSearchResults(results);
> });
> ```
>
> 2. **증분 렌더링 (Incremental Rendering):**
> ```
> Frame 1: [컴포넌트 A 렌더링]
> Frame 2: [컴포넌트 B, C 렌더링] (사용자 입력 처리)
> Frame 3: [컴포넌트 D, E 렌더링]
> ```
>
> 3. **Time Slicing:**
> - 작업을 작은 청크로 나누어 브라우저에 제어권 반환
> - 60fps를 유지하면서 대규모 업데이트 수행
>
> **Fiber 노드 구조:**
> ```javascript
> {
>   type: 'div',           // 컴포넌트 타입
>   key: null,             // key
>   props: {...},          // props
>   return: FiberNode,     // 부모 fiber
>   child: FiberNode,      // 첫 번째 자식
>   sibling: FiberNode,    // 다음 형제
>   alternate: FiberNode,  // 이전 상태의 fiber (더블 버퍼링)
>   effectTag: 'UPDATE',   // 수행할 작업 (PLACEMENT, UPDATE, DELETION)
> }
> ```

---

**Q9.** React.lazy와 Suspense를 사용한 Code Splitting의 장점과 동작 원리를 설명해주세요.

> **Code Splitting이란:**
> - 번들을 여러 개의 작은 청크로 나누는 기법
> - 초기 로딩 시 필요한 코드만 로드하여 성능 향상
>
> **React.lazy:**
> - 동적 import()를 사용하여 컴포넌트를 지연 로딩
> - 컴포넌트가 실제로 렌더링될 때 코드를 불러옴
>
> ```jsx
> // 일반 import (번들에 포함됨)
> import HeavyComponent from './HeavyComponent';
>
> // React.lazy (별도 청크로 분리됨)
> const HeavyComponent = React.lazy(() => import('./HeavyComponent'));
> ```
>
> **Suspense:**
> - lazy 컴포넌트가 로딩되는 동안 fallback UI를 표시
> - 비동기 작업의 로딩 상태를 선언적으로 관리
>
> ```jsx
> import React, { Suspense } from 'react';
>
> const ProfilePage = React.lazy(() => import('./ProfilePage'));
> const SettingsPage = React.lazy(() => import('./SettingsPage'));
>
> function App() {
>   return (
>     <Suspense fallback={<div>로딩 중...</div>}>
>       <ProfilePage />
>       {/* ProfilePage 로딩 중에 "로딩 중..." 표시 */}
>     </Suspense>
>   );
> }
> ```
>
> **동작 원리:**
> ```
> 1. React.lazy(() => import('./Component'))
>    ↓
> 2. 컴포넌트 렌더링 시도
>    ↓
> 3. 아직 로드되지 않음 → Promise throw
>    ↓
> 4. 가장 가까운 Suspense가 캐치
>    ↓
> 5. fallback UI 렌더링
>    ↓
> 6. import() Promise 완료
>    ↓
> 7. 실제 컴포넌트 렌더링
> ```
>
> **장점:**
>
> 1. **초기 로딩 시간 단축:**
> ```
> Before: bundle.js (1MB) → 로딩 느림
> After:  main.js (200KB) + route1.js + route2.js + ... → 빠른 초기 로딩
> ```
>
> 2. **메모리 효율:**
> - 사용하지 않는 코드는 메모리에 올라가지 않음
>
> 3. **캐싱 효율:**
> - 변경되지 않은 청크는 브라우저 캐시 활용
>
> **실무 패턴:**
> ```jsx
> // 1. Route-based Code Splitting (가장 일반적)
> import { BrowserRouter, Routes, Route } from 'react-router-dom';
>
> const Home = React.lazy(() => import('./routes/Home'));
> const Dashboard = React.lazy(() => import('./routes/Dashboard'));
> const Settings = React.lazy(() => import('./routes/Settings'));
>
> function App() {
>   return (
>     <BrowserRouter>
>       <Suspense fallback={<PageLoader />}>
>         <Routes>
>           <Route path="/" element={<Home />} />
>           <Route path="/dashboard" element={<Dashboard />} />
>           <Route path="/settings" element={<Settings />} />
>         </Routes>
>       </Suspense>
>     </BrowserRouter>
>   );
> }
>
> // 2. 조건부 로딩
> function AdminPanel() {
>   const [showAdvanced, setShowAdvanced] = useState(false);
>
>   const AdvancedSettings = React.lazy(() =>
>     import('./AdvancedSettings')
>   );
>
>   return (
>     <div>
>       <button onClick={() => setShowAdvanced(true)}>
>         고급 설정 보기
>       </button>
>
>       {showAdvanced && (
>         <Suspense fallback={<Spinner />}>
>           <AdvancedSettings />
>         </Suspense>
>       )}
>     </div>
>   );
> }
>
> // 3. Named Export 처리
> const MyComponent = React.lazy(() =>
>   import('./MyComponent').then(module => ({
>     default: module.MyComponent // named export를 default로 변환
>   }))
> );
> ```
>
> **주의사항:**
> - lazy 컴포넌트는 반드시 Suspense 내부에서 렌더링
> - Server-Side Rendering(SSR)에서는 기본적으로 지원 안 됨 (React 18+ 개선)
> - Error Boundary와 함께 사용하여 로딩 실패 처리

## 심화/실무

**Q10.** 불필요한 리렌더링을 방지하는 방법들을 구체적인 예시와 함께 설명해주세요.

> **1. React.memo로 컴포넌트 메모이제이션:**
> ```jsx
> // ❌ 부모가 리렌더링될 때마다 리렌더링됨
> function ExpensiveChild({ data }) {
>   // 복잡한 연산...
>   return <div>{data}</div>;
> }
>
> // ✅ props가 변경될 때만 리렌더링
> const ExpensiveChild = React.memo(function ExpensiveChild({ data }) {
>   // 복잡한 연산...
>   return <div>{data}</div>;
> });
> ```
>
> **2. useCallback으로 함수 메모이제이션:**
> ```jsx
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   // ❌ 매 렌더링마다 새 함수 생성 → 자식 리렌더링
>   const handleClick = () => {
>     console.log('clicked');
>   };
>
>   // ✅ 함수 재사용 → 자식 리렌더링 방지
>   const handleClick = useCallback(() => {
>     console.log('clicked');
>   }, []); // 의존성 배열 비어있으면 한 번만 생성
>
>   return <MemoizedChild onClick={handleClick} />;
> }
> ```
>
> **3. useMemo로 계산 비용이 큰 값 메모이제이션:**
> ```jsx
> function SearchResults({ query, items }) {
>   // ❌ 매 렌더링마다 필터링 수행
>   const filteredItems = items.filter(item =>
>     item.name.toLowerCase().includes(query.toLowerCase())
>   );
>
>   // ✅ query나 items가 변경될 때만 재계산
>   const filteredItems = useMemo(() =>
>     items.filter(item =>
>       item.name.toLowerCase().includes(query.toLowerCase())
>     ),
>     [query, items]
>   );
>
>   return <List items={filteredItems} />;
> }
> ```
>
> **4. 상태 끌어올리기 방지 (State Colocation):**
> ```jsx
> // ❌ 전체 Form이 매번 리렌더링
> function Form() {
>   const [email, setEmail] = useState('');
>   const [password, setPassword] = useState('');
>   const [username, setUsername] = useState('');
>
>   return (
>     <div>
>       <EmailInput value={email} onChange={setEmail} />
>       <PasswordInput value={password} onChange={setPassword} />
>       <UsernameInput value={username} onChange={setUsername} />
>       <ExpensiveComponent /> {/* email 입력 시에도 리렌더링됨 */}
>     </div>
>   );
> }
>
> // ✅ 상태를 해당 컴포넌트에만 위치
> function EmailInput() {
>   const [email, setEmail] = useState('');
>   return <input value={email} onChange={e => setEmail(e.target.value)} />;
> }
>
> function Form() {
>   return (
>     <div>
>       <EmailInput />      {/* 독립적으로 리렌더링 */}
>       <PasswordInput />   {/* 독립적으로 리렌더링 */}
>       <UsernameInput />   {/* 독립적으로 리렌더링 */}
>       <ExpensiveComponent /> {/* 리렌더링 안 됨 */}
>     </div>
>   );
> }
> ```
>
> **5. children prop 패턴:**
> ```jsx
> // ❌ count 변경 시 ExpensiveComponent도 리렌더링
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   return (
>     <div>
>       <button onClick={() => setCount(c => c + 1)}>{count}</button>
>       <ExpensiveComponent />
>     </div>
>   );
> }
>
> // ✅ children은 리렌더링 안 됨
> function Counter({ children }) {
>   const [count, setCount] = useState(0);
>
>   return (
>     <div>
>       <button onClick={() => setCount(c => c + 1)}>{count}</button>
>       {children}
>     </div>
>   );
> }
>
> function App() {
>   return (
>     <Counter>
>       <ExpensiveComponent /> {/* Counter 리렌더링 시에도 안 바뀜 */}
>     </Counter>
>   );
> }
> ```
>
> **6. 불변성 유지:**
> ```jsx
> function TodoList() {
>   const [todos, setTodos] = useState([]);
>
>   // ❌ 같은 배열을 수정 → React가 변경 감지 못함
>   const addTodo = (text) => {
>     todos.push({ id: Date.now(), text });
>     setTodos(todos); // 같은 참조!
>   };
>
>   // ✅ 새 배열 생성 → React가 변경 감지
>   const addTodo = (text) => {
>     setTodos([...todos, { id: Date.now(), text }]);
>   };
> }
> ```
>
> **7. key prop 올바르게 사용:**
> ```jsx
> // ❌ index를 key로 사용 (항목 순서 변경 시 문제)
> {items.map((item, index) => (
>   <Item key={index} {...item} />
> ))}
>
> // ✅ 고유한 ID 사용
> {items.map(item => (
>   <Item key={item.id} {...item} />
> ))}
> ```

---

**Q11.** React DevTools Profiler를 사용하여 성능 문제를 진단하고 해결한 경험이 있나요?

> **React DevTools Profiler 사용법:**
>
> **1. Profiler 탭 열기:**
> - Chrome DevTools → React DevTools → Profiler 탭
> - 녹화 버튼(⚫) 클릭하여 프로파일링 시작
>
> **2. 주요 측정 항목:**
>
> ```
> Flamegraph (불꽃 그래프):
> ┌─────────────────────────────────────┐
> │ App (15.2ms)                        │
> ├──────────────┬─────────────────────┤
> │ Header       │ Content (12.8ms)    │
> │ (2.1ms)      ├──────────┬──────────┤
> │              │ Sidebar  │ MainView │
> │              │ (0.3ms)  │ (12.1ms) │
> └──────────────┴──────────┴──────────┘
>
> → MainView가 가장 많은 시간 소요 (병목 지점)
> ```
>
> | 측정 항목 | 의미 | 해석 |
> |----------|------|------|
> | **Render duration** | 컴포넌트 렌더링 소요 시간 | 느리면 최적화 필요 |
> | **Gray bar** | 렌더링되지 않음 (최적화됨) | React.memo 등이 작동 |
> | **Yellow/Orange** | 보통 속도 | 수용 가능한 범위 |
> | **Red bar** | 느린 렌더링 | 즉시 최적화 필요 |
>
> **3. 실제 진단 예시:**
>
> **문제 발견:**
> ```jsx
> // 문제가 있는 코드
> function UserList({ users }) {
>   // Profiler에서 빨간색으로 표시됨 (50ms+)
>   return (
>     <div>
>       {users.map((user, index) => (
>         <UserCard key={index} user={user} /> // ❌ index as key
>       ))}
>     </div>
>   );
> }
>
> function UserCard({ user }) {
>   // 매 렌더링마다 새로운 객체 생성
>   const style = { background: user.active ? 'green' : 'gray' };
>
>   return <div style={style}>{user.name}</div>;
> }
> ```
>
> **Profiler에서 확인된 사항:**
> - UserList 렌더링 시간: 52.3ms (빨간색)
> - 모든 UserCard가 매번 리렌더링됨
> - 정렬 시 모든 카드가 재마운트됨 (key가 index라서)
>
> **해결 방법:**
> ```jsx
> // ✅ 최적화된 코드
> function UserList({ users }) {
>   return (
>     <div>
>       {users.map(user => (
>         <UserCard key={user.id} user={user} /> // ✅ unique key
>       ))}
>     </div>
>   );
> }
>
> const UserCard = React.memo(function UserCard({ user }) {
>   // useMemo로 style 메모이제이션
>   const style = useMemo(
>     () => ({ background: user.active ? 'green' : 'gray' }),
>     [user.active]
>   );
>
>   return <div style={style}>{user.name}</div>;
> });
> ```
>
> **최적화 후 Profiler 결과:**
> - UserList 렌더링 시간: 3.1ms (회색/초록색)
> - 변경된 UserCard만 리렌더링
> - 정렬 시 카드 재사용 (key가 id라서)
>
> **4. 유용한 Profiler 기능:**
>
> ```
> ⚙️ Settings:
> - "Highlight updates when components render"
>   → 리렌더링되는 컴포넌트를 화면에서 하이라이트
>
> - "Record why each component rendered"
>   → 왜 리렌더링되었는지 이유 표시
>
> 📊 Ranked Chart:
> - 렌더링 시간 순으로 컴포넌트 정렬
> - 가장 느린 컴포넌트부터 최적화
> ```
>
> **5. 성능 최적화 체크리스트:**
> ```
> □ 불필요한 리렌더링이 있는가? → React.memo
> □ 큰 리스트를 렌더링하는가? → Virtualization
> □ 복잡한 계산을 반복하는가? → useMemo
> □ 함수를 props로 전달하는가? → useCallback
> □ key를 올바르게 사용하는가? → unique ID
> □ 상태가 적절한 위치에 있는가? → State colocation
> ```

---

**Q12.** Concurrent Mode(현재 Concurrent Features)의 등장 배경과 주요 기능(useTransition, useDeferredValue)을 설명해주세요.

> **등장 배경:**
>
> **문제 상황:**
> ```jsx
> function SearchPage() {
>   const [query, setQuery] = useState('');
>   const [results, setResults] = useState([]);
>
>   // 사용자가 타이핑할 때마다 검색
>   const handleChange = (e) => {
>     const value = e.target.value;
>     setQuery(value);
>     // 수천 개의 결과를 필터링 → UI가 버벅임
>     setResults(searchDatabase(value));
>   };
>
>   return (
>     <div>
>       <input value={query} onChange={handleChange} />
>       {/* 사용자 입력이 느려짐 (메인 스레드 블로킹) */}
>       <SearchResults results={results} />
>     </div>
>   );
> }
> ```
>
> **기존 방식의 한계:**
> - 모든 업데이트가 동일한 우선순위
> - 긴급한 업데이트(사용자 입력)와 덜 긴급한 업데이트(검색 결과) 구분 불가
> - 큰 업데이트가 UI를 블로킹
>
> **Concurrent Features의 해결책:**
> - 업데이트에 우선순위 부여
> - 긴급한 업데이트를 먼저 처리
> - 덜 긴급한 업데이트는 백그라운드에서 준비
> - 사용자 경험 개선
>
> ---
>
> **1. useTransition:**
> - 상태 업데이트를 긴급하지 않음(non-urgent)으로 표시
> - UI 블로킹 없이 큰 업데이트 수행
>
> ```jsx
> import { useTransition, useState } from 'react';
>
> function SearchPage() {
>   const [query, setQuery] = useState('');
>   const [results, setResults] = useState([]);
>   const [isPending, startTransition] = useTransition();
>
>   const handleChange = (e) => {
>     const value = e.target.value;
>
>     // 긴급 업데이트: 즉시 반영 (사용자 입력)
>     setQuery(value);
>
>     // 낮은 우선순위 업데이트: 백그라운드에서 처리
>     startTransition(() => {
>       setResults(searchDatabase(value)); // 무거운 연산
>     });
>   };
>
>   return (
>     <div>
>       <input value={query} onChange={handleChange} />
>       {/* isPending으로 로딩 상태 표시 */}
>       {isPending && <Spinner />}
>       <SearchResults results={results} />
>     </div>
>   );
> }
> ```
>
> **useTransition 동작 원리:**
> ```
> 사용자 타이핑: "R" → "Re" → "Rea" → "Reac" → "React"
>
> 기존 방식:
> [R 입력] → [검색 수행(50ms)] → [화면 업데이트]
>          ↓ (사용자가 기다려야 함)
> [e 입력] → [검색 수행(50ms)] → [화면 업데이트]
>
> useTransition 사용:
> [R 입력] → [즉시 화면 업데이트] ← 사용자는 계속 타이핑 가능
>          → [백그라운드 검색(50ms)]
> [e 입력] → [즉시 화면 업데이트]
>          → [이전 검색 취소, 새 검색 시작]
> ```
>
> ---
>
> **2. useDeferredValue:**
> - 값의 업데이트를 지연시킴
> - 긴급한 업데이트가 완료된 후 업데이트
>
> ```jsx
> import { useDeferredValue, useState, memo } from 'react';
>
> function SearchPage() {
>   const [query, setQuery] = useState('');
>   // query의 지연된 버전
>   const deferredQuery = useDeferredValue(query);
>
>   return (
>     <div>
>       {/* 사용자 입력은 즉시 반영 */}
>       <input
>         value={query}
>         onChange={e => setQuery(e.target.value)}
>       />
>
>       {/* 검색 결과는 지연되어 표시 (백그라운드 업데이트) */}
>       <SearchResults query={deferredQuery} />
>     </div>
>   );
> }
>
> // memo와 함께 사용하여 최적화
> const SearchResults = memo(function SearchResults({ query }) {
>   const results = searchDatabase(query); // 무거운 연산
>   return <ResultList results={results} />;
> });
> ```
>
> **useDeferredValue 동작:**
> ```
> query:         "R" → "Re" → "Rea" → "React"
>                ↓      ↓       ↓        ↓
> deferredQuery: ""  → "R"  → "Re"  → "React"
>                (지연)  (지연)   (지연)
>
> 사용자가 빠르게 타이핑하면 중간 값들은 건너뛰고
> 마지막 값만 검색에 사용
> ```
>
> ---
>
> **useTransition vs useDeferredValue:**
>
> | 구분 | useTransition | useDeferredValue |
> |------|--------------|------------------|
> | 제어 대상 | 상태 업데이트 | 값 자체 |
> | 사용 시점 | setState를 감쌀 수 있을 때 | props나 상태를 지연시킬 때 |
> | isPending | 제공함 | 제공 안 함 |
> | 적합한 경우 | 직접 상태 업데이트 제어 | 받은 값을 지연시켜야 할 때 |
>
> **언제 사용하나:**
> ```jsx
> // ✅ useTransition: 상태 업데이트를 직접 제어
> const handleClick = () => {
>   startTransition(() => {
>     setTab('posts'); // 탭 전환 (무거운 렌더링)
>   });
> };
>
> // ✅ useDeferredValue: 받은 props/state를 지연
> function SlowList({ items }) {
>   const deferredItems = useDeferredValue(items);
>   return <List items={deferredItems} />;
> }
> ```
>
> ---
>
> **3. Concurrent Features의 주요 특징:**
>
> **가. 중단 가능한 렌더링:**
> ```
> [긴급 업데이트 발생]
>    ↓
> [진행 중인 낮은 우선순위 렌더링 중단]
>    ↓
> [긴급 업데이트 처리]
>    ↓
> [낮은 우선순위 렌더링 재개 또는 폐기]
> ```
>
> **나. 자동 배치 (Automatic Batching):**
> ```jsx
> // React 18+: 모든 업데이트가 자동 배치됨
> function handleClick() {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // 두 업데이트가 하나의 리렌더링으로 배치됨
> }
>
> setTimeout(() => {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // React 17: 두 번 리렌더링
>   // React 18: 한 번만 리렌더링 (자동 배치)
> }, 1000);
> ```
>
> **다. Suspense 개선:**
> ```jsx
> // React 18: Suspense가 더 강력해짐
> <Suspense fallback={<Spinner />}>
>   <ProfilePage /> {/* 데이터 페칭 중 */}
> </Suspense>
> ```

## 꼬리질문 대비

**Q13.** index를 key로 사용하면 안 되는 이유를 구체적인 시나리오와 함께 설명해주세요.

> **문제 시나리오 1: 항목 추가/삭제**
>
> ```jsx
> // 초기 상태
> const items = ['Apple', 'Banana', 'Cherry'];
>
> // index를 key로 사용
> {items.map((item, index) => (
>   <TodoItem key={index} text={item} />
> ))}
>
> // 렌더링 결과:
> // <TodoItem key={0} text="Apple" />
> // <TodoItem key={1} text="Banana" />
> // <TodoItem key={2} text="Cherry" />
> ```
>
> **맨 앞에 'Mango' 추가:**
> ```jsx
> const items = ['Mango', 'Apple', 'Banana', 'Cherry'];
>
> // 새 렌더링:
> // <TodoItem key={0} text="Mango" />   ← 이전 key={0}은 "Apple"이었음
> // <TodoItem key={1} text="Apple" />   ← 이전 key={1}은 "Banana"였음
> // <TodoItem key={2} text="Banana" />  ← 이전 key={2}는 "Cherry"였음
> // <TodoItem key={3} text="Cherry" />  ← 새로 추가됨
>
> // React의 판단:
> // - key={0}: "Apple" → "Mango" (업데이트)
> // - key={1}: "Banana" → "Apple" (업데이트)
> // - key={2}: "Cherry" → "Banana" (업데이트)
> // - key={3}: 새로 추가
> // → 모든 항목을 업데이트 (비효율적!)
> ```
>
> **고유 ID를 key로 사용:**
> ```jsx
> const items = [
>   { id: 'mango', text: 'Mango' },
>   { id: 'apple', text: 'Apple' },
>   { id: 'banana', text: 'Banana' },
>   { id: 'cherry', text: 'Cherry' }
> ];
>
> {items.map(item => (
>   <TodoItem key={item.id} text={item.text} />
> ))}
>
> // React의 판단:
> // - key="apple": 유지 (재사용)
> // - key="banana": 유지 (재사용)
> // - key="cherry": 유지 (재사용)
> // - key="mango": 새로 추가
> // → "Mango"만 추가하고 나머지는 재사용 (효율적!)
> ```
>
> ---
>
> **문제 시나리오 2: 컴포넌트 상태 유지 문제**
>
> ```jsx
> function TodoItem({ text }) {
>   const [checked, setChecked] = useState(false);
>
>   return (
>     <div>
>       <input
>         type="checkbox"
>         checked={checked}
>         onChange={e => setChecked(e.target.checked)}
>       />
>       <span>{text}</span>
>     </div>
>   );
> }
>
> // 초기 상태
> const items = ['Task 1', 'Task 2', 'Task 3'];
>
> // 사용자가 "Task 2"를 체크함
> // [☐ Task 1] [☑ Task 2] [☐ Task 3]
>
> // "Task 1"을 삭제
> const items = ['Task 2', 'Task 3'];
>
> // index를 key로 사용한 경우:
> // key={0} (이전 "Task 1") → key={0} (현재 "Task 2")
> // → React는 같은 컴포넌트로 인식
> // → 이전 "Task 1"의 상태(unchecked)를 "Task 2"에 적용
> // 결과: [☐ Task 2] [☑ Task 3] ← 잘못된 상태!
>
> // id를 key로 사용한 경우:
> // key="task1" 삭제, key="task2" 유지
> // → "Task 2"의 상태(checked) 올바르게 유지
> // 결과: [☑ Task 2] [☐ Task 3] ← 올바른 상태!
> ```
>
> ---
>
> **문제 시나리오 3: 리스트 정렬**
>
> ```jsx
> function ProductList({ products, sortBy }) {
>   const sorted = [...products].sort((a, b) =>
>     sortBy === 'price' ? a.price - b.price : a.name.localeCompare(b.name)
>   );
>
>   return sorted.map((product, index) => (
>     <ProductCard key={index} product={product} />
>   ));
> }
>
> // 초기 상태 (이름순):
> // key={0}: "Apple"
> // key={1}: "Banana"  ← 사용자가 장바구니에 추가
> // key={2}: "Cherry"
>
> // 가격순으로 정렬:
> // key={0}: "Banana" ← React는 이전 key={0} (Apple)과 같다고 판단
> // key={1}: "Apple"  ← 이전 key={1} (Banana)의 상태를 받음
> // key={2}: "Cherry"
>
> // 결과: "Apple"이 장바구니에 담긴 것처럼 보임 (잘못된 상태)
> ```
>
> ---
>
> **index를 key로 사용해도 되는 경우:**
>
> ```jsx
> // ✅ 정적 리스트 (추가/삭제/재정렬 없음)
> const DAYS = ['월', '화', '수', '목', '금', '토', '일'];
> {DAYS.map((day, index) => (
>   <li key={index}>{day}</li>
> ))}
>
> // ✅ 항목이 항상 끝에만 추가됨 (재정렬 없음)
> const logs = [...oldLogs, newLog];
> {logs.map((log, index) => (
>   <LogEntry key={index} log={log} />
> ))}
> ```
>
> **올바른 key 선택 기준:**
> ```
> 1순위: 데이터베이스 ID (user.id, product.id)
> 2순위: 안정적인 고유 식별자 (uuid, timestamp + random)
> 3순위: 데이터 내용의 해시값
> 마지막: index (정적이고 재정렬되지 않는 리스트만)
> ```

---

**Q14.** React 18의 자동 배치(Automatic Batching)는 무엇이며, 이전 버전과 어떤 차이가 있나요?

> **배치(Batching)란:**
> - 여러 상태 업데이트를 하나의 리렌더링으로 그룹화
> - 불필요한 리렌더링을 방지하여 성능 향상
>
> ```jsx
> function handleClick() {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   setName('React');
>
>   // 배치 없음: 3번 리렌더링
>   // 배치 있음: 1번 리렌더링
> }
> ```
>
> ---
>
> **React 17 이전의 배치:**
>
> ```jsx
> // ✅ React 이벤트 핸들러 내부: 배치됨
> function handleClick() {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // 1번만 리렌더링 ✅
> }
>
> // ❌ Promise, setTimeout 등: 배치 안 됨
> function handleClick() {
>   fetch('/api').then(() => {
>     setCount(c => c + 1); // 리렌더링 1
>     setFlag(f => !f);     // 리렌더링 2
>   });
> }
>
> setTimeout(() => {
>   setCount(c => c + 1); // 리렌더링 1
>   setFlag(f => !f);     // 리렌더링 2
> }, 1000);
>
> // ❌ Native 이벤트 리스너: 배치 안 됨
> element.addEventListener('click', () => {
>   setCount(c => c + 1); // 리렌더링 1
>   setFlag(f => !f);     // 리렌더링 2
> });
> ```
>
> ---
>
> **React 18의 자동 배치 (Automatic Batching):**
>
> ```jsx
> // ✅ React 이벤트 핸들러: 배치됨 (이전과 동일)
> function handleClick() {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // 1번만 리렌더링 ✅
> }
>
> // ✅ Promise: 자동 배치됨 (새로운 기능!)
> function handleClick() {
>   fetch('/api').then(() => {
>     setCount(c => c + 1);
>     setFlag(f => !f);
>     // 1번만 리렌더링 ✅
>   });
> }
>
> // ✅ setTimeout: 자동 배치됨 (새로운 기능!)
> setTimeout(() => {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // 1번만 리렌더링 ✅
> }, 1000);
>
> // ✅ Native 이벤트: 자동 배치됨 (새로운 기능!)
> element.addEventListener('click', () => {
>   setCount(c => c + 1);
>   setFlag(f => !f);
>   // 1번만 리렌더링 ✅
> });
> ```
>
> ---
>
> **자동 배치 동작 원리:**
>
> ```
> React 17:
> [이벤트 핸들러 시작]
>   → setState 1
>   → setState 2
>   → setState 3
> [이벤트 핸들러 종료] → 배치된 리렌더링
>
> [Promise 콜백 시작]
>   → setState 1 → 리렌더링
>   → setState 2 → 리렌더링
> [Promise 콜백 종료]
>
>
> React 18:
> [어떤 컨텍스트든]
>   → setState 1
>   → setState 2
>   → setState 3
> [마이크로태스크 체크포인트] → 배치된 리렌더링
> ```
>
> ---
>
> **자동 배치 비활성화:**
>
> ```jsx
> import { flushSync } from 'react-dom';
>
> function handleClick() {
>   // 즉시 리렌더링 (배치 안 함)
>   flushSync(() => {
>     setCount(c => c + 1);
>   }); // 리렌더링 1
>
>   // 즉시 리렌더링 (배치 안 함)
>   flushSync(() => {
>     setFlag(f => !f);
>   }); // 리렌더링 2
>
>   // DOM이 즉시 업데이트되어야 하는 경우에만 사용
>   // 예: 스크롤 위치, 포커스 등
> }
> ```
>
> ---
>
> **성능 개선 예시:**
>
> ```jsx
> // React 17: 3번 리렌더링
> // React 18: 1번 리렌더링
>
> function TodoApp() {
>   const [todos, setTodos] = useState([]);
>   const [count, setCount] = useState(0);
>   const [loading, setLoading] = useState(false);
>
>   const fetchTodos = async () => {
>     setLoading(true); // React 17: 리렌더링 1
>
>     const data = await fetch('/api/todos').then(r => r.json());
>
>     setTodos(data);       // React 17: 리렌더링 2
>     setCount(data.length); // React 17: 리렌더링 3
>     setLoading(false);    // React 17: 리렌더링 4
>
>     // React 18: 모두 배치되어 1번만 리렌더링!
>   };
>
>   return (
>     <div>
>       {loading ? <Spinner /> : <TodoList todos={todos} />}
>       <p>Total: {count}</p>
>     </div>
>   );
> }
> ```
>
> ---
>
> **마이그레이션 가이드:**
>
> ```jsx
> // React 17 → 18 업그레이드 시 대부분 문제없음
>
> // ⚠️ 주의: 다음 패턴에 의존하는 경우 수정 필요
> function handleClick() {
>   setCount(c => c + 1);
>
>   // React 17: count는 아직 업데이트 안 됨
>   // React 18: count는 여전히 업데이트 안 됨 (동일)
>   console.log(count);
>
>   setFlag(f => !f);
>
>   // React 17: DOM에서 count 읽기 가능 (2번 리렌더링되므로)
>   // React 18: DOM에서 count 아직 안 바뀜 (배치되므로)
>   const element = document.getElementById('count');
>   console.log(element.textContent); // ⚠️ 다를 수 있음
> }
>
> // 해결: flushSync 사용
> flushSync(() => {
>   setCount(c => c + 1);
> });
> // 이제 DOM이 업데이트됨
> ```

---

**Q15.** 대규모 리스트를 렌더링할 때 성능을 최적화하는 방법은 무엇인가요? (Virtual Scrolling, Windowing 등)

> **문제 상황:**
> ```jsx
> // ❌ 10,000개 항목을 모두 렌더링
> function ProductList({ products }) {
>   return (
>     <div>
>       {products.map(product => (
>         <ProductCard key={product.id} product={product} />
>       ))}
>     </div>
>   );
> }
>
> // 문제:
> // - 초기 렌더링 시간: 수 초
> // - 메모리 사용량: 매우 높음
> // - 스크롤 성능: 버벅임
> // - 실제로 보이는 항목: 10~20개
> ```
>
> ---
>
> **1. Virtual Scrolling / Windowing:**
>
> **개념:**
> - 화면에 보이는 항목만 실제 DOM에 렌더링
> - 스크롤 시 DOM 요소를 재사용
>
> ```
> 전체 리스트 (10,000개):
> ┌─────────────────┐
> │ [보이지 않음]    │ ← DOM에 없음
> ├─────────────────┤
> │ Item 50         │ ← 렌더링됨
> │ Item 51         │ ← 렌더링됨
> │ Item 52         │ ← 렌더링됨
> │ ...             │
> │ Item 70         │ ← 렌더링됨
> ├─────────────────┤
> │ [보이지 않음]    │ ← DOM에 없음
> └─────────────────┘
>
> 실제 렌더링: ~20개
> 메모리 절약: 99.8%
> ```
>
> **react-window 사용:**
> ```jsx
> import { FixedSizeList } from 'react-window';
>
> function ProductList({ products }) {
>   // 각 항목을 렌더링하는 컴포넌트
>   const Row = ({ index, style }) => (
>     <div style={style}>
>       <ProductCard product={products[index]} />
>     </div>
>   );
>
>   return (
>     <FixedSizeList
>       height={600}           // 컨테이너 높이
>       itemCount={products.length}  // 전체 항목 수
>       itemSize={80}          // 각 항목의 높이
>       width="100%"
>     >
>       {Row}
>     </FixedSizeList>
>   );
> }
> ```
>
> **가변 크기 항목:**
> ```jsx
> import { VariableSizeList } from 'react-window';
>
> function CommentList({ comments }) {
>   // 각 항목의 높이를 계산
>   const getItemSize = (index) => {
>     const comment = comments[index];
>     // 댓글 길이에 따라 높이 계산
>     return Math.max(50, comment.text.length / 2);
>   };
>
>   const Row = ({ index, style }) => (
>     <div style={style}>
>       <Comment comment={comments[index]} />
>     </div>
>   );
>
>   return (
>     <VariableSizeList
>       height={600}
>       itemCount={comments.length}
>       itemSize={getItemSize}
>       width="100%"
>     >
>       {Row}
>     </VariableSizeList>
>   );
> }
> ```
>
> ---
>
> **2. react-virtualized (더 많은 기능):**
>
> ```jsx
> import { List, AutoSizer } from 'react-virtualized';
>
> function UserList({ users }) {
>   const rowRenderer = ({ key, index, style }) => (
>     <div key={key} style={style}>
>       <UserCard user={users[index]} />
>     </div>
>   );
>
>   return (
>     <AutoSizer>
>       {({ height, width }) => (
>         <List
>           width={width}
>           height={height}
>           rowCount={users.length}
>           rowHeight={100}
>           rowRenderer={rowRenderer}
>         />
>       )}
>     </AutoSizer>
>   );
> }
> ```
>
> **그리드 레이아웃:**
> ```jsx
> import { Grid } from 'react-virtualized';
>
> function ImageGallery({ images }) {
>   const cellRenderer = ({ columnIndex, key, rowIndex, style }) => {
>     const index = rowIndex * 3 + columnIndex; // 3열 그리드
>     if (index >= images.length) return null;
>
>     return (
>       <div key={key} style={style}>
>         <img src={images[index].url} alt="" />
>       </div>
>     );
>   };
>
>   return (
>     <Grid
>       cellRenderer={cellRenderer}
>       columnCount={3}        // 3열
>       columnWidth={200}
>       height={600}
>       rowCount={Math.ceil(images.length / 3)}
>       rowHeight={200}
>       width={600}
>     />
>   );
> }
> ```
>
> ---
>
> **3. Infinite Scrolling (무한 스크롤):**
>
> ```jsx
> import { useInfiniteQuery } from 'react-query';
> import { useInView } from 'react-intersection-observer';
>
> function InfiniteProductList() {
>   const { ref, inView } = useInView();
>
>   const {
>     data,
>     fetchNextPage,
>     hasNextPage,
>     isFetchingNextPage,
>   } = useInfiniteQuery(
>     'products',
>     ({ pageParam = 0 }) => fetchProducts(pageParam),
>     {
>       getNextPageParam: (lastPage, pages) => lastPage.nextCursor,
>     }
>   );
>
>   // 스크롤이 바닥에 닿으면 다음 페이지 로드
>   React.useEffect(() => {
>     if (inView && hasNextPage) {
>       fetchNextPage();
>     }
>   }, [inView, hasNextPage, fetchNextPage]);
>
>   return (
>     <div>
>       {data?.pages.map((page, i) => (
>         <React.Fragment key={i}>
>           {page.products.map(product => (
>             <ProductCard key={product.id} product={product} />
>           ))}
>         </React.Fragment>
>       ))}
>
>       {/* 감지용 요소 */}
>       <div ref={ref}>
>         {isFetchingNextPage && <Spinner />}
>       </div>
>     </div>
>   );
> }
> ```
>
> ---
>
> **4. 페이지네이션:**
>
> ```jsx
> function PaginatedList({ items, itemsPerPage = 20 }) {
>   const [currentPage, setCurrentPage] = useState(1);
>
>   // 현재 페이지 항목만 계산
>   const currentItems = useMemo(() => {
>     const start = (currentPage - 1) * itemsPerPage;
>     return items.slice(start, start + itemsPerPage);
>   }, [items, currentPage, itemsPerPage]);
>
>   const totalPages = Math.ceil(items.length / itemsPerPage);
>
>   return (
>     <div>
>       {/* 20개만 렌더링 */}
>       {currentItems.map(item => (
>         <ItemCard key={item.id} item={item} />
>       ))}
>
>       <Pagination
>         currentPage={currentPage}
>         totalPages={totalPages}
>         onPageChange={setCurrentPage}
>       />
>     </div>
>   );
> }
> ```
>
> ---
>
> **5. 최적화 조합 전략:**
>
> ```jsx
> // 최고의 성능을 위한 조합
> const OptimizedList = React.memo(function OptimizedList({ items }) {
>   // 1. Virtual Scrolling
>   const Row = React.memo(({ index, style }) => {
>     const item = items[index];
>
>     // 2. 이미지 Lazy Loading
>     return (
>       <div style={style}>
>         <img
>           src={item.thumbnail}
>           loading="lazy"  // 네이티브 lazy loading
>           alt={item.title}
>         />
>         <h3>{item.title}</h3>
>       </div>
>     );
>   });
>
>   return (
>     <FixedSizeList
>       height={600}
>       itemCount={items.length}
>       itemSize={120}
>       width="100%"
>       // 3. Overscan으로 스크롤 부드럽게
>       overscanCount={5}  // 화면 밖 5개 미리 렌더링
>     >
>       {Row}
>     </FixedSizeList>
>   );
> });
> ```
>
> ---
>
> **성능 비교:**
>
> | 방법 | 10,000개 렌더링 시간 | 메모리 사용 | 스크롤 FPS |
> |------|---------------------|-----------|-----------|
> | **일반 렌더링** | 3-5초 | 500MB+ | 10-20 FPS |
> | **Virtual Scrolling** | 0.1초 | 50MB | 60 FPS |
> | **Pagination** | 0.05초 | 10MB | 60 FPS |
> | **Infinite Scroll** | 0.1초/페이지 | 점진적 증가 | 60 FPS |
>
> **언제 어떤 방법을 사용하나:**
> ```
> Virtual Scrolling:
> - 매우 긴 리스트 (수천~수만 개)
> - 모든 데이터가 이미 로드됨
> - 예: 로그 뷰어, 채팅 히스토리
>
> Infinite Scroll:
> - 데이터가 서버에서 점진적으로 로드됨
> - 끝이 불분명한 피드
> - 예: 소셜 미디어 피드, 검색 결과
>
> Pagination:
> - 사용자가 특정 페이지로 이동해야 함
> - 데이터를 명확하게 구분
> - 예: 테이블, 검색 결과, 제품 목록
> ```
