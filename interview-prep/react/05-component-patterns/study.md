# 5. React 컴포넌트 패턴

---

## Controlled vs Uncontrolled Components

### 개념

**Controlled Component (제어 컴포넌트)**
- React state가 "single source of truth"
- 입력 값이 항상 React state와 동기화
- 모든 변경사항이 이벤트 핸들러를 통해 state 업데이트

**Uncontrolled Component (비제어 컴포넌트)**
- DOM 자체가 데이터의 source of truth
- ref를 사용하여 필요할 때만 값을 가져옴
- React state를 거치지 않음

### 코드 비교

```jsx
// ===== Controlled Component =====
function ControlledInput() {
  const [value, setValue] = useState('');

  const handleChange = (e) => {
    setValue(e.target.value);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Submitted value:', value);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={value}  // state로 제어
        onChange={handleChange}
      />
      <p>현재 값: {value}</p>  {/* 실시간 표시 가능 */}
    </form>
  );
}

// ===== Uncontrolled Component =====
function UncontrolledInput() {
  const inputRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Submitted value:', inputRef.current.value);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        ref={inputRef}
        defaultValue="초기값"  // 초기값만 설정
      />
      {/* 제출 전까지는 값을 알 수 없음 */}
    </form>
  );
}
```

### 비교표

| 특성 | Controlled | Uncontrolled |
|------|-----------|--------------|
| 데이터 관리 | React state | DOM |
| 값 접근 | state 변수 | ref.current.value |
| 초기값 설정 | value prop | defaultValue prop |
| 실시간 검증 | 가능 | 불가능 |
| 성능 | 매 입력마다 리렌더링 | 리렌더링 없음 |
| 제어 수준 | 완전 제어 | 제한적 |

### 사용 시점

**Controlled Component 사용**
```jsx
// 1. 실시간 유효성 검사
function EmailInput() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const value = e.target.value;
    setEmail(value);

    if (!value.includes('@')) {
      setError('유효한 이메일을 입력하세요');
    } else {
      setError('');
    }
  };

  return (
    <>
      <input value={email} onChange={handleChange} />
      {error && <span style={{ color: 'red' }}>{error}</span>}
    </>
  );
}

// 2. 입력 포맷팅
function PhoneInput() {
  const [phone, setPhone] = useState('');

  const handleChange = (e) => {
    const numbers = e.target.value.replace(/\D/g, '');
    const formatted = numbers.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
    setPhone(formatted);
  };

  return <input value={phone} onChange={handleChange} />;
}

// 3. 조건부 활성화
function ConditionalForm() {
  const [agreed, setAgreed] = useState(false);
  const [username, setUsername] = useState('');

  return (
    <>
      <input
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        disabled={!agreed}
      />
      <label>
        <input
          type="checkbox"
          checked={agreed}
          onChange={(e) => setAgreed(e.target.checked)}
        />
        약관 동의
      </label>
    </>
  );
}
```

**Uncontrolled Component 사용**
```jsx
// 1. 파일 업로드 (controlled 불가능)
function FileUpload() {
  const fileRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    const file = fileRef.current.files[0];
    uploadFile(file);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input type="file" ref={fileRef} />
      <button>업로드</button>
    </form>
  );
}

// 2. 대량 폼 필드 (성능 최적화)
function LargeForm() {
  const formRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(formRef.current);
    const data = Object.fromEntries(formData);
    submitData(data);
  };

  return (
    <form ref={formRef} onSubmit={handleSubmit}>
      {/* 100개의 input - 각각 state 관리하면 성능 문제 */}
      <input name="field1" defaultValue="" />
      <input name="field2" defaultValue="" />
      {/* ... */}
      <button>제출</button>
    </form>
  );
}
```

---

## 합성(Composition) vs 상속(Inheritance)

### React의 철학: "합성을 상속보다 선호"

### 합성(Composition)

