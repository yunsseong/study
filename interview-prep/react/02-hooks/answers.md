# React Hooks 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** useState의 동작 원리와 상태 업데이트가 비동기적으로 처리되는 이유는 무엇인가요?

> **동작 원리:**
> - React는 컴포넌트 호출 순서를 기반으로 각 Hook의 상태를 내부 배열에 저장합니다.
> - 각 useState 호출은 배열의 특정 인덱스와 매핑되어, 리렌더링 시에도 동일한 상태를 참조합니다.
> - setState 호출 시 상태가 즉시 변경되는 것이 아니라, 업데이트 큐에 등록됩니다.
>
> **비동기 처리 이유:**
> - **배칭(Batching):** 여러 setState 호출을 하나의 리렌더링으로 묶어 성능을 최적화합니다.
> - **일관성 유지:** 렌더링 도중 상태가 변경되면 일관성 문제가 발생하므로, 렌더링이 완료된 후 상태를 업데이트합니다.
> - **성능 최적화:** 불필요한 중간 렌더링을 방지하여 UI 업데이트를 효율적으로 처리합니다.
>
> ```javascript
> // 배칭 예시
> const [count, setCount] = useState(0);
>
> const handleClick = () => {
>   setCount(count + 1); // count: 0 → 1
>   setCount(count + 1); // count: 0 → 1 (여전히 0 기반)
>   setCount(count + 1); // count: 0 → 1 (여전히 0 기반)
>   // 결과: 1 (3이 아님)
> };
>
> // 해결: 함수형 업데이트
> const handleClick = () => {
>   setCount(prev => prev + 1); // 1
>   setCount(prev => prev + 1); // 2
>   setCount(prev => prev + 1); // 3
>   // 결과: 3
> };
> ```

---

**Q2.** useEffect는 어떤 생명주기 메서드를 대체하며, 언제 실행되나요?

> **대체하는 생명주기 메서드:**
> - `componentDidMount` - 마운트 시 (의존성 배열 `[]`)
> - `componentDidUpdate` - 업데이트 시 (의존성 배열에 값 포함)
> - `componentWillUnmount` - 언마운트 시 (cleanup 함수)
>
> **실행 시점:**
> - 렌더링이 완료되고 DOM이 업데이트된 **후** 비동기적으로 실행됩니다.
> - 브라우저 페인트가 완료된 이후에 실행되므로, UI 블로킹을 방지합니다.
>
> **실행 흐름:**
> ```
> [컴포넌트 렌더링] → [DOM 업데이트] → [브라우저 페인트] → [useEffect 실행]
> ```
>
> ```javascript
> // 마운트 시 한 번만 실행
> useEffect(() => {
>   console.log('컴포넌트 마운트됨');
> }, []);
>
> // count 변경 시마다 실행
> useEffect(() => {
>   console.log('count 변경:', count);
> }, [count]);
>
> // 모든 렌더링 시 실행 (권장하지 않음)
> useEffect(() => {
>   console.log('렌더링 완료');
> });
> ```

---

**Q3.** useEffect의 cleanup 함수는 언제, 왜 사용하나요?

> **언제 사용:**
> - 컴포넌트가 언마운트될 때
> - useEffect가 다시 실행되기 전 (의존성 배열 값 변경 시)
>
> **왜 사용:**
> - **메모리 누수 방지:** 이벤트 리스너, 타이머, 구독 등을 정리
> - **부작용 제거:** 이전 effect의 영향을 제거하고 새로운 effect를 실행
> - **성능 최적화:** 불필요한 리소스 해제
>
> **실행 순서:**
> ```
> [컴포넌트 마운트] → [effect 실행]
>   ↓
> [상태/props 변경] → [cleanup 실행] → [effect 재실행]
>   ↓
> [컴포넌트 언마운트] → [cleanup 실행]
> ```
>
> ```javascript
> // 이벤트 리스너 정리
> useEffect(() => {
>   const handleResize = () => console.log(window.innerWidth);
>   window.addEventListener('resize', handleResize);
>
>   return () => {
>     window.removeEventListener('resize', handleResize); // cleanup
>   };
> }, []);
>
> // 타이머 정리
> useEffect(() => {
>   const timer = setInterval(() => {
>     console.log('tick');
>   }, 1000);
>
>   return () => clearInterval(timer); // cleanup
> }, []);
>
> // WebSocket 구독 정리
> useEffect(() => {
>   const socket = new WebSocket('ws://example.com');
>   socket.onmessage = (event) => console.log(event.data);
>
>   return () => socket.close(); // cleanup
> }, []);
> ```

---

**Q4.** useRef는 무엇이며, useState와 어떤 차이가 있나요?

> **useRef란:**
> - 렌더링 간에 유지되는 변경 가능한 값을 저장하는 Hook입니다.
> - `.current` 프로퍼티를 통해 값에 접근하며, 값 변경 시 리렌더링이 발생하지 않습니다.
>
> **useState vs useRef 비교:**
>
> | 특성 | useState | useRef |
> |------|----------|--------|
> | 값 변경 시 리렌더링 | O | X |
> | 값 접근 방식 | 직접 접근 | `.current`로 접근 |
> | 주요 용도 | UI에 반영되는 상태 | DOM 참조, 렌더링 무관한 값 |
> | 변경 시점 | 비동기 (다음 렌더링) | 즉시 (동기) |
> | 초기값 설정 | `useState(초기값)` | `useRef(초기값)` |
>
> **사용 예시:**
>
> ```javascript
> // 1. DOM 요소 참조
> const inputRef = useRef(null);
>
> const focusInput = () => {
>   inputRef.current.focus();
> };
>
> return <input ref={inputRef} />;
>
> // 2. 이전 값 저장
> const [count, setCount] = useState(0);
> const prevCountRef = useRef();
>
> useEffect(() => {
>   prevCountRef.current = count;
> }, [count]);
>
> // 3. 렌더링 무관한 값 저장
> const renderCount = useRef(0);
>
> useEffect(() => {
>   renderCount.current++; // 리렌더링 없이 값 증가
> });
>
> // 4. 타이머 ID 저장
> const timerRef = useRef(null);
>
> const startTimer = () => {
>   timerRef.current = setInterval(() => {
>     console.log('tick');
>   }, 1000);
> };
>
> const stopTimer = () => {
>   clearInterval(timerRef.current);
> };
> ```

---

**Q5.** Custom Hook이란 무엇이며, 왜 사용하나요?

