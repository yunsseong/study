# 2. React Hooks

---

## useState

### 기본 개념

useState는 함수형 컴포넌트에서 상태를 관리하기 위한 Hook입니다.

```javascript
const [state, setState] = useState(initialValue);
```

### 동작 원리

React는 컴포넌트 호출 순서를 기반으로 Hook의 상태를 내부 배열에 저장합니다.

```
[내부 동작 구조]

컴포넌트 렌더링 → React Fiber 노드 생성
                  ↓
           Hook 체인 생성
                  ↓
    [Hook0] → [Hook1] → [Hook2] → null
      ↓         ↓         ↓
   state:0   state:''  state:[]
```

각 useState 호출은 배열의 특정 인덱스와 매핑됩니다:

```javascript
// 첫 렌더링
hooks = [
  { state: 'Alice', setState: f },  // index 0
  { state: 25, setState: f },       // index 1
  { state: [], setState: f },       // index 2
]

// 리렌더링 시 동일한 인덱스로 매칭
```

### 비동기 업데이트 (Batching)

setState 호출은 즉시 실행되지 않고 업데이트 큐에 등록됩니다.

```javascript
const [count, setCount] = useState(0);

const handleClick = () => {
  setCount(count + 1); // count: 0 → 큐: [1]
  setCount(count + 1); // count: 0 → 큐: [1] (덮어씀)
  setCount(count + 1); // count: 0 → 큐: [1] (덮어씀)
  // 결과: 1
};
```

**배칭 흐름:**

```
[이벤트 핸들러 시작]
    ↓
setState 호출들 → 업데이트 큐에 추가
    ↓
[이벤트 핸들러 종료]
    ↓
큐의 업데이트들을 하나의 리렌더링으로 처리
    ↓
[DOM 업데이트]
```

### 함수형 업데이트

이전 상태를 기반으로 업데이트할 때 사용합니다.

```javascript
// 일반 업데이트: 현재 스냅샷 값 사용
setCount(count + 1);

// 함수형 업데이트: 최신 상태를 인자로 받음
setCount(prevCount => prevCount + 1);
```

**비교:**

```javascript
// 일반 업데이트
setCount(count + 1); // 0 + 1 = 1
setCount(count + 1); // 0 + 1 = 1
setCount(count + 1); // 0 + 1 = 1
// 최종: 1

// 함수형 업데이트
setCount(prev => prev + 1); // 0 + 1 = 1
setCount(prev => prev + 1); // 1 + 1 = 2
setCount(prev => prev + 1); // 2 + 1 = 3
// 최종: 3
```

### 사용 예시

```javascript
// 1. 단순 상태
const [count, setCount] = useState(0);

// 2. 객체 상태
const [user, setUser] = useState({ name: '', age: 0 });
setUser(prev => ({ ...prev, name: 'Alice' })); // 병합

// 3. 배열 상태
const [items, setItems] = useState([]);
setItems(prev => [...prev, newItem]); // 추가
setItems(prev => prev.filter(item => item.id !== id)); // 삭제

// 4. 지연 초기화 (초기값이 비용이 큰 경우)
const [state, setState] = useState(() => {
  const initialState = computeExpensiveValue();
  return initialState;
});
```

---

## useEffect

### 기본 개념

useEffect는 부수 효과(side effects)를 처리하는 Hook입니다.

```javascript
useEffect(() => {
  // 부수 효과 실행

  return () => {
    // cleanup 함수 (선택)
  };
}, [dependencies]);
```

### 생명주기 대체

```
클래스 컴포넌트              함수형 컴포넌트 (useEffect)
─────────────────────────────────────────────────────
componentDidMount          useEffect(() => { ... }, [])
componentDidUpdate         useEffect(() => { ... }, [deps])
componentWillUnmount       useEffect(() => { return () => { ... } }, [])
```

### 실행 시점

```
[컴포넌트 함수 실행]
    ↓
[JSX 반환]
    ↓
[Virtual DOM 생성]
    ↓
[실제 DOM에 커밋]
    ↓
[브라우저 페인트]
    ↓
[useEffect 실행] ← 비동기 (UI 블로킹 방지)
```