**기본 합성 - children 사용**
```jsx
function Dialog({ title, children }) {
  return (
    <div className="dialog">
      <h1 className="dialog-title">{title}</h1>
      <div className="dialog-content">
        {children}
      </div>
    </div>
  );
}

// 사용
<Dialog title="알림">
  <p>저장되었습니다.</p>
  <button>확인</button>
</Dialog>
```

**특수화(Specialization)**
```jsx
// 일반적인 컴포넌트
function Dialog({ title, children }) {
  return (
    <div className="dialog">
      <h1>{title}</h1>
      <div>{children}</div>
    </div>
  );
}

// 특수화된 컴포넌트들
function WelcomeDialog() {
  return (
    <Dialog title="환영합니다">
      <p>우리 사이트에 방문해주셔서 감사합니다!</p>
    </Dialog>
  );
}

function ConfirmDialog({ message, onConfirm, onCancel }) {
  return (
    <Dialog title="확인">
      <p>{message}</p>
      <button onClick={onConfirm}>확인</button>
      <button onClick={onCancel}>취소</button>
    </Dialog>
  );
}
```

**여러 슬롯을 가진 합성**
```jsx
function SplitPane({ left, right }) {
  return (
    <div className="split-pane">
      <div className="split-pane-left">
        {left}
      </div>
      <div className="split-pane-right">
        {right}
      </div>
    </div>
  );
}

// 사용
<SplitPane
  left={<ContactList />}
  right={<ChatWindow />}
/>
```

### 상속 (React에서 권장하지 않음)

```jsx
// ❌ 이렇게 하지 마세요
class BaseButton extends React.Component {
  handleClick = () => {
    console.log('Clicked');
  }

  render() {
    return <button onClick={this.handleClick}>{this.props.children}</button>;
  }
}

class PrimaryButton extends BaseButton {
  render() {
    return (
      <button
        onClick={this.handleClick}
        className="btn-primary"
      >
        {this.props.children}
      </button>
    );
  }
}

// ✅ 대신 합성을 사용하세요
function Button({ variant = 'default', onClick, children }) {
  return (
    <button
      onClick={onClick}
      className={`btn btn-${variant}`}
    >
      {children}
    </button>
  );
}

function PrimaryButton({ onClick, children }) {
  return <Button variant="primary" onClick={onClick}>{children}</Button>;
}
```

### 합성을 권장하는 이유

```
1. 유연성
   ├─ 런타임에 컴포넌트 조합 가능
   └─ props를 통한 동적 구성

2. 명확성
   ├─ 컴포넌트 간 관계가 명시적
   └─ 데이터 흐름이 추적 가능

3. 재사용성
   ├─ 작은 단위로 분리
   └─ 여러 곳에서 조합하여 재사용

4. 함수형 프로그래밍
   ├─ Hooks와 잘 어울림
   └─ 클래스 상속의 복잡성 회피
```

---

## HOC (Higher-Order Component)

### 개념

- 컴포넌트를 받아서 새로운 컴포넌트를 반환하는 함수
- 컴포넌트 로직을 재사용하기 위한 고급 패턴
- "컴포넌트를 위한 고차 함수"

```
Component A ──┐
              ├─→ HOC ─→ Enhanced Component
Component B ──┘
```

### 기본 패턴

```jsx
// HOC 정의
function withLoading(Component) {
  return function WithLoadingComponent({ isLoading, ...props }) {
    if (isLoading) {
      return <div>로딩 중...</div>;
    }
    return <Component {...props} />;
  };
}

// 사용
function UserList({ users }) {
  return (
    <ul>
      {users.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}

const UserListWithLoading = withLoading(UserList);

// 렌더링
<UserListWithLoading isLoading={loading} users={users} />
```

### 실무 예시

**1. 인증 체크 HOC**
```jsx
function withAuth(Component) {
  return function WithAuthComponent(props) {
    const { isAuthenticated, user } = useAuth();

    if (!isAuthenticated) {
      return <Navigate to="/login" />;
    }

    return <Component {...props} user={user} />;
  };
}

// 사용
const ProtectedDashboard = withAuth(Dashboard);
const ProtectedSettings = withAuth(Settings);
```