> **Custom Hook이란:**
> - `use`로 시작하는 이름을 가진 JavaScript 함수입니다.
> - 내부에서 다른 Hook을 사용할 수 있으며, 상태 로직을 재사용 가능하게 만듭니다.
>
> **왜 사용:**
> - **로직 재사용:** 여러 컴포넌트에서 동일한 상태 로직을 공유
> - **관심사 분리:** 복잡한 로직을 컴포넌트에서 분리하여 가독성 향상
> - **테스트 용이성:** 독립적으로 테스트 가능
> - **코드 구조화:** 비즈니스 로직과 UI 로직 분리
>
> **예시:**
>
> ```javascript
> // 1. 폼 입력 관리 Custom Hook
> function useInput(initialValue) {
>   const [value, setValue] = useState(initialValue);
>
>   const handleChange = (e) => {
>     setValue(e.target.value);
>   };
>
>   const reset = () => {
>     setValue(initialValue);
>   };
>
>   return { value, onChange: handleChange, reset };
> }
>
> // 사용
> function LoginForm() {
>   const email = useInput('');
>   const password = useInput('');
>
>   return (
>     <form>
>       <input {...email} type="email" />
>       <input {...password} type="password" />
>       <button onClick={() => { email.reset(); password.reset(); }}>
>         초기화
>       </button>
>     </form>
>   );
> }
>
> // 2. API 데이터 페칭 Custom Hook
> function useFetch(url) {
>   const [data, setData] = useState(null);
>   const [loading, setLoading] = useState(true);
>   const [error, setError] = useState(null);
>
>   useEffect(() => {
>     const fetchData = async () => {
>       try {
>         setLoading(true);
>         const response = await fetch(url);
>         const json = await response.json();
>         setData(json);
>       } catch (err) {
>         setError(err);
>       } finally {
>         setLoading(false);
>       }
>     };
>
>     fetchData();
>   }, [url]);
>
>   return { data, loading, error };
> }
>
> // 사용
> function UserProfile({ userId }) {
>   const { data, loading, error } = useFetch(`/api/users/${userId}`);
>
>   if (loading) return <div>로딩 중...</div>;
>   if (error) return <div>에러 발생: {error.message}</div>;
>   return <div>{data.name}</div>;
> }
>
> // 3. 로컬 스토리지 동기화 Custom Hook
> function useLocalStorage(key, initialValue) {
>   const [storedValue, setStoredValue] = useState(() => {
>     try {
>       const item = window.localStorage.getItem(key);
>       return item ? JSON.parse(item) : initialValue;
>     } catch (error) {
>       return initialValue;
>     }
>   });
>
>   const setValue = (value) => {
>     try {
>       const valueToStore = value instanceof Function ? value(storedValue) : value;
>       setStoredValue(valueToStore);
>       window.localStorage.setItem(key, JSON.stringify(valueToStore));
>     } catch (error) {
>       console.error(error);
>     }
>   };
>
>   return [storedValue, setValue];
> }
> ```

---

## 비교/구분 (6~9)

**Q6.** useMemo와 useCallback의 차이는 무엇인가요?

> **기본 차이:**
> - `useMemo`: **값**을 메모이제이션 (계산 결과를 캐싱)
> - `useCallback`: **함수**를 메모이제이션 (함수 참조를 캐싱)
>
> **비교표:**
>
> | 특성 | useMemo | useCallback |
> |------|---------|-------------|
> | 반환값 | 계산된 값 | 메모이제이션된 함수 |
> | 사용 목적 | 비용이 큰 계산 최적화 | 함수 재생성 방지 |
> | 동등한 표현 | `useMemo(() => fn, deps)` | `useCallback(fn, deps)` |
> | 주요 사용 사례 | 복잡한 연산, 필터링, 정렬 | 자식 컴포넌트 props, 이벤트 핸들러 |
>
> **내부 동작:**
> ```javascript
> // useCallback은 useMemo의 특수 케이스
> useCallback(fn, deps) === useMemo(() => fn, deps)
> ```
>
> **예시:**
>
> ```javascript
> function SearchComponent({ items }) {
>   const [query, setQuery] = useState('');
>   const [count, setCount] = useState(0);
>
>   // useMemo: 필터링 결과값을 메모이제이션
>   const filteredItems = useMemo(() => {
>     console.log('필터링 실행'); // query 변경 시에만 실행
>     return items.filter(item =>
>       item.name.toLowerCase().includes(query.toLowerCase())
>     );
>   }, [items, query]); // count 변경 시에는 재계산 안 함
>
>   // useCallback: 함수 자체를 메모이제이션
>   const handleSearch = useCallback((e) => {
>     console.log('함수 재생성'); // query 변경 시에만 재생성
>     setQuery(e.target.value);
>   }, [query]); // count 변경 시에는 재생성 안 함
>
>   return (
>     <div>
>       <input onChange={handleSearch} />
>       <button onClick={() => setCount(count + 1)}>Count: {count}</button>
>       <ItemList items={filteredItems} />
>     </div>
>   );
> }
>
> // 자식 컴포넌트에 함수 전달 시 useCallback 필수
> const MemoizedChild = React.memo(({ onAction }) => {
>   console.log('자식 렌더링');
>   return <button onClick={onAction}>액션</button>;
> });
>
> function Parent() {
>   const [count, setCount] = useState(0);
>
>   // useCallback 없으면: count 변경 → 함수 재생성 → 자식 리렌더링
>   // useCallback 사용: count 변경 → 함수 재사용 → 자식 리렌더링 안 함
>   const handleAction = useCallback(() => {
>     console.log('액션 실행');
>   }, []);
>
>   return (
>     <div>
>       <button onClick={() => setCount(count + 1)}>Count: {count}</button>
>       <MemoizedChild onAction={handleAction} />
>     </div>
>   );
> }
> ```

---

**Q7.** useReducer와 useState의 차이는 무엇이며, 언제 useReducer를 사용해야 하나요?

> **기본 차이:**
> - `useState`: 단순한 상태 관리
> - `useReducer`: 복잡한 상태 로직을 reducer 함수로 분리하여 관리
>
> **비교표:**
>
> | 특성 | useState | useReducer |
> |------|----------|------------|
> | 상태 업데이트 방식 | 직접 값 설정 | action 디스패치 |
> | 복잡도 | 단순 | 복잡 |
> | 로직 위치 | 컴포넌트 내부 | reducer 함수 (외부 가능) |
> | 테스트 용이성 | 낮음 | 높음 (reducer 독립 테스트) |
> | 상태 추적 | 어려움 | 쉬움 (action 로그) |
>
> **useReducer 사용 시점:**
> - 여러 상태가 연관되어 함께 업데이트되는 경우
> - 상태 업데이트 로직이 복잡한 경우
> - 다음 상태가 이전 상태에 의존하는 경우
> - 상태 업데이트 로직을 컴포넌트 외부로 분리하고 싶은 경우
> - Redux 패턴에 익숙한 경우
>
> **예시:**
>
> ```javascript
> // useState: 간단한 카운터
> function Counter() {
>   const [count, setCount] = useState(0);
>
>   return (
>     <div>
>       <p>Count: {count}</p>
>       <button onClick={() => setCount(count + 1)}>+</button>
>       <button onClick={() => setCount(count - 1)}>-</button>
>     </div>
>   );
> }
>
> // useReducer: 복잡한 폼 상태 관리
> const initialState = {
>   username: '',
>   email: '',
>   password: '',
>   errors: {},
>   isSubmitting: false,
> };
>
> function formReducer(state, action) {
>   switch (action.type) {
>     case 'SET_FIELD':
>       return {
>         ...state,
>         [action.field]: action.value,
>         errors: { ...state.errors, [action.field]: null }, // 에러 초기화
>       };
>     case 'SET_ERROR':
>       return {
>         ...state,
>         errors: { ...state.errors, ...action.errors },
>       };
>     case 'SUBMIT_START':
>       return { ...state, isSubmitting: true };
>     case 'SUBMIT_SUCCESS':
>       return initialState; // 초기 상태로 리셋
>     case 'SUBMIT_FAIL':
>       return { ...state, isSubmitting: false };
>     case 'RESET':
>       return initialState;
>     default:
>       return state;
>   }
> }
>
> function RegistrationForm() {
>   const [state, dispatch] = useReducer(formReducer, initialState);
>
>   const handleChange = (field) => (e) => {
>     dispatch({ type: 'SET_FIELD', field, value: e.target.value });
>   };
>
>   const handleSubmit = async (e) => {
>     e.preventDefault();
>     dispatch({ type: 'SUBMIT_START' });
>
>     try {
>       await api.register(state);
>       dispatch({ type: 'SUBMIT_SUCCESS' });
>     } catch (error) {
>       dispatch({ type: 'SET_ERROR', errors: error.response.data });
>       dispatch({ type: 'SUBMIT_FAIL' });
>     }
>   };
>
>   return (
>     <form onSubmit={handleSubmit}>
>       <input value={state.username} onChange={handleChange('username')} />
>       {state.errors.username && <span>{state.errors.username}</span>}
>
>       <input value={state.email} onChange={handleChange('email')} />
>       {state.errors.email && <span>{state.errors.email}</span>}
>
>       <input type="password" value={state.password} onChange={handleChange('password')} />
>       {state.errors.password && <span>{state.errors.password}</span>}
>
>       <button disabled={state.isSubmitting}>
>         {state.isSubmitting ? '제출 중...' : '가입하기'}
>       </button>
>
>       <button type="button" onClick={() => dispatch({ type: 'RESET' })}>
>         초기화
>       </button>
>     </form>
>   );
> }
>
> // useReducer: 장바구니 관리
> function cartReducer(state, action) {
>   switch (action.type) {
>     case 'ADD_ITEM':
>       const existing = state.items.find(item => item.id === action.item.id);
>       if (existing) {
>         return {
>           ...state,
>           items: state.items.map(item =>
>             item.id === action.item.id
>               ? { ...item, quantity: item.quantity + 1 }
>               : item
>           ),
>           total: state.total + action.item.price,
>         };
>       }
>       return {
>         ...state,
>         items: [...state.items, { ...action.item, quantity: 1 }],
>         total: state.total + action.item.price,
>       };
>     case 'REMOVE_ITEM':
>       const item = state.items.find(item => item.id === action.id);
>       return {
>         ...state,
>         items: state.items.filter(item => item.id !== action.id),
>         total: state.total - (item.price * item.quantity),
>       };
>     case 'UPDATE_QUANTITY':
>       const oldItem = state.items.find(item => item.id === action.id);
>       const newQuantity = action.quantity;
>       const priceDiff = (newQuantity - oldItem.quantity) * oldItem.price;
>
>       return {
>         ...state,
>         items: state.items.map(item =>
>           item.id === action.id
>             ? { ...item, quantity: newQuantity }
>             : item
>         ),
>         total: state.total + priceDiff,
>       };
>     case 'CLEAR_CART':
>       return { items: [], total: 0 };
>     default:
>       return state;
>   }
> }
> ```

