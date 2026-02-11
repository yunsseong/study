# React 상태 관리 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Props Drilling 문제가 무엇인지 설명하고, 실무에서 어떤 불편함이 있는지 말씀해주세요.

> **Props Drilling**은 상위 컴포넌트의 상태를 여러 단계의 하위 컴포넌트로 전달하기 위해 중간 컴포넌트들이 실제로 사용하지 않는 props를 단순히 전달만 하는 현상입니다.
>
> **실무에서의 문제점:**
> - **코드 가독성 저하**: 중간 컴포넌트들이 불필요한 props로 복잡해짐
> - **유지보수 어려움**: props 이름이나 타입이 변경되면 모든 중간 단계를 수정해야 함
> - **컴포넌트 재사용성 감소**: 특정 props에 의존하게 되어 독립적으로 사용하기 어려움
> - **리팩토링 부담**: 컴포넌트 구조 변경 시 props 전달 경로를 모두 수정해야 함
>
> ```jsx
> // Props Drilling 예시
> function App() {
>   const [user, setUser] = useState({ name: 'Kim' });
>   return <Parent user={user} />;
> }
>
> // Middle은 user를 사용하지 않지만 전달만 함
> function Parent({ user }) {
>   return <Middle user={user} />;
> }
>
> function Middle({ user }) {
>   return <Child user={user} />;
> }
>
> // 실제로 사용하는 곳
> function Child({ user }) {
>   return <div>{user.name}</div>;
> }
> ```

---

**Q2.** Context API의 사용법과 동작 원리를 설명해주세요. createContext, Provider, useContext의 역할은 무엇인가요?

> **Context API**는 React에서 제공하는 전역 상태 관리 도구로, Props Drilling 없이 컴포넌트 트리 전체에 데이터를 공유할 수 있습니다.
>
> **세 가지 핵심 요소:**
>
> 1. **createContext**: Context 객체를 생성
>    - 기본값을 설정할 수 있음
>    - Provider와 Consumer를 포함
>
> 2. **Provider**: 하위 컴포넌트에 값을 제공
>    - value prop으로 공유할 데이터 전달
>    - Provider 하위의 모든 컴포넌트가 접근 가능
>
> 3. **useContext**: Context 값을 읽어오는 Hook
>    - 가장 가까운 Provider의 value를 반환
>    - Provider가 없으면 기본값 사용
>
> ```jsx
> // 1. Context 생성
> const UserContext = createContext(null);
>
> // 2. Provider로 값 제공
> function App() {
>   const [user, setUser] = useState({ name: 'Kim' });
>
>   return (
>     <UserContext.Provider value={{ user, setUser }}>
>       <Parent />
>     </UserContext.Provider>
>   );
> }
>
> // 3. useContext로 값 사용 (중간 컴포넌트 건너뜀)
> function DeepChild() {
>   const { user, setUser } = useContext(UserContext);
>   return <div>{user.name}</div>;
> }
> ```
>
> **동작 원리:**
> - Provider의 value가 변경되면 해당 Context를 구독하는 모든 컴포넌트가 리렌더링됨
> - Context는 React 내부의 구독 메커니즘을 통해 변경사항을 전파

---

**Q3.** Context API를 사용할 때 발생할 수 있는 리렌더링 문제와 해결 방법을 설명해주세요.

> **리렌더링 문제:**
> Context의 value가 변경되면 해당 Context를 useContext로 구독하는 **모든 하위 컴포넌트가 무조건 리렌더링**됩니다. 특정 값만 사용하는 컴포넌트도 Context의 다른 값이 변경되면 함께 리렌더링됩니다.
>
> **문제 발생 예시:**
> ```jsx
> function App() {
>   const [user, setUser] = useState({ name: 'Kim' });
>   const [theme, setTheme] = useState('dark');
>
>   // 객체가 매번 새로 생성되어 리렌더링 유발
>   return (
>     <AppContext.Provider value={{ user, setUser, theme, setTheme }}>
>       <UserProfile />  {/* theme 변경 시에도 리렌더링됨 */}
>     </AppContext.Provider>
>   );
> }
> ```
>
> **해결 방법:**
>
> **1. Context 분리**
> ```jsx
> const UserContext = createContext();
> const ThemeContext = createContext();
>
> function App() {
>   const [user, setUser] = useState({ name: 'Kim' });
>   const [theme, setTheme] = useState('dark');
>
>   return (
>     <UserContext.Provider value={{ user, setUser }}>
>       <ThemeContext.Provider value={{ theme, setTheme }}>
>         <UserProfile />  {/* theme 변경에 영향 받지 않음 */}
>       </ThemeContext.Provider>
>     </UserContext.Provider>
>   );
> }
> ```
>
> **2. useMemo로 value 메모이제이션**
> ```jsx
> function App() {
>   const [user, setUser] = useState({ name: 'Kim' });
>
>   const value = useMemo(() => ({ user, setUser }), [user]);
>
>   return (
>     <UserContext.Provider value={value}>
>       <UserProfile />
>   );
> }
> ```
>
> **3. Context Selector 패턴 (라이브러리 사용)**
> ```jsx
> // use-context-selector 라이브러리
> const user = useContextSelector(AppContext, state => state.user);
> // user만 변경될 때만 리렌더링
> ```

---

**Q4.** Redux의 핵심 개념인 Store, Action, Reducer, Dispatch에 대해 각각 설명해주세요.

> **Redux의 4가지 핵심 개념:**
>
> **1. Store (스토어)**
> - 애플리케이션의 **전역 상태를 저장하는 객체**
> - 단일 Store를 사용 (Single Source of Truth)
> - `getState()`, `dispatch()`, `subscribe()` 메서드 제공
> ```jsx
> const store = createStore(rootReducer);
> const currentState = store.getState();
> ```
>
> **2. Action (액션)**
> - **상태 변경을 설명하는 순수 JavaScript 객체**
> - 반드시 `type` 필드를 가져야 함
> - 추가 데이터는 `payload`로 전달
> ```jsx
> // Action 객체
> const action = {
>   type: 'USER_LOGIN',
>   payload: { userId: 123, name: 'Kim' }
> };
>
> // Action Creator 함수
> const login = (user) => ({
>   type: 'USER_LOGIN',
>   payload: user
> });
> ```
>
> **3. Reducer (리듀서)**
> - **이전 상태와 액션을 받아 새로운 상태를 반환하는 순수 함수**
> - `(previousState, action) => newState`
> - 불변성을 유지하며 상태를 업데이트
> ```jsx
> const userReducer = (state = initialState, action) => {
>   switch (action.type) {
>     case 'USER_LOGIN':
>       return {
>         ...state,
>         user: action.payload,
>         isLoggedIn: true
>       };
>     case 'USER_LOGOUT':
>       return {
>         ...state,
>         user: null,
>         isLoggedIn: false
>       };
>     default:
>       return state;
>   }
> };
> ```
>
> **4. Dispatch (디스패치)**
> - **액션을 Store에 전달하여 상태 변경을 트리거하는 함수**
> - Store의 유일한 상태 변경 방법
> ```jsx
> // 액션 디스패치
> store.dispatch(login({ userId: 123, name: 'Kim' }));
>
> // React 컴포넌트에서
> const dispatch = useDispatch();
> dispatch(login(userData));
> ```

---

**Q5.** Redux의 데이터 흐름(단방향 데이터 흐름)을 설명하고, 왜 단방향인지 이유를 말씀해주세요.

> **Redux의 단방향 데이터 흐름:**
>
> ```
> ┌─────────────────────────────────────────────────┐
> │                                                 │
> │  1. 사용자 이벤트 (버튼 클릭, 입력 등)          │
> │                   ↓                             │
> │  2. Dispatch(Action) - 액션 발송                │
> │                   ↓                             │
> │  3. Middleware (선택사항)                       │
> │                   ↓                             │
> │  4. Reducer - 새로운 상태 계산                  │
> │                   ↓                             │
> │  5. Store - 상태 업데이트                       │
> │                   ↓                             │
> │  6. UI 리렌더링 - 새로운 상태 반영              │
> │                   │                             │
> └───────────────────┘                             │
>       (다시 1번으로)
> ```
>
> **구체적인 흐름:**
> ```jsx
> // 1. 사용자 이벤트
> <button onClick={() => dispatch(increment())}>+1</button>
>
> // 2. Action Dispatch
> dispatch({ type: 'INCREMENT' })
>
> // 3. Reducer 실행
> const counterReducer = (state = 0, action) => {
>   switch (action.type) {
>     case 'INCREMENT':
>       return state + 1;  // 새로운 상태 반환
>   }
> };
>
> // 4. Store 업데이트
> // 5. 구독 중인 컴포넌트 리렌더링
> const count = useSelector(state => state.counter);
> ```
>
> **왜 단방향인가?**
>
> **1. 예측 가능성 (Predictability)**
> - 상태 변경 경로가 하나뿐이어서 추적이 쉬움
> - 디버깅 시 Action 로그만 보면 전체 흐름 파악 가능
>
> **2. 추적 가능성 (Traceability)**
> - Redux DevTools로 모든 Action과 상태 변화 추적
> - Time-travel debugging 가능 (과거 상태로 되돌리기)
>
> **3. 테스트 용이성**
> - Reducer는 순수 함수라 입출력만 테스트하면 됨
> - 복잡한 mocking 없이 단위 테스트 가능
>
> **4. 유지보수성**
> - 상태 변경 로직이 Reducer에 집중됨
> - 양방향이면 여러 곳에서 상태 변경 가능 → 버그 추적 어려움

---

## 비교/구분 (6~9)

**Q6.** Redux Toolkit이 등장한 이유와 기존 Redux와의 차이점을 설명해주세요.

