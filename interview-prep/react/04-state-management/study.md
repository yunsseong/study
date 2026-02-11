# 4. React 상태 관리

---

## Props Drilling 문제

### 정의
상위 컴포넌트의 상태를 여러 단계의 하위 컴포넌트로 전달하기 위해, 중간 컴포넌트들이 실제로 사용하지 않는 props를 단순히 전달만 하는 현상

### 문제점
- **가독성 저하**: 중간 컴포넌트가 불필요한 props로 복잡해짐
- **유지보수 어려움**: props 변경 시 모든 중간 단계 수정 필요
- **재사용성 감소**: 특정 props에 의존하게 되어 독립적 사용 불가
- **리팩토링 부담**: 컴포넌트 구조 변경 시 전체 경로 수정

### 예시 코드

```jsx
// Props Drilling 발생
function App() {
  const [user, setUser] = useState({ name: 'Kim', age: 25 });
  return <Parent user={user} />;
}

function Parent({ user }) {
  // user를 사용하지 않지만 전달만 함
  return <Middle user={user} />;
}

function Middle({ user }) {
  // user를 사용하지 않지만 전달만 함
  return <Child user={user} />;
}

function Child({ user }) {
  // 실제로 사용하는 곳
  return <div>{user.name}</div>;
}
```

### 다이어그램

```
┌──────────┐
│   App    │  user 상태 소유
└────┬─────┘
     │ user (props)
     ▼
┌──────────┐
│  Parent  │  user 사용 안 함, 전달만
└────┬─────┘
     │ user (props)
     ▼
┌──────────┐
│  Middle  │  user 사용 안 함, 전달만
└────┬─────┘
     │ user (props)
     ▼
┌──────────┐
│  Child   │  user 실제 사용
└──────────┘
```

---

## Context API

### 개념
React에서 제공하는 전역 상태 관리 도구로, Props Drilling 없이 컴포넌트 트리 전체에 데이터를 공유

### 핵심 3요소

**1. createContext - Context 객체 생성**
```jsx
const UserContext = createContext(defaultValue);
```

**2. Provider - 값 제공**
```jsx
<UserContext.Provider value={{ user, setUser }}>
  <ChildComponents />
</UserContext.Provider>
```

**3. useContext - 값 읽기**
```jsx
const { user, setUser } = useContext(UserContext);
```

### 사용법

```jsx
import { createContext, useContext, useState } from 'react';

// 1. Context 생성
const UserContext = createContext(null);

// 2. Provider로 값 제공
function App() {
  const [user, setUser] = useState({ name: 'Kim' });

  return (
    <UserContext.Provider value={{ user, setUser }}>
      <Parent />
    </UserContext.Provider>
  );
}

// 3. 중간 컴포넌트는 props 불필요
function Parent() {
  return <Child />;
}

// 4. useContext로 직접 접근
function Child() {
  const { user, setUser } = useContext(UserContext);
  return (
    <div>
      <p>{user.name}</p>
      <button onClick={() => setUser({ name: 'Lee' })}>
        이름 변경
      </button>
    </div>
  );
}
```

### 동작 원리

```
┌──────────────────────────────────┐
│  <Provider value={data}>         │  값 제공
│                                  │
│    ┌──────┐      ┌──────┐       │
│    │  A   │      │  B   │       │
│    └──┬───┘      └──┬───┘       │
│       │             │            │
│       ▼             ▼            │
│    ┌──────┐      ┌──────┐       │
│    │  C   │      │  D   │       │  useContext로
│    └──────┘      └──────┘       │  어디서든 접근
│                                  │
└──────────────────────────────────┘
```

### 리렌더링 문제와 해결

**문제: Provider의 value 변경 시 모든 구독 컴포넌트 리렌더링**

```jsx
// ❌ 문제 발생
function App() {
  const [user, setUser] = useState({ name: 'Kim' });
  const [theme, setTheme] = useState('dark');

  // 매 렌더링마다 새 객체 생성 → 모든 구독자 리렌더링
  return (
    <AppContext.Provider value={{ user, setUser, theme, setTheme }}>
      <UserProfile />  {/* theme만 바뀌어도 리렌더링됨 */}
    </AppContext.Provider>
  );
}
```

