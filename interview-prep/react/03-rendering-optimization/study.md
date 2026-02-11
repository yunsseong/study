# 3. React 렌더링 & 최적화

> React의 성능을 이해하고 최적화하기 위한 핵심 개념들

---

## Virtual DOM

### 개념

**Virtual DOM**은 실제 DOM의 경량화된 JavaScript 객체 표현입니다.

```
                    React Component
                          ↓
                  [Virtual DOM 생성]
                          ↓
        ┌─────────────────┴─────────────────┐
        ↓                                     ↓
    이전 Virtual DOM                    새 Virtual DOM
        │                                     │
        └──────────────→ [Diffing] ←─────────┘
                          ↓
                    [변경점 계산]
                          ↓
                    [실제 DOM 반영]
                     (최소한의 변경)
```

### 실제 DOM vs Virtual DOM

| 구분 | 실제 DOM | Virtual DOM |
|------|---------|------------|
| 타입 | 브라우저 API 객체 | JavaScript 객체 |
| 조작 비용 | 높음 (reflow, repaint) | 낮음 (메모리 연산) |
| 업데이트 속도 | 느림 | 빠름 |
| 직접 조작 | 가능 | 불가능 (React가 관리) |

### Virtual DOM 업데이트 과정

```javascript
// 1. 초기 렌더링
const element = {
  type: 'div',
  props: {
    className: 'container',
    children: [
      {
        type: 'h1',
        props: { children: 'Hello' }
      }
    ]
  }
};

// 2. 상태 변경 후 새로운 Virtual DOM
const newElement = {
  type: 'div',
  props: {
    className: 'container',
    children: [
      {
        type: 'h1',
        props: { children: 'Hello React' } // 변경됨
      }
    ]
  }
};

// 3. Diffing: 차이점 찾기
// → h1의 텍스트만 변경되었음을 감지

// 4. Patch: 실제 DOM에 최소 변경 적용
document.querySelector('h1').textContent = 'Hello React';
```

### 장점

1. **성능 최적화**: 변경된 부분만 실제 DOM에 반영
2. **배치 업데이트**: 여러 변경사항을 한 번에 처리
3. **크로스 플랫폼**: React Native 등 다른 렌더 대상 지원
4. **선언적 프로그래밍**: 개발자는 최종 상태만 정의

---

## Reconciliation (재조정)

### 개념

Reconciliation은 React가 Virtual DOM의 변경사항을 실제 DOM에 효율적으로 반영하는 알고리즘입니다.

### 두 가지 핵심 가정

```
1. 서로 다른 타입의 엘리먼트는 다른 트리를 생성

   Before:          After:
   <div>            <span>
     <Child />        <Child />
   </div>           </span>

   → div를 제거하고 span 새로 생성
   → Child도 언마운트 후 재마운트


2. key prop으로 어떤 자식이 동일한지 표시

   Before:                  After:
   <li key="a">A</li>       <li key="a">A</li>
   <li key="b">B</li>       <li key="c">C</li>  ← 새로 추가
   <li key="c">C</li>       <li key="b">B</li>

   → key로 식별하여 B를 재사용
```

### Diffing 전략

#### 1. 엘리먼트 타입이 다른 경우

```jsx
// 이전
<div className="container">
  <Counter />
</div>

// 새로운
<span className="container">
  <Counter />
</span>

// React의 동작:
// 1. 기존 div와 모든 자식 제거 (Counter 언마운트)
// 2. 새로운 span과 자식 생성 (Counter 마운트)
```

#### 2. 엘리먼트 타입이 같은 경우

```jsx
// 이전
<div className="before" title="old" />

// 새로운
<div className="after" title="old" />

// React의 동작:
// 1. 같은 DOM 노드 유지
// 2. 변경된 속성만 업데이트 (className만 변경)
```

#### 3. 자식 엘리먼트 비교

```jsx
// key 없는 경우
<ul>
  <li>Apple</li>
  <li>Banana</li>
</ul>

// 맨 앞에 추가
<ul>
  <li>Mango</li>
  <li>Apple</li>
  <li>Banana</li>
</ul>

// → 모든 li를 업데이트 (비효율적)


// key 있는 경우
<ul>
  <li key="apple">Apple</li>
  <li key="banana">Banana</li>
</ul>

// 맨 앞에 추가
<ul>
  <li key="mango">Mango</li>
  <li key="apple">Apple</li>
  <li key="banana">Banana</li>
</ul>

// → Mango만 추가, Apple/Banana 재사용 (효율적)
```