> **Redux Toolkit 등장 배경:**
>
> **기존 Redux의 문제점:**
> 1. **보일러플레이트 코드 과다**: Action type, Action creator, Reducer를 일일이 작성
> 2. **설정 복잡성**: Store 설정, 미들웨어 연결이 복잡
> 3. **불변성 관리 어려움**: 직접 spread 연산자로 불변성 유지
> 4. **초보자 진입장벽**: 배워야 할 개념이 너무 많음
>
> **Redux Toolkit의 개선사항:**
>
> **1. createSlice - 보일러플레이트 감소**
> ```jsx
> // 기존 Redux
> const INCREMENT = 'counter/increment';
> const DECREMENT = 'counter/decrement';
>
> const increment = () => ({ type: INCREMENT });
> const decrement = () => ({ type: DECREMENT });
>
> const counterReducer = (state = 0, action) => {
>   switch (action.type) {
>     case INCREMENT: return state + 1;
>     case DECREMENT: return state - 1;
>     default: return state;
>   }
> };
>
> // Redux Toolkit - 한 번에 정의
> const counterSlice = createSlice({
>   name: 'counter',
>   initialState: 0,
>   reducers: {
>     increment: (state) => state + 1,  // Immer 덕분에 직접 수정 가능
>     decrement: (state) => state - 1,
>   }
> });
>
> export const { increment, decrement } = counterSlice.actions;
> ```
>
> **2. configureStore - 간편한 설정**
> ```jsx
> // 기존 Redux
> const store = createStore(
>   rootReducer,
>   applyMiddleware(thunk, logger)
> );
>
> // Redux Toolkit - 기본 설정 자동 포함
> const store = configureStore({
>   reducer: {
>     counter: counterSlice.reducer,
>     user: userSlice.reducer,
>   },
>   // Redux Thunk, DevTools 자동 포함
> });
> ```
>
> **3. Immer 내장 - 불변성 자동 관리**
> ```jsx
> // 기존 Redux - spread 연산자 사용
> case 'UPDATE_USER':
>   return {
>     ...state,
>     user: {
>       ...state.user,
>       name: action.payload
>     }
>   };
>
> // Redux Toolkit - 직접 수정 가능 (Immer가 처리)
> reducers: {
>   updateUser: (state, action) => {
>     state.user.name = action.payload;  // 직관적!
>   }
> }
> ```
>
> **4. createAsyncThunk - 비동기 처리 간소화**
> ```jsx
> const fetchUser = createAsyncThunk(
>   'user/fetch',
>   async (userId) => {
>     const response = await api.getUser(userId);
>     return response.data;
>   }
> );
>
> // pending, fulfilled, rejected 자동 생성
> extraReducers: (builder) => {
>   builder
>     .addCase(fetchUser.pending, (state) => {
>       state.loading = true;
>     })
>     .addCase(fetchUser.fulfilled, (state, action) => {
>       state.user = action.payload;
>       state.loading = false;
>     });
> }
> ```
>
> **핵심 차이점 요약:**
> - 코드량: 70% 감소
> - 학습 곡선: 완만함
> - 불변성: 자동 관리
> - 비동기: 표준화된 패턴 제공
> - 기본 설정: 좋은 기본값 포함 (DevTools, Thunk 등)

---

**Q7.** Redux와 Context API를 비교했을 때, 각각 어떤 상황에서 사용하는 것이 적절한가요?

> **Redux vs Context API 비교:**
>
> | 비교 항목 | Context API | Redux (+ Toolkit) |
> |----------|-------------|-------------------|
> | **학습 곡선** | 낮음 (React 기본 API) | 중간 (개념 학습 필요) |
> | **보일러플레이트** | 적음 | 적음 (Toolkit 사용 시) |
> | **상태 구조** | 자유로움 | 명확한 구조 (Slice) |
> | **DevTools** | 없음 | 강력한 디버깅 도구 |
> | **미들웨어** | 없음 | 다양한 미들웨어 지원 |
> | **성능 최적화** | 수동 (분리, useMemo) | 내장 (Selector, Reselect) |
> | **비동기 처리** | 직접 구현 | 표준화된 패턴 (Thunk, Saga) |
> | **타임 트래블** | 불가능 | 가능 (시간 여행 디버깅) |
>
> **Context API 사용 적합 상황:**
>
> ```jsx
> // 1. 간단한 전역 상태 (테마, 언어, 사용자 인증)
> const ThemeContext = createContext();
>
> function App() {
>   const [theme, setTheme] = useState('light');
>   return (
>     <ThemeContext.Provider value={{ theme, setTheme }}>
>       <Layout />
>     </ThemeContext.Provider>
>   );
> }
>
> // 2. 깊은 계층 구조의 Props 전달 회피
> const UserContext = createContext();
>
> // 3. 자주 변경되지 않는 상태
> const ConfigContext = createContext();
> ```
>
> **Redux 사용 적합 상황:**
>
> ```jsx
> // 1. 복잡한 상태 로직
> const cartSlice = createSlice({
>   name: 'cart',
>   initialState: { items: [], total: 0 },
>   reducers: {
>     addItem: (state, action) => {
>       // 복잡한 계산 로직
>     },
>     removeItem: (state, action) => { /* ... */ },
>     applyDiscount: (state, action) => { /* ... */ },
>   }
> });
>
> // 2. 여러 컴포넌트가 같은 상태를 읽고 수정
> function ProductList() {
>   const dispatch = useDispatch();
>   const cart = useSelector(state => state.cart);
>   // 여러 컴포넌트에서 동일한 cart 상태 접근
> }
>
> // 3. 비동기 로직이 많은 경우
> const fetchProducts = createAsyncThunk(
>   'products/fetch',
>   async (category) => {
>     const response = await api.getProducts(category);
>     return response.data;
>   }
> );
>
> // 4. 상태 변경 이력 추적이 필요한 경우
> // Redux DevTools로 모든 Action 추적
>
> // 5. 테스트가 중요한 대규모 프로젝트
> // Reducer는 순수 함수라 테스트 쉬움
> ```
>
> **실무 선택 기준:**
>
> **Context API 선택:**
> - 소규모 프로젝트 (컴포넌트 < 50개)
> - 상태 변경 빈도가 낮음
> - 디버깅 도구 불필요
> - 빠른 프로토타이핑
>
> **Redux 선택:**
> - 중대형 프로젝트
> - 복잡한 상태 로직
> - 많은 비동기 작업
> - 팀 협업 (명확한 규칙 필요)
> - 디버깅 중요 (Time-travel)
>
> **함께 사용:**
> - Redux: 복잡한 비즈니스 로직, 글로벌 상태
> - Context: 테마, 언어, 간단한 UI 상태

---

**Q8.** Redux 미들웨어(Redux Thunk, Redux Saga)가 무엇이고, 왜 필요한지 설명해주세요.

> **Redux 미들웨어란?**
>
> **액션이 디스패치되고 리듀서에 도달하기 전에 실행되는 중간 처리 계층**입니다. 비동기 로직, 로깅, 에러 처리 등 부수 효과(side effects)를 처리할 수 있습니다.
>
> ```
> Dispatch(Action) → [Middleware] → Reducer → Store
>                      ↑
>                  여기서 처리!
> ```
>
> **왜 필요한가?**
>
> Reducer는 **순수 함수**여야 하므로 다음을 할 수 없습니다:
> - API 호출 (비동기 작업)
> - 랜덤 값 생성
> - Date.now() 같은 비결정적 함수
> - 로깅, 에러 리포팅
>
> 이런 작업들을 미들웨어에서 처리합니다.
>
> ---
>
> ### **1. Redux Thunk**
>
> **가장 간단한 비동기 미들웨어 - 액션 대신 함수를 디스패치**
>
> ```jsx
> // 일반 액션 (동기)
> dispatch({ type: 'INCREMENT' });
>
> // Thunk - 함수를 디스패치 (비동기 가능)
> dispatch((dispatch, getState) => {
>   // 비동기 작업 가능
> });
> ```
>
> **사용 예시:**
> ```jsx
> // Thunk 액션 크리에이터
> const fetchUser = (userId) => {
>   return async (dispatch, getState) => {
>     // 로딩 시작
>     dispatch({ type: 'USER_FETCH_START' });
>
>     try {
>       const response = await api.getUser(userId);
>       // 성공
>       dispatch({
>         type: 'USER_FETCH_SUCCESS',
>         payload: response.data
>       });
>     } catch (error) {
>       // 실패
>       dispatch({
>         type: 'USER_FETCH_ERROR',
>         payload: error.message
>       });
>     }
>   };
> };
>
> // 컴포넌트에서 사용
> dispatch(fetchUser(123));
> ```
>
> **Redux Toolkit의 createAsyncThunk:**
> ```jsx
> // 더 간편한 Thunk 생성
> const fetchUser = createAsyncThunk(
>   'user/fetch',
>   async (userId, { rejectWithValue }) => {
>     try {
>       const response = await api.getUser(userId);
>       return response.data;
>     } catch (error) {
>       return rejectWithValue(error.message);
>     }
>   }
> );
>
> // pending, fulfilled, rejected 액션 자동 생성
> extraReducers: (builder) => {
>   builder
>     .addCase(fetchUser.pending, (state) => {
>       state.loading = true;
>     })
>     .addCase(fetchUser.fulfilled, (state, action) => {
>       state.user = action.payload;
>       state.loading = false;
>     })
>     .addCase(fetchUser.rejected, (state, action) => {
>       state.error = action.payload;
>       state.loading = false;
>     });
> }
> ```
>
> **장점:**
> - 간단하고 직관적
> - 학습 곡선 낮음
> - Promise 기반이라 익숙함
>
> **단점:**
> - 복잡한 비동기 흐름 관리 어려움
> - 테스트가 다소 까다로움
>
> ---
>
> ### **2. Redux Saga**
>
> **Generator 함수 기반의 고급 미들웨어 - 복잡한 비동기 플로우 관리**
>
> ```jsx
> import { call, put, takeEvery } from 'redux-saga/effects';
>
> // Saga - Generator 함수
> function* fetchUserSaga(action) {
>   try {
>     // call: 비동기 함수 호출
>     const user = yield call(api.getUser, action.payload);
>
>     // put: 액션 디스패치
>     yield put({
>       type: 'USER_FETCH_SUCCESS',
>       payload: user
>     });
>   } catch (error) {
>     yield put({
>       type: 'USER_FETCH_ERROR',
>       payload: error.message
>     });
>   }
> }
>
> // Watcher Saga
> function* watchFetchUser() {
>   // USER_FETCH_REQUEST 액션을 감시
>   yield takeEvery('USER_FETCH_REQUEST', fetchUserSaga);
> }
> ```
>
> **고급 기능:**
> ```jsx
> // 1. 디바운싱 (연속 요청 방지)
> function* watchSearchQuery() {
>   yield debounce(500, 'SEARCH_QUERY', searchSaga);
> }
>
> // 2. 최신 요청만 처리 (이전 요청 취소)
> function* watchFetchUser() {
>   yield takeLatest('USER_FETCH_REQUEST', fetchUserSaga);
> }
>
> // 3. 동시 실행 제한
> function* watchUpload() {
>   yield takeLeading('UPLOAD_FILE', uploadSaga);
> }
>
> // 4. 병렬 실행
> function* fetchAllData() {
>   const [users, posts] = yield all([
>     call(api.getUsers),
>     call(api.getPosts)
>   ]);
> }
>
> // 5. Race condition 처리
> function* loginSaga() {
>   const { response, timeout } = yield race({
>     response: call(api.login),
>     timeout: delay(5000)
>   });
>
>   if (timeout) {
>     yield put({ type: 'LOGIN_TIMEOUT' });
>   }
> }
> ```
>
> **장점:**
> - 복잡한 비동기 플로우 관리 용이
> - 테스트 쉬움 (Generator 특성)
> - 취소, 재시도, 디바운싱 등 고급 기능
> - 선언적 코드
>
> **단점:**
> - 학습 곡선 높음 (Generator 이해 필요)
> - 보일러플레이트 많음
> - 번들 크기 증가
>
> ---
>
> ### **Thunk vs Saga 선택 기준:**
>
> | 상황 | 추천 |
> |------|------|
> | 간단한 API 호출 | Thunk |
> | Promise 체이닝 | Thunk |
> | 복잡한 비동기 플로우 | Saga |
> | 요청 취소/재시도 | Saga |
> | 디바운싱/쓰로틀링 | Saga |
> | WebSocket, 폴링 | Saga |
> | 소규모 프로젝트 | Thunk |
> | 대규모 엔터프라이즈 | Saga |