---

**Q8.** useLayoutEffect와 useEffect의 차이는 무엇이며, 언제 useLayoutEffect를 사용해야 하나요?

> **실행 시점 차이:**
>
> ```
> useEffect:
> [렌더링] → [DOM 업데이트] → [브라우저 페인트] → [useEffect 실행]
>
> useLayoutEffect:
> [렌더링] → [DOM 업데이트] → [useLayoutEffect 실행] → [브라우저 페인트]
> ```
>
> **비교표:**
>
> | 특성 | useEffect | useLayoutEffect |
> |------|-----------|-----------------|
> | 실행 시점 | 브라우저 페인트 **후** | 브라우저 페인트 **전** |
> | 동기/비동기 | 비동기 | 동기 |
> | UI 블로킹 | 없음 | 있음 |
> | 사용 빈도 | 대부분의 경우 | 드물게 |
> | 주요 용도 | 데이터 페칭, 구독, 로깅 | DOM 측정, 스크롤 위치, 애니메이션 |
>
> **useLayoutEffect 사용 시점:**
> - DOM 측정이 필요한 경우 (엘리먼트 크기, 위치)
> - DOM 변경 후 즉시 스타일 업데이트가 필요한 경우
> - 화면 깜빡임(flickering)을 방지해야 하는 경우
> - 스크롤 위치를 조정해야 하는 경우
>
> **예시:**
>
> ```javascript
> // useEffect: 화면 깜빡임 발생
> function Tooltip() {
>   const [tooltipHeight, setTooltipHeight] = useState(0);
>   const tooltipRef = useRef(null);
>
>   useEffect(() => {
>     const height = tooltipRef.current.getBoundingClientRect().height;
>     setTooltipHeight(height);
>     // 문제: 브라우저가 먼저 그린 후 높이 조정 → 깜빡임
>   }, []);
>
>   return (
>     <div
>       ref={tooltipRef}
>       style={{ top: `calc(100% + ${tooltipHeight}px)` }}
>     >
>       Tooltip
>     </div>
>   );
> }
>
> // useLayoutEffect: 깜빡임 없음
> function Tooltip() {
>   const [tooltipHeight, setTooltipHeight] = useState(0);
>   const tooltipRef = useRef(null);
>
>   useLayoutEffect(() => {
>     const height = tooltipRef.current.getBoundingClientRect().height;
>     setTooltipHeight(height);
>     // 브라우저가 그리기 전에 높이 조정 → 깜빡임 없음
>   }, []);
>
>   return (
>     <div
>       ref={tooltipRef}
>       style={{ top: `calc(100% + ${tooltipHeight}px)` }}
>     >
>       Tooltip
>     </div>
>   );
> }
>
> // 스크롤 위치 복원
> function ScrollRestoration() {
>   const scrollPositionRef = useRef(0);
>
>   useLayoutEffect(() => {
>     // 이전 스크롤 위치로 즉시 복원 (깜빡임 방지)
>     window.scrollTo(0, scrollPositionRef.current);
>   });
>
>   useEffect(() => {
>     const handleScroll = () => {
>       scrollPositionRef.current = window.scrollY;
>     };
>
>     window.addEventListener('scroll', handleScroll);
>     return () => window.removeEventListener('scroll', handleScroll);
>   }, []);
>
>   return <div>Content...</div>;
> }
>
> // 모달 포커스 트랩
> function Modal({ isOpen, children }) {
>   const modalRef = useRef(null);
>
>   useLayoutEffect(() => {
>     if (isOpen) {
>       // 모달 열리자마자 포커스 이동 (페인트 전)
>       const firstFocusable = modalRef.current.querySelector('button, input');
>       firstFocusable?.focus();
>     }
>   }, [isOpen]);
>
>   if (!isOpen) return null;
>
>   return <div ref={modalRef}>{children}</div>;
> }
> ```
>
> **주의사항:**
> - useLayoutEffect는 동기적으로 실행되어 UI를 블로킹하므로, 꼭 필요한 경우에만 사용
> - 대부분의 경우 useEffect로 충분하며, 성능상 더 유리
> - 서버 사이드 렌더링(SSR)에서는 useLayoutEffect 경고 발생 (브라우저 전용)

---

**Q9.** useRef로 저장한 값과 일반 변수의 차이는 무엇인가요?