---

## Fiber 아키텍처

### Stack Reconciler의 한계 (React 15 이전)

```
문제: 동기적, 재귀적 트리 순회

┌─────────────────────────────────────────────┐
│  [렌더링 시작]                               │
│       ↓                                      │
│  [컴포넌트 A 처리]                            │
│       ↓                                      │
│  [컴포넌트 B 처리]                            │
│       ↓                                      │
│  [컴포넌트 C 처리] ... (수백 개)               │
│       ↓                                      │
│  [렌더링 완료]                                │
│                                              │
│  ← 중단 불가능 (메인 스레드 블로킹)             │
│  ← 사용자 입력 무시                            │
│  ← 애니메이션 끊김                             │
└─────────────────────────────────────────────┘

결과: 60fps 유지 실패 → 화면 버벅임
```

### Fiber의 해결책

**Fiber**: 작업을 중단 가능한 작은 단위로 분할

```
Fiber 아키텍처:

[프레임 1 (16ms)]
  ├─ 컴포넌트 A 처리 (5ms)
  ├─ 컴포넌트 B 처리 (8ms)
  └─ 남은 시간: 3ms
      → 브라우저가 다른 작업 수행 가능
      → 사용자 입력 처리 ✓
      → 애니메이션 렌더링 ✓

[프레임 2 (16ms)]
  ├─ 컴포넌트 C 처리 (10ms)
  ├─ 컴포넌트 D 처리 (4ms)
  └─ 남은 시간: 2ms

60fps 유지 ✓
```

### Fiber 노드 구조

```javascript
{
  // 식별 정보
  type: 'div',              // 컴포넌트 타입
  key: null,                // key prop

  // 관계
  return: parentFiber,      // 부모 fiber
  child: firstChildFiber,   // 첫 번째 자식
  sibling: nextSiblingFiber, // 다음 형제

  // 상태
  memoizedState: {...},     // 현재 상태
  memoizedProps: {...},     // 현재 props

  // 업데이트
  pendingProps: {...},      // 새로운 props
  updateQueue: [...],       // 업데이트 큐

  // 더블 버퍼링
  alternate: workInProgressFiber, // 작업 중인 복사본

  // 작업 정보
  effectTag: 'UPDATE',      // 수행할 작업 (PLACEMENT, UPDATE, DELETION)
  nextEffect: nextFiber,    // 다음 effect

  // 우선순위
  lanes: 0b0010,            // 작업 우선순위
}
```

### 주요 특징

#### 1. 작업 우선순위

```javascript
우선순위 레벨:

Immediate (동기)     - 사용자 입력, 클릭, 포커스
UserBlocking (250ms) - 스크롤, 애니메이션
Normal (5초)         - 데이터 페칭, 네트워크 응답
Low (10초)           - 분석, 로깅
Idle (무한)          - 백그라운드 작업


예시:
┌────────────────────────────────────┐
│ [사용자 클릭] (Immediate)           │
│       ↓                             │
│ [진행 중인 데이터 페칭 중단]          │
│       ↓                             │
│ [클릭 처리]                          │
│       ↓                             │
│ [데이터 페칭 재개 또는 폐기]          │
└────────────────────────────────────┘
```

#### 2. Time Slicing (시간 분할)

```
작업 분할:

전체 작업: 컴포넌트 1000개 렌더링

[16ms 청크 1]
  ├─ 컴포넌트 1-50 처리
  └─ [yield] → 브라우저에게 제어권 반환

[16ms 청크 2]
  ├─ 컴포넌트 51-100 처리
  └─ [yield]

[16ms 청크 3]
  ├─ 컴포넌트 101-150 처리
  └─ [yield]

...

결과: 60fps 유지하면서 1000개 렌더링 완료
```

#### 3. 증분 렌더링 (Incremental Rendering)

```jsx
function App() {
  return (
    <div>
      <Header />           {/* 우선순위: 높음 */}
      <Suspense fallback={<Spinner />}>
        <HeavyContent />   {/* 우선순위: 낮음, 나중에 렌더링 */}
      </Suspense>
    </div>
  );
}

// 렌더링 순서:
// 1. Header 렌더링
// 2. Spinner 렌더링 (fallback)
// 3. 유휴 시간에 HeavyContent 준비
// 4. 준비 완료되면 Spinner → HeavyContent 교체
```