---

**Q9.** 서버 상태(Server State)와 클라이언트 상태(Client State)의 차이점을 설명하고, 각각 어떻게 관리해야 하는지 말씀해주세요.

> **서버 상태 vs 클라이언트 상태:**
>
> ### **클라이언트 상태 (Client State)**
>
> **애플리케이션 UI에서만 사용되는 상태**
>
> ```jsx
> // 예시
> const [isModalOpen, setIsModalOpen] = useState(false);  // 모달 열림/닫힘
> const [theme, setTheme] = useState('dark');             // 테마
> const [selectedTab, setSelectedTab] = useState(0);      // 탭 인덱스
> const [formData, setFormData] = useState({});           // 입력 폼 데이터
> ```
>
> **특징:**
> - 클라이언트에서만 존재
> - 새로고침하면 사라짐 (localStorage 제외)
> - 동기적으로 즉시 변경 가능
> - 캐싱 불필요
>
> ---
>
> ### **서버 상태 (Server State)**
>
> **서버에서 가져온 데이터로, 클라이언트에서는 '캐시'일 뿐**
>
> ```jsx
> // 예시
> const [users, setUsers] = useState([]);           // 사용자 목록
> const [products, setProducts] = useState([]);     // 상품 목록
> const [userProfile, setUserProfile] = useState(null); // 프로필 정보
> ```
>
> **특징:**
> - 서버가 원본 소유
> - 비동기적으로 가져옴
> - **언제든 stale(오래된) 상태가 될 수 있음**
> - 여러 컴포넌트에서 같은 데이터 필요
> - 캐싱, 동기화, 업데이트가 복잡함
>
> **서버 상태의 문제점:**
> ```jsx
> // 문제 1: 중복 요청
> function UserList() {
>   useEffect(() => {
>     fetchUsers();  // API 호출
>   }, []);
> }
>
> function UserCount() {
>   useEffect(() => {
>     fetchUsers();  // 같은 데이터를 또 호출!
>   }, []);
> }
>
> // 문제 2: 오래된 데이터
> // 5분 전에 가져온 데이터를 계속 보여줌
> // 다른 사용자가 수정했을 수도 있는데...
>
> // 문제 3: 로딩/에러 상태 관리
> const [users, setUsers] = useState([]);
> const [loading, setLoading] = useState(false);
> const [error, setError] = useState(null);
> // 매번 이렇게 3개를 관리해야 함
> ```
>
> ---
>
> ### **관리 방법:**
>
> **클라이언트 상태 관리:**
>
> ```jsx
> // 1. useState (로컬 상태)
> function Modal() {
>   const [isOpen, setIsOpen] = useState(false);
>   return <Dialog open={isOpen} />;
> }
>
> // 2. Context API (전역 UI 상태)
> const ThemeContext = createContext();
>
> // 3. Zustand (간단한 전역 상태)
> const useUIStore = create((set) => ({
>   theme: 'light',
>   setTheme: (theme) => set({ theme })
> }));
>
> // 4. Redux (복잡한 UI 로직)
> const uiSlice = createSlice({
>   name: 'ui',
>   initialState: { sidebarOpen: false },
>   reducers: {
>     toggleSidebar: (state) => {
>       state.sidebarOpen = !state.sidebarOpen;
>     }
>   }
> });
> ```
>
> **서버 상태 관리:**
>
> ```jsx
> // ❌ 잘못된 방법 - useState로 관리
> function UserList() {
>   const [users, setUsers] = useState([]);
>   const [loading, setLoading] = useState(false);
>
>   useEffect(() => {
>     setLoading(true);
>     api.getUsers().then(data => {
>       setUsers(data);
>       setLoading(false);
>     });
>   }, []);
> }
>
> // ✅ 올바른 방법 - React Query/TanStack Query
> function UserList() {
>   const { data: users, isLoading, error } = useQuery({
>     queryKey: ['users'],
>     queryFn: api.getUsers,
>     staleTime: 5 * 60 * 1000,  // 5분간 신선
>     cacheTime: 10 * 60 * 1000,  // 10분간 캐시
>   });
>
>   if (isLoading) return <Spinner />;
>   if (error) return <Error />;
>   return <List data={users} />;
> }
>
> // ✅ SWR (대안)
> function UserList() {
>   const { data, error, isLoading } = useSWR('/api/users', fetcher);
> }
>
> // ✅ Redux + RTK Query
> const { data: users, isLoading } = useGetUsersQuery();
> ```
>
> **서버 상태 라이브러리가 해주는 것:**
> - **자동 캐싱**: 같은 데이터 중복 요청 방지
> - **자동 리페칭**: 창 포커스 시, 일정 시간 후 자동 갱신
> - **Optimistic Updates**: 서버 응답 전에 UI 먼저 업데이트
> - **Pagination, Infinite Scroll**: 내장 지원
> - **로딩/에러 상태**: 자동 관리
> - **Devtools**: 캐시 상태 시각화
>
> ---
>
> ### **실무 패턴:**
>
> ```jsx
> // 클라이언트 상태: Zustand
> const useUIStore = create((set) => ({
>   theme: 'light',
>   sidebarOpen: false,
>   toggleSidebar: () => set(state => ({
>     sidebarOpen: !state.sidebarOpen
>   }))
> }));
>
> // 서버 상태: React Query
> function App() {
>   // UI 상태
>   const theme = useUIStore(state => state.theme);
>
>   // 서버 상태
>   const { data: user } = useQuery({
>     queryKey: ['user'],
>     queryFn: api.getCurrentUser
>   });
>
>   return (
>     <ThemeProvider theme={theme}>
>       <UserProfile user={user} />
>     </ThemeProvider>
>   );
> }
> ```
>
> **핵심 원칙:**
> - **서버 상태는 서버 상태 라이브러리로** (React Query, SWR)
> - **클라이언트 상태는 가벼운 도구로** (useState, Zustand, Context)
> - **Redux는 복잡한 클라이언트 로직이 있을 때만**

---

## 심화/실무 (10~12)

**Q10.** React Query(TanStack Query)의 핵심 개념과 주요 기능(useQuery, useMutation, 캐싱, stale-while-revalidate)을 설명해주세요.