**해결 방법**

**1. Context 분리**
```jsx
const UserContext = createContext();
const ThemeContext = createContext();

function App() {
  const [user, setUser] = useState({ name: 'Kim' });
  const [theme, setTheme] = useState('dark');

  return (
    <UserContext.Provider value={{ user, setUser }}>
      <ThemeContext.Provider value={{ theme, setTheme }}>
        <UserProfile />  {/* theme 변경에 영향 없음 */}
      </ThemeContext.Provider>
    </UserContext.Provider>
  );
}
```

**2. useMemo로 value 메모이제이션**
```jsx
function App() {
  const [user, setUser] = useState({ name: 'Kim' });

  const value = useMemo(() => ({ user, setUser }), [user]);

  return (
    <UserContext.Provider value={value}>
      <UserProfile />
    </UserContext.Provider>
  );
}
```

---

## Redux

### 핵심 개념

**Redux의 3가지 원칙**
1. **Single Source of Truth**: 하나의 Store에 모든 상태 저장
2. **State is Read-Only**: 상태 변경은 오직 Action 디스패치로만
3. **Changes are Made with Pure Functions**: Reducer는 순수 함수

### 4가지 핵심 요소

**1. Store (스토어)**
- 전역 상태를 저장하는 객체
- 앱에 단 하나만 존재

```jsx
const store = createStore(rootReducer);
const state = store.getState();
```

**2. Action (액션)**
- 상태 변경을 설명하는 순수 객체
- 반드시 `type` 필드 포함

```jsx
// Action 객체
const action = {
  type: 'USER_LOGIN',
  payload: { userId: 123, name: 'Kim' }
};

// Action Creator
const login = (user) => ({
  type: 'USER_LOGIN',
  payload: user
});
```

**3. Reducer (리듀서)**
- 이전 상태와 액션을 받아 새 상태를 반환하는 순수 함수
- `(previousState, action) => newState`

```jsx
const userReducer = (state = initialState, action) => {
  switch (action.type) {
    case 'USER_LOGIN':
      return {
        ...state,
        user: action.payload,
        isLoggedIn: true
      };
    case 'USER_LOGOUT':
      return {
        ...state,
        user: null,
        isLoggedIn: false
      };
    default:
      return state;
  }
};
```

**4. Dispatch (디스패치)**
- 액션을 Store에 전달하는 함수
- 상태 변경의 유일한 방법

```jsx
store.dispatch(login({ userId: 123, name: 'Kim' }));

// React 컴포넌트에서
const dispatch = useDispatch();
dispatch(login(userData));
```

### 데이터 흐름 (단방향)

```
┌─────────────────────────────────────────┐
│                                         │
│  1. User Event (버튼 클릭, 입력)        │
│            ↓                            │
│  2. Dispatch(Action)                    │
│            ↓                            │
│  3. Middleware (optional)               │
│            ↓                            │
│  4. Reducer (새로운 상태 계산)          │
│            ↓                            │
│  5. Store (상태 업데이트)               │
│            ↓                            │
│  6. UI Re-render (새로운 상태 반영)     │
│            │                            │
└────────────┘                            │
     (다시 1번으로)
```

**흐름 예시**
```jsx
// 1. 사용자 이벤트
<button onClick={() => dispatch(increment())}>+1</button>

// 2. Action Dispatch
dispatch({ type: 'INCREMENT' })

// 3. Reducer 실행
const counterReducer = (state = 0, action) => {
  switch (action.type) {
    case 'INCREMENT':
      return state + 1;  // 새 상태 반환
    default:
      return state;
  }
};

// 4. Store 업데이트 및 구독자 리렌더링
const count = useSelector(state => state.counter);
```

**단방향이 중요한 이유**
- **예측 가능성**: 상태 변경 경로가 하나뿐
- **디버깅 용이**: Action 로그로 전체 흐름 추적
- **Time-travel Debugging**: Redux DevTools로 과거 상태 재현
- **테스트 용이**: Reducer는 순수 함수라 단위 테스트 쉬움

---

## Redux Toolkit

### 등장 배경