### 의존성 배열 (Dependency Array)

| 패턴 | 실행 시점 | 용도 |
|------|----------|------|
| `useEffect(fn)` | 매 렌더링마다 | 거의 사용 안 함 |
| `useEffect(fn, [])` | 마운트 시 1회 | 초기화, 구독 설정 |
| `useEffect(fn, [a, b])` | a 또는 b 변경 시 | 특정 값 감지 |

**의존성 비교 방식:**

```javascript
// React 내부: Object.is() 사용 (얕은 비교)
function areHookInputsEqual(nextDeps, prevDeps) {
  for (let i = 0; i < prevDeps.length; i++) {
    if (Object.is(nextDeps[i], prevDeps[i])) {
      continue;
    }
    return false; // 하나라도 다르면 effect 재실행
  }
  return true;
}
```

**주의사항:**

```javascript
// ❌ 객체/배열은 매번 새로 생성 → 무한 루프
const options = { key: 'value' };
useEffect(() => {
  fetchData(options);
}, [options]); // options 참조가 항상 변경됨

// ✅ 해결: useMemo로 메모이제이션
const options = useMemo(() => ({ key: 'value' }), []);
useEffect(() => {
  fetchData(options);
}, [options]);

// ✅ 또는 객체 내부 값만 의존
const key = 'value';
useEffect(() => {
  fetchData({ key });
}, [key]);
```

### Cleanup 함수

Cleanup 함수는 다음 상황에서 실행됩니다:

```
1. 컴포넌트 언마운트 시
2. Effect 재실행 전 (의존성 변경 시)
```

**실행 순서:**

```
[초기 렌더링]
    ↓
Effect 실행
    ↓
[의존성 변경]
    ↓
Cleanup 실행 ← 이전 effect 정리
    ↓
Effect 재실행 ← 새 effect
    ↓
[언마운트]
    ↓
Cleanup 실행 ← 최종 정리
```

**사용 예시:**

```javascript
// 1. 이벤트 리스너 정리
useEffect(() => {
  const handleScroll = () => console.log(window.scrollY);
  window.addEventListener('scroll', handleScroll);

  return () => {
    window.removeEventListener('scroll', handleScroll);
  };
}, []);

// 2. 타이머 정리
useEffect(() => {
  const timer = setInterval(() => {
    console.log('tick');
  }, 1000);

  return () => clearInterval(timer);
}, []);

// 3. WebSocket 정리
useEffect(() => {
  const ws = new WebSocket('ws://example.com');

  ws.onmessage = (event) => {
    console.log(event.data);
  };

  return () => ws.close();
}, []);

// 4. 비동기 작업 취소
useEffect(() => {
  const abortController = new AbortController();

  fetch('/api/data', { signal: abortController.signal })
    .then(res => res.json())
    .then(data => setData(data))
    .catch(err => {
      if (err.name !== 'AbortError') {
        console.error(err);
      }
    });

  return () => abortController.abort();
}, []);
```

### 무한 루프 주의

```javascript
// ❌ 무한 루프 패턴들
useEffect(() => {
  setCount(count + 1); // 상태 변경 → 리렌더링 → effect 실행 → ...
}); // 의존성 배열 없음

useEffect(() => {
  setItems([...items, newItem]); // items 변경 → effect 실행 → items 변경 → ...
}, [items]);

// ✅ 해결 방법
useEffect(() => {
  setCount(1);
}, []); // 1회만 실행

useEffect(() => {
  setItems(prev => [...prev, newItem]);
}, []); // 함수형 업데이트로 의존성 제거
```

---

## useRef

### 기본 개념

useRef는 렌더링 간에 유지되는 변경 가능한 값을 저장합니다.

```javascript
const ref = useRef(initialValue);
ref.current // 값 접근
```

### useState vs useRef

```
┌─────────────────┬──────────────┬──────────────┐
│      특성       │   useState   │    useRef    │
├─────────────────┼──────────────┼──────────────┤
│ 값 변경 시      │ 리렌더링 O   │ 리렌더링 X   │
│ 값 접근 방식    │ 직접 접근    │ .current     │
│ 변경 시점       │ 비동기       │ 동기 (즉시)  │
│ 주요 용도       │ UI 상태      │ DOM, 값 유지 │
│ 메모리 위치     │ React fiber  │ React fiber  │
└─────────────────┴──────────────┴──────────────┘
```