> **주요 차이:**
>
> | 특성 | useRef | 일반 변수 (let/const) |
> |------|--------|----------------------|
> | 렌더링 간 유지 | O | X (매번 초기화) |
> | 값 변경 시 리렌더링 | X | X |
> | 참조 동일성 | O (항상 동일 객체) | X (매번 새로 생성) |
> | 사용 범위 | 컴포넌트 전체 생명주기 | 단일 렌더링 사이클 |
> | 메모리 위치 | React 내부 (fiber) | 함수 스코프 |
>
> **상세 비교:**
>
> ```javascript
> function Component() {
>   // 일반 변수: 매 렌더링마다 0으로 초기화
>   let normalCount = 0;
>
>   // useRef: 렌더링 간에 값 유지
>   const refCount = useRef(0);
>
>   // useState: 렌더링 간에 값 유지 + 변경 시 리렌더링
>   const [stateCount, setStateCount] = useState(0);
>
>   const handleClick = () => {
>     normalCount++;      // 다음 렌더링에서 다시 0
>     refCount.current++; // 값 유지되지만 리렌더링 안 됨
>     setStateCount(prev => prev + 1); // 값 유지 + 리렌더링
>
>     console.log('normalCount:', normalCount);     // 1
>     console.log('refCount:', refCount.current);   // 누적됨
>     console.log('stateCount:', stateCount);       // 이전 값 (비동기)
>   };
>
>   return <button onClick={handleClick}>클릭</button>;
> }
>
> // 렌더링 흐름:
> // 1. 초기 렌더링: normalCount=0, refCount=0, stateCount=0
> // 2. 클릭: normalCount=1, refCount=1, stateCount=1 (다음 렌더링에 반영)
> // 3. 리렌더링: normalCount=0 (초기화), refCount=1 (유지), stateCount=1 (업데이트됨)
> // 4. 또 클릭: normalCount=1, refCount=2, stateCount=2
> // 5. 리렌더링: normalCount=0, refCount=2, stateCount=2
> ```
>
> **실전 예시:**
>
> ```javascript
> // 잘못된 예: 일반 변수 사용
> function Timer() {
>   let timerId; // 매 렌더링마다 undefined로 초기화
>
>   const start = () => {
>     timerId = setInterval(() => console.log('tick'), 1000);
>   };
>
>   const stop = () => {
>     clearInterval(timerId); // timerId가 undefined일 수 있음!
>   };
>
>   return (
>     <div>
>       <button onClick={start}>시작</button>
>       <button onClick={stop}>중지</button>
>     </div>
>   );
> }
>
> // 올바른 예: useRef 사용
> function Timer() {
>   const timerIdRef = useRef(null); // 렌더링 간에 값 유지
>
>   const start = () => {
>     timerIdRef.current = setInterval(() => console.log('tick'), 1000);
>   };
>
>   const stop = () => {
>     clearInterval(timerIdRef.current); // 항상 최신 값 참조
>   };
>
>   return (
>     <div>
>       <button onClick={start}>시작</button>
>       <button onClick={stop}>중지</button>
>     </div>
>   );
> }
>
> // 렌더링 카운트 추적
> function RenderCounter() {
>   // 잘못된 예: 일반 변수
>   let normalRenderCount = 0;
>   normalRenderCount++; // 항상 1
>
>   // 올바른 예: useRef
>   const refRenderCount = useRef(0);
>   refRenderCount.current++; // 누적됨
>
>   const [count, setCount] = useState(0);
>
>   return (
>     <div>
>       <p>일반 변수 렌더링 카운트: {normalRenderCount}</p> {/* 항상 1 */}
>       <p>useRef 렌더링 카운트: {refRenderCount.current}</p> {/* 실제 카운트 */}
>       <button onClick={() => setCount(count + 1)}>리렌더링</button>
>     </div>
>   );
> }
> ```
>
> **언제 무엇을 사용할까:**
> - **일반 변수**: 렌더링 내에서만 사용되는 임시 값
> - **useRef**: 렌더링 간에 유지되지만 UI에 영향 없는 값 (타이머 ID, 이전 값, DOM 참조)
> - **useState**: UI에 반영되어야 하는 상태

---

## 심화/실무 (10~12)

**Q10.** useEffect의 의존성 배열(dependency array)은 어떻게 동작하며, 빈 배열을 넣으면 어떻게 되나요?

> **동작 원리:**
> - React는 의존성 배열의 각 값을 이전 렌더링의 값과 `Object.is()` 비교합니다.
> - 하나라도 변경되면 effect를 다시 실행합니다.
> - 얕은 비교(shallow comparison)를 사용하므로, 객체/배열은 참조가 변경되어야 합니다.
>
> **의존성 배열 패턴:**
>
> | 패턴 | 동작 | 사용 시점 |
> |------|------|----------|
> | `useEffect(() => {}, [a, b])` | `a` 또는 `b` 변경 시 실행 | 특정 값 의존 |
> | `useEffect(() => {}, [])` | 마운트 시 1회만 실행 | 초기화, 구독 설정 |
> | `useEffect(() => {})` | 매 렌더링마다 실행 | 거의 사용 안 함 (무한 루프 위험) |
>
> **빈 배열 `[]` 사용:**
> - `componentDidMount`와 동일하게 마운트 시 1회만 실행
> - cleanup 함수는 언마운트 시 1회만 실행 (`componentWillUnmount`)
>
> **예시:**
>
> ```javascript
> function Component({ userId }) {
>   const [data, setData] = useState(null);
>
>   // 1. 특정 값 의존: userId 변경 시마다 실행
>   useEffect(() => {
>     fetchUserData(userId).then(setData);
>   }, [userId]);
>
>   // 2. 빈 배열: 마운트 시 1회만 실행
>   useEffect(() => {
>     console.log('컴포넌트 마운트됨');
>
>     return () => {
>       console.log('컴포넌트 언마운트됨');
>     };
>   }, []);
>
>   // 3. 의존성 배열 없음: 매 렌더링마다 실행 (위험!)
>   useEffect(() => {
>     console.log('렌더링 완료');
>     // 주의: 여기서 setState 호출하면 무한 루프!
>   });
>
>   return <div>{data?.name}</div>;
> }
>
> // 무한 루프 예시와 해결
> function InfiniteLoopExample() {
>   const [count, setCount] = useState(0);
>
>   // 잘못된 예: 무한 루프
>   useEffect(() => {
>     setCount(count + 1); // 상태 변경 → 리렌더링 → effect 실행 → 상태 변경 → ...
>   }); // 의존성 배열 없음
>
>   // 잘못된 예: 객체 의존성으로 무한 루프
>   const options = { page: 1 }; // 매 렌더링마다 새 객체
>   useEffect(() => {
>     fetchData(options);
>   }, [options]); // options 참조가 항상 변경됨 → 무한 루프
>
>   // 해결 1: 빈 배열
>   useEffect(() => {
>     setCount(1);
>   }, []); // 1회만 실행
>
>   // 해결 2: 객체를 useMemo로 메모이제이션
>   const options = useMemo(() => ({ page: 1 }), []);
>   useEffect(() => {
>     fetchData(options);
>   }, [options]); // options 참조가 유지됨
>
>   // 해결 3: 객체 내부 값만 의존
>   const page = 1;
>   useEffect(() => {
>     fetchData({ page });
>   }, [page]);
> }
>
> // 의존성 배열 검증
> function DataFetcher({ endpoint, filters }) {
>   const [data, setData] = useState(null);
>
>   useEffect(() => {
>     // endpoint와 filters 모두 사용하므로 의존성 배열에 포함
>     const url = `${endpoint}?${new URLSearchParams(filters)}`;
>     fetch(url).then(res => res.json()).then(setData);
>   }, [endpoint, filters]); // ESLint가 자동으로 검증
>
>   return <div>{JSON.stringify(data)}</div>;
> }
>
> // 함수 의존성 문제와 해결
> function SearchComponent({ onSearch }) {
>   const [query, setQuery] = useState('');
>
>   // 문제: onSearch가 매 렌더링마다 새로운 함수면 effect가 계속 실행
>   useEffect(() => {
>     if (query) {
>       onSearch(query);
>     }
>   }, [query, onSearch]); // onSearch 의존
>
>   // 해결 1: 부모에서 useCallback 사용
>   // Parent: const onSearch = useCallback((q) => { ... }, []);
>
>   // 해결 2: useCallback으로 감싸기 (비권장, 책임 전가)
>   const stableOnSearch = useCallback(onSearch, []);
>   useEffect(() => {
>     if (query) {
>       stableOnSearch(query);
>     }
>   }, [query, stableOnSearch]);
> }
> ```
>
> **의존성 배열 주의사항:**
> - 객체/배열은 참조 비교이므로, 값이 같아도 새로 생성되면 변경으로 간주
> - 함수를 의존성에 포함할 때는 useCallback으로 메모이제이션 권장
> - ESLint의 `exhaustive-deps` 규칙을 활성화하여 누락된 의존성 자동 검증
> - 의존성을 의도적으로 제거하는 것은 버그의 원인 (stale closure)

---

**Q11.** React Hooks에서 발생할 수 있는 stale closure 문제는 무엇이며, 어떻게 해결하나요?