> **React Query (TanStack Query)는 서버 상태 관리 라이브러리**로, API 데이터 페칭, 캐싱, 동기화를 자동화합니다.
>
> ---
>
> ### **1. useQuery - 데이터 조회**
>
> ```jsx
> import { useQuery } from '@tanstack/react-query';
>
> function UserProfile({ userId }) {
>   const {
>     data,        // 서버 응답 데이터
>     isLoading,   // 최초 로딩 중
>     isFetching,  // 백그라운드 갱신 중
>     error,       // 에러 객체
>     refetch      // 수동 리페치 함수
>   } = useQuery({
>     queryKey: ['user', userId],  // 캐시 키
>     queryFn: () => api.getUser(userId),  // 데이터 가져오는 함수
>     staleTime: 5 * 60 * 1000,    // 5분간 신선
>     cacheTime: 10 * 60 * 1000,   // 10분간 캐시 보관
>     retry: 3,                     // 실패 시 3번 재시도
>     refetchOnWindowFocus: true,   // 창 포커스 시 자동 갱신
>   });
>
>   if (isLoading) return <Spinner />;
>   if (error) return <Error message={error.message} />;
>
>   return <div>{data.name}</div>;
> }
> ```
>
> **queryKey의 역할:**
> - 캐시 식별자 (배열 형태)
> - 의존성 배열 역할 (값이 바뀌면 자동 리페치)
> ```jsx
> // userId가 바뀌면 자동으로 새로운 데이터 페칭
> useQuery({
>   queryKey: ['user', userId],
>   queryFn: () => api.getUser(userId)
> });
> ```
>
> ---
>
> ### **2. useMutation - 데이터 변경**
>
> ```jsx
> import { useMutation, useQueryClient } from '@tanstack/react-query';
>
> function UpdateProfile() {
>   const queryClient = useQueryClient();
>
>   const mutation = useMutation({
>     mutationFn: (userData) => api.updateUser(userData),
>
>     // 성공 시 캐시 무효화 → 자동 리페치
>     onSuccess: () => {
>       queryClient.invalidateQueries({ queryKey: ['user'] });
>     },
>
>     // 에러 처리
>     onError: (error) => {
>       alert(error.message);
>     }
>   });
>
>   const handleSubmit = (formData) => {
>     mutation.mutate(formData);
>   };
>
>   return (
>     <form onSubmit={handleSubmit}>
>       {mutation.isLoading && <Spinner />}
>       {mutation.isError && <Error />}
>       {mutation.isSuccess && <Success />}
>       <button type="submit">저장</button>
>     </form>
>   );
> }
> ```
>
> **Optimistic Update (낙관적 업데이트):**
> ```jsx
> const mutation = useMutation({
>   mutationFn: updateUser,
>
>   onMutate: async (newUser) => {
>     // 진행 중인 리페치 취소
>     await queryClient.cancelQueries({ queryKey: ['user'] });
>
>     // 이전 값 저장 (롤백용)
>     const previousUser = queryClient.getQueryData(['user']);
>
>     // 캐시에 즉시 반영 (서버 응답 전)
>     queryClient.setQueryData(['user'], newUser);
>
>     return { previousUser };  // context로 전달
>   },
>
>   // 실패 시 롤백
>   onError: (err, newUser, context) => {
>     queryClient.setQueryData(['user'], context.previousUser);
>   },
>
>   // 성공/실패 관계없이 리페치
>   onSettled: () => {
>     queryClient.invalidateQueries({ queryKey: ['user'] });
>   }
> });
> ```
>
> ---
>
> ### **3. 캐싱 (Caching)**
>
> **React Query는 자동으로 캐시를 관리합니다:**
>
> ```
> [fresh] ──(staleTime 경과)──> [stale] ──(cacheTime 경과)──> [삭제]
>   ↑                              ↓
>   │                         자동 리페치
>   └──────────────────────────────┘
> ```
>
> ```jsx
> useQuery({
>   queryKey: ['todos'],
>   queryFn: fetchTodos,
>
>   staleTime: 5 * 60 * 1000,   // 5분간 fresh
>   // fresh 상태일 때는 리페치 안 함 (캐시 사용)
>
>   cacheTime: 10 * 60 * 1000,  // 10분간 메모리 보관
>   // 컴포넌트 언마운트 후에도 10분간 캐시 유지
> });
> ```
>
> **캐시 동작 예시:**
> ```jsx
> // 컴포넌트 A
> function ComponentA() {
>   const { data } = useQuery({
>     queryKey: ['todos'],
>     queryFn: fetchTodos
>   });
>   // API 호출 (캐시 없음)
> }
>
> // 컴포넌트 B (동시에 렌더링)
> function ComponentB() {
>   const { data } = useQuery({
>     queryKey: ['todos'],
>     queryFn: fetchTodos
>   });
>   // API 호출 안 함! (A의 캐시 사용)
> }
> ```
>
> ---
>
> ### **4. Stale-While-Revalidate (SWR)**
>
> **"오래된 데이터를 먼저 보여주고, 백그라운드에서 갱신"**
>
> ```jsx
> useQuery({
>   queryKey: ['user'],
>   queryFn: fetchUser,
>   staleTime: 0,  // 즉시 stale 상태
>   refetchOnWindowFocus: true
> });
> ```
>
> **동작 과정:**
> ```
> 1. 사용자가 페이지 방문
>    → 캐시 데이터 즉시 표시 (stale이지만 보여줌)
>
> 2. 동시에 백그라운드에서 리페치 시작
>    → 사용자는 로딩 스피너를 안 봄!
>
> 3. 새 데이터 도착
>    → UI 자동 업데이트
> ```
>
> **실제 사용 예:**
> ```jsx
> function Dashboard() {
>   const { data, isFetching } = useQuery({
>     queryKey: ['dashboard'],
>     queryFn: fetchDashboardData,
>     staleTime: 30 * 1000,  // 30초간 신선
>     refetchInterval: 60 * 1000,  // 1분마다 자동 갱신
>   });
>
>   return (
>     <div>
>       {isFetching && <RefreshIndicator />}  {/* 작은 아이콘만 */}
>       <DashboardContent data={data} />      {/* 데이터는 바로 표시 */}
>     </div>
>   );
> }
> ```
>
> ---
>
> ### **5. 기타 주요 기능**
>
> **Pagination:**
> ```jsx
> function PostList() {
>   const [page, setPage] = useState(1);
>
>   const { data } = useQuery({
>     queryKey: ['posts', page],
>     queryFn: () => fetchPosts(page),
>     keepPreviousData: true,  // 페이지 전환 시 이전 데이터 유지
>   });
> }
> ```
>
> **Infinite Scroll:**
> ```jsx
> const {
>   data,
>   fetchNextPage,
>   hasNextPage,
> } = useInfiniteQuery({
>   queryKey: ['posts'],
>   queryFn: ({ pageParam = 1 }) => fetchPosts(pageParam),
>   getNextPageParam: (lastPage) => lastPage.nextCursor,
> });
> ```
>
> **Dependent Queries (순차 쿼리):**
> ```jsx
> // 사용자 정보를 먼저 가져온 후, 그 정보로 프로젝트 조회
> const { data: user } = useQuery({
>   queryKey: ['user'],
>   queryFn: fetchUser
> });
>
> const { data: projects } = useQuery({
>   queryKey: ['projects', user?.id],
>   queryFn: () => fetchProjects(user.id),
>   enabled: !!user,  // user가 있을 때만 실행
> });
> ```
>
> **Prefetching (미리 가져오기):**
> ```jsx
> const queryClient = useQueryClient();
>
> const handleMouseEnter = () => {
>   // 마우스 hover 시 미리 데이터 가져오기
>   queryClient.prefetchQuery({
>     queryKey: ['post', postId],
>     queryFn: () => fetchPost(postId)
>   });
> };
> ```
>
> ---
>
> ### **핵심 정리:**
>
> | 기능 | 설명 | 장점 |
> |------|------|------|
> | **useQuery** | 데이터 조회 | 자동 캐싱, 리페칭 |
> | **useMutation** | 데이터 변경 | Optimistic Update 지원 |
> | **캐싱** | 자동 메모리 관리 | 중복 요청 방지 |
> | **SWR** | Stale-While-Revalidate | 빠른 UX, 최신 데이터 |
> | **자동 리페칭** | 창 포커스, 재연결 시 | 항상 최신 상태 유지 |
>
> **React Query를 쓰면:**
> - 로딩/에러 상태 자동 관리
> - 캐시 중복 제거
> - 백그라운드 자동 갱신
> - Optimistic UI 쉬움
> - 서버 상태와 UI 분리

---

**Q11.** Zustand, Recoil, Jotai 같은 경량 상태 관리 라이브러리들의 특징과 각각의 장단점을 비교해주세요.

> **경량 상태 관리 라이브러리 비교:**
>
> ---
>
> ### **1. Zustand**
>
> **"가장 간단한 전역 상태 관리"**
>
> ```jsx
> import create from 'zustand';
>
> // Store 생성 (Redux보다 간단)
> const useStore = create((set) => ({
>   count: 0,
>   user: null,
>
>   increment: () => set((state) => ({ count: state.count + 1 })),
>   decrement: () => set((state) => ({ count: state.count - 1 })),
>   setUser: (user) => set({ user }),
> }));
>
> // 컴포넌트에서 사용
> function Counter() {
>   const count = useStore((state) => state.count);
>   const increment = useStore((state) => state.increment);
>
>   return <button onClick={increment}>{count}</button>;
> }
> ```
>
> **특징:**
> - **보일러플레이트 최소**: Provider 불필요, 간단한 API
> - **자동 최적화**: 선택한 상태만 구독 (불필요한 리렌더링 없음)
> - **미들웨어 지원**: Redux DevTools, persist, immer 등
> - **Flux 패턴**: Redux와 유사하지만 훨씬 간단
>
> **장점:**
> - 학습 곡선이 거의 없음
> - 번들 크기 작음 (~1KB)
> - TypeScript 지원 우수
> - 비동기 처리 간단 (추가 라이브러리 불필요)
>
> **단점:**
> - 디버깅 도구 제한적 (Redux DevTools는 쓸 수 있음)
> - 복잡한 상태 로직엔 Redux만 못함
>
> **적합한 상황:**
> - 중소규모 프로젝트
> - Redux는 과하고 Context는 부족할 때
> - 빠른 개발이 필요할 때
>
> ---
>
> ### **2. Recoil**
>
> **"원자(Atom) 기반 상태 관리 - Facebook 제작"**
>
> ```jsx
> import { atom, selector, useRecoilState, useRecoilValue } from 'recoil';
>
> // Atom: 상태의 단위
> const countState = atom({
>   key: 'countState',  // 고유 키
>   default: 0,
> });
>
> // Selector: 파생 상태 (Computed)
> const doubleCountState = selector({
>   key: 'doubleCountState',
>   get: ({ get }) => {
>     const count = get(countState);
>     return count * 2;
>   },
> });
>
> // 컴포넌트에서 사용
> function Counter() {
>   const [count, setCount] = useRecoilState(countState);
>   const doubleCount = useRecoilValue(doubleCountState);
>
>   return (
>     <div>
>       <p>Count: {count}</p>
>       <p>Double: {doubleCount}</p>
>       <button onClick={() => setCount(count + 1)}>+1</button>
>     </div>
>   );
> }
>
> // App에 Provider 필요
> function App() {
>   return (
>     <RecoilRoot>
>       <Counter />
>     </RecoilRoot>
>   );
> }
> ```
>
> **특징:**
> - **Atom**: 독립적인 상태 조각 (여러 개 생성 가능)
> - **Selector**: 파생 상태, 비동기 데이터 지원
> - **동시성 모드 호환**: React 18의 Concurrent Mode 지원
> - **그래프 기반**: Atom 간 의존성 자동 관리
>
> **장점:**
> - React와 완벽한 통합 (React 팀 제작)
> - 비동기 selector로 데이터 페칭 가능
> - 세밀한 리렌더링 최적화
> - 타임 트래블 디버깅
>
> **단점:**
> - 아직 실험적 상태 (API 변경 가능)
> - Provider 필요
> - 번들 크기 상대적으로 큼
> - 학습 곡선 있음 (Atom, Selector 개념)
>
> **적합한 상황:**
> - Facebook/Meta 생태계 사용 시
> - 복잡한 파생 상태가 많을 때
> - React 18+ 프로젝트
>
> ---
>
> ### **3. Jotai**
>
> **"더 간단한 Recoil - Primitive + Atomic"**
>
> ```jsx
> import { atom, useAtom } from 'jotai';
>
> // Atom 생성 (key 불필요!)
> const countAtom = atom(0);
> const userAtom = atom({ name: 'Kim' });
>
> // 파생 Atom
> const doubleCountAtom = atom((get) => get(countAtom) * 2);
>
> // 컴포넌트에서 사용
> function Counter() {
>   const [count, setCount] = useAtom(countAtom);
>   const [doubleCount] = useAtom(doubleCountAtom);
>
>   return (
>     <div>
>       <p>{count} / {doubleCount}</p>
>       <button onClick={() => setCount(c => c + 1)}>+1</button>
>     </div>
>   );
> }
>
> // Provider 선택적
> function App() {
>   return <Counter />;  // Provider 없어도 됨!
> }
> ```
>
> **특징:**
> - **Recoil보다 간단**: key 불필요, API 최소화
> - **Bottom-up 접근**: 필요한 Atom만 정의
> - **TypeScript 퍼스트**: 타입 추론 자동
> - **Provider 선택적**: 없어도 작동
>
> **장점:**
> - 극도로 작은 번들 크기 (~3KB)
> - Recoil보다 간단한 API
> - 보일러플레이트 거의 없음
> - 비동기 Atom 지원
>
> **단점:**
> - 생태계가 작음 (커뮤니티, 라이브러리)
> - 디버깅 도구 제한적
> - 복잡한 앱에선 검증 부족
>
> **적합한 상황:**
> - 소규모 프로젝트
> - Atomic한 상태 관리 선호
> - 최소한의 보일러플레이트 원할 때
>
> ---
>
> ### **비교표:**
>
> | 항목 | Zustand | Recoil | Jotai |
> |------|---------|--------|-------|
> | **번들 크기** | ~1KB | ~14KB | ~3KB |
> | **Provider** | 불필요 | 필요 | 선택적 |
> | **보일러플레이트** | 최소 | 중간 | 최소 |
> | **학습 곡선** | 매우 낮음 | 중간 | 낮음 |
> | **TypeScript** | 우수 | 우수 | 최고 |
> | **DevTools** | Redux DevTools | Recoil DevTools | 제한적 |
> | **비동기** | 수동 처리 | Selector 지원 | Atom 지원 |
> | **상태 구조** | Flux (중앙) | Atomic (분산) | Atomic (분산) |
> | **React 통합** | 보통 | 최고 | 우수 |
> | **생태계** | 성장 중 | Meta 지원 | 작음 |
>
> ---
>
> ### **선택 가이드:**
>
> **Zustand 선택:**
> ```jsx
> // Redux는 과하고, 전역 상태가 필요할 때
> const useAuthStore = create((set) => ({
>   user: null,
>   login: (user) => set({ user }),
>   logout: () => set({ user: null })
> }));
> ```
> - 빠른 개발 속도 필요
> - Redux 경험 있음 (유사한 패턴)
> - 미들웨어 필요 (persist, devtools)
>
> **Recoil 선택:**
> ```jsx
> // 복잡한 파생 상태, 비동기 selector
> const currentUserQuery = selector({
>   key: 'currentUser',
>   get: async () => {
>     const response = await api.getCurrentUser();
>     return response.data;
>   }
> });
> ```
> - Meta/Facebook 생태계 사용
> - 복잡한 상태 의존성
> - React 18 Concurrent Mode 사용
>
> **Jotai 선택:**
> ```jsx
> // 간단한 전역 상태, 보일러플레이트 최소화
> const themeAtom = atom('light');
> const userAtom = atom(null);
> ```
> - 최소한의 코드
> - 작은 번들 크기 중요
> - Atomic 패턴 선호
>
> ---
>
> ### **실무 조합 예:**
>
> ```jsx
> // UI 상태: Zustand (간단, 동기적)
> const useUIStore = create((set) => ({
>   theme: 'light',
>   sidebarOpen: false,
>   toggleSidebar: () => set(state => ({
>     sidebarOpen: !state.sidebarOpen
>   }))
> }));
>
> // 서버 상태: React Query (비동기, 캐싱)
> const { data: user } = useQuery({
>   queryKey: ['user'],
>   queryFn: fetchUser
> });
>
> // 복잡한 클라이언트 로직: Redux Toolkit (필요 시)
> const cartSlice = createSlice({
>   name: 'cart',
>   initialState: [],
>   reducers: { /* ... */ }
> });
> ```