**기존 Redux의 문제점**
- 보일러플레이트 코드 과다 (Action type, Creator, Reducer 분리)
- Store 설정 복잡
- 불변성 관리 어려움 (spread 연산자 남발)
- 초보자 진입장벽 높음

### 주요 개선사항

**1. createSlice - 보일러플레이트 감소**

```jsx
// ❌ 기존 Redux
const INCREMENT = 'counter/increment';
const DECREMENT = 'counter/decrement';

const increment = () => ({ type: INCREMENT });
const decrement = () => ({ type: DECREMENT });

const counterReducer = (state = 0, action) => {
  switch (action.type) {
    case INCREMENT: return state + 1;
    case DECREMENT: return state - 1;
    default: return state;
  }
};

// ✅ Redux Toolkit
const counterSlice = createSlice({
  name: 'counter',
  initialState: 0,
  reducers: {
    increment: (state) => state + 1,
    decrement: (state) => state - 1,
  }
});

export const { increment, decrement } = counterSlice.actions;
```

**2. configureStore - 간편한 설정**

```jsx
// ❌ 기존 Redux
const store = createStore(
  rootReducer,
  applyMiddleware(thunk, logger)
);

// ✅ Redux Toolkit - DevTools, Thunk 자동 포함
const store = configureStore({
  reducer: {
    counter: counterSlice.reducer,
    user: userSlice.reducer,
  }
});
```

**3. Immer 내장 - 불변성 자동 관리**

```jsx
// ❌ 기존 Redux - spread 연산자
case 'UPDATE_USER':
  return {
    ...state,
    user: {
      ...state.user,
      name: action.payload
    }
  };

// ✅ Redux Toolkit - 직접 수정 (Immer가 처리)
reducers: {
  updateUser: (state, action) => {
    state.user.name = action.payload;  // 직관적!
  }
}
```

**4. createAsyncThunk - 비동기 처리 간소화**

```jsx
const fetchUser = createAsyncThunk(
  'user/fetch',
  async (userId) => {
    const response = await api.getUser(userId);
    return response.data;
  }
);

// pending, fulfilled, rejected 자동 생성
extraReducers: (builder) => {
  builder
    .addCase(fetchUser.pending, (state) => {
      state.loading = true;
    })
    .addCase(fetchUser.fulfilled, (state, action) => {
      state.user = action.payload;
      state.loading = false;
    })
    .addCase(fetchUser.rejected, (state, action) => {
      state.error = action.error.message;
      state.loading = false;
    });
}
```

---

## Redux 미들웨어

### 개념
액션이 디스패치되고 리듀서에 도달하기 전에 실행되는 중간 처리 계층

```
Dispatch(Action) → [Middleware] → Reducer → Store
                      ↑
                  비동기, 로깅 등
```

### 필요한 이유
Reducer는 순수 함수여야 하므로 다음을 할 수 없음:
- API 호출 (비동기)
- 랜덤 값 생성
- 로깅, 에러 리포팅

### 1. Redux Thunk

**함수를 디스패치해서 비동기 처리**

```jsx
// Thunk 액션 크리에이터
const fetchUser = (userId) => {
  return async (dispatch, getState) => {
    dispatch({ type: 'USER_FETCH_START' });

    try {
      const response = await api.getUser(userId);
      dispatch({
        type: 'USER_FETCH_SUCCESS',
        payload: response.data
      });
    } catch (error) {
      dispatch({
        type: 'USER_FETCH_ERROR',
        payload: error.message
      });
    }
  };
};

// 사용
dispatch(fetchUser(123));
```

**Redux Toolkit의 createAsyncThunk**
```jsx
const fetchUser = createAsyncThunk(
  'user/fetch',
  async (userId, { rejectWithValue }) => {
    try {
      const response = await api.getUser(userId);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
```

### 2. Redux Saga

**Generator 함수 기반의 고급 미들웨어**

```jsx
import { call, put, takeEvery } from 'redux-saga/effects';

// Saga
function* fetchUserSaga(action) {
  try {
    const user = yield call(api.getUser, action.payload);
    yield put({ type: 'USER_FETCH_SUCCESS', payload: user });
  } catch (error) {
    yield put({ type: 'USER_FETCH_ERROR', payload: error.message });
  }
}

// Watcher
function* watchFetchUser() {
  yield takeEvery('USER_FETCH_REQUEST', fetchUserSaga);
}
```