---

## 리렌더링 조건

### 리렌더링이 발생하는 경우

```jsx
function Parent() {
  const [count, setCount] = useState(0);
  const [name, setName] = useState('React');

  return <Child count={count} />;
}
```

| 조건 | 트리거 | 예시 |
|------|--------|------|
| **State 변경** | `setState` 호출 | `setCount(1)` |
| **Props 변경** | 부모가 다른 값 전달 | `count: 0 → 1` |
| **부모 리렌더링** | 부모 컴포넌트가 리렌더링 | Parent 리렌더링 → Child 리렌더링 |
| **Context 변경** | `Context.Provider`의 value 변경 | `<ThemeContext.Provider value={theme}>` |
| **강제 업데이트** | `forceUpdate()` (클래스) | 권장하지 않음 |

### 리렌더링 전파

```jsx
function App() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <button onClick={() => setCount(c => c + 1)}>
        {count}
      </button>
      <Header />         {/* count 안 쓰지만 리렌더링됨 */}
      <Content />        {/* count 안 쓰지만 리렌더링됨 */}
      <Footer />         {/* count 안 쓰지만 리렌더링됨 */}
    </div>
  );
}

// 부모가 리렌더링되면 모든 자식도 기본적으로 리렌더링됨
```

**리렌더링 트리:**

```
App (count 변경)
 ├─ button (리렌더링 ✓)
 ├─ Header (리렌더링 ✓) ← props 안 바뀌어도!
 ├─ Content (리렌더링 ✓) ← props 안 바뀌어도!
 └─ Footer (리렌더링 ✓) ← props 안 바뀌어도!
```

### 리렌더링이 발생하지 않는 경우

```jsx
// 1. 같은 값으로 setState
const [count, setCount] = useState(0);
setCount(0); // count가 이미 0이면 리렌더링 안 됨

// 2. 같은 참조로 setState
const [user, setUser] = useState({ name: 'Lee' });
setUser(user); // 같은 객체 참조 → 리렌더링 안 됨

// 3. React.memo로 props 변경 감지
const Child = React.memo(({ name }) => <div>{name}</div>);
// props가 안 바뀌면 부모가 리렌더링되어도 Child는 안 됨
```

---

## React.memo

### 개념

컴포넌트를 메모이제이션하여 props가 변경되지 않으면 리렌더링을 건너뜁니다.

```jsx
// 일반 컴포넌트
function ExpensiveComponent({ data }) {
  // 복잡한 계산...
  return <div>{data}</div>;
}

// 메모이제이션된 컴포넌트
const ExpensiveComponent = React.memo(function ExpensiveComponent({ data }) {
  // 복잡한 계산...
  return <div>{data}</div>;
});
```

### 동작 원리

```
부모 리렌더링
    ↓
React.memo 확인
    ↓
  [이전 props와 새 props 비교]
    ↓
    ├─ 같음 → 이전 렌더링 결과 재사용 (리렌더링 스킵)
    └─ 다름 → 컴포넌트 리렌더링
```

### 얕은 비교 (Shallow Comparison)

```javascript
// Object.is() 사용 (대부분 === 와 동일)

// 원시 타입: 값 비교
Object.is(5, 5);           // true ✓
Object.is('hello', 'hello'); // true ✓

// 객체/배열: 참조 비교
Object.is({ a: 1 }, { a: 1 }); // false ✗ (다른 객체)
Object.is([1], [1]);           // false ✗ (다른 배열)

const obj = { a: 1 };
Object.is(obj, obj);           // true ✓ (같은 참조)
```

### 문제 상황과 해결

```jsx
// ❌ 문제: 매 렌더링마다 새 객체/함수 생성
function Parent() {
  const [count, setCount] = useState(0);

  // 매번 새 객체 → 얕은 비교 실패
  const user = { name: 'Lee', age: 30 };

  // 매번 새 함수 → 얕은 비교 실패
  const handleClick = () => console.log('clicked');

  return <MemoizedChild user={user} onClick={handleClick} />;
}

// ✅ 해결: useMemo, useCallback 사용
function Parent() {
  const [count, setCount] = useState(0);

  // 객체 메모이제이션
  const user = useMemo(() => ({ name: 'Lee', age: 30 }), []);

  // 함수 메모이제이션
  const handleClick = useCallback(() => {
    console.log('clicked');
  }, []);

  return <MemoizedChild user={user} onClick={handleClick} />;
}
```