**2. 데이터 페칭 HOC**
```jsx
function withData(url) {
  return function(Component) {
    return function WithDataComponent(props) {
      const [data, setData] = useState(null);
      const [loading, setLoading] = useState(true);
      const [error, setError] = useState(null);

      useEffect(() => {
        fetch(url)
          .then(res => res.json())
          .then(data => {
            setData(data);
            setLoading(false);
          })
          .catch(err => {
            setError(err);
            setLoading(false);
          });
      }, []);

      if (loading) return <Spinner />;
      if (error) return <Error error={error} />;

      return <Component {...props} data={data} />;
    };
  };
}

// 사용
const UserList = withData('/api/users')(({ data }) => (
  <ul>
    {data.map(user => <li key={user.id}>{user.name}</li>)}
  </ul>
));
```

**3. HOC 조합**
```jsx
import { compose } from 'redux'; // 또는 직접 구현

const enhance = compose(
  withAuth,
  withLoading,
  withData('/api/users')
);

const EnhancedUserList = enhance(UserList);

// 동작 순서:
// withData → withLoading → withAuth → UserList
```

### 장단점

**장점**
- 로직 재사용
- 여러 컴포넌트에 동일한 기능 적용
- 조합 가능 (compose)

**단점**
- Wrapper Hell (중첩 깊어짐)
- Props 이름 충돌 가능
- ref 전달 복잡
- displayName 관리 필요

### 현재 사용 추세

```
HOC (2015-2018)  →  Render Props (2018-2019)  →  Custom Hooks (2019-현재)
      ↓                      ↓                            ↓
   감소 추세            거의 안씀                    주류 패턴
```

---

## Render Props 패턴

### 개념

함수를 prop으로 전달하여 무엇을 렌더링할지 컴포넌트에게 알려주는 패턴

### 기본 패턴

```jsx
// Render Props 컴포넌트
class MouseTracker extends React.Component {
  state = { x: 0, y: 0 };

  handleMouseMove = (event) => {
    this.setState({
      x: event.clientX,
      y: event.clientY
    });
  };

  render() {
    return (
      <div onMouseMove={this.handleMouseMove}>
        {/* render prop에 상태 전달 */}
        {this.props.render(this.state)}
      </div>
    );
  }
}

// 사용
<MouseTracker
  render={({ x, y }) => (
    <h1>마우스 위치: ({x}, {y})</h1>
  )}
/>
```

### Children as Function 패턴

```jsx
function DataFetcher({ url, children }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(url)
      .then(res => res.json())
      .then(data => {
        setData(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, [url]);

  // children을 함수로 호출
  return children({ data, loading, error });
}

// 사용
<DataFetcher url="/api/users">
  {({ data, loading, error }) => {
    if (loading) return <Spinner />;
    if (error) return <Error error={error} />;
    return <UserList users={data} />;
  }}
</DataFetcher>
```

### 실무 예시: Toggle

```jsx
function Toggle({ children }) {
  const [on, setOn] = useState(false);

  const toggle = () => setOn(prev => !prev);

  return children({ on, toggle });
}

// 사용 - 다양한 UI로 활용 가능
<Toggle>
  {({ on, toggle }) => (
    <div>
      <button onClick={toggle}>
        {on ? '켜짐' : '꺼짐'}
      </button>
      {on && <div>토글된 컨텐츠</div>}
    </div>
  )}
</Toggle>

// 다른 UI
<Toggle>
  {({ on, toggle }) => (
    <label>
      <input type="checkbox" checked={on} onChange={toggle} />
      {on ? '활성화' : '비활성화'}
    </label>
  )}
</Toggle>
```

### HOC vs Render Props

```jsx
// HOC - 감싸는 방식
const MouseComponent = withMouse(MyComponent);

// Render Props - 전달하는 방식
<Mouse>
  {mouse => <MyComponent mouse={mouse} />}
</Mouse>
```

**비교**