---

**Q12.** 프로젝트에서 상태 관리 라이브러리를 선택할 때 고려해야 할 기준은 무엇인가요?

> **상태 관리 라이브러리 선택 기준:**
>
> ---
>
> ### **1. 상태의 종류와 복잡도**
>
> **먼저 상태를 분류하세요:**
>
> ```jsx
> // 서버 상태 (Server State)
> const { data: users } = useQuery(['users'], fetchUsers);
> // → React Query / SWR
>
> // 클라이언트 UI 상태 (Client State)
> const [isOpen, setIsOpen] = useState(false);
> // → useState / Context
>
> // 전역 클라이언트 상태
> const theme = useStore(state => state.theme);
> // → Zustand / Redux
>
> // 복잡한 비즈니스 로직
> const cart = useSelector(state => state.cart);
> // → Redux Toolkit
> ```
>
> **상태 복잡도별 추천:**
>
> | 복잡도 | 상태 예시 | 추천 도구 |
> |--------|-----------|-----------|
> | **매우 간단** | 모달 열림/닫힘, 테마 | useState |
> | **간단** | 인증 상태, 언어 설정 | Context API, Zustand |
> | **중간** | 사용자 프로필, 장바구니 | Zustand, Redux Toolkit |
> | **복잡** | 대시보드, 워크플로우 관리 | Redux Toolkit + Saga |
> | **서버 데이터** | API 조회, 캐싱 | React Query, SWR |
>
> ---
>
> ### **2. 프로젝트 규모**
>
> **소규모 (< 10개 컴포넌트)**
> ```jsx
> // useState + props만으로도 충분
> function App() {
>   const [user, setUser] = useState(null);
>   return <Profile user={user} onUpdate={setUser} />;
> }
> ```
>
> **중규모 (10~50개 컴포넌트)**
> ```jsx
> // Context API 또는 Zustand
> const useAuthStore = create((set) => ({
>   user: null,
>   login: (user) => set({ user }),
> }));
> ```
>
> **대규모 (50개 이상, 엔터프라이즈)**
> ```jsx
> // Redux Toolkit + React Query
> const store = configureStore({
>   reducer: {
>     auth: authSlice,
>     cart: cartSlice,
>     ui: uiSlice,
>   }
> });
> ```
>
> ---
>
> ### **3. 팀 경험과 학습 곡선**
>
> **팀 경험 고려:**
>
> ```jsx
> // 팀이 Redux 경험 많음 → Redux Toolkit
> const cartSlice = createSlice({
>   name: 'cart',
>   initialState: [],
>   reducers: { addItem, removeItem }
> });
>
> // 팀이 React만 알고 있음 → Zustand
> const useStore = create((set) => ({
>   cart: [],
>   addItem: (item) => set((state) => ({
>     cart: [...state.cart, item]
>   }))
> }));
>
> // 빠른 프로토타이핑 → useState + Context
> const CartContext = createContext();
> ```
>
> **학습 곡선:**
> ```
> useState < Context < Zustand < Redux Toolkit < Redux + Saga
> ```
>
> ---
>
> ### **4. 성능 요구사항**
>
> **대량 데이터, 빈번한 업데이트:**
>
> ```jsx
> // ❌ Context - 전체 리렌더링
> const AppContext = createContext();
> // 하나의 값만 바뀌어도 모든 구독자 리렌더링
>
> // ✅ Zustand - 선택적 구독
> const count = useStore(state => state.count);
> // count만 변경될 때만 리렌더링
>
> // ✅ Redux - Selector 최적화
> const user = useSelector(state => state.user, shallowEqual);
> ```
>
> **번들 크기 민감 (모바일):**
> ```
> Jotai (3KB) < Zustand (1KB) < Recoil (14KB) < Redux Toolkit (20KB)
> ```
>
> ---
>
> ### **5. 개발자 경험 (DX)**
>
> **디버깅 도구 필요 여부:**
>
> ```jsx
> // Redux DevTools - 타임 트래블 디버깅
> import { configureStore } from '@reduxjs/toolkit';
> // 모든 Action, State 변화 추적
>
> // React Query DevTools - 캐시 상태 시각화
> import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
> <ReactQueryDevtools initialIsOpen={false} />
> ```
>
> **TypeScript 지원:**
> ```tsx
> // Zustand - 타입 안전성 우수
> interface StoreState {
>   count: number;
>   increment: () => void;
> }
> const useStore = create<StoreState>((set) => ({ /* ... */ }));
>
> // Jotai - 자동 타입 추론
> const countAtom = atom(0);  // number로 추론됨
> ```
>
> ---
>
> ### **6. 비동기 처리 요구사항**
>
> **비동기 로직이 많은 경우:**
>
> ```jsx
> // React Query - 서버 상태 전용
> const { data, isLoading } = useQuery({
>   queryKey: ['users'],
>   queryFn: fetchUsers
> });
>
> // Redux Toolkit + Thunk - 클라이언트 비동기 로직
> const fetchUser = createAsyncThunk(
>   'user/fetch',
>   async (userId) => await api.getUser(userId)
> );
>
> // Redux Saga - 복잡한 비동기 플로우
> function* watchFetchUser() {
>   yield takeLatest('USER_FETCH', fetchUserSaga);
> }
> ```
>
> ---
>
> ### **7. 실무 의사결정 플로우차트**
>
> ```
> 상태 관리 필요?
>    ├─ No → useState만 사용
>    └─ Yes
>        │
>        ├─ 서버 데이터인가?
>        │   └─ Yes → React Query / SWR
>        │
>        └─ 클라이언트 상태인가?
>            │
>            ├─ 간단한 전역 상태? (테마, 인증)
>            │   └─ Yes → Context API / Zustand
>            │
>            ├─ 복잡한 비즈니스 로직?
>            │   └─ Yes → Redux Toolkit
>            │
>            ├─ 빠른 프로토타이핑?
>            │   └─ Yes → Zustand
>            │
>            └─ 대규모 엔터프라이즈?
>                └─ Yes → Redux Toolkit + React Query
> ```
>
> ---
>
> ### **8. 실무 조합 추천**
>
> **스타트업 / MVP:**
> ```jsx
> // Zustand (클라이언트) + React Query (서버)
> const useAuthStore = create((set) => ({
>   user: null,
>   login: (user) => set({ user })
> }));
>
> const { data: products } = useQuery(['products'], fetchProducts);
> ```
> - 빠른 개발
> - 작은 번들 크기
> - 학습 곡선 낮음
>
> **중규모 프로젝트:**
> ```jsx
> // Redux Toolkit (복잡한 로직) + React Query (서버)
> const store = configureStore({
>   reducer: {
>     cart: cartSlice,
>     ui: uiSlice
>   }
> });
> ```
> - 명확한 구조
> - 디버깅 용이
> - 확장 가능
>
> **대규모 엔터프라이즈:**
> ```jsx
> // Redux Toolkit + Saga + React Query
> const store = configureStore({
>   reducer: rootReducer,
>   middleware: [sagaMiddleware]
> });
> ```
> - 복잡한 비동기 플로우 관리
> - 엄격한 패턴 적용
> - 팀 협업 용이
>
> ---
>
> ### **체크리스트:**
>
> ✅ **상태 종류 분류 완료** (서버 vs 클라이언트)
> ✅ **프로젝트 규모 파악** (소/중/대)
> ✅ **팀 기술 스택 확인** (Redux 경험 여부)
> ✅ **성능 요구사항 정의** (번들 크기, 리렌더링)
> ✅ **디버깅 도구 필요성** (DevTools)
> ✅ **비동기 로직 복잡도** (단순/복잡)
> ✅ **유지보수 기간 고려** (장기/단기)
>
> **핵심 원칙:**
> - **서버 상태는 서버 상태 라이브러리로** (React Query)
> - **간단한 전역 상태는 가벼운 도구로** (Zustand)
> - **복잡한 로직은 Redux로** (Redux Toolkit)
> - **과도한 도구는 금물** (필요 이상 쓰지 말 것)