**고급 기능**
```jsx
// 디바운싱
yield debounce(500, 'SEARCH_QUERY', searchSaga);

// 최신 요청만 처리 (이전 요청 취소)
yield takeLatest('USER_FETCH_REQUEST', fetchUserSaga);

// 병렬 실행
const [users, posts] = yield all([
  call(api.getUsers),
  call(api.getPosts)
]);

// Race condition 처리
const { response, timeout } = yield race({
  response: call(api.login),
  timeout: delay(5000)
});
```

### Thunk vs Saga 비교

| 항목 | Thunk | Saga |
|------|-------|------|
| **학습 곡선** | 낮음 | 높음 (Generator 이해 필요) |
| **보일러플레이트** | 적음 | 많음 |
| **비동기 복잡도** | 단순 | 복잡한 플로우 관리 |
| **테스트** | 다소 어려움 | 쉬움 |
| **고급 기능** | 제한적 | 풍부 (취소, 재시도, 디바운싱) |
| **번들 크기** | 작음 | 큼 |
| **적합한 프로젝트** | 소중규모 | 대규모 엔터프라이즈 |

---

## 서버 상태 관리

### 서버 상태 vs 클라이언트 상태

| 구분 | 서버 상태 | 클라이언트 상태 |
|------|-----------|-----------------|
| **데이터 출처** | 서버 (API) | 클라이언트 (UI) |
| **소유권** | 서버가 원본 소유 | 클라이언트만 소유 |
| **동기화** | 필요 (서버와 동기화) | 불필요 |
| **특성** | 비동기, 캐싱, stale 가능 | 동기, 즉시 변경 |
| **예시** | 사용자 목록, 상품 정보 | 모달 상태, 테마, 탭 인덱스 |

### React Query (TanStack Query)

**핵심 개념**
- 서버 상태를 캐싱하고 자동으로 동기화
- 중복 요청 제거
- 백그라운드 자동 갱신
- Stale-While-Revalidate 전략

**useQuery - 데이터 조회**

```jsx
import { useQuery } from '@tanstack/react-query';

function UserProfile({ userId }) {
  const {
    data,        // 서버 응답 데이터
    isLoading,   // 최초 로딩
    isFetching,  // 백그라운드 갱신
    error,       // 에러 객체
    refetch      // 수동 리페치
  } = useQuery({
    queryKey: ['user', userId],  // 캐시 키 (의존성)
    queryFn: () => api.getUser(userId),
    staleTime: 5 * 60 * 1000,    // 5분간 fresh
    cacheTime: 10 * 60 * 1000,   // 10분간 캐시 보관
    retry: 3,
    refetchOnWindowFocus: true,
  });

  if (isLoading) return <Spinner />;
  if (error) return <Error message={error.message} />;

  return <div>{data.name}</div>;
}
```

**useMutation - 데이터 변경**

```jsx
import { useMutation, useQueryClient } from '@tanstack/react-query';

function UpdateProfile() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (userData) => api.updateUser(userData),

    // 성공 시 캐시 무효화
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user'] });
    },

    onError: (error) => {
      alert(error.message);
    }
  });

  return (
    <button onClick={() => mutation.mutate(formData)}>
      {mutation.isLoading ? '저장 중...' : '저장'}
    </button>
  );
}
```

**캐싱 메커니즘**

```
[fresh] ──(staleTime 경과)──> [stale] ──(cacheTime 경과)──> [삭제]
  ↑                              ↓
  │                         자동 리페치
  └──────────────────────────────┘
```

**Stale-While-Revalidate (SWR)**

```
1. 사용자 페이지 방문
   → 캐시 데이터 즉시 표시 (stale이지만 보여줌)

2. 동시에 백그라운드에서 리페치
   → 로딩 스피너 없음!

3. 새 데이터 도착
   → UI 자동 업데이트
```

**Optimistic Update**