| 측면 | HOC | Render Props |
|------|-----|--------------|
| 문법 | 래핑 | 중첩 |
| Props 충돌 | 가능 | 없음 |
| 렌더링 제어 | HOC가 제어 | 사용자가 제어 |
| Wrapper Hell | 발생 | Callback Hell 발생 |

---

## Custom Hooks로 로직 재사용

### Custom Hooks가 HOC/Render Props를 대체

```jsx
// ===== HOC 방식 (옛날) =====
const UserList = withAuth(withData('/api/users')(UserListComponent));

// ===== Render Props 방식 (옛날) =====
<Auth>
  {auth => (
    <DataFetcher url="/api/users">
      {data => <UserListComponent auth={auth} data={data} />}
    </DataFetcher>
  )}
</Auth>

// ===== Custom Hooks 방식 (현재) =====
function UserList() {
  const auth = useAuth();
  const data = useData('/api/users');

  return <UserListComponent auth={auth} data={data} />;
}
```

### Custom Hook 예시

**1. useToggle**
```jsx
function useToggle(initialValue = false) {
  const [value, setValue] = useState(initialValue);

  const toggle = useCallback(() => {
    setValue(prev => !prev);
  }, []);

  const setTrue = useCallback(() => {
    setValue(true);
  }, []);

  const setFalse = useCallback(() => {
    setValue(false);
  }, []);

  return { value, toggle, setTrue, setFalse };
}

// 사용
function Modal() {
  const { value: isOpen, toggle, setFalse } = useToggle();

  return (
    <>
      <button onClick={toggle}>모달 열기</button>
      {isOpen && (
        <div className="modal">
          <button onClick={setFalse}>닫기</button>
        </div>
      )}
    </>
  );
}
```

**2. useFetch**
```jsx
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetch(url)
      .then(res => res.json())
      .then(data => {
        setData(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, [url]);

  return { data, loading, error };
}

// 사용
function UserList() {
  const { data, loading, error } = useFetch('/api/users');

  if (loading) return <Spinner />;
  if (error) return <Error error={error} />;

  return (
    <ul>
      {data.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}
```

**3. useLocalStorage**
```jsx
function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(error);
      return initialValue;
    }
  });

  const setValue = (value) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
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
    <button onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}>
      현재 테마: {theme}
    </button>
  );
}
```

### 장점

```
1. 간결성
   - Wrapper Hell 없음
   - 코드가 읽기 쉬움

2. 유연성
   - 로직만 재사용
   - UI는 자유롭게 구성

3. 조합 용이
   - 여러 Hook을 조합
   - 순서 제어 가능

4. TypeScript 친화적
   - 타입 추론 잘 됨
```

---

## Compound Components 패턴

### 개념

여러 컴포넌트가 함께 작동하여 하나의 기능을 구성하는 패턴

```
HTML의 <select>와 <option> 관계와 유사

<select>         ←─┐
  <option />       ├─ 함께 동작
  <option />       │
</select>        ←─┘
```

### Context 기반 구현

```jsx
// Context 생성
const TabContext = React.createContext();

// 부모 컴포넌트
function Tabs({ children, defaultTab }) {
  const [activeTab, setActiveTab] = useState(defaultTab);

  return (
    <TabContext.Provider value={{ activeTab, setActiveTab }}>
      <div className="tabs">{children}</div>
    </TabContext.Provider>
  );
}

// 자식 컴포넌트들
function TabList({ children }) {
  return <div className="tab-list">{children}</div>;
}

function Tab({ id, children }) {
  const { activeTab, setActiveTab } = useContext(TabContext);

  return (
    <button
      className={activeTab === id ? 'tab active' : 'tab'}
      onClick={() => setActiveTab(id)}
    >
      {children}
    </button>
  );
}

function TabPanel({ id, children }) {
  const { activeTab } = useContext(TabContext);

  if (activeTab !== id) return null;

  return <div className="tab-panel">{children}</div>;
}

// 컴포넌트 연결
Tabs.List = TabList;
Tabs.Tab = Tab;
Tabs.Panel = TabPanel;
```