> **Stale Closure란:**
> - 클로저가 생성될 당시의 오래된(stale) 값을 참조하는 문제입니다.
> - useEffect, useCallback 등에서 상태나 props를 클로저로 캡처할 때 발생합니다.
>
> **발생 원리:**
> ```
> [렌더링 1] count=0 → 클로저 생성 (count=0 캡처)
>   ↓
> [렌더링 2] count=1 → 새 클로저 생성 (count=1 캡처)
>   ↓
> 하지만 이전 클로저는 여전히 count=0을 참조 (stale)
> ```
>
> **예시와 해결 방법:**
>
> ```javascript
> // 1. useEffect에서의 Stale Closure
> function Counter() {
>   const [count, setCount] = useState(0);
>
>   // 문제: 빈 배열로 1회만 실행 → count는 항상 0
>   useEffect(() => {
>     const interval = setInterval(() => {
>       console.log('Count:', count); // 항상 0 출력 (stale)
>       setCount(count + 1); // 항상 0 + 1 = 1
>     }, 1000);
>
>     return () => clearInterval(interval);
>   }, []); // count 의존성 누락
>
>   // 해결 1: 함수형 업데이트 (권장)
>   useEffect(() => {
>     const interval = setInterval(() => {
>       setCount(prev => {
>         console.log('Count:', prev); // 최신 값
>         return prev + 1;
>       });
>     }, 1000);
>
>     return () => clearInterval(interval);
>   }, []); // count 불필요
>
>   // 해결 2: 의존성 배열에 count 추가 (비권장, interval 재생성)
>   useEffect(() => {
>     const interval = setInterval(() => {
>       console.log('Count:', count); // 최신 값
>       setCount(count + 1);
>     }, 1000);
>
>     return () => clearInterval(interval);
>   }, [count]); // count 변경 시마다 interval 재생성
>
>   // 해결 3: useRef로 최신 값 추적
>   const countRef = useRef(count);
>
>   useEffect(() => {
>     countRef.current = count; // 항상 최신 값으로 업데이트
>   });
>
>   useEffect(() => {
>     const interval = setInterval(() => {
>       console.log('Count:', countRef.current); // 최신 값
>       setCount(prev => prev + 1);
>     }, 1000);
>
>     return () => clearInterval(interval);
>   }, []);
>
>   return <div>Count: {count}</div>;
> }
>
> // 2. 이벤트 핸들러에서의 Stale Closure
> function Message() {
>   const [message, setMessage] = useState('Hello');
>
>   // 문제: useCallback이 message를 캡처
>   const handleClick = useCallback(() => {
>     setTimeout(() => {
>       alert(message); // stale: 클릭 시점의 message 출력
>     }, 3000);
>   }, []); // message 의존성 누락
>
>   // 테스트:
>   // 1. "Hello" 상태에서 버튼 클릭
>   // 2. 즉시 "World"로 변경
>   // 3. 3초 후 "Hello" 알림 (stale)
>
>   // 해결 1: 의존성 배열에 message 추가
>   const handleClick = useCallback(() => {
>     setTimeout(() => {
>       alert(message); // 최신 message
>     }, 3000);
>   }, [message]); // message 변경 시 함수 재생성
>
>   // 해결 2: useRef로 최신 값 추적
>   const messageRef = useRef(message);
>
>   useEffect(() => {
>     messageRef.current = message;
>   });
>
>   const handleClick = useCallback(() => {
>     setTimeout(() => {
>       alert(messageRef.current); // 항상 최신 message
>     }, 3000);
>   }, []); // 함수 재생성 없음
>
>   return (
>     <div>
>       <input value={message} onChange={e => setMessage(e.target.value)} />
>       <button onClick={handleClick}>3초 후 메시지 표시</button>
>     </div>
>   );
> }
>
> // 3. Custom Hook에서의 Stale Closure
> function useInterval(callback, delay) {
>   // 문제: callback이 고정됨
>   useEffect(() => {
>     const interval = setInterval(callback, delay);
>     return () => clearInterval(interval);
>   }, [delay]); // callback 의존성 누락
>
>   // 해결: useRef로 최신 callback 유지
>   const savedCallback = useRef(callback);
>
>   useEffect(() => {
>     savedCallback.current = callback; // 매 렌더링마다 업데이트
>   });
>
>   useEffect(() => {
>     const interval = setInterval(() => {
>       savedCallback.current(); // 항상 최신 callback 호출
>     }, delay);
>
>     return () => clearInterval(interval);
>   }, [delay]);
> }
>
> function CounterWithInterval() {
>   const [count, setCount] = useState(0);
>
>   useInterval(() => {
>     console.log('Count:', count); // 항상 최신 count
>     setCount(count + 1);
>   }, 1000);
>
>   return <div>Count: {count}</div>;
> }
>
> // 4. 복잡한 상태 업데이트에서의 Stale Closure
> function TodoList() {
>   const [todos, setTodos] = useState([]);
>
>   // 문제: todos를 직접 참조
>   const addTodo = useCallback((text) => {
>     setTodos([...todos, { id: Date.now(), text }]); // stale todos
>   }, []); // todos 의존성 누락
>
>   // 해결: 함수형 업데이트
>   const addTodo = useCallback((text) => {
>     setTodos(prevTodos => [...prevTodos, { id: Date.now(), text }]);
>   }, []); // todos 불필요
>
>   return (
>     <div>
>       <button onClick={() => addTodo('New Todo')}>추가</button>
>       {todos.map(todo => <div key={todo.id}>{todo.text}</div>)}
>     </div>
>   );
> }
> ```
>
> **해결 전략 우선순위:**
> 1. **함수형 업데이트** (가장 권장): `setState(prev => ...)`
> 2. **useRef로 최신 값 추적**: 함수나 복잡한 로직에 사용
> 3. **의존성 배열 추가**: 단순한 경우, 재생성 비용이 적을 때
> 4. **useReducer**: 복잡한 상태 로직은 reducer로 분리
>
> **예방 방법:**
> - ESLint의 `exhaustive-deps` 규칙 활성화
> - 의존성 배열을 의도적으로 비우지 않기
> - useCallback/useMemo 남용 피하기 (대부분의 경우 불필요)

---

**Q12.** useEffect 내부에서 비동기 함수를 어떻게 처리해야 하나요?