---

## 꼬리질문 대비 (13~15)

**Q13.** 전역 상태를 남용하면 어떤 문제가 발생하나요? 상태를 어디에 둘지 결정하는 기준은 무엇인가요?

> **전역 상태 남용의 문제점:**
>
> ### **1. 불필요한 리렌더링**
>
> ```jsx
> // ❌ 나쁜 예: 모든 상태를 전역에
> const useGlobalStore = create((set) => ({
>   user: null,
>   theme: 'light',
>   modalOpen: false,        // 지역 상태로 충분!
>   hoverState: false,       // 지역 상태로 충분!
>   tempInputValue: '',      // 지역 상태로 충분!
> }));
>
> function Component() {
>   // modalOpen만 필요한데 모든 변경에 리렌더링됨
>   const modalOpen = useGlobalStore(state => state.modalOpen);
> }
>
> // ✅ 좋은 예: 전역 vs 지역 구분
> const useAuthStore = create((set) => ({
>   user: null,              // 여러 곳에서 필요 → 전역
>   theme: 'light',          // 여러 곳에서 필요 → 전역
> }));
>
> function Modal() {
>   const [isOpen, setIsOpen] = useState(false);  // 이 컴포넌트만 필요 → 지역
>   const [inputValue, setInputValue] = useState('');  // 지역
> }
> ```
>
> ### **2. 컴포넌트 재사용성 감소**
>
> ```jsx
> // ❌ 전역 상태에 강하게 결합
> function UserCard() {
>   const user = useGlobalStore(state => state.user);
>   // 이제 이 컴포넌트는 전역 store 없이는 사용 불가능
> }
>
> // ✅ Props로 받아서 재사용 가능
> function UserCard({ user }) {
>   // 어디서든 사용 가능, 테스트도 쉬움
> }
> ```
>
> ### **3. 테스트 어려움**
>
> ```jsx
> // ❌ 전역 상태 의존
> function Component() {
>   const data = useGlobalStore(state => state.data);
>   // 테스트 시 전체 store를 mock 해야 함
> }
>
> // ✅ Props 의존
> function Component({ data }) {
>   // 테스트 시 props만 전달하면 됨
> }
> ```
>
> ### **4. 디버깅 복잡도 증가**
>
> ```jsx
> // 전역 상태가 많으면
> // "이 값을 누가 언제 왜 바꿨지?" → 추적 어려움
> // 여러 컴포넌트가 같은 전역 상태를 수정할 수 있음
> ```
>
> ### **5. 번들 크기 증가**
>
> ```jsx
> // 전역 상태 라이브러리는 모든 페이지에 포함됨
> // 불필요한 상태까지 모든 곳에 로드
> ```
>
> ---
>
> ### **상태 위치 결정 기준 (State Colocation)**
>
> **원칙: "상태는 가능한 한 사용하는 곳 가까이에"**
>
> ```
> 지역 상태 (useState) → 상위로 올리기 (Lift up) → Context → 전역 상태
>       ↑                      ↑                    ↑           ↑
>    기본값              props drilling 3단계 이상   특정 트리   앱 전체
> ```
>
> ---
>
> ### **의사결정 플로우차트:**
>
> ```jsx
> // Q1. 이 상태를 여러 컴포넌트가 사용하나?
> const [isOpen, setIsOpen] = useState(false);
> // → No → useState (지역 상태)
>
> // Q2. 부모-자식 간 전달인가?
> function Parent() {
>   const [user, setUser] = useState(null);
>   return <Child user={user} />;  // Props로 충분
> }
>
> // Q3. Props Drilling이 3단계 이상인가?
> <A> → <B> → <C> → <D user={user} />
> // → Yes → Context 고려
>
> const UserContext = createContext();
>
> // Q4. 앱 전체에서 사용하나? (인증, 테마 등)
> const useAuthStore = create((set) => ({
>   user: null,
>   login: (user) => set({ user })
> }));
> // → Yes → 전역 상태
> ```
>
> ---
>
> ### **구체적인 기준:**
>
> **지역 상태 (useState):**
> ```jsx
> function SearchBox() {
>   const [query, setQuery] = useState('');  // ✅
>   const [isOpen, setIsOpen] = useState(false);  // ✅
>   const [hovered, setHovered] = useState(false);  // ✅
>
>   // 이 컴포넌트 내부에서만 사용
>   // 다른 곳에서 필요 없음
> }
> ```
>
> **상위로 올리기 (Lifting State Up):**
> ```jsx
> function Parent() {
>   const [selectedId, setSelectedId] = useState(null);  // ✅
>
>   return (
>     <>
>       <List onSelect={setSelectedId} />
>       <Detail id={selectedId} />
>     </>
>   );
> }
> // List와 Detail이 공유 → 공통 부모로
> ```
>
> **Context API:**
> ```jsx
> // 깊은 계층, 자주 변경 안 됨, 특정 트리에만 필요
> const ThemeContext = createContext();
> const LanguageContext = createContext();
>
> function Layout() {
>   const [theme, setTheme] = useState('light');
>
>   return (
>     <ThemeContext.Provider value={{ theme, setTheme }}>
>       <Header />
>       <Main />
>       <Footer />
>     </ThemeContext.Provider>
>   );
> }
> ```
>
> **전역 상태 (Redux, Zustand 등):**
> ```jsx
> // 앱 전체에서 필요, 복잡한 로직, 자주 변경
> const useAuthStore = create((set) => ({
>   user: null,
>   isAuthenticated: false,
>   login: async (credentials) => {
>     const user = await api.login(credentials);
>     set({ user, isAuthenticated: true });
>   }
> }));
>
> // 여러 페이지, 여러 컴포넌트에서 사용
> ```
>
> ---
>
> ### **실무 예시:**
>
> **전자상거래 앱:**
>
> ```jsx
> // ✅ 전역 상태
> const useAuthStore = create(() => ({
>   user: null,              // 모든 페이지에서 필요
>   cart: [],                // 여러 곳에서 읽기/쓰기
> }));
>
> // ✅ Context (특정 트리)
> const CheckoutContext = createContext();  // 결제 플로우에만 필요
>
> // ✅ 지역 상태
> function ProductCard() {
>   const [quantity, setQuantity] = useState(1);  // 이 카드에만 필요
>   const [isHovered, setIsHovered] = useState(false);
> }
> ```
>
> ---
>
> ### **안티패턴 예시:**
>
> ```jsx
> // ❌ 잘못된 전역 상태 사용
> const useGlobalStore = create((set) => ({
>   // 모달 상태를 전역에
>   isModalOpen: false,          // ❌ 해당 페이지 컴포넌트의 지역 상태로 충분
>   modalTitle: '',              // ❌
>   modalContent: '',            // ❌
>
>   // Form 입력값을 전역에
>   loginForm: {                 // ❌ Form 컴포넌트의 지역 상태로 충분
>     email: '',
>     password: ''
>   },
>
>   // UI 임시 상태를 전역에
>   hoveredItemId: null,         // ❌ 지역 상태로
>   selectedTabIndex: 0,         // ❌ URL param이나 지역 상태로
> }));
>
> // ✅ 올바른 사용
> function LoginPage() {
>   // 지역 상태
>   const [isModalOpen, setIsModalOpen] = useState(false);
>   const [formData, setFormData] = useState({ email: '', password: '' });
>
>   // 전역 상태 (로그인 성공 후 저장)
>   const login = useAuthStore(state => state.login);
>
>   const handleSubmit = async () => {
>     await login(formData);  // 성공하면 전역 상태에 저장
>   };
> }
> ```
>
> ---
>
> ### **핵심 정리:**
>
> **전역 상태는 이럴 때만:**
> - ✅ 앱 전체에서 사용 (인증, 테마, 언어)
> - ✅ 여러 페이지에서 공유 (장바구니, 알림)
> - ✅ 복잡한 비즈니스 로직 (워크플로우, 대시보드)
>
> **지역 상태로 충분한 경우:**
> - ❌ 모달 열림/닫힘
> - ❌ Form 입력값 (제출 전)
> - ❌ Hover, Focus 같은 UI 상태
> - ❌ 한 컴포넌트에서만 사용
>
> **"가장 가까운 공통 조상"에 상태를 두는 것이 React의 철학입니다!**

---

**Q14.** Redux의 불변성(Immutability) 원칙이 왜 중요한지, Immer 같은 라이브러리가 어떻게 도움이 되는지 설명해주세요.