### 사용 예시

```jsx
function App() {
  return (
    <Tabs defaultTab="profile">
      <Tabs.List>
        <Tabs.Tab id="profile">프로필</Tabs.Tab>
        <Tabs.Tab id="settings">설정</Tabs.Tab>
      </Tabs.List>

      <Tabs.Panel id="profile">
        <h2>프로필</h2>
      </Tabs.Panel>

      <Tabs.Panel id="settings">
        <h2>설정</h2>
      </Tabs.Panel>
    </Tabs>
  );
}
```

### 실무 예시: Accordion

```jsx
const AccordionContext = React.createContext();

function Accordion({ children, allowMultiple = false }) {
  const [openItems, setOpenItems] = useState([]);

  const toggleItem = (id) => {
    if (allowMultiple) {
      setOpenItems(prev =>
        prev.includes(id)
          ? prev.filter(item => item !== id)
          : [...prev, id]
      );
    } else {
      setOpenItems(prev => prev.includes(id) ? [] : [id]);
    }
  };

  return (
    <AccordionContext.Provider value={{ openItems, toggleItem }}>
      <div className="accordion">{children}</div>
    </AccordionContext.Provider>
  );
}

function AccordionItem({ children }) {
  return <div className="accordion-item">{children}</div>;
}

function AccordionHeader({ id, children }) {
  const { openItems, toggleItem } = useContext(AccordionContext);
  const isOpen = openItems.includes(id);

  return (
    <button onClick={() => toggleItem(id)}>
      {children}
      <span>{isOpen ? '▲' : '▼'}</span>
    </button>
  );
}

function AccordionPanel({ id, children }) {
  const { openItems } = useContext(AccordionContext);

  if (!openItems.includes(id)) return null;

  return <div className="accordion-panel">{children}</div>;
}

Accordion.Item = AccordionItem;
Accordion.Header = AccordionHeader;
Accordion.Panel = AccordionPanel;

// 사용
<Accordion allowMultiple>
  <Accordion.Item>
    <Accordion.Header id="1">질문 1</Accordion.Header>
    <Accordion.Panel id="1">답변 1</Accordion.Panel>
  </Accordion.Item>
  <Accordion.Item>
    <Accordion.Header id="2">질문 2</Accordion.Header>
    <Accordion.Panel id="2">답변 2</Accordion.Panel>
  </Accordion.Item>
</Accordion>
```

---

## Container/Presentational 패턴

### Hooks 이전 (옛날 방식)

```jsx
// ===== Presentational Component =====
// - UI만 담당
// - props를 통해 데이터/콜백 받음
// - 상태 없음
function UserList({ users, onUserClick }) {
  return (
    <ul>
      {users.map(user => (
        <li key={user.id} onClick={() => onUserClick(user)}>
          {user.name}
        </li>
      ))}
    </ul>
  );
}

// ===== Container Component =====
// - 로직 담당
// - 데이터 fetching, 상태 관리
// - Presentational에 데이터 전달
class UserListContainer extends React.Component {
  state = { users: [], loading: true };

  componentDidMount() {
    fetchUsers().then(users => {
      this.setState({ users, loading: false });
    });
  }

  handleUserClick = (user) => {
    console.log('Clicked:', user);
  };

  render() {
    if (this.state.loading) return <Spinner />;

    return (
      <UserList
        users={this.state.users}
        onUserClick={this.handleUserClick}
      />
    );
  }
}
```

### Hooks 이후 (현대 방식)

```jsx
// ===== Custom Hook (로직) =====
function useUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUsers().then(users => {
      setUsers(users);
      setLoading(false);
    });
  }, []);

  const handleUserClick = (user) => {
    console.log('Clicked:', user);
  };

  return { users, loading, handleUserClick };
}

// ===== Component (로직 + UI) =====
function UserListPage() {
  const { users, loading, handleUserClick } = useUsers();

  if (loading) return <Spinner />;

  return (
    <ul>
      {users.map(user => (
        <li key={user.id} onClick={() => handleUserClick(user)}>
          {user.name}
        </li>
      ))}
    </ul>
  );
}
```