### 사용 시나리오

**1. DOM 요소 참조**

```javascript
function InputFocus() {
  const inputRef = useRef(null);

  const handleFocus = () => {
    inputRef.current.focus();
  };

  return (
    <>
      <input ref={inputRef} />
      <button onClick={handleFocus}>포커스</button>
    </>
  );
}
```

**2. 이전 값 저장**

```javascript
function Counter() {
  const [count, setCount] = useState(0);
  const prevCountRef = useRef();

  useEffect(() => {
    prevCountRef.current = count;
  }, [count]);

  return (
    <div>
      현재: {count}, 이전: {prevCountRef.current}
    </div>
  );
}
```

**3. 렌더링과 무관한 값 저장**

```javascript
function Timer() {
  const intervalRef = useRef(null);

  const start = () => {
    intervalRef.current = setInterval(() => {
      console.log('tick');
    }, 1000);
  };

  const stop = () => {
    clearInterval(intervalRef.current);
  };

  useEffect(() => {
    return () => clearInterval(intervalRef.current);
  }, []);

  return (
    <>
      <button onClick={start}>시작</button>
      <button onClick={stop}>중지</button>
    </>
  );
}
```

**4. 렌더링 카운트 추적**

```javascript
function RenderCounter() {
  const renderCount = useRef(0);

  useEffect(() => {
    renderCount.current++; // 리렌더링 없이 증가
  });

  return <div>렌더링 횟수: {renderCount.current}</div>;
}
```

### useRef vs 일반 변수

```javascript
function Component() {
  let normalVar = 0;        // 매 렌더링마다 0으로 초기화
  const refVar = useRef(0); // 렌더링 간에 유지

  const handleClick = () => {
    normalVar++;        // 다음 렌더링에서 다시 0
    refVar.current++;   // 값 유지됨
    console.log('normalVar:', normalVar);       // 1
    console.log('refVar:', refVar.current);     // 누적
  };

  return <button onClick={handleClick}>클릭</button>;
}
```

---

## useMemo와 useCallback

### 메모이제이션이란

이전에 계산한 값을 저장해두고 재사용하는 기법입니다.

```
[첫 렌더링]
    ↓
값 계산 → 캐시에 저장
    ↓
[리렌더링]
    ↓
의존성 비교 → 같으면 캐시에서 반환
             → 다르면 재계산 후 캐시 업데이트
```

### useMemo

**값**을 메모이제이션합니다.

```javascript
const memoizedValue = useMemo(() => computeExpensiveValue(a, b), [a, b]);
```

**사용 시점:**

```javascript
// ✅ 비용이 큰 계산
const sortedItems = useMemo(() => {
  return items
    .sort((a, b) => a.value - b.value)
    .filter(item => item.active)
    .map(item => transformItem(item));
}, [items]);

// ❌ 단순 계산 (메모이제이션 불필요)
const doubled = useMemo(() => count * 2, [count]);
```

### useCallback

**함수**를 메모이제이션합니다.

```javascript
const memoizedCallback = useCallback(() => {
  doSomething(a, b);
}, [a, b]);
```

**관계:**

```javascript
// useCallback은 useMemo의 특수 케이스
useCallback(fn, deps) === useMemo(() => fn, deps)
```

### 비교

```
┌──────────────┬──────────────────┬──────────────────┐
│    특성      │     useMemo      │   useCallback    │
├──────────────┼──────────────────┼──────────────────┤
│ 반환값       │ 계산된 값        │ 함수             │
│ 용도         │ 비용 큰 계산     │ 함수 재생성 방지 │
│ 사용 예      │ 정렬, 필터링     │ 이벤트 핸들러    │
└──────────────┴──────────────────┴──────────────────┘
```

### 실전 예시