> **기본 규칙:**
> - useEffect의 콜백 함수는 `async`가 될 수 없습니다.
> - cleanup 함수를 반환해야 하는데, `async` 함수는 Promise를 반환하기 때문입니다.
>
> **잘못된 예:**
> ```javascript
> // ❌ 작동하지 않음: useEffect는 async 함수를 받을 수 없음
> useEffect(async () => {
>   const data = await fetchData();
>   setData(data);
> }, []);
> ```
>
> **올바른 패턴:**
>
> ```javascript
> // 1. 내부에 async 함수 정의 (가장 일반적)
> useEffect(() => {
>   const fetchData = async () => {
>     try {
>       const response = await fetch('/api/data');
>       const data = await response.json();
>       setData(data);
>     } catch (error) {
>       setError(error);
>     } finally {
>       setLoading(false);
>     }
>   };
>
>   fetchData();
> }, []);
>
> // 2. IIFE (즉시 실행 함수) 사용
> useEffect(() => {
>   (async () => {
>     try {
>       const response = await fetch('/api/data');
>       const data = await response.json();
>       setData(data);
>     } catch (error) {
>       setError(error);
>     }
>   })();
> }, []);
>
> // 3. Promise.then 사용 (레거시 스타일)
> useEffect(() => {
>   fetch('/api/data')
>     .then(response => response.json())
>     .then(data => setData(data))
>     .catch(error => setError(error))
>     .finally(() => setLoading(false));
> }, []);
> ```
>
> **중요: Cleanup과 Race Condition 처리:**
>
> ```javascript
> // 문제: Race Condition (경쟁 상태)
> function UserProfile({ userId }) {
>   const [user, setUser] = useState(null);
>
>   useEffect(() => {
>     const fetchUser = async () => {
>       const data = await fetch(`/api/users/${userId}`);
>       setUser(data); // 문제: userId가 변경되어도 이전 요청이 완료되면 덮어씀
>     };
>
>     fetchUser();
>   }, [userId]);
>
>   // 시나리오:
>   // 1. userId=1로 요청 (느림, 3초 소요)
>   // 2. userId=2로 변경 → 새 요청 (빠름, 1초 소요)
>   // 3. userId=2 응답 도착 → setUser(user2)
>   // 4. userId=1 응답 도착 → setUser(user1) ← 잘못된 결과!
> }
>
> // 해결 1: 취소 플래그 (권장)
> function UserProfile({ userId }) {
>   const [user, setUser] = useState(null);
>
>   useEffect(() => {
>     let isCancelled = false;
>
>     const fetchUser = async () => {
>       try {
>         const response = await fetch(`/api/users/${userId}`);
>         const data = await response.json();
>
>         if (!isCancelled) { // cleanup 실행 안 됐으면 업데이트
>           setUser(data);
>         }
>       } catch (error) {
>         if (!isCancelled) {
>           setError(error);
>         }
>       }
>     };
>
>     fetchUser();
>
>     return () => {
>       isCancelled = true; // cleanup 시 플래그 설정
>     };
>   }, [userId]);
> }
>
> // 해결 2: AbortController (최신 방식, 권장)
> function UserProfile({ userId }) {
>   const [user, setUser] = useState(null);
>
>   useEffect(() => {
>     const abortController = new AbortController();
>
>     const fetchUser = async () => {
>       try {
>         const response = await fetch(`/api/users/${userId}`, {
>           signal: abortController.signal, // 취소 신호 연결
>         });
>         const data = await response.json();
>         setUser(data);
>       } catch (error) {
>         if (error.name === 'AbortError') {
>           console.log('요청 취소됨');
>         } else {
>           setError(error);
>         }
>       }
>     };
>
>     fetchUser();
>
>     return () => {
>       abortController.abort(); // cleanup 시 요청 취소
>     };
>   }, [userId]);
> }
>
> // 복잡한 예: 로딩, 에러, 재시도 처리
> function DataFetcher({ endpoint }) {
>   const [data, setData] = useState(null);
>   const [loading, setLoading] = useState(true);
>   const [error, setError] = useState(null);
>
>   useEffect(() => {
>     const abortController = new AbortController();
>     let retryCount = 0;
>     const maxRetries = 3;
>
>     const fetchData = async () => {
>       setLoading(true);
>       setError(null);
>
>       while (retryCount <= maxRetries) {
>         try {
>           const response = await fetch(endpoint, {
>             signal: abortController.signal,
>           });
>
>           if (!response.ok) {
>             throw new Error(`HTTP error! status: ${response.status}`);
>           }
>
>           const json = await response.json();
>           setData(json);
>           setLoading(false);
>           return; // 성공 시 종료
>
>         } catch (err) {
>           if (err.name === 'AbortError') {
>             return; // 취소된 요청은 무시
>           }
>
>           retryCount++;
>
>           if (retryCount > maxRetries) {
>             setError(err);
>             setLoading(false);
>           } else {
>             console.log(`재시도 ${retryCount}/${maxRetries}`);
>             await new Promise(resolve => setTimeout(resolve, 1000 * retryCount)); // 지수 백오프
>           }
>         }
>       }
>     };
>
>     fetchData();
>
>     return () => {
>       abortController.abort();
>     };
>   }, [endpoint]);
>
>   if (loading) return <div>로딩 중...</div>;
>   if (error) return <div>에러: {error.message}</div>;
>   return <div>{JSON.stringify(data)}</div>;
> }
>
> // Custom Hook으로 추상화 (재사용 가능)
> function useFetch(url) {
>   const [state, setState] = useState({
>     data: null,
>     loading: true,
>     error: null,
>   });
>
>   useEffect(() => {
>     const abortController = new AbortController();
>
>     const fetchData = async () => {
>       setState(prev => ({ ...prev, loading: true, error: null }));
>
>       try {
>         const response = await fetch(url, {
>           signal: abortController.signal,
>         });
>         const data = await response.json();
>         setState({ data, loading: false, error: null });
>       } catch (error) {
>         if (error.name !== 'AbortError') {
>           setState({ data: null, loading: false, error });
>         }
>       }
>     };
>
>     fetchData();
>
>     return () => abortController.abort();
>   }, [url]);
>
>   return state;
> }
>
> // 사용
> function UserList() {
>   const { data, loading, error } = useFetch('/api/users');
>
>   if (loading) return <div>로딩 중...</div>;
>   if (error) return <div>에러: {error.message}</div>;
>   return <ul>{data.map(user => <li key={user.id}>{user.name}</li>)}</ul>;
> }
> ```
>
> **권장 패턴:**
> 1. AbortController로 요청 취소 구현
> 2. 취소 플래그로 상태 업데이트 방지
> 3. 로딩/에러 상태를 명확히 관리
> 4. Custom Hook으로 재사용 가능하게 추상화
> 5. 실무에서는 React Query, SWR 같은 라이브러리 사용 권장

---

## 꼬리질문 대비 (13~15)

**Q13.** Hook의 규칙(Rules of Hooks)은 무엇이며, 왜 지켜야 하나요?