### 변화 요약

```
[Hooks 이전]
Container (로직)  +  Presentational (UI)
   ↓                       ↓
2개 파일               많은 props 전달

[Hooks 이후]
Custom Hook (로직)  +  Component (Hook 사용 + UI)
   ↓                       ↓
1개 파일               간결한 코드
```

### 여전히 유효한 Presentational

```jsx
// 재사용 가능한 UI 컴포넌트는 여전히 분리
function Button({ variant, onClick, children }) {
  return (
    <button className={`btn btn-${variant}`} onClick={onClick}>
      {children}
    </button>
  );
}

function Card({ title, children }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <div>{children}</div>
    </div>
  );
}
```

---

## Error Boundary

### 개념

- 하위 컴포넌트 트리의 JavaScript 에러를 캐치
- 에러 로깅
- 대체 UI 표시

### 구현 (클래스 컴포넌트만 가능)

```jsx
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  // 에러 발생 시 state 업데이트
  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  // 에러 정보 로깅
  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
    // 에러 리포팅 서비스로 전송
    logErrorToService(error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      // 대체 UI
      return (
        <div>
          <h1>문제가 발생했습니다.</h1>
          <button onClick={() => this.setState({ hasError: false })}>
            다시 시도
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

### 사용

```jsx
// 전체 앱 감싸기
<ErrorBoundary>
  <App />
</ErrorBoundary>

// 특정 영역만 감싸기
<div>
  <h1>대시보드</h1>

  <ErrorBoundary FallbackComponent={WidgetError}>
    <Widget1 />
  </ErrorBoundary>

  <ErrorBoundary FallbackComponent={WidgetError}>
    <Widget2 />
  </ErrorBoundary>
</div>
```

### 잡을 수 있는 에러 vs 없는 에러

```jsx
// ✅ 잡을 수 있음
class MyComponent extends React.Component {
  render() {
    throw new Error('렌더링 에러');  // ✅
    return <div />;
  }

  componentDidMount() {
    throw new Error('생명주기 에러');  // ✅
  }
}

// ❌ 잡을 수 없음
function MyComponent() {
  const handleClick = () => {
    throw new Error('이벤트 핸들러 에러');  // ❌
  };

  useEffect(() => {
    setTimeout(() => {
      throw new Error('비동기 에러');  // ❌
    }, 1000);
  }, []);

  return <button onClick={handleClick}>클릭</button>;
}
```

### 함수형 컴포넌트에서 사용

```jsx
// react-error-boundary 라이브러리 사용
import { ErrorBoundary } from 'react-error-boundary';

function ErrorFallback({ error, resetErrorBoundary }) {
  return (
    <div role="alert">
      <p>에러 발생:</p>
      <pre>{error.message}</pre>
      <button onClick={resetErrorBoundary}>다시 시도</button>
    </div>
  );
}

function App() {
  return (
    <ErrorBoundary
      FallbackComponent={ErrorFallback}
      onReset={() => {
        // 상태 리셋
      }}
    >
      <MyComponent />
    </ErrorBoundary>
  );
}
```

---

## React.forwardRef와 useImperativeHandle

### React.forwardRef

**문제 상황**
```jsx
function Input(props) {
  return <input {...props} />;
}

// ref를 전달할 수 없음
const ref = useRef();
<Input ref={ref} />  // 경고 발생
```

**해결**
```jsx
const Input = React.forwardRef((props, ref) => {
  return <input ref={ref} {...props} />;
});

// ref 전달 가능
const ref = useRef();
<Input ref={ref} />  // ✅

useEffect(() => {
  ref.current.focus();  // DOM 접근 가능
}, []);
```

### useImperativeHandle

**부모에게 노출할 메서드 커스터마이징**

```jsx
const FancyInput = React.forwardRef((props, ref) => {
  const inputRef = useRef();

  useImperativeHandle(ref, () => ({
    // 부모에게 노출할 메서드만 정의
    focus: () => {
      inputRef.current.focus();
    },
    clear: () => {
      inputRef.current.value = '';
    },
    getValue: () => {
      return inputRef.current.value;
    }
  }));

  return <input ref={inputRef} {...props} />;
});