```javascript
// 자식 컴포넌트 최적화
const MemoizedChild = React.memo(({ items, onAction }) => {
  console.log('자식 렌더링');
  return (
    <div>
      {items.map(item => <div key={item.id}>{item.name}</div>)}
      <button onClick={onAction}>액션</button>
    </div>
  );
});

function Parent() {
  const [count, setCount] = useState(0);
  const [items, setItems] = useState([
    { id: 1, name: 'Item 1' },
    { id: 2, name: 'Item 2' },
  ]);

  // useMemo: items가 변하지 않으면 동일한 배열 참조 유지
  const memoizedItems = useMemo(() => items, [items]);

  // useCallback: count 변경 시에도 함수 재생성 안 됨
  const handleAction = useCallback(() => {
    console.log('액션 실행');
  }, []);

  return (
    <div>
      <button onClick={() => setCount(count + 1)}>
        Count: {count}
      </button>
      {/* count 변경 시에도 자식 리렌더링 안 됨 */}
      <MemoizedChild items={memoizedItems} onAction={handleAction} />
    </div>
  );
}
```

### 남용 주의

**메모이제이션 비용:**

```
1. 메모리: 이전 값 + 의존성 배열 저장
2. 비교 연산: 매 렌더링마다 의존성 비교
3. 함수 호출: Hook 실행 오버헤드
```

**비용 vs 이득 분석:**

```javascript
// ❌ 손해: 단순 연산
const doubled = useMemo(() => count * 2, [count]);
// 곱셈 비용 < 메모이제이션 비용

// ✅ 이득: 복잡한 연산
const processed = useMemo(() => {
  return hugeArray
    .filter(item => item.active)
    .sort((a, b) => a.value - b.value)
    .map(item => complexTransform(item));
}, [hugeArray]);
// 연산 비용 > 메모이제이션 비용
```

---

## useReducer

### 기본 개념

복잡한 상태 로직을 reducer 함수로 분리하여 관리합니다.

```javascript
const [state, dispatch] = useReducer(reducer, initialState);
```

### Reducer 함수

```javascript
function reducer(state, action) {
  switch (action.type) {
    case 'ACTION_TYPE':
      return newState;
    default:
      return state;
  }
}
```

### useState vs useReducer

```
┌─────────────────┬─────────────────┬────────────────┐
│      특성       │    useState     │  useReducer    │
├─────────────────┼─────────────────┼────────────────┤
│ 상태 업데이트   │ 직접 값 설정    │ action 디스패치│
│ 로직 위치       │ 컴포넌트 내부   │ reducer 함수   │
│ 복잡도          │ 단순            │ 복잡           │
│ 테스트 용이성   │ 낮음            │ 높음           │
│ 상태 추적       │ 어려움          │ 쉬움 (action)  │
└─────────────────┴─────────────────┴────────────────┘
```

### 사용 시점

- 여러 상태가 연관되어 함께 업데이트
- 상태 업데이트 로직이 복잡
- 다음 상태가 이전 상태에 의존
- 로직을 컴포넌트 외부로 분리
- Redux 패턴 선호

### 실전 예시

**폼 관리:**

```javascript
const initialState = {
  username: '',
  email: '',
  password: '',
  errors: {},
  isSubmitting: false,
};

function formReducer(state, action) {
  switch (action.type) {
    case 'SET_FIELD':
      return {
        ...state,
        [action.field]: action.value,
        errors: { ...state.errors, [action.field]: null },
      };
    case 'SET_ERROR':
      return {
        ...state,
        errors: { ...state.errors, ...action.errors },
      };
    case 'SUBMIT_START':
      return { ...state, isSubmitting: true };
    case 'SUBMIT_SUCCESS':
      return initialState;
    case 'SUBMIT_FAIL':
      return { ...state, isSubmitting: false };
    case 'RESET':
      return initialState;
    default:
      return state;
  }
}

function Form() {
  const [state, dispatch] = useReducer(formReducer, initialState);

  const handleChange = (field) => (e) => {
    dispatch({ type: 'SET_FIELD', field, value: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch({ type: 'SUBMIT_START' });

    try {
      await api.register(state);
      dispatch({ type: 'SUBMIT_SUCCESS' });
    } catch (error) {
      dispatch({ type: 'SET_ERROR', errors: error.response.data });
      dispatch({ type: 'SUBMIT_FAIL' });
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={state.username} onChange={handleChange('username')} />
      {state.errors.username && <span>{state.errors.username}</span>}

      <button disabled={state.isSubmitting}>
        {state.isSubmitting ? '제출 중...' : '가입하기'}
      </button>
    </form>
  );
}
```