> ### **Redux의 불변성 원칙**
>
> **Redux는 상태를 직접 수정하지 않고 항상 새로운 객체를 반환해야 합니다.**
>
> ---
>
> ### **왜 불변성이 중요한가?**
>
> **1. 변경 감지 (Change Detection)**
>
> ```jsx
> // React는 참조 비교로 변경 감지
> const prevState = { count: 1 };
> const nextState = { count: 1 };
>
> prevState === nextState  // false → 변경됨으로 인식
>
> // ❌ 가변 업데이트
> const state = { count: 1 };
> state.count = 2;  // 같은 객체를 수정
> // state === state → true (변경 안 된 것으로 인식)
> // → 리렌더링 안 됨!
>
> // ✅ 불변 업데이트
> const state = { count: 1 };
> const newState = { ...state, count: 2 };  // 새 객체 생성
> // state !== newState → false (변경됨 인식)
> // → 리렌더링됨!
> ```
>
> **2. 타임 트래블 디버깅**
>
> ```jsx
> // 불변성 덕분에 이전 상태를 모두 보관 가능
> const history = [
>   { count: 0 },  // 상태 1
>   { count: 1 },  // 상태 2
>   { count: 2 },  // 상태 3
> ];
>
> // Redux DevTools에서 과거로 되돌리기 가능
> // 가변 방식이면 모든 상태가 { count: 2 }로 바뀜
> ```
>
> **3. 예측 가능성 (Predictability)**
>
> ```jsx
> // 불변 방식 - 예측 가능
> const state1 = { user: { name: 'Kim' } };
> const state2 = updateUser(state1, 'Lee');
> console.log(state1.user.name);  // 'Kim' (원본 유지)
> console.log(state2.user.name);  // 'Lee'
>
> // 가변 방식 - 예측 불가능
> const state = { user: { name: 'Kim' } };
> updateUser(state, 'Lee');  // state를 직접 수정
> console.log(state.user.name);  // 'Lee' (원본이 바뀜!)
> ```
>
> **4. 성능 최적화**
>
> ```jsx
> // React.memo, useMemo 등이 참조 비교로 작동
> const MemoComponent = React.memo(({ user }) => {
>   // user 객체가 같으면 리렌더링 안 함
> });
>
> // 불변성 유지 → 참조 비교만으로 변경 감지
> // 가변 방식 → 깊은 비교 필요 (비용 큼)
> ```
>
> ---
>
> ### **불변 업데이트의 어려움**
>
> **기존 Redux - Spread 연산자 사용:**
>
> ```jsx
> // ❌ 가변 업데이트 (절대 금지!)
> const userReducer = (state, action) => {
>   state.user.name = action.payload;  // 직접 수정 ❌
>   return state;
> };
>
> // ✅ 얕은 객체 불변 업데이트
> const userReducer = (state, action) => {
>   return {
>     ...state,
>     user: {
>       ...state.user,
>       name: action.payload
>     }
>   };
> };
>
> // 😰 중첩된 객체 불변 업데이트 (복잡!)
> const state = {
>   users: {
>     byId: {
>       1: { name: 'Kim', address: { city: 'Seoul' } }
>     }
>   }
> };
>
> // 'Seoul' → 'Busan' 변경하려면...
> const newState = {
>   ...state,
>   users: {
>     ...state.users,
>     byId: {
>       ...state.users.byId,
>       1: {
>         ...state.users.byId[1],
>         address: {
>           ...state.users.byId[1].address,
>           city: 'Busan'
>         }
>       }
>     }
>   }
> };
> // 😱 너무 복잡함!
>
> // 😰 배열 불변 업데이트
> // 항목 추가
> const newState = {
>   ...state,
>   todos: [...state.todos, newTodo]
> };
>
> // 항목 삭제
> const newState = {
>   ...state,
>   todos: state.todos.filter(todo => todo.id !== action.id)
> };
>
> // 항목 수정
> const newState = {
>   ...state,
>   todos: state.todos.map(todo =>
>     todo.id === action.id
>       ? { ...todo, completed: true }
>       : todo
>   )
> };
> ```
>
> **문제점:**
> - 코드 복잡도 증가
> - 실수하기 쉬움 (spread 빠뜨림)
> - 가독성 저하
> - 성능 이슈 (깊은 복사)
>
> ---
>
> ### **Immer 라이브러리의 해결책**
>
> **"불변 업데이트를 가변 방식처럼 작성"**
>
> ```jsx
> import { produce } from 'immer';
>
> // 기존 방식 (Spread)
> const newState = {
>   ...state,
>   user: {
>     ...state.user,
>     name: 'Lee'
>   }
> };
>
> // Immer 방식 (직접 수정)
> const newState = produce(state, draft => {
>   draft.user.name = 'Lee';  // 직접 수정하는 것처럼!
> });
> // → Immer가 내부적으로 불변 업데이트 수행
> ```
>
> **복잡한 중첩 구조:**
>
> ```jsx
> // ❌ 기존 방식 - 지옥의 spread
> const newState = {
>   ...state,
>   users: {
>     ...state.users,
>     byId: {
>       ...state.users.byId,
>       1: {
>         ...state.users.byId[1],
>         address: {
>           ...state.users.byId[1].address,
>           city: 'Busan'
>         }
>       }
>     }
>   }
> };
>
> // ✅ Immer - 직관적!
> const newState = produce(state, draft => {
>   draft.users.byId[1].address.city = 'Busan';
> });
> ```
>
> **배열 조작:**
>
> ```jsx
> // 기존 방식
> const newState = {
>   ...state,
>   todos: state.todos.map(todo =>
>     todo.id === 5
>       ? { ...todo, completed: true }
>       : todo
>   )
> };
>
> // Immer 방식
> const newState = produce(state, draft => {
>   const todo = draft.todos.find(t => t.id === 5);
>   todo.completed = true;  // 직접 수정!
> });
>
> // 배열 추가
> const newState = produce(state, draft => {
>   draft.todos.push(newTodo);  // push 사용 가능!
> });
>
> // 배열 삭제
> const newState = produce(state, draft => {
>   const index = draft.todos.findIndex(t => t.id === 5);
>   draft.todos.splice(index, 1);  // splice 사용 가능!
> });
> ```
>
> ---
>
> ### **Redux Toolkit에 Immer 내장**
>
> **Redux Toolkit은 Immer를 기본 포함:**
>
> ```jsx
> // 기존 Redux - Spread 필수
> const todosReducer = (state = [], action) => {
>   switch (action.type) {
>     case 'ADD_TODO':
>       return [...state, action.payload];  // Spread
>     case 'TOGGLE_TODO':
>       return state.map(todo =>
>         todo.id === action.id
>           ? { ...todo, completed: !todo.completed }
>           : todo
>       );
>     default:
>       return state;
>   }
> };
>
> // Redux Toolkit - 직접 수정 가능 (Immer 내장)
> const todosSlice = createSlice({
>   name: 'todos',
>   initialState: [],
>   reducers: {
>     addTodo: (state, action) => {
>       state.push(action.payload);  // 직접 수정!
>     },
>     toggleTodo: (state, action) => {
>       const todo = state.find(t => t.id === action.id);
>       todo.completed = !todo.completed;  // 직접 수정!
>     }
>   }
> });
> ```
>
> **복잡한 예시:**
>
> ```jsx
> const userSlice = createSlice({
>   name: 'user',
>   initialState: {
>     profile: {
>       name: '',
>       address: {
>         city: '',
>         zipCode: ''
>       }
>     },
>     orders: []
>   },
>   reducers: {
>     updateCity: (state, action) => {
>       // 직접 수정 가능! (Immer가 처리)
>       state.profile.address.city = action.payload;
>     },
>     addOrder: (state, action) => {
>       state.orders.push(action.payload);
>     },
>     updateOrder: (state, action) => {
>       const order = state.orders.find(o => o.id === action.id);
>       order.status = 'completed';
>     }
>   }
> });
> ```
>
> ---
>
> ### **Immer 동작 원리**
>
> ```jsx
> const state = { count: 1, user: { name: 'Kim' } };
>
> const newState = produce(state, draft => {
>   draft.count = 2;  // draft는 Proxy 객체
> });
>
> // Immer 내부 과정:
> // 1. state를 Proxy로 감싸서 draft 생성
> // 2. draft 수정 사항을 추적
> // 3. 수정된 부분만 새로운 객체로 복사
> // 4. 나머지는 원본 객체 재사용 (구조적 공유)
>
> console.log(state.count);      // 1 (원본 유지)
> console.log(newState.count);   // 2
> console.log(state.user === newState.user);  // true (재사용!)
> ```
>
> ---
>
> ### **주의사항**
>
> **1. return 문 주의:**
> ```jsx
> // ❌ 혼용하면 안 됨
> reducers: {
>   addTodo: (state, action) => {
>     state.push(action.payload);  // 직접 수정
>     return state;  // ❌ return하면 안 됨!
>   }
> }
>
> // ✅ 둘 중 하나만 사용
> // 방법 1: 직접 수정 (return 없음)
> addTodo: (state, action) => {
>   state.push(action.payload);
> }
>
> // 방법 2: 새 객체 반환 (Immer 안 씀)
> setTodos: (state, action) => {
>   return action.payload;
> }
> ```
>
> **2. 비동기는 안 됨:**
> ```jsx
> // ❌ 비동기 안 됨
> reducers: {
>   addTodo: async (state, action) => {  // ❌
>     const data = await api.getTodo();
>     state.todos.push(data);
>   }
> }
>
> // ✅ createAsyncThunk 사용
> const addTodo = createAsyncThunk(
>   'todos/add',
>   async () => await api.getTodo()
> );
> ```
>
> ---
>
> ### **성능 비교**
>
> ```jsx
> // Spread (전체 복사)
> const newState = {
>   ...state,
>   todos: [...state.todos, newTodo]
> };
> // → 모든 todo를 복사
>
> // Immer (구조적 공유)
> const newState = produce(state, draft => {
>   draft.todos.push(newTodo);
> });
> // → 변경된 부분만 복사, 나머지 재사용
> // → 메모리 효율적!
> ```
>
> ---
>
> ### **핵심 정리**
>
> **불변성이 중요한 이유:**
> - React의 변경 감지 (참조 비교)
> - 타임 트래블 디버깅
> - 예측 가능한 코드
> - 성능 최적화 (React.memo 등)
>
> **Immer의 장점:**
> - 직관적인 코드 (가변처럼 작성)
> - 불변성 자동 보장
> - 구조적 공유로 성능 최적화
> - Redux Toolkit에 기본 내장
>
> **Redux Toolkit 사용 시:**
> - `createSlice` 내부에서 직접 수정 OK
> - Immer가 자동으로 불변 업데이트 처리
> - Spread 연산자 불필요

---

**Q15.** 실무에서 여러 상태 관리 도구를 함께 사용한 경험이 있나요? 예를 들어 Redux + React Query를 함께 사용하는 경우는 어떤가요?