// 사용
function Form() {
  const fancyInputRef = useRef();

  const handleSubmit = () => {
    const value = fancyInputRef.current.getValue();
    console.log(value);
    fancyInputRef.current.clear();
  };

  return (
    <>
      <FancyInput ref={fancyInputRef} />
      <button onClick={handleSubmit}>제출</button>
    </>
  );
}
```

### 실무 활용: 비디오 플레이어

```jsx
const VideoPlayer = React.forwardRef((props, ref) => {
  const videoRef = useRef();

  useImperativeHandle(ref, () => ({
    play: () => videoRef.current.play(),
    pause: () => videoRef.current.pause(),
    seekTo: (time) => {
      videoRef.current.currentTime = time;
    },
    getCurrentTime: () => videoRef.current.currentTime
  }));

  return <video ref={videoRef} {...props} />;
});

function VideoControls() {
  const playerRef = useRef();

  return (
    <>
      <VideoPlayer ref={playerRef} src="video.mp4" />
      <button onClick={() => playerRef.current.play()}>재생</button>
      <button onClick={() => playerRef.current.pause()}>정지</button>
      <button onClick={() => playerRef.current.seekTo(30)}>30초로</button>
    </>
  );
}
```

---

## Portal

### 개념

부모 컴포넌트의 DOM 계층 외부에 있는 DOM 노드로 자식을 렌더링

```jsx
import { createPortal } from 'react-dom';

function Modal({ children }) {
  return createPortal(
    <div className="modal">
      {children}
    </div>,
    document.getElementById('modal-root')  // 다른 DOM 위치
  );
}
```

### HTML 구조

```html
<body>
  <div id="root">
    <!-- React 앱 메인 컨텐츠 -->
    <div class="app">
      <button>모달 열기</button>
    </div>
  </div>

  <div id="modal-root">
    <!-- Portal로 렌더링된 모달 -->
    <div class="modal">...</div>
  </div>
</body>
```

### 사용 시점

**1. Modal/Dialog**
```jsx
function Modal({ isOpen, onClose, children }) {
  if (!isOpen) return null;

  return createPortal(
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>,
    document.body
  );
}
```

**2. Tooltip**
```jsx
function Tooltip({ children, content, targetRef }) {
  const [position, setPosition] = useState({ top: 0, left: 0 });

  useEffect(() => {
    if (targetRef.current) {
      const rect = targetRef.current.getBoundingClientRect();
      setPosition({
        top: rect.bottom + window.scrollY,
        left: rect.left + window.scrollX
      });
    }
  }, [targetRef]);

  return createPortal(
    <div
      className="tooltip"
      style={{ position: 'absolute', ...position }}
    >
      {content}
    </div>,
    document.body
  );
}
```

### 이벤트 버블링 특징

```
DOM 트리: 물리적 위치
<body>
  <div id="root">
    <button onClick={handleClick} />  ← 이벤트 핸들러
  </div>
  <div id="modal-root">
    <button>Portal 버튼</button>  ← 클릭 발생
  </div>
</body>

React 트리: 논리적 위치
<Parent onClick={handleClick}>
  <Portal>
    <button>Portal 버튼</button>  ← React 트리에서는 자식
  </Portal>
</Parent>

→ Portal 버튼 클릭 시 React 트리를 따라 이벤트 버블링
→ Parent의 handleClick 실행됨
```

---

## Suspense와 ErrorBoundary 조합

### 기본 조합

```jsx
import { Suspense } from 'react';

function App() {
  return (
    <ErrorBoundary FallbackComponent={ErrorFallback}>
      <Suspense fallback={<Spinner />}>
        <UserProfile />
      </Suspense>
    </ErrorBoundary>
  );
}