**장바구니 관리:**

```javascript
function cartReducer(state, action) {
  switch (action.type) {
    case 'ADD_ITEM':
      const existing = state.items.find(item => item.id === action.item.id);
      if (existing) {
        return {
          ...state,
          items: state.items.map(item =>
            item.id === action.item.id
              ? { ...item, quantity: item.quantity + 1 }
              : item
          ),
          total: state.total + action.item.price,
        };
      }
      return {
        ...state,
        items: [...state.items, { ...action.item, quantity: 1 }],
        total: state.total + action.item.price,
      };

    case 'REMOVE_ITEM':
      const item = state.items.find(item => item.id === action.id);
      return {
        ...state,
        items: state.items.filter(item => item.id !== action.id),
        total: state.total - (item.price * item.quantity),
      };

    case 'UPDATE_QUANTITY':
      const oldItem = state.items.find(item => item.id === action.id);
      const priceDiff = (action.quantity - oldItem.quantity) * oldItem.price;
      return {
        ...state,
        items: state.items.map(item =>
          item.id === action.id
            ? { ...item, quantity: action.quantity }
            : item
        ),
        total: state.total + priceDiff,
      };

    case 'CLEAR_CART':
      return { items: [], total: 0 };

    default:
      return state;
  }
}
```

---

## Custom Hooks

### 기본 개념

`use`로 시작하는 이름의 함수로, 내부에서 다른 Hook을 사용할 수 있습니다.

```javascript
function useCustomHook(params) {
  // Hook 로직
  return values;
}
```

### 사용 이유

```
┌────────────────────────────────────────┐
│ 1. 로직 재사용: 여러 컴포넌트에서 공유 │
│ 2. 관심사 분리: 비즈니스 로직과 UI 분리│
│ 3. 테스트 용이성: 독립적으로 테스트    │
│ 4. 코드 구조화: 복잡한 로직 추상화     │
└────────────────────────────────────────┘
```

### 실전 예시

**1. 폼 입력 관리**

```javascript
function useInput(initialValue) {
  const [value, setValue] = useState(initialValue);

  const handleChange = (e) => {
    setValue(e.target.value);
  };

  const reset = () => {
    setValue(initialValue);
  };

  return { value, onChange: handleChange, reset };
}

// 사용
function LoginForm() {
  const email = useInput('');
  const password = useInput('');

  return (
    <form>
      <input {...email} type="email" />
      <input {...password} type="password" />
      <button onClick={() => { email.reset(); password.reset(); }}>
        초기화
      </button>
    </form>
  );
}
```

**2. 데이터 페칭**

```javascript
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const abortController = new AbortController();

    const fetchData = async () => {
      try {
        setLoading(true);
        const response = await fetch(url, {
          signal: abortController.signal,
        });
        const json = await response.json();
        setData(json);
      } catch (err) {
        if (err.name !== 'AbortError') {
          setError(err);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();

    return () => abortController.abort();
  }, [url]);

  return { data, loading, error };
}

// 사용
function UserProfile({ userId }) {
  const { data, loading, error } = useFetch(`/api/users/${userId}`);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>에러: {error.message}</div>;
  return <div>{data.name}</div>;
}
```

**3. 로컬 스토리지 동기화**

```javascript
function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      return initialValue;
    }
  });

  const setValue = (value) => {
    try {
      const valueToStore = value instanceof Function
        ? value(storedValue)
        : value;

      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error(error);
    }
  };

  return [storedValue, setValue];
}

// 사용
function Settings() {
  const [theme, setTheme] = useLocalStorage('theme', 'light');

  return (
    <select value={theme} onChange={e => setTheme(e.target.value)}>
      <option value="light">라이트</option>
      <option value="dark">다크</option>
    </select>
  );
}
```

**4. 인터벌 (Dan Abramov 패턴)**