> **Hook의 2가지 규칙:**
>
> **1. 최상위(top level)에서만 Hook을 호출하세요**
> - 반복문, 조건문, 중첩 함수 내부에서 Hook 호출 금지
>
> **2. React 함수 내에서만 Hook을 호출하세요**
> - React 함수 컴포넌트 내부
> - Custom Hook 내부
> - 일반 JavaScript 함수에서 호출 금지
>
> **왜 지켜야 하나요:**
>
> React는 Hook 호출 순서를 기반으로 상태를 관리합니다.
>
> ```
> [내부 동작 원리]
> React는 각 컴포넌트마다 Hook을 배열로 저장:
>
> 첫 렌더링:
> hooks = [
>   useState('Alice'),    // index 0
>   useEffect(fn1, []),   // index 1
>   useState(25),         // index 2
> ]
>
> 두 번째 렌더링:
> hooks = [
>   useState('Alice'),    // index 0 - 첫 번째 useState와 매칭
>   useEffect(fn1, []),   // index 1 - 첫 번째 useEffect와 매칭
>   useState(25),         // index 2 - 두 번째 useState와 매칭
> ]
> ```
>
> **잘못된 예:**
>
> ```javascript
> // ❌ 조건문 내부에서 Hook 호출
> function Form() {
>   const [name, setName] = useState('');
>
>   if (name !== '') {
>     useEffect(() => {
>       localStorage.setItem('formData', name);
>     });
>   }
>
>   // 문제:
>   // 렌더링 1: name='' → useEffect 건너뜀 → hooks = [useState]
>   // 렌더링 2: name='Alice' → useEffect 실행 → hooks = [useState, useEffect]
>   // 렌더링 3: name='' → useEffect 건너뜀 → hooks = [useState]
>   // → 인덱스 불일치로 상태 깨짐!
> }
>
> // ❌ 반복문 내부에서 Hook 호출
> function TodoList({ todos }) {
>   const items = todos.map((todo, index) => {
>     const [checked, setChecked] = useState(false); // 매번 다른 개수의 Hook 생성
>     return <Todo key={index} checked={checked} onChange={setChecked} />;
>   });
>
>   // 문제: todos 개수가 변하면 Hook 개수도 변함 → 상태 깨짐
> }
>
> // ❌ 일반 함수에서 Hook 호출
> function formatUser(user) {
>   const [formattedName] = useState(user.name.toUpperCase());
>   return formattedName; // React 컨텍스트 없음 → 에러
> }
>
> // ❌ 이벤트 핸들러에서 Hook 호출
> function Button() {
>   const handleClick = () => {
>     const [count, setCount] = useState(0); // React 렌더링 사이클 외부
>     setCount(count + 1);
>   };
>
>   return <button onClick={handleClick}>클릭</button>;
> }
> ```
>
> **올바른 예:**
>
> ```javascript
> // ✅ 조건부 로직은 Hook 내부로 이동
> function Form() {
>   const [name, setName] = useState('');
>
>   useEffect(() => {
>     if (name !== '') { // 조건은 Hook 내부에
>       localStorage.setItem('formData', name);
>     }
>   }, [name]);
> }
>
> // ✅ 조건부 실행은 의존성 배열로 제어
> function Profile({ isLoggedIn }) {
>   const [user, setUser] = useState(null);
>
>   useEffect(() => {
>     if (isLoggedIn) {
>       fetchUser().then(setUser);
>     }
>   }, [isLoggedIn]);
> }
>
> // ✅ 반복되는 상태는 컴포넌트로 분리
> function TodoItem({ todo }) {
>   const [checked, setChecked] = useState(false);
>   return <Todo checked={checked} onChange={setChecked} />;
> }
>
> function TodoList({ todos }) {
>   return todos.map(todo => <TodoItem key={todo.id} todo={todo} />);
> }
>
> // ✅ Custom Hook 사용
> function useFormattedUser(user) {
>   const [formattedName] = useState(user.name.toUpperCase());
>   return formattedName;
> }
>
> function UserProfile({ user }) {
>   const formattedName = useFormattedUser(user); // React 컴포넌트 내부에서 호출
>   return <div>{formattedName}</div>;
> }
>
> // ✅ 조기 반환(early return) 전에 모든 Hook 호출
> function Component({ shouldRender }) {
>   // 모든 Hook을 최상위에서 먼저 호출
>   const [count, setCount] = useState(0);
>   const [name, setName] = useState('');
>
>   // 조기 반환은 Hook 호출 이후에
>   if (!shouldRender) {
>     return null;
>   }
>
>   return <div>{count} - {name}</div>;
> }
> ```
>
> **검증 도구:**
> - ESLint 플러그인: `eslint-plugin-react-hooks`
> - 자동으로 규칙 위반 감지 및 경고
> ```json
> {
>   "plugins": ["react-hooks"],
>   "rules": {
>     "react-hooks/rules-of-hooks": "error",
>     "react-hooks/exhaustive-deps": "warn"
>   }
> }
> ```

---

**Q14.** useState의 함수형 업데이트는 언제, 왜 사용하나요?

> **함수형 업데이트란:**
> - setState에 값 대신 함수를 전달하는 방식
> - 함수는 이전 상태를 인자로 받아 새 상태를 반환
>
> **형식:**
> ```javascript
> // 일반 업데이트
> setState(newValue);
>
> // 함수형 업데이트
> setState(prevState => newValue);
> ```
>
> **언제 사용:**
>
> **1. 이전 상태를 기반으로 업데이트할 때 (필수)**
> ```javascript
> // ❌ 잘못된 예: 배칭으로 인한 문제
> const [count, setCount] = useState(0);
>
> const handleClick = () => {
>   setCount(count + 1); // count: 0 → 1
>   setCount(count + 1); // count: 0 → 1 (여전히 0 기반)
>   setCount(count + 1); // count: 0 → 1 (여전히 0 기반)
>   // 결과: 1 (예상: 3)
> };
>
> // ✅ 올바른 예: 함수형 업데이트
> const handleClick = () => {
>   setCount(prev => prev + 1); // 0 → 1
>   setCount(prev => prev + 1); // 1 → 2
>   setCount(prev => prev + 1); // 2 → 3
>   // 결과: 3
> };
> ```
>
> **2. useEffect/useCallback의 의존성을 제거할 때**
> ```javascript
> // ❌ 의존성에 count 포함 → 매번 재생성
> const [count, setCount] = useState(0);
>
> const increment = useCallback(() => {
>   setCount(count + 1);
> }, [count]); // count 변경마다 함수 재생성
>
> // ✅ 함수형 업데이트 → 의존성 제거
> const increment = useCallback(() => {
>   setCount(prev => prev + 1);
> }, []); // count 불필요, 함수 재생성 없음
>
> // useEffect 예시
> // ❌
> useEffect(() => {
>   const interval = setInterval(() => {
>     setCount(count + 1);
>   }, 1000);
>   return () => clearInterval(interval);
> }, [count]); // count 변경마다 interval 재생성
>
> // ✅
> useEffect(() => {
>   const interval = setInterval(() => {
>     setCount(prev => prev + 1);
>   }, 1000);
>   return () => clearInterval(interval);
> }, []); // count 불필요
> ```
>
> **3. 복잡한 상태 업데이트 시 안전성 보장**
> ```javascript
> const [todos, setTodos] = useState([]);
>
> // ❌ 직접 참조: stale closure 위험
> const addTodo = useCallback((text) => {
>   setTodos([...todos, { id: Date.now(), text }]);
> }, []); // todos 의존성 누락 → stale
>
> // ✅ 함수형 업데이트: 항상 최신 상태
> const addTodo = useCallback((text) => {
>   setTodos(prevTodos => [...prevTodos, { id: Date.now(), text }]);
> }, []); // 의존성 불필요
>
> const removeTodo = useCallback((id) => {
>   setTodos(prevTodos => prevTodos.filter(todo => todo.id !== id));
> }, []);
>
> const toggleTodo = useCallback((id) => {
>   setTodos(prevTodos =>
>     prevTodos.map(todo =>
>       todo.id === id ? { ...todo, done: !todo.done } : todo
>     )
>   );
> }, []);
> ```
>
> **4. 성능 최적화가 필요할 때**
> ```javascript
> // 자식 컴포넌트에 전달되는 함수
> function Parent() {
>   const [items, setItems] = useState([]);
>
>   // ✅ 함수형 업데이트 → useCallback 의존성 배열 비움 → 자식 리렌더링 방지
>   const addItem = useCallback((item) => {
>     setItems(prev => [...prev, item]);
>   }, []); // items 불필요
>
>   return <ChildComponent onAdd={addItem} />; // addItem은 절대 변하지 않음
> }
>
> const ChildComponent = React.memo(({ onAdd }) => {
>   // onAdd가 변하지 않으므로 리렌더링 안 됨
>   return <button onClick={() => onAdd('new')}>추가</button>;
> });
> ```
>
> **왜 사용:**
> - **정확성**: 항상 최신 상태를 기반으로 업데이트
> - **성능**: 불필요한 의존성 제거 → 재생성 방지
> - **안전성**: stale closure 문제 예방
> - **가독성**: 상태 업데이트 로직을 명확하게 표현
>
> **추가 예시:**
> ```javascript
> // 토글 패턴
> const [isOpen, setIsOpen] = useState(false);
> const toggle = useCallback(() => {
>   setIsOpen(prev => !prev); // 이전 값의 반대
> }, []);
>
> // 객체 상태 업데이트
> const [user, setUser] = useState({ name: '', age: 0 });
> const updateName = useCallback((name) => {
>   setUser(prev => ({ ...prev, name })); // 이전 객체 유지하며 병합
> }, []);
>
> // 카운터 리셋
> const [count, setCount] = useState(0);
> const reset = useCallback(() => {
>   setCount(0); // 이전 값 불필요하면 직접 값 사용 가능
> }, []);
> ```

---