```jsx
const mutation = useMutation({
  mutationFn: updateUser,

  onMutate: async (newUser) => {
    // 진행 중인 리페치 취소
    await queryClient.cancelQueries({ queryKey: ['user'] });

    // 이전 값 저장
    const previousUser = queryClient.getQueryData(['user']);

    // 캐시에 즉시 반영 (서버 응답 전)
    queryClient.setQueryData(['user'], newUser);

    return { previousUser };
  },

  // 실패 시 롤백
  onError: (err, newUser, context) => {
    queryClient.setQueryData(['user'], context.previousUser);
  },

  // 성공/실패 후 리페치
  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ['user'] });
  }
});
```

---

## 경량 상태 관리 라이브러리

### 1. Zustand

**특징: 가장 간단한 전역 상태 관리**

```jsx
import create from 'zustand';

// Store 생성
const useStore = create((set) => ({
  count: 0,
  user: null,

  increment: () => set((state) => ({ count: state.count + 1 })),
  decrement: () => set((state) => ({ count: state.count - 1 })),
  setUser: (user) => set({ user }),
}));

// 사용
function Counter() {
  const count = useStore((state) => state.count);
  const increment = useStore((state) => state.increment);

  return <button onClick={increment}>{count}</button>;
}
```

**장점**
- Provider 불필요
- 보일러플레이트 최소
- 번들 크기 작음 (~1KB)
- 자동 최적화 (선택한 상태만 구독)

### 2. Recoil

**특징: 원자(Atom) 기반 - Facebook 제작**

```jsx
import { atom, selector, useRecoilState, useRecoilValue } from 'recoil';

// Atom: 상태의 단위
const countState = atom({
  key: 'countState',
  default: 0,
});

// Selector: 파생 상태
const doubleCountState = selector({
  key: 'doubleCountState',
  get: ({ get }) => {
    const count = get(countState);
    return count * 2;
  },
});

// 사용
function Counter() {
  const [count, setCount] = useRecoilState(countState);
  const doubleCount = useRecoilValue(doubleCountState);

  return (
    <div>
      <p>{count} / {doubleCount}</p>
      <button onClick={() => setCount(count + 1)}>+1</button>
    </div>
  );
}

// App에 Provider 필요
function App() {
  return (
    <RecoilRoot>
      <Counter />
    </RecoilRoot>
  );
}
```

**장점**
- React와 완벽한 통합
- 비동기 Selector 지원
- 세밀한 리렌더링 최적화
- React 18 Concurrent Mode 지원

### 3. Jotai

**특징: 더 간단한 Recoil**

```jsx
import { atom, useAtom } from 'jotai';

// Atom 생성 (key 불필요!)
const countAtom = atom(0);

// 파생 Atom
const doubleCountAtom = atom((get) => get(countAtom) * 2);

// 사용 (Provider 선택적)
function Counter() {
  const [count, setCount] = useAtom(countAtom);
  const [doubleCount] = useAtom(doubleCountAtom);

  return (
    <div>
      <p>{count} / {doubleCount}</p>
      <button onClick={() => setCount(c => c + 1)}>+1</button>
    </div>
  );
}
```

**장점**
- 극도로 작은 번들 크기 (~3KB)
- Recoil보다 간단한 API
- key 불필요
- TypeScript 타입 추론 자동

### 비교표

| 항목 | Zustand | Recoil | Jotai |
|------|---------|--------|-------|
| **번들 크기** | ~1KB | ~14KB | ~3KB |
| **Provider** | 불필요 | 필요 | 선택적 |
| **보일러플레이트** | 최소 | 중간 | 최소 |
| **학습 곡선** | 매우 낮음 | 중간 | 낮음 |
| **상태 구조** | Flux (중앙) | Atomic (분산) | Atomic (분산) |
| **DevTools** | Redux DevTools | Recoil DevTools | 제한적 |
| **비동기** | 수동 처리 | Selector 지원 | Atom 지원 |

---

## 상태 관리 선택 가이드

### 상태 종류별 도구 선택