```javascript
function useInterval(callback, delay) {
  const savedCallback = useRef(callback);

  // 매 렌더링마다 최신 callback 저장
  useEffect(() => {
    savedCallback.current = callback;
  });

  useEffect(() => {
    if (delay === null) return;

    const interval = setInterval(() => {
      savedCallback.current(); // 항상 최신 callback 호출
    }, delay);

    return () => clearInterval(interval);
  }, [delay]);
}

// 사용
function Counter() {
  const [count, setCount] = useState(0);

  useInterval(() => {
    setCount(count + 1); // 항상 최신 count 참조
  }, 1000);

  return <div>{count}</div>;
}
```

---

## Hook 규칙 (Rules of Hooks)

### 2가지 규칙

```
┌──────────────────────────────────────────────┐
│ 1. 최상위(top level)에서만 Hook을 호출하세요│
│    - 반복문, 조건문, 중첩 함수 내부 금지    │
│                                              │
│ 2. React 함수 내에서만 Hook을 호출하세요    │
│    - React 함수 컴포넌트                    │
│    - Custom Hook                             │
│    - 일반 JavaScript 함수 금지              │
└──────────────────────────────────────────────┘
```

### 왜 지켜야 하나

React는 Hook 호출 순서를 기반으로 상태를 관리합니다.

```
[정상적인 경우]

렌더링 1:
hooks = [useState, useEffect, useState]
         ↓        ↓          ↓
       index 0  index 1   index 2

렌더링 2:
hooks = [useState, useEffect, useState]
         ↓        ↓          ↓
       index 0  index 1   index 2
       (매칭됨)

[잘못된 경우: 조건문 내부에서 Hook 호출]

렌더링 1 (조건 false):
hooks = [useState, useState]
         ↓        ↓
       index 0  index 1

렌더링 2 (조건 true):
hooks = [useState, useEffect, useState]
         ↓        ↓          ↓
       index 0  index 1   index 2

→ 인덱스 불일치 → 상태 깨짐!
```

### 잘못된 예

```javascript
// ❌ 조건문 내부
function Component({ condition }) {
  if (condition) {
    const [state, setState] = useState(0); // 조건부 Hook
  }
}

// ❌ 반복문 내부
function List({ items }) {
  items.forEach(item => {
    const [selected, setSelected] = useState(false); // 반복문 내 Hook
  });
}

// ❌ 중첩 함수 내부
function Component() {
  const handleClick = () => {
    const [count, setCount] = useState(0); // 이벤트 핸들러 내 Hook
  };
}

// ❌ 일반 JavaScript 함수
function normalFunction() {
  const [value, setValue] = useState(0); // React 컨텍스트 없음
}

// ❌ 조기 반환 후 Hook
function Component({ shouldRender }) {
  if (!shouldRender) return null; // 조기 반환

  const [state, setState] = useState(0); // 조건부로 호출됨
}
```

### 올바른 예

```javascript
// ✅ 조건 로직은 Hook 내부로
function Component({ condition }) {
  const [state, setState] = useState(0);

  useEffect(() => {
    if (condition) { // 조건은 Hook 내부에
      doSomething();
    }
  }, [condition]);
}

// ✅ 반복되는 상태는 컴포넌트로 분리
function Item({ item }) {
  const [selected, setSelected] = useState(false);
  return <div>{item.name}</div>;
}

function List({ items }) {
  return items.map(item => <Item key={item.id} item={item} />);
}

// ✅ 모든 Hook을 최상위에서 먼저 호출
function Component({ shouldRender }) {
  // Hook을 먼저 호출
  const [state, setState] = useState(0);
  const [count, setCount] = useState(0);

  // 조기 반환은 이후에
  if (!shouldRender) return null;

  return <div>{state} - {count}</div>;
}
```

### ESLint 검증

```json
{
  "plugins": ["react-hooks"],
  "rules": {
    "react-hooks/rules-of-hooks": "error",
    "react-hooks/exhaustive-deps": "warn"
  }
}
```

---

## useLayoutEffect vs useEffect

### 실행 시점 차이