// 흐름:
// 1. 로딩 → Suspense fallback
// 2. 에러 → ErrorBoundary fallback
// 3. 성공 → UserProfile 렌더링
```

### 재사용 가능한 AsyncBoundary

```jsx
function AsyncBoundary({
  children,
  loadingFallback = <Spinner />,
  errorFallback = <ErrorMessage />
}) {
  return (
    <ErrorBoundary FallbackComponent={errorFallback}>
      <Suspense fallback={loadingFallback}>
        {children}
      </Suspense>
    </ErrorBoundary>
  );
}

// 사용
<AsyncBoundary>
  <UserProfile />
</AsyncBoundary>
```

### React Query와 조합

```jsx
import { QueryErrorResetBoundary } from '@tanstack/react-query';

function App() {
  return (
    <QueryErrorResetBoundary>
      {({ reset }) => (
        <ErrorBoundary
          onReset={reset}
          FallbackComponent={ErrorFallback}
        >
          <Suspense fallback={<Spinner />}>
            <UserList />
          </Suspense>
        </ErrorBoundary>
      )}
    </QueryErrorResetBoundary>
  );
}

function UserList() {
  const { data } = useQuery({
    queryKey: ['users'],
    queryFn: fetchUsers,
    suspense: true  // Suspense 모드
  });

  return <ul>{data.map(...)}</ul>;
}
```

---

## TypeScript와 React

### Props 타입 정의

```typescript
interface ButtonProps {
  label: string;
  onClick: () => void;
  disabled?: boolean;
}

function Button({ label, onClick, disabled = false }: ButtonProps) {
  return <button onClick={onClick} disabled={disabled}>{label}</button>;
}
```

### 제네릭 컴포넌트

```typescript
interface ListProps<T> {
  items: T[];
  renderItem: (item: T) => React.ReactNode;
}

function List<T>({ items, renderItem }: ListProps<T>) {
  return (
    <ul>
      {items.map((item, index) => (
        <li key={index}>{renderItem(item)}</li>
      ))}
    </ul>
  );
}

// 사용
interface User {
  id: number;
  name: string;
}

<List<User>
  items={users}
  renderItem={(user) => <span>{user.name}</span>}  // user는 User 타입
/>
```

### 이벤트 핸들러 타입

```typescript
function Form() {
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    console.log(e.target.value);
  };

  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    console.log('clicked');
  };

  return (
    <form onSubmit={handleSubmit}>
      <input onChange={handleChange} />
      <button onClick={handleClick}>제출</button>
    </form>
  );
}
```

---

## 면접 핵심 정리

### 패턴 선택 가이드

```
로직 재사용 필요?
  → Custom Hooks (현재 주류)
  → HOC (레거시, 라이브러리)
  → Render Props (거의 안씀)

컴포넌트 조합 필요?
  → Composition (children, props)
  → Compound Components (Context 기반)

상태 관리 방식?
  → Controlled (실시간 검증, 포맷팅)
  → Uncontrolled (파일 업로드, 대량 폼)

에러 처리?
  → ErrorBoundary + Suspense
  → AsyncBoundary 패턴

DOM 접근?
  → forwardRef + useImperativeHandle
  → 최소한으로 사용

다른 DOM 위치 렌더링?
  → Portal (Modal, Tooltip, Toast)
```

### 면접 예상 질문 대비

1. "Controlled와 Uncontrolled 언제 쓰나요?"
   → 실시간 검증/포맷팅 vs 제출 시에만 값 필요

2. "HOC 대신 왜 Custom Hooks를 쓰나요?"
   → Wrapper Hell 없음, 간결함, 조합 용이

3. "Error Boundary가 못 잡는 에러는?"
   → 이벤트 핸들러, 비동기 코드, 자기 자신 에러

4. "Portal은 언제 쓰나요?"
   → Modal, Tooltip 등 부모 DOM 제약 벗어날 때

5. "forwardRef는 왜 필요한가요?"
   → 함수형 컴포넌트에서 ref 전달하기 위해