### 커스텀 비교 함수

```jsx
const MemoizedComponent = React.memo(
  Component,
  (prevProps, nextProps) => {
    // true 반환: 리렌더링 스킵 (같다고 판단)
    // false 반환: 리렌더링 수행 (다르다고 판단)

    // 예: id만 비교
    return prevProps.id === nextProps.id;
  }
);

// 사용 예시: 깊은 비교
const DeepMemoComponent = React.memo(
  Component,
  (prevProps, nextProps) => {
    return JSON.stringify(prevProps) === JSON.stringify(nextProps);
    // 주의: 비용이 큼, 간단한 객체만 권장
  }
);
```

---

## Code Splitting & Lazy Loading

### React.lazy

동적 import를 사용하여 컴포넌트를 지연 로딩합니다.

```jsx
// 일반 import: 번들에 포함
import HeavyComponent from './HeavyComponent';

// React.lazy: 별도 청크로 분리
const HeavyComponent = React.lazy(() => import('./HeavyComponent'));
```

**번들 구조 비교:**

```
일반 import:
└─ bundle.js (1.5MB)
   ├─ App code
   ├─ HeavyComponent code
   └─ 모든 라이브러리

React.lazy:
├─ main.js (200KB) ← 초기 로딩
├─ HeavyComponent.chunk.js (300KB) ← 필요시 로딩
└─ vendors.chunk.js (1MB)
```

### Suspense

lazy 컴포넌트 로딩 중 fallback UI를 표시합니다.

```jsx
import React, { Suspense } from 'react';

const ProfilePage = React.lazy(() => import('./ProfilePage'));

function App() {
  return (
    <Suspense fallback={<div>로딩 중...</div>}>
      <ProfilePage />
    </Suspense>
  );
}
```

**동작 흐름:**

```
1. <ProfilePage /> 렌더링 시도
     ↓
2. 컴포넌트 아직 로드 안 됨
     ↓
3. Promise throw (Suspense가 catch)
     ↓
4. fallback UI 표시 (<div>로딩 중...</div>)
     ↓
5. import() 완료
     ↓
6. ProfilePage 렌더링
```

### 실무 패턴

#### 1. Route-based Code Splitting

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

const Home = React.lazy(() => import('./routes/Home'));
const About = React.lazy(() => import('./routes/About'));
const Dashboard = React.lazy(() => import('./routes/Dashboard'));

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
```

#### 2. 조건부 로딩

```jsx
function AdminPanel() {
  const [showSettings, setShowSettings] = useState(false);

  const AdvancedSettings = React.lazy(() =>
    import('./AdvancedSettings')
  );

  return (
    <div>
      <button onClick={() => setShowSettings(true)}>
        고급 설정 열기
      </button>

      {showSettings && (
        <Suspense fallback={<Spinner />}>
          <AdvancedSettings />
        </Suspense>
      )}
    </div>
  );
}
```

#### 3. Named Export 처리

```jsx
// MyComponent.js
export function MyComponent() { ... }

// App.js
const MyComponent = React.lazy(() =>
  import('./MyComponent').then(module => ({
    default: module.MyComponent  // named export → default
  }))
);
```

---

## Concurrent Features

### 등장 배경

**문제:**

```jsx
// 사용자가 타이핑할 때마다 검색
function SearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const handleChange = (e) => {
    setQuery(e.target.value);
    setResults(heavySearch(e.target.value)); // 무거운 연산
  };

  return (
    <div>
      <input value={query} onChange={handleChange} />
      <Results results={results} />
    </div>
  );
}

// 문제: 사용자 입력이 느려짐 (UI 블로킹)
```

**해결:**

```
기존: 모든 업데이트가 동일한 우선순위
     → 무거운 업데이트가 UI 블로킹

Concurrent Features: 업데이트에 우선순위 부여
     → 긴급한 업데이트(사용자 입력) 먼저 처리
     → 덜 긴급한 업데이트(검색 결과) 백그라운드 처리
```

### useTransition

상태 업데이트를 낮은 우선순위로 표시합니다.

```jsx
import { useTransition } from 'react';

function SearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [isPending, startTransition] = useTransition();

  const handleChange = (e) => {
    const value = e.target.value;

    // 긴급 업데이트: 즉시 반영
    setQuery(value);

    // 낮은 우선순위: 백그라운드에서 처리
    startTransition(() => {
      setResults(heavySearch(value));
    });
  };

  return (
    <div>
      <input value={query} onChange={handleChange} />
      {isPending && <Spinner />}
      <Results results={results} />
    </div>
  );
}
```

**동작 방식:**

```
사용자 타이핑: "R" → "Re" → "Rea" → "React"

[R 입력]
  ├─ setQuery("R") → 즉시 화면 업데이트 ✓
  └─ startTransition(() => setResults(...))
      → 백그라운드 작업 시작

[e 입력] (R 검색이 아직 진행 중)
  ├─ setQuery("Re") → 즉시 화면 업데이트 ✓
  └─ startTransition(() => setResults(...))
      → 이전 작업 취소, 새 작업 시작

결과: 사용자는 계속 부드럽게 타이핑 가능
```

### useDeferredValue

값의 업데이트를 지연시킵니다.

```jsx
import { useDeferredValue } from 'react';

function SearchPage() {
  const [query, setQuery] = useState('');
  const deferredQuery = useDeferredValue(query);

  return (
    <div>
      {/* 사용자 입력은 즉시 반영 */}
      <input
        value={query}
        onChange={e => setQuery(e.target.value)}
      />

      {/* 검색은 지연되어 실행 */}
      <SearchResults query={deferredQuery} />
    </div>
  );
}

const SearchResults = React.memo(({ query }) => {
  const results = heavySearch(query);
  return <Results results={results} />;
});
```

**동작 방식:**

```
query:         "R" → "Re" → "Rea" → "React"
               ↓      ↓       ↓        ↓
deferredQuery: ""  → "R"  → "Re"  → "React"
               (지연)  (지연)   (지연)    (최종)

빠른 타이핑 시 중간 값들은 건너뛰고 마지막 값만 사용
```

### useTransition vs useDeferredValue

| 구분 | useTransition | useDeferredValue |
|------|--------------|------------------|
| 제어 대상 | 상태 업데이트 함수 | 값 자체 |
| 사용 시점 | setState를 직접 호출 | props/상태를 받아서 지연 |
| isPending | 제공 ✓ | 제공 ✗ |
| 적합한 경우 | 업데이트 로직 제어 | 받은 값 지연 처리 |

**사용 예시:**

```jsx
// useTransition: setState 직접 제어
function TabContainer() {
  const [tab, setTab] = useState('home');
  const [isPending, startTransition] = useTransition();

  const handleClick = (newTab) => {
    startTransition(() => {
      setTab(newTab); // 탭 전환 (무거운 렌더링)
    });
  };

  return (
    <div>
      <button onClick={() => handleClick('home')}>Home</button>
      <button onClick={() => handleClick('posts')}>Posts</button>
      {isPending && <Spinner />}
      <TabContent tab={tab} />
    </div>
  );
}

// useDeferredValue: 받은 props 지연
function SlowList({ items }) {
  const deferredItems = useDeferredValue(items);
  // items가 빠르게 변해도 deferredItems는 천천히 변함
  return <List items={deferredItems} />;
}
```

---

## 성능 측정 도구

### React DevTools Profiler

#### 사용법

```
1. Chrome DevTools 열기
2. React 탭 → Profiler 선택
3. ⚫ 녹화 버튼 클릭
4. 앱에서 작업 수행
5. ⏹ 정지 버튼 클릭
6. 결과 분석
```

#### Flamegraph (불꽃 그래프)

```
                     App (18.5ms)
        ┌──────────────┴───────────────┐
    Header (2.3ms)              Content (15.8ms)
                        ┌─────────┴────────────┐
                   Sidebar (1.2ms)      MainView (14.1ms)
                                    ┌──────┴─────────┐
                                List (12.5ms)  Detail (1.3ms)

← List가 병목 지점 (가장 긴 렌더링 시간)
```

#### 색상 의미

| 색상 | 의미 | 조치 |
|------|------|------|
| **회색** | 렌더링 안 됨 | 최적화 잘 됨 ✓ |
| **초록색** | 빠른 렌더링 | 문제없음 ✓ |
| **노란색** | 보통 속도 | 필요시 최적화 |
| **주황색** | 느린 렌더링 | 최적화 권장 |
| **빨간색** | 매우 느림 | 즉시 최적화 필요 |

#### 주요 기능

```jsx
// 1. "Record why each component rendered" 활성화
→ 리렌더링 이유 표시