```
useEffect:
┌─────────────┐   ┌──────────┐   ┌───────────┐   ┌────────────────┐
│ 컴포넌트    │ → │ DOM      │ → │ 브라우저  │ → │ useEffect 실행 │
│ 렌더링      │   │ 업데이트 │   │ 페인트    │   │ (비동기)       │
└─────────────┘   └──────────┘   └───────────┘   └────────────────┘

useLayoutEffect:
┌─────────────┐   ┌──────────┐   ┌────────────────────┐   ┌───────────┐
│ 컴포넌트    │ → │ DOM      │ → │ useLayoutEffect    │ → │ 브라우저  │
│ 렌더링      │   │ 업데이트 │   │ 실행 (동기)        │   │ 페인트    │
└─────────────┘   └──────────┘   └────────────────────┘   └───────────┘
```

### 비교표

```
┌──────────────┬─────────────────┬──────────────────┐
│    특성      │   useEffect     │ useLayoutEffect  │
├──────────────┼─────────────────┼──────────────────┤
│ 실행 시점    │ 페인트 후       │ 페인트 전        │
│ 동기/비동기  │ 비동기          │ 동기             │
│ UI 블로킹    │ 없음            │ 있음             │
│ 사용 빈도    │ 대부분          │ 드물게           │
│ 주요 용도    │ 데이터 페칭     │ DOM 측정         │
└──────────────┴─────────────────┴──────────────────┘
```

### 사용 시점

**useLayoutEffect를 사용해야 하는 경우:**

- DOM 측정 (요소 크기, 위치)
- DOM 변경 후 즉시 스타일 업데이트
- 화면 깜빡임(flickering) 방지
- 스크롤 위치 조정

### 예시

```javascript
// useEffect: 깜빡임 발생
function Tooltip() {
  const [height, setHeight] = useState(0);
  const ref = useRef(null);

  useEffect(() => {
    const h = ref.current.getBoundingClientRect().height;
    setHeight(h);
    // 문제: 브라우저가 먼저 그림 → 높이 조정 → 깜빡임
  }, []);

  return <div ref={ref} style={{ top: `calc(100% + ${height}px)` }}>
    Tooltip
  </div>;
}

// useLayoutEffect: 깜빡임 없음
function Tooltip() {
  const [height, setHeight] = useState(0);
  const ref = useRef(null);

  useLayoutEffect(() => {
    const h = ref.current.getBoundingClientRect().height;
    setHeight(h);
    // 브라우저가 그리기 전에 높이 조정 → 깜빡임 없음
  }, []);

  return <div ref={ref} style={{ top: `calc(100% + ${height}px)` }}>
    Tooltip
  </div>;
}
```

### 주의사항

```
1. useLayoutEffect는 동기 실행 → UI 블로킹
2. 대부분의 경우 useEffect로 충분
3. SSR에서는 경고 발생 (브라우저 전용)
4. 성능 문제 시 useEffect로 변경 고려
```

---

## 클로저와 Stale Closure

### Stale Closure란

클로저가 생성될 당시의 오래된(stale) 값을 참조하는 문제입니다.

```
[렌더링 1] count=0 → 클로저 생성 (count=0 캡처)
    ↓
[렌더링 2] count=1 → 새 클로저 생성 (count=1 캡처)
    ↓
이전 클로저는 여전히 count=0 참조 (stale)
```

### 발생 상황

```javascript
// 문제 상황
function Counter() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Count:', count); // 항상 0 (stale)
      setCount(count + 1); // 항상 0 + 1 = 1
    }, 1000);

    return () => clearInterval(interval);
  }, []); // count 의존성 누락

  return <div>{count}</div>;
}
```

**왜 발생하나:**

```
1. useEffect가 count=0일 때 실행
2. setInterval 콜백이 count=0을 클로저로 캡처
3. count가 1, 2, 3으로 변해도
4. interval 콜백은 여전히 count=0을 참조
```

### 해결 방법

**1. 함수형 업데이트 (권장)**

```javascript
useEffect(() => {
  const interval = setInterval(() => {
    setCount(prev => prev + 1); // 항상 최신 값
  }, 1000);

  return () => clearInterval(interval);
}, []); // count 불필요
```

**2. useRef로 최신 값 추적**