> **실무에서는 여러 상태 관리 도구를 조합해서 사용하는 것이 일반적입니다.**
>
> ---
>
> ### **Redux + React Query 조합 (가장 흔한 패턴)**
>
> **역할 분담:**
>
> ```jsx
> // React Query: 서버 상태 관리
> const { data: products, isLoading } = useQuery({
>   queryKey: ['products'],
>   queryFn: api.getProducts,
>   staleTime: 5 * 60 * 1000,
> });
>
> // Redux: 복잡한 클라이언트 상태 관리
> const cart = useSelector(state => state.cart);
> const dispatch = useDispatch();
>
> const addToCart = (product) => {
>   dispatch(cartSlice.actions.addItem({
>     id: product.id,
>     quantity: 1,
>     price: product.price
>   }));
> };
> ```
>
> **실무 예시 - 전자상거래:**
>
> ```jsx
> // ✅ React Query - 서버 데이터
> // 1. 상품 목록 조회
> const { data: products } = useQuery({
>   queryKey: ['products', category],
>   queryFn: () => api.getProducts(category)
> });
>
> // 2. 사용자 정보 조회
> const { data: user } = useQuery({
>   queryKey: ['user'],
>   queryFn: api.getCurrentUser,
>   staleTime: Infinity,  // 로그인 중엔 안 바뀜
> });
>
> // 3. 주문 목록 조회
> const { data: orders } = useQuery({
>   queryKey: ['orders', user?.id],
>   queryFn: () => api.getOrders(user.id),
>   enabled: !!user,
> });
>
> // ✅ Redux - 클라이언트 상태 + 복잡한 로직
> const store = configureStore({
>   reducer: {
>     // 1. 장바구니 (복잡한 계산 로직)
>     cart: cartSlice.reducer,
>     // 2. UI 상태
>     ui: uiSlice.reducer,
>     // 3. 필터/정렬 상태
>     filters: filtersSlice.reducer,
>   }
> });
>
> const cartSlice = createSlice({
>   name: 'cart',
>   initialState: {
>     items: [],
>     discount: 0,
>     coupon: null,
>   },
>   reducers: {
>     addItem: (state, action) => {
>       const existing = state.items.find(i => i.id === action.payload.id);
>       if (existing) {
>         existing.quantity++;
>       } else {
>         state.items.push({ ...action.payload, quantity: 1 });
>       }
>     },
>     applyCoupon: (state, action) => {
>       // 복잡한 할인 계산 로직
>       state.coupon = action.payload;
>       state.discount = calculateDiscount(state.items, action.payload);
>     },
>     // ... 복잡한 비즈니스 로직
>   }
> });
> ```
>
> **왜 이렇게 나누나?**
>
> | 항목 | React Query | Redux |
> |------|-------------|-------|
> | **데이터 출처** | 서버 | 클라이언트 |
> | **캐싱** | 자동 | 수동 |
> | **자동 갱신** | 지원 | 없음 |
> | **로딩/에러** | 자동 관리 | 수동 |
> | **복잡한 로직** | 부적합 | 적합 |
> | **타임 트래블** | 없음 | 가능 |
>
> ---
>
> ### **Zustand + React Query 조합 (가벼운 조합)**
>
> **스타트업/MVP에 적합:**
>
> ```jsx
> // React Query - 서버 상태
> const { data: user } = useQuery({
>   queryKey: ['user'],
>   queryFn: api.getCurrentUser
> });
>
> // Zustand - 간단한 클라이언트 상태
> const useUIStore = create((set) => ({
>   theme: 'light',
>   sidebarOpen: true,
>
>   setTheme: (theme) => set({ theme }),
>   toggleSidebar: () => set((state) => ({
>     sidebarOpen: !state.sidebarOpen
>   })),
> }));
>
> function App() {
>   const theme = useUIStore((state) => state.theme);
>   const { data: posts } = useQuery(['posts'], api.getPosts);
>
>   return (
>     <ThemeProvider theme={theme}>
>       <PostList posts={posts} />
>     </ThemeProvider>
>   );
> }
> ```
>
> ---
>
> ### **Context + React Query 조합 (소규모)**
>
> **간단한 프로젝트:**
>
> ```jsx
> // React Query - 서버 상태
> const { data: user } = useQuery(['user'], api.getUser);
>
> // Context - 간단한 UI 상태
> const ThemeContext = createContext();
> const LanguageContext = createContext();
>
> function App() {
>   const [theme, setTheme] = useState('light');
>   const [language, setLanguage] = useState('ko');
>
>   return (
>     <ThemeContext.Provider value={{ theme, setTheme }}>
>       <LanguageContext.Provider value={{ language, setLanguage }}>
>         <QueryClientProvider client={queryClient}>
>           <Router />
>         </QueryClientProvider>
>       </LanguageContext.Provider>
>     </ThemeContext.Provider>
>   );
> }
> ```
>
> ---
>
> ### **실무 아키텍처 예시**
>
> **대규모 엔터프라이즈:**
>
> ```jsx
> // 1. React Query - 서버 상태 전담
> const queryClient = new QueryClient({
>   defaultOptions: {
>     queries: {
>       staleTime: 5 * 60 * 1000,
>       cacheTime: 10 * 60 * 1000,
>       refetchOnWindowFocus: false,
>     }
>   }
> });
>
> // 2. Redux Toolkit - 복잡한 클라이언트 로직
> const store = configureStore({
>   reducer: {
>     cart: cartSlice.reducer,
>     checkout: checkoutSlice.reducer,
>     filters: filtersSlice.reducer,
>   },
>   middleware: (getDefaultMiddleware) =>
>     getDefaultMiddleware().concat(sagaMiddleware),
> });
>
> // 3. Zustand - 간단한 UI 상태
> const useUIStore = create((set) => ({
>   theme: 'light',
>   sidebarOpen: false,
>   notifications: [],
> }));
>
> // 4. Context - 특정 트리의 상태
> const CheckoutContext = createContext();
>
> // 앱 구조
> function App() {
>   return (
>     <Provider store={store}>  {/* Redux */}
>       <QueryClientProvider client={queryClient}>  {/* React Query */}
>         <Router>
>           <Layout />
>         </Router>
>       </QueryClientProvider>
>     </Provider>
>   );
> }
> ```
>
> ---
>
> ### **상호작용 패턴**
>
> **패턴 1: React Query → Redux (서버 데이터로 클라이언트 상태 초기화)**
>
> ```jsx
> function ProductPage() {
>   const dispatch = useDispatch();
>
>   // React Query로 상품 조회
>   const { data: product } = useQuery({
>     queryKey: ['product', productId],
>     queryFn: () => api.getProduct(productId),
>     onSuccess: (data) => {
>       // 성공 시 Redux에 저장 (선택사항)
>       dispatch(setCurrentProduct(data));
>     }
>   });
>
>   // Redux의 장바구니에 추가
>   const handleAddToCart = () => {
>     dispatch(addToCart(product));
>   };
> }
> ```
>
> **패턴 2: Redux → React Query (클라이언트 상태로 쿼리 제어)**
>
> ```jsx
> function Dashboard() {
>   // Redux에서 필터 상태 가져오기
>   const filters = useSelector(state => state.filters);
>
>   // 필터에 따라 React Query 실행
>   const { data: products } = useQuery({
>     queryKey: ['products', filters],  // 필터가 바뀌면 재조회
>     queryFn: () => api.getProducts(filters),
>     enabled: filters.category !== null,
>   });
> }
> ```
>
> **패턴 3: Optimistic Update 조합**
>
> ```jsx
> function TodoList() {
>   const dispatch = useDispatch();
>   const queryClient = useQueryClient();
>
>   const mutation = useMutation({
>     mutationFn: api.updateTodo,
>
>     onMutate: async (newTodo) => {
>       // 1. React Query 캐시에 즉시 반영
>       await queryClient.cancelQueries(['todos']);
>       const previous = queryClient.getQueryData(['todos']);
>       queryClient.setQueryData(['todos'], (old) => [...old, newTodo]);
>
>       // 2. Redux에도 즉시 반영
>       dispatch(addTodoOptimistic(newTodo));
>
>       return { previous };
>     },
>
>     onError: (err, newTodo, context) => {
>       // 실패 시 롤백
>       queryClient.setQueryData(['todos'], context.previous);
>       dispatch(removeTodoOptimistic(newTodo.id));
>     },
>   });
> }
> ```
>
> ---
>
> ### **실무 팁**
>
> **1. 명확한 역할 분담:**
> ```jsx
> // ✅ 좋은 예
> // React Query: 모든 API 호출
> // Redux: 장바구니, 체크아웃 플로우
> // Zustand: 테마, 언어, 사이드바
>
> // ❌ 나쁜 예
> // Redux에도 user 데이터, React Query에도 user 데이터
> // → 어디가 진실의 원천인지 불명확!
> ```
>
> **2. 중복 방지:**
> ```jsx
> // ❌ 피해야 할 패턴
> // React Query로 가져온 데이터를 Redux에 또 저장
> const { data: user } = useQuery(['user'], api.getUser);
> dispatch(setUser(user));  // 불필요!
>
> // ✅ React Query만 사용
> const { data: user } = useQuery(['user'], api.getUser);
> // 필요한 곳에서 바로 사용
> ```
>
> **3. 일관된 패턴:**
> ```jsx
> // 팀 전체가 따를 규칙 정의
> const RULES = {
>   serverState: 'React Query',      // API 데이터
>   clientLogic: 'Redux Toolkit',    // 복잡한 로직
>   uiState: 'Zustand',              // 간단한 UI
>   formState: 'react-hook-form',    // 폼 상태
> };
> ```
>
> ---
>
> ### **조합 선택 가이드**
>
> | 프로젝트 규모 | 추천 조합 |
> |---------------|-----------|
> | **소규모** | React Query + Context |
> | **중규모** | React Query + Zustand |
> | **대규모** | React Query + Redux Toolkit |
> | **엔터프라이즈** | React Query + Redux + Saga + Zustand |
>
> ---
>
> ### **핵심 정리**
>
> **실무 조합 원칙:**
> - **서버 상태는 항상 React Query/SWR**
> - **클라이언트 상태는 복잡도에 따라 선택**
>   - 간단 → Context / Zustand
>   - 복잡 → Redux Toolkit
> - **중복 저장 금지** (하나의 상태는 하나의 도구로)
> - **명확한 경계 설정** (팀 컨벤션 문서화)
>
> **가장 흔한 조합:**
> 1. React Query + Zustand (스타트업)
> 2. React Query + Redux Toolkit (중대형)
> 3. React Query + Context (소규모)