**Q15.** useMemo와 useCallback을 과도하게 사용하면 오히려 성능이 저하될 수 있는 이유는 무엇인가요?

> **메모이제이션의 비용:**
>
> useMemo와 useCallback도 공짜가 아닙니다. 다음과 같은 비용이 발생합니다:
>
> **1. 메모리 비용**
> - 이전 값과 의존성 배열을 메모리에 저장
> - 메모이제이션된 값/함수가 많을수록 메모리 사용량 증가
>
> **2. 비교 연산 비용**
> - 매 렌더링마다 의존성 배열의 각 값을 이전 값과 비교 (`Object.is()`)
> - 의존성이 많을수록 비교 연산 증가
>
> **3. 함수 호출 오버헤드**
> - useMemo/useCallback 자체의 실행 비용
> - Hook 호출, 의존성 비교, 결과 반환의 과정
>
> **비용 분석:**
>
> ```javascript
> // 메모이제이션 없음
> function Component() {
>   const value = a + b; // 단순 연산 (매우 빠름, ~1 마이크로초)
>   return <div>{value}</div>;
> }
>
> // 메모이제이션 있음
> function Component() {
>   const value = useMemo(() => a + b, [a, b]);
>   // 비용:
>   // 1. useMemo 호출 오버헤드
>   // 2. 의존성 배열 [a, b] 생성
>   // 3. 이전 [a, b]와 현재 [a, b] 비교
>   // 4. 같으면 캐시된 값 반환, 다르면 함수 실행
>   // → 총 비용이 단순 연산보다 클 수 있음!
>
>   return <div>{value}</div>;
> }
>
> // 언제 메모이제이션이 이득인가?
> // → 계산 비용 > 메모이제이션 비용 일 때만
>
> function Component({ items }) {
>   // ✅ 이득: 비용이 큰 연산
>   const sortedItems = useMemo(() => {
>     return items
>       .sort((a, b) => a.value - b.value)
>       .filter(item => item.active)
>       .map(item => ({ ...item, formatted: formatItem(item) }));
>   }, [items]); // 정렬, 필터링, 매핑 비용 > 메모이제이션 비용
>
>   // ❌ 손해: 비용이 작은 연산
>   const doubled = useMemo(() => count * 2, [count]); // 곱셈 비용 < 메모이제이션 비용
> }
> ```
>
> **과도한 사용의 문제점:**
>
> ```javascript
> // ❌ 안티패턴: 모든 것을 메모이제이션
> function OverOptimizedComponent({ user, theme }) {
>   const fullName = useMemo(() => `${user.firstName} ${user.lastName}`, [user.firstName, user.lastName]);
>   const age = useMemo(() => new Date().getFullYear() - user.birthYear, [user.birthYear]);
>   const isAdult = useMemo(() => age >= 18, [age]);
>   const greeting = useMemo(() => `Hello, ${fullName}!`, [fullName]);
>   const backgroundColor = useMemo(() => theme === 'dark' ? '#000' : '#fff', [theme]);
>
>   // 문제점:
>   // 1. 메모리: 5개의 캐시된 값 저장
>   // 2. 비교 연산: 총 6개의 의존성 비교 (firstName, lastName, birthYear, age, fullName, theme)
>   // 3. 가독성: 코드 복잡도 증가
>   // 4. 실제 성능 이득: 거의 없음 (모두 단순 연산)
>
>   return <div style={{ backgroundColor }}>{greeting} ({isAdult ? '성인' : '미성년자'})</div>;
> }
>
> // ✅ 적절한 최적화: 필요한 것만 메모이제이션
> function OptimizedComponent({ user, theme }) {
>   const fullName = `${user.firstName} ${user.lastName}`; // 메모이제이션 불필요
>   const age = new Date().getFullYear() - user.birthYear;
>   const isAdult = age >= 18;
>   const greeting = `Hello, ${fullName}!`;
>   const backgroundColor = theme === 'dark' ? '#000' : '#fff';
>
>   return <div style={{ backgroundColor }}>{greeting} ({isAdult ? '성인' : '미성년자'})</div>;
> }
> ```
>
> **useCallback 과도 사용의 문제:**
>
> ```javascript
> // ❌ 과도한 useCallback
> function Form() {
>   const [name, setName] = useState('');
>   const [email, setEmail] = useState('');
>
>   const handleNameChange = useCallback((e) => {
>     setName(e.target.value);
>   }, []); // 함수형 업데이트 아님 → 의미 없음
>
>   const handleEmailChange = useCallback((e) => {
>     setEmail(e.target.value);
>   }, []);
>
>   const handleSubmit = useCallback((e) => {
>     e.preventDefault();
>     console.log(name, email);
>   }, [name, email]); // 의존성 때문에 매번 재생성 → 메모이제이션 의미 없음
>
>   // 문제: 자식 컴포넌트가 React.memo되지 않았으면 useCallback 불필요
>   return (
>     <form onSubmit={handleSubmit}>
>       <input onChange={handleNameChange} /> {/* 일반 input: memo 안 됨 */}
>       <input onChange={handleEmailChange} /> {/* 일반 input: memo 안 됨 */}
>     </form>
>   );
> }
>
> // ✅ useCallback이 필요한 경우
> const MemoizedInput = React.memo(({ onChange }) => {
>   console.log('MemoizedInput 렌더링');
>   return <input onChange={onChange} />;
> });
>
> function Form() {
>   const [name, setName] = useState('');
>   const [count, setCount] = useState(0); // 관련 없는 상태
>
>   // ✅ React.memo된 자식 컴포넌트에 전달 → 유의미
>   const handleChange = useCallback((e) => {
>     setName(e.target.value);
>   }, []);
>
>   return (
>     <div>
>       <MemoizedInput onChange={handleChange} /> {/* count 변경 시 리렌더링 안 됨 */}
>       <button onClick={() => setCount(count + 1)}>Count: {count}</button>
>     </div>
>   );
> }
> ```
>
> **언제 사용해야 하나:**
>
> **useMemo 사용 시점:**
> - 비용이 큰 계산 (정렬, 필터링, 복잡한 변환)
> - 렌더링마다 새 객체/배열 생성을 피해야 할 때 (자식 컴포넌트 props)
> - 의존성이 자주 변하지 않을 때
>
> **useCallback 사용 시점:**
> - React.memo로 최적화된 자식 컴포넌트에 함수를 전달할 때
> - useEffect의 의존성 배열에 함수를 포함해야 할 때
> - 함수형 업데이트로 의존성을 제거할 수 있을 때
>
> **사용하지 말아야 할 때:**
> - 단순 계산 (덧셈, 문자열 결합, 조건 연산)
> - 자식 컴포넌트가 memo되지 않았을 때 (useCallback)
> - 의존성이 매번 변하는 경우 (메모이제이션 효과 없음)
> - 프로파일링 없이 "혹시 몰라서" 하는 최적화
>
> **권장 접근:**
> ```javascript
> // 1. 먼저 메모이제이션 없이 작성
> function Component({ items, filter }) {
>   const filtered = items.filter(item => item.category === filter);
>   return <List items={filtered} />;
> }
>
> // 2. 성능 문제가 발생하면 프로파일링
> // React DevTools Profiler로 측정
>
> // 3. 병목 지점만 최적화
> function Component({ items, filter }) {
>   // items가 크고 filter 변경이 드물면 useMemo 추가
>   const filtered = useMemo(
>     () => items.filter(item => item.category === filter),
>     [items, filter]
>   );
>   return <List items={filtered} />;
> }
> ```
>
> **결론:**
> - 메모이제이션은 측정 후에 필요한 곳에만 적용
> - "premature optimization is the root of all evil" (조기 최적화는 모든 악의 근원)
> - 가독성과 유지보수성을 먼저 고려하고, 성능 문제가 실제로 발생했을 때 최적화