```javascript
const countRef = useRef(count);

useEffect(() => {
  countRef.current = count; // 항상 최신 값으로 업데이트
});

useEffect(() => {
  const interval = setInterval(() => {
    console.log('Count:', countRef.current); // 최신 값
    setCount(prev => prev + 1);
  }, 1000);

  return () => clearInterval(interval);
}, []);
```

**3. 의존성 배열에 추가 (비권장)**

```javascript
useEffect(() => {
  const interval = setInterval(() => {
    console.log('Count:', count);
    setCount(count + 1);
  }, 1000);

  return () => clearInterval(interval);
}, [count]); // count 변경마다 interval 재생성 (비효율적)
```

### 이벤트 핸들러 예시

```javascript
function Message() {
  const [message, setMessage] = useState('Hello');

  // 문제: stale closure
  const handleClick = useCallback(() => {
    setTimeout(() => {
      alert(message); // 클릭 시점의 message (stale)
    }, 3000);
  }, []); // message 의존성 누락

  // 해결: useRef
  const messageRef = useRef(message);

  useEffect(() => {
    messageRef.current = message;
  });

  const handleClick = useCallback(() => {
    setTimeout(() => {
      alert(messageRef.current); // 항상 최신 message
    }, 3000);
  }, []);

  return (
    <div>
      <input value={message} onChange={e => setMessage(e.target.value)} />
      <button onClick={handleClick}>3초 후 메시지</button>
    </div>
  );
}
```

---

## 면접 핵심 정리

### useState

```
핵심 개념:
- Hook 호출 순서 기반 상태 관리
- 비동기 업데이트 (배칭)
- 함수형 업데이트로 stale closure 방지

면접 포인트:
Q: 왜 비동기인가?
A: 배칭으로 성능 최적화, 일관성 유지

Q: 함수형 업데이트는 언제?
A: 이전 상태 기반 업데이트, 의존성 제거
```

### useEffect

```
핵심 개념:
- 렌더링 후 비동기 실행
- cleanup으로 메모리 누수 방지
- 의존성 배열로 실행 제어

면접 포인트:
Q: cleanup은 언제?
A: 언마운트, effect 재실행 전

Q: 빈 배열 []의 의미?
A: componentDidMount와 동일 (1회 실행)

Q: 무한 루프 방지?
A: 의존성 배열 정확히 설정, 함수형 업데이트
```

### useRef

```
핵심 개념:
- 렌더링 간 값 유지, 변경 시 리렌더링 없음
- DOM 참조, 이전 값 저장

면접 포인트:
Q: useState와 차이?
A: 리렌더링 없음, 동기 업데이트

Q: 주요 용도?
A: DOM 접근, 타이머 ID, 이전 값 추적
```

### useMemo & useCallback

```
핵심 개념:
- useMemo: 값 메모이제이션
- useCallback: 함수 메모이제이션
- 과도한 사용은 성능 저하

면접 포인트:
Q: 언제 사용?
A: 비용 큰 계산, React.memo된 자식에 props 전달

Q: 남용 시 문제?
A: 메모리 비용, 비교 연산 비용 > 계산 비용
```

### Hook 규칙

```
핵심 개념:
1. 최상위에서만 호출
2. React 함수 내에서만 호출

면접 포인트:
Q: 왜 조건문 내부에서 안 되나?
A: Hook 호출 순서로 상태 관리 → 순서 변경 시 깨짐

Q: 검증 방법?
A: ESLint react-hooks/rules-of-hooks
```

### Stale Closure

```
핵심 개념:
- 클로저가 오래된 값 참조
- useEffect, useCallback에서 발생

면접 포인트:
Q: 해결 방법?
A: 함수형 업데이트, useRef로 최신 값 추적

Q: 왜 발생?
A: 클로저 생성 시점의 값 캡처 → 변경되어도 유지
```

### 비동기 처리

```
핵심 개념:
- useEffect는 async 불가
- cleanup으로 race condition 방지
- AbortController로 요청 취소

면접 포인트:
Q: useEffect에서 async 사용?
A: 내부에 async 함수 정의 후 호출

Q: race condition 방지?
A: cleanup에서 취소 플래그 또는 AbortController
```