| 상태 종류 | 추천 도구 | 이유 |
|-----------|-----------|------|
| **서버 상태** | React Query, SWR | 캐싱, 자동 갱신, 중복 제거 |
| **간단한 전역 상태** | Context API, Zustand | 보일러플레이트 최소 |
| **복잡한 클라이언트 상태** | Redux Toolkit, Zustand | 디버깅, 미들웨어, 구조화 |
| **폼 상태** | react-hook-form, Formik | 성능 최적화, 유효성 검사 |
| **URL 상태** | React Router, Next.js | 북마크, 공유 가능 |

### 프로젝트 규모별 조합

**소규모 (< 10개 컴포넌트)**
```jsx
// useState + props로 충분
function App() {
  const [user, setUser] = useState(null);
  return <Profile user={user} />;
}
```

**중규모 (10~50개 컴포넌트)**
```jsx
// React Query (서버) + Zustand (클라이언트)
const { data: products } = useQuery(['products'], fetchProducts);
const theme = useStore(state => state.theme);
```

**대규모 (50개 이상)**
```jsx
// Redux Toolkit + React Query
const store = configureStore({
  reducer: {
    cart: cartSlice,
    ui: uiSlice
  }
});
```

### 의사결정 플로우차트

```
상태 관리 필요?
 ├─ No → useState
 └─ Yes
     │
     ├─ 서버 데이터? → React Query / SWR
     │
     └─ 클라이언트 상태
         │
         ├─ 간단한 전역 상태? → Context / Zustand
         ├─ 복잡한 로직? → Redux Toolkit
         ├─ 빠른 프로토타이핑? → Zustand
         └─ 대규모 엔터프라이즈? → Redux + React Query
```

### 실무 조합 예시

**스타트업/MVP**
```jsx
// Zustand + React Query
const useAuthStore = create((set) => ({
  user: null,
  login: (user) => set({ user })
}));

const { data: products } = useQuery(['products'], fetchProducts);
```

**중규모 프로젝트**
```jsx
// Redux Toolkit + React Query
const store = configureStore({
  reducer: {
    cart: cartSlice,
    ui: uiSlice
  }
});
```

**대규모 엔터프라이즈**
```jsx
// Redux + Saga + React Query
const store = configureStore({
  reducer: rootReducer,
  middleware: [sagaMiddleware]
});
```

---

## 면접 핵심 정리

### 반드시 알아야 할 개념

**1. Props Drilling과 해결 방법**
- Context API, 전역 상태 관리

**2. Context API의 한계**
- 리렌더링 문제, 성능 이슈

**3. Redux의 핵심**
- Store, Action, Reducer, Dispatch
- 단방향 데이터 흐름
- 불변성 원칙

**4. Redux Toolkit의 이점**
- 보일러플레이트 감소
- Immer 내장
- createAsyncThunk

**5. 서버 상태 vs 클라이언트 상태**
- React Query로 서버 상태 관리
- 경량 라이브러리로 클라이언트 상태 관리

**6. 상태 관리 선택 기준**
- 프로젝트 규모
- 상태 복잡도
- 팀 경험
- 성능 요구사항

### 자주 나오는 꼬리질문

**Q. Redux와 Context API의 차이는?**
- Redux: 복잡한 로직, DevTools, 미들웨어, 대규모
- Context: 간단한 전역 상태, 소규모, 자주 안 바뀌는 데이터

**Q. Redux Toolkit을 왜 쓰나요?**
- 보일러플레이트 감소, Immer 내장, 좋은 기본 설정

**Q. 전역 상태를 남용하면?**
- 불필요한 리렌더링, 재사용성 감소, 테스트 어려움

**Q. React Query가 필요한 이유?**
- 서버 상태의 특성: 캐싱, 동기화, stale 관리

**Q. 어떤 상태 관리 도구를 선택하나요?**
- 서버 상태: React Query
- 간단한 전역: Zustand
- 복잡한 로직: Redux Toolkit

### 실무 팁

**상태 위치 결정**
```
지역 상태 → 상위로 올리기 → Context → 전역 상태
   ↑            ↑               ↑          ↑
useState    Lift up      특정 트리    앱 전체
```

**조합 사용**
- 서버 상태: React Query
- 클라이언트 상태: Zustand / Redux
- 중복 저장 금지

**성능 최적화**
- Context 분리
- Selector 사용 (Redux)
- useMemo로 value 메모이제이션