리렌더링 이유:
- Props changed
- State changed
- Parent rendered
- Hooks changed

// 2. "Highlight updates when components render" 활성화
→ 화면에서 리렌더링되는 컴포넌트 하이라이트
```

---

## 최적화 체크리스트

### 불필요한 리렌더링 방지

```jsx
// 1. React.memo
const ExpensiveChild = React.memo(({ data }) => {
  return <HeavyComponent data={data} />;
});

// 2. useCallback (함수 메모이제이션)
const handleClick = useCallback(() => {
  console.log('clicked');
}, []);

// 3. useMemo (값 메모이제이션)
const filteredList = useMemo(() => {
  return items.filter(item => item.active);
}, [items]);

// 4. State Colocation (상태를 가까운 곳에)
// ❌ 전역 상태
function App() {
  const [email, setEmail] = useState('');
  return <EmailInput value={email} onChange={setEmail} />;
}

// ✅ 로컬 상태
function EmailInput() {
  const [email, setEmail] = useState('');
  return <input value={email} onChange={e => setEmail(e.target.value)} />;
}

// 5. children prop 패턴
function Counter({ children }) {
  const [count, setCount] = useState(0);
  return (
    <div>
      <button onClick={() => setCount(c => c + 1)}>{count}</button>
      {children} {/* count 변경 시에도 리렌더링 안 됨 */}
    </div>
  );
}
```

### 큰 리스트 최적화

```jsx
// 1. Virtual Scrolling
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={10000}
  itemSize={50}
  width="100%"
>
  {Row}
</FixedSizeList>

// 2. Pagination
const currentItems = items.slice(
  (page - 1) * itemsPerPage,
  page * itemsPerPage
);

// 3. Infinite Scroll
const { data, fetchNextPage } = useInfiniteQuery(...);
```

### Code Splitting

```jsx
// 1. Route-based
const Home = React.lazy(() => import('./Home'));

// 2. Component-based
const HeavyModal = React.lazy(() => import('./HeavyModal'));

// 3. Library-based
const Chart = React.lazy(() => import('react-chartjs-2'));
```

---

## 면접 핵심 정리

### 1분 요약

```
Q: React의 렌더링 최적화 방법은?

A:
1. Virtual DOM으로 최소한의 DOM 조작
2. React.memo로 컴포넌트 메모이제이션
3. useCallback/useMemo로 값/함수 메모이제이션
4. Code Splitting으로 초기 로딩 최적화
5. Concurrent Features로 업데이트 우선순위 관리
6. Virtual Scrolling으로 큰 리스트 최적화
7. React Profiler로 병목 지점 찾기
```

### 핵심 개념 정리

| 개념 | 목적 | 사용 시점 |
|------|------|----------|
| **Virtual DOM** | 효율적인 DOM 업데이트 | 항상 (내부 동작) |
| **React.memo** | 컴포넌트 리렌더링 방지 | props 변경 드물 때 |
| **useMemo** | 값 재계산 방지 | 계산 비용 클 때 |
| **useCallback** | 함수 재생성 방지 | 함수를 props로 전달 시 |
| **React.lazy** | 코드 분할 | 초기 로딩 최적화 |
| **useTransition** | 업데이트 우선순위 | 무거운 업데이트 있을 때 |
| **Profiler** | 성능 측정 | 최적화 전/후 |

### 자주하는 실수

```jsx
// ❌ index를 key로 사용 (재정렬되는 리스트)
{items.map((item, index) => <Item key={index} />)}

// ❌ 매번 새 객체/함수 생성
<Child config={{ a: 1 }} onClick={() => {}} />

// ❌ 과도한 최적화 (오히려 느려질 수 있음)
const trivialValue = useMemo(() => a + b, [a, b]);

// ❌ 의존성 배열 누락
const value = useMemo(() => compute(data), []); // data 변경 무시됨

// ❌ React.memo + 객체 props (얕은 비교 실패)
const Child = React.memo(({ user }) => ...);
<Child user={{ name: 'Lee' }} /> // 매번 새 객체
```

### 꼭 기억할 것

1. **측정 후 최적화**: 추측하지 말고 Profiler로 측정
2. **조기 최적화 금지**: 병목이 확인된 곳만 최적화
3. **key는 안정적이고 고유하게**: index 사용 주의
4. **메모이제이션은 trade-off**: 비용 vs 이득 고려
5. **Concurrent Features 활용**: React 18+ 우선순위 관리

