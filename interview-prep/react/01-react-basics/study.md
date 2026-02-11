# 1. React 기초

---

## JSX란?

JSX(JavaScript XML)는 JavaScript의 확장 문법으로, HTML과 유사한 구조로 UI를 작성할 수 있게 해줍니다.

### JSX 변환 과정

```
JSX 코드                      Babel 트랜스파일           실제 실행 코드
┌─────────────────────┐      ────────────────>      ┌─────────────────────────┐
│ <div className="box">│                            │ React.createElement(    │
│   <h1>Hello</h1>    │                            │   'div',                │
│ </div>              │                            │   {className: 'box'},   │
└─────────────────────┘                            │   React.createElement(  │
                                                   │     'h1',               │
                                                   │     null,               │
                                                   │     'Hello'             │
                                                   │   )                     │
                                                   │ )                       │
                                                   └─────────────────────────┘
```

### JSX 기본 문법

```jsx
// 1. JavaScript 표현식 사용 (중괄호 {})
function Welcome({ name }) {
  const greeting = "안녕하세요";
  return <h1>{greeting}, {name}님!</h1>;
}

// 2. 속성(Props) 전달
function Button() {
  return (
    <button
      className="primary"  // HTML의 class가 아닌 className
      onClick={handleClick}  // camelCase 사용
      disabled={isLoading}
    >
      클릭
    </button>
  );
}

// 3. 자식 요소
function App() {
  return (
    <div>
      <Header />
      <Main>
        <Article />
        <Sidebar />
      </Main>
      <Footer />
    </div>
  );
}

// 4. JSX도 표현식이다
const element = <h1>Hello</h1>;
const elements = [
  <li key="1">첫 번째</li>,
  <li key="2">두 번째</li>
];
```

### JSX의 장점

1. **가독성**: HTML과 유사한 구조로 직관적
2. **타입 안정성**: 컴파일 타임에 오류 검출
3. **XSS 방지**: 자동 이스케이프 처리
4. **개발 효율**: HTML과 JavaScript를 함께 작성

---

## 컴포넌트

React 컴포넌트는 UI를 독립적이고 재사용 가능한 조각으로 나눈 것입니다.

### 함수형 컴포넌트 vs 클래스 컴포넌트

```jsx
// ===== 함수형 컴포넌트 (권장) =====
import { useState, useEffect } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    document.title = `카운트: ${count}`;
  }, [count]);

  return (
    <div>
      <p>카운트: {count}</p>
      <button onClick={() => setCount(count + 1)}>
        증가
      </button>
    </div>
  );
}

// ===== 클래스 컴포넌트 (레거시) =====
import React, { Component } from 'react';

class Counter extends Component {
  constructor(props) {
    super(props);
    this.state = { count: 0 };
    this.handleClick = this.handleClick.bind(this);  // this 바인딩 필요
  }

  componentDidMount() {
    document.title = `카운트: ${this.state.count}`;
  }

  componentDidUpdate() {
    document.title = `카운트: ${this.state.count}`;
  }

  handleClick() {
    this.setState({ count: this.state.count + 1 });
  }

  render() {
    return (
      <div>
        <p>카운트: {this.state.count}</p>
        <button onClick={this.handleClick}>증가</button>
      </div>
    );
  }
}
```

### 비교표

| 특징 | 함수형 컴포넌트 | 클래스 컴포넌트 |
|------|----------------|----------------|
| **문법** | 함수로 정의 | class로 정의 |
| **상태 관리** | useState Hook | this.state |
| **생명주기** | useEffect Hook | 생명주기 메서드 |
| **this 바인딩** | 불필요 | 필요 |
| **코드 길이** | 간결 | 상대적으로 장황 |
| **성능** | 약간 유리 | 약간 불리 |
| **테스트** | 쉬움 | 상대적으로 어려움 |
| **공식 권장** | ✅ 권장 | ⚠️ 레거시 |

---

## Props와 State

### 데이터 흐름 다이어그램

```
┌─────────────────────────────────────────┐
│         Parent Component                │
│  ┌──────────────────────────────────┐   │
│  │  state = {                       │   │
│  │    userName: "홍길동",           │   │
│  │    userAge: 25                   │   │
│  │  }                               │   │
│  └──────────────────────────────────┘   │
│           │                              │
│           │ props로 전달                 │
│           ▼                              │
│  ┌──────────────────────────────────┐   │
│  │     Child Component              │   │
│  │  props: {                        │   │
│  │    name: "홍길동",  (읽기 전용)  │   │
│  │    age: 25                       │   │
│  │  }                               │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### Props (Properties)

```jsx
// 부모 컴포넌트
function App() {
  return (
    <UserCard
      name="김철수"
      age={30}
      email="kim@example.com"
      isAdmin={true}
    />
  );
}

// 자식 컴포넌트 - Props 받기
function UserCard({ name, age, email, isAdmin }) {
  // props는 읽기 전용 - 변경 불가
  // name = "다른 이름";  // ❌ 에러!

  return (
    <div className="user-card">
      <h2>{name} {isAdmin && '(관리자)'}</h2>
      <p>나이: {age}세</p>
      <p>이메일: {email}</p>
    </div>
  );
}

// 기본값 설정
function Button({ text = "클릭", onClick }) {
  return <button onClick={onClick}>{text}</button>;
}
```

### State

```jsx
import { useState } from 'react';

function LoginForm() {
  // State 선언
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      await login(email, password);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}  // State 업데이트
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      {error && <p className="error">{error}</p>}
      <button disabled={isLoading}>
        {isLoading ? '로그인 중...' : '로그인'}
      </button>
    </form>
  );
}
```

### Props vs State 비교표

| 구분 | Props | State |
|------|-------|-------|
| **정의** | 부모로부터 전달받는 데이터 | 컴포넌트 내부에서 관리하는 데이터 |
| **변경 가능성** | 불변(immutable) | 가변(mutable) |
| **변경 주체** | 부모 컴포넌트 | 컴포넌트 자신 |
| **변경 방법** | 부모에서 새 props 전달 | setState / useState setter |
| **용도** | 컴포넌트 간 데이터 전달 | 동적 데이터 관리 |
| **리렌더링** | Props 변경 시 | State 변경 시 |

---

## Virtual DOM

### Virtual DOM 동작 원리

```
Step 1: 초기 렌더링
┌─────────────────────────┐
│   React 컴포넌트        │
│   const App = () => (   │
│     <div>               │
│       <h1>Hello</h1>    │
│     </div>              │
│   )                     │
└─────────────────────────┘
          │
          ▼
┌─────────────────────────┐      ┌─────────────────────────┐
│   Virtual DOM Tree      │ ───> │   Real DOM              │
│   {                     │      │   <div>                 │
│     type: 'div',        │      │     <h1>Hello</h1>      │
│     props: {            │      │   </div>                │
│       children: {...}   │      │                         │
│     }                   │      │                         │
│   }                     │      │                         │
└─────────────────────────┘      └─────────────────────────┘

Step 2: State 변경 후 업데이트
┌─────────────────────────┐
│   State Changed!        │
│   text: "Hello" → "Hi"  │
└─────────────────────────┘
          │
          ▼
┌─────────────────────────┐      ┌─────────────────────────┐
│   New Virtual DOM       │      │   Old Virtual DOM       │
│   {                     │      │   {                     │
│     type: 'div',        │      │     type: 'div',        │
│     props: {            │      │     props: {            │
│       children: {       │      │       children: {       │
│         type: 'h1',     │      │         type: 'h1',     │
│         props: {        │      │         props: {        │
│           children: 'Hi'│      │           children:     │
│         }               │      │           'Hello'       │
│       }                 │      │         }               │
│     }                   │      │       }                 │
│   }                     │      │     }                   │
└─────────────────────────┘      └─────────────────────────┘
          │                                   │
          └─────────────┬─────────────────────┘
                        │
                        ▼
            ┌─────────────────────────┐
            │   Diffing Algorithm     │
            │   비교 후 차이점 찾기   │
            │   - h1의 text만 변경됨  │
            └─────────────────────────┘
                        │
                        ▼
            ┌─────────────────────────┐
            │   Real DOM Update       │
            │   h1.textContent = "Hi" │
            │   (최소한의 변경만)     │
            └─────────────────────────┘
```

### Reconciliation (재조정) 알고리즘

```jsx
// 예시: 효율적인 리스트 업데이트
function TodoList({ todos }) {
  return (
    <ul>
      {todos.map(todo => (
        <li key={todo.id}>  {/* key가 중요! */}
          {todo.text}
        </li>
      ))}
    </ul>
  );
}

// ===== key가 없으면 =====
// Before: [A, B, C]
// After:  [A, B, C, D]
// → 모든 li를 비교하고 마지막에 D 추가

// ===== key가 있으면 =====
// Before: [A(key:1), B(key:2), C(key:3)]
// After:  [A(key:1), B(key:2), C(key:3), D(key:4)]
// → key로 빠르게 비교, D만 추가 (O(n) 복잡도)
```

### Virtual DOM의 장점

1. **성능 최적화**: 변경된 부분만 실제 DOM에 반영
2. **배치 업데이트**: 여러 변경사항을 한 번에 처리
3. **크로스 플랫폼**: React Native에서 네이티브 UI로 변환 가능
4. **선언적 프로그래밍**: 개발자는 최종 상태만 정의

---

## 이벤트 처리

### SyntheticEvent

React는 브라우저의 네이티브 이벤트를 래핑한 SyntheticEvent를 사용합니다.

```jsx
function EventExample() {
  // ===== 기본 이벤트 처리 =====
  const handleClick = (e) => {
    e.preventDefault();  // 기본 동작 방지
    e.stopPropagation(); // 이벤트 전파 중단

    console.log(e.type);        // "click"
    console.log(e.target);      // 실제 클릭된 요소
    console.log(e.currentTarget); // 이벤트 핸들러가 연결된 요소
  };

  // ===== 매개변수 전달 =====
  const handleDelete = (id) => {
    console.log(`${id} 삭제`);
  };

  // ===== 폼 제출 =====
  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData);
    console.log(data);
  };

  return (
    <div>
      {/* 방법 1: 인라인 화살표 함수 */}
      <button onClick={(e) => handleClick(e)}>클릭</button>

      {/* 방법 2: 함수 참조 */}
      <button onClick={handleClick}>클릭</button>

      {/* 방법 3: 매개변수 전달 */}
      <button onClick={() => handleDelete(123)}>삭제</button>

      {/* 폼 이벤트 */}
      <form onSubmit={handleSubmit}>
        <input name="username" />
        <button type="submit">제출</button>
      </form>
    </div>
  );
}
```

### 이벤트 위임 (Event Delegation)

```
React 17 이후 이벤트 위임 구조
┌────────────────────────────────────────────┐
│  document                                  │
│  ┌──────────────────────────────────────┐  │
│  │  root (ReactDOM.render 위치)         │  │
│  │  ← 모든 이벤트가 여기에 위임됨       │  │
│  │  ┌────────────────────────────────┐  │  │
│  │  │  <div id="app">                │  │  │
│  │  │    <button>버튼 1</button>     │  │  │
│  │  │    <button>버튼 2</button>     │  │  │
│  │  │    <button>버튼 3</button>     │  │  │
│  │  │  </div>                        │  │  │
│  │  └────────────────────────────────┘  │  │
│  │                                      │  │
│  │  실제로는 각 버튼에 리스너가 없고,   │  │
│  │  root에서 이벤트를 감지하여 처리    │  │
│  └──────────────────────────────────────┘  │
└────────────────────────────────────────────┘
```

### 일반 DOM 이벤트와의 차이

| 구분 | React (SyntheticEvent) | 일반 DOM |
|------|------------------------|----------|
| **네이밍** | camelCase (onClick) | 소문자 (onclick) |
| **값** | 함수 전달 | 문자열 또는 함수 |
| **기본 동작 방지** | preventDefault() 명시 | return false 가능 |
| **크로스 브라우저** | 일관된 동작 보장 | 브라우저별 차이 |
| **이벤트 풀링** | 재사용으로 성능 최적화 | 없음 |

```jsx
// React
<button onClick={handleClick}>클릭</button>

// 일반 DOM
<button onclick="handleClick()">클릭</button>
// 또는
button.addEventListener('click', handleClick);
```

---

## 조건부 렌더링

```jsx
function Dashboard({ user, isLoading, error }) {

  // ===== 1. if-else 문 =====
  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorMessage error={error} />;
  }

  if (!user) {
    return <LoginPrompt />;
  }

  // ===== 2. 삼항 연산자 =====
  return (
    <div>
      {user.isAdmin ? (
        <AdminPanel />
      ) : (
        <UserPanel />
      )}
    </div>
  );
}

function UserProfile({ user }) {
  // ===== 3. 논리 AND (&&) 연산자 =====
  return (
    <div>
      <h1>{user.name}</h1>
      {user.isPremium && <PremiumBadge />}
      {user.notifications.length > 0 && (
        <NotificationBadge count={user.notifications.length} />
      )}

      {/* ⚠️ 주의: 0이 렌더링될 수 있음 */}
      {user.age && <p>나이: {user.age}</p>}
      {/* 개선: 명시적 비교 */}
      {user.age > 0 && <p>나이: {user.age}</p>}
    </div>
  );
}

function StatusDisplay({ status }) {
  // ===== 4. switch 문을 함수로 =====
  const getStatusComponent = () => {
    switch (status) {
      case 'loading':
        return <LoadingSpinner />;
      case 'success':
        return <SuccessMessage />;
      case 'error':
        return <ErrorMessage />;
      case 'idle':
      default:
        return <IdleState />;
    }
  };

  return <div>{getStatusComponent()}</div>;
}

function ComplexConditional({ data }) {
  // ===== 5. 즉시 실행 함수 (IIFE) =====
  return (
    <div>
      {(() => {
        if (data.type === 'video') {
          return <VideoPlayer src={data.url} />;
        } else if (data.type === 'image') {
          return <ImageViewer src={data.url} />;
        } else if (data.type === 'audio') {
          return <AudioPlayer src={data.url} />;
        } else {
          return <TextContent text={data.content} />;
        }
      })()}
    </div>
  );
}
```

---

## 리스트 렌더링과 Key

### Key의 중요성

```
Key가 없을 때 (비효율적):
Before: [A, B, C]
After:  [A, X, B, C]

React의 비교:
  A === A  ✓ (재사용)
  B === X  ✗ (업데이트)
  C === B  ✗ (업데이트)
  없음 === C  (새로 생성)
→ 3번의 DOM 조작!

Key가 있을 때 (효율적):
Before: [A(id:1), B(id:2), C(id:3)]
After:  [A(id:1), X(id:4), B(id:2), C(id:3)]

React의 비교 (key 기반):
  id:1 존재 ✓ (재사용)
  id:4 새로움 (생성)
  id:2 존재 ✓ (재사용)
  id:3 존재 ✓ (재사용)
→ 1번의 DOM 조작! (X만 추가)
```

### 올바른 Key 사용법

```jsx
function TodoList({ todos }) {
  return (
    <ul>
      {/* ✅ 좋음: 고유한 ID 사용 */}
      {todos.map(todo => (
        <li key={todo.id}>
          {todo.text}
        </li>
      ))}

      {/* ❌ 나쁨: 인덱스 사용 (순서가 바뀔 수 있는 경우) */}
      {todos.map((todo, index) => (
        <li key={index}>  {/* 항목 추가/삭제/정렬 시 문제 */}
          {todo.text}
        </li>
      ))}

      {/* ❌ 최악: 랜덤 값 사용 */}
      {todos.map(todo => (
        <li key={Math.random()}>  {/* 매번 새로 생성됨! */}
          {todo.text}
        </li>
      ))}
    </ul>
  );
}

// 인덱스를 key로 사용해도 괜찮은 경우:
// 1. 리스트가 정적이고 변하지 않음
// 2. 항목이 재정렬되지 않음
// 3. 항목이 추가/삭제되지 않음
function StaticList({ items }) {
  return (
    <ul>
      {items.map((item, index) => (
        <li key={index}>{item}</li>  // OK
      ))}
    </ul>
  );
}
```

### Key가 없을 때의 문제점

```jsx
// 문제 상황: 입력 필드가 있는 리스트
function BadExample() {
  const [items, setItems] = useState([
    { id: 1, name: '항목 1' },
    { id: 2, name: '항목 2' }
  ]);

  // 맨 앞에 새 항목 추가
  const addItem = () => {
    setItems([{ id: 3, name: '새 항목' }, ...items]);
  };

  return (
    <>
      <button onClick={addItem}>추가</button>
      {items.map((item, index) => (
        // ❌ key를 index로 사용
        <div key={index}>
          <input defaultValue={item.name} />
        </div>
      ))}
    </>
  );

  // 문제: 새 항목 추가 시 입력 값이 엉킴
  // index 0의 입력 값이 그대로 유지되어 버그 발생
}

function GoodExample() {
  const [items, setItems] = useState([
    { id: 1, name: '항목 1' },
    { id: 2, name: '항목 2' }
  ]);

  const addItem = () => {
    setItems([{ id: Date.now(), name: '새 항목' }, ...items]);
  };

  return (
    <>
      <button onClick={addItem}>추가</button>
      {items.map(item => (
        // ✅ key를 고유 ID로 사용
        <div key={item.id}>
          <input defaultValue={item.name} />
        </div>
      ))}
    </>
  );
}
```

---

## 단방향 데이터 흐름

### 데이터 흐름 구조

```
┌─────────────────────────────────────────────────────┐
│                  App (최상위)                       │
│  ┌───────────────────────────────────────────────┐  │
│  │  state: { users, posts, settings }            │  │
│  └───────────────────────────────────────────────┘  │
│                  │                                   │
│                  │ props ↓ (아래로만 전달)           │
│                  ▼                                   │
│  ┌───────────────────────────────────────────────┐  │
│  │              UserList                         │  │
│  │  props: { users }                             │  │
│  │              │                                 │  │
│  │              │ props ↓                         │  │
│  │              ▼                                 │  │
│  │  ┌────────────────────────────────────────┐   │  │
│  │  │         UserItem                       │   │  │
│  │  │  props: { user, onUpdate }             │   │  │
│  │  │                                        │   │  │
│  │  │  [수정 버튼]                           │   │  │
│  │  │      │                                 │   │  │
│  │  │      │ 콜백 함수 호출 ↑                │   │  │
│  │  └──────┼─────────────────────────────────┘   │  │
│  │         │                                     │  │
│  │         │ 콜백 함수 호출 ↑                    │  │
│  └─────────┼─────────────────────────────────────┘  │
│            │                                        │
│            │ setState 호출 ↑                        │
│            ▼                                        │
│  ┌───────────────────────────────────────────────┐  │
│  │  State 업데이트                                │  │
│  │  → 새로운 props로 리렌더링 ↓                   │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

핵심:
- 데이터는 위에서 아래로만 전달 (props)
- 자식이 부모의 state를 직접 수정할 수 없음
- 콜백 함수를 통해서만 상위 state 변경 가능
```

### 실제 구현 예시

```jsx
// ===== 최상위 컴포넌트 =====
function TodoApp() {
  const [todos, setTodos] = useState([
    { id: 1, text: '리액트 공부', completed: false },
    { id: 2, text: '면접 준비', completed: true }
  ]);

  // 콜백 함수들 (자식에게 전달)
  const addTodo = (text) => {
    const newTodo = {
      id: Date.now(),
      text,
      completed: false
    };
    setTodos([...todos, newTodo]);
  };

  const toggleTodo = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id ? { ...todo, completed: !todo.completed } : todo
    ));
  };

  const deleteTodo = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
  };

  return (
    <div>
      {/* props로 데이터와 콜백 전달 ↓ */}
      <TodoInput onAdd={addTodo} />
      <TodoList
        todos={todos}
        onToggle={toggleTodo}
        onDelete={deleteTodo}
      />
    </div>
  );
}

// ===== 중간 컴포넌트 =====
function TodoList({ todos, onToggle, onDelete }) {
  return (
    <ul>
      {todos.map(todo => (
        // props를 그대로 전달 ↓
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={onToggle}
          onDelete={onDelete}
        />
      ))}
    </ul>
  );
}

// ===== 최하위 컴포넌트 =====
function TodoItem({ todo, onToggle, onDelete }) {
  // props를 직접 수정할 수 없음
  // todo.completed = true;  ❌ 에러!

  // 콜백 함수를 통해서만 변경 요청 ↑
  return (
    <li>
      <input
        type="checkbox"
        checked={todo.completed}
        onChange={() => onToggle(todo.id)}  // 콜백 호출 ↑
      />
      <span>{todo.text}</span>
      <button onClick={() => onDelete(todo.id)}>삭제</button>
    </li>
  );
}

function TodoInput({ onAdd }) {
  const [text, setText] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (text.trim()) {
      onAdd(text);  // 콜백 호출 ↑
      setText('');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={text}
        onChange={(e) => setText(e.target.value)}
      />
      <button>추가</button>
    </form>
  );
}
```

### 양방향 바인딩과의 비교

```jsx
// ===== Vue (양방향 바인딩) =====
// 부모와 자식이 같은 데이터를 공유하고 양쪽에서 수정 가능
<template>
  <child-component v-model="sharedData" />
</template>

// ===== React (단방향 흐름) =====
// 명시적으로 props와 콜백을 통해서만 통신
function Parent() {
  const [data, setData] = useState('');

  return (
    <ChildComponent
      value={data}              // 데이터 전달 ↓
      onChange={setData}        // 변경 함수 전달 ↓
    />
  );
}

function ChildComponent({ value, onChange }) {
  return (
    <input
      value={value}
      onChange={(e) => onChange(e.target.value)}  // 콜백 호출 ↑
    />
  );
}
```

### 단방향 흐름의 장점

1. **예측 가능성**: 데이터 흐름이 명확하여 버그 추적이 쉬움
2. **디버깅 용이**: 데이터 변경 지점이 명확함
3. **유지보수성**: 컴포넌트 간 의존성이 명확함
4. **테스트 용이**: 입력(props)과 출력이 명확하여 테스트하기 쉬움

---

## Fragment

### Fragment가 필요한 이유

```jsx
// ❌ 문제: 불필요한 div 래퍼
function Table() {
  return (
    <table>
      <tbody>
        <TableRow />
      </tbody>
    </table>
  );
}

function TableRow() {
  return (
    <div>  {/* ❌ tbody 안에 div는 유효하지 않은 HTML! */}
      <td>셀 1</td>
      <td>셀 2</td>
    </div>
  );
}

// ✅ 해결: Fragment 사용
function TableRow() {
  return (
    <>  {/* Fragment: DOM에 렌더링되지 않음 */}
      <td>셀 1</td>
      <td>셀 2</td>
    </>
  );
}

// 또는 명시적으로
import { Fragment } from 'react';

function TableRow() {
  return (
    <Fragment>
      <td>셀 1</td>
      <td>셀 2</td>
    </Fragment>
  );
}
```

### Fragment 사용 사례

```jsx
// ===== 1. 리스트에서 key가 필요한 경우 =====
function Glossary({ items }) {
  return (
    <dl>
      {items.map(item => (
        // 단축 문법(<>)은 key를 받을 수 없음
        <Fragment key={item.id}>
          <dt>{item.term}</dt>
          <dd>{item.description}</dd>
        </Fragment>
      ))}
    </dl>
  );
}

// ===== 2. Flexbox/Grid 레이아웃 =====
function Layout() {
  return (
    <div className="flex-container">
      {/* div로 감싸면 flex 레이아웃이 깨짐 */}
      <>
        <Header />
        <Main />
        <Footer />
      </>
    </div>
  );
}

// ===== 3. 조건부 렌더링 =====
function UserInfo({ user }) {
  return (
    <div>
      {user.isLoggedIn ? (
        <>
          <h1>환영합니다, {user.name}님</h1>
          <p>마지막 로그인: {user.lastLogin}</p>
          <button>로그아웃</button>
        </>
      ) : (
        <button>로그인</button>
      )}
    </div>
  );
}
```

### Fragment vs div 비교

```
=== div 사용 시 ===
<div id="root">
  <div class="app">
    <div>              ← 불필요한 래퍼
      <h1>제목</h1>
      <p>내용</p>
    </div>
  </div>
</div>

CSS 셀렉터: .app > div > h1  (복잡함)
DOM 노드: 1개 추가

=== Fragment 사용 시 ===
<div id="root">
  <div class="app">
    <h1>제목</h1>     ← 깔끔한 구조
    <p>내용</p>
  </div>
</div>

CSS 셀렉터: .app > h1  (간단함)
DOM 노드: 추가 없음 (성능 향상)
```

---

## 리렌더링

### 리렌더링 발생 조건

```
리렌더링 트리거:

1. State 변경
   ┌──────────────┐
   │  Component   │
   │  [setState]  │ ──> 리렌더링
   └──────────────┘

2. Props 변경
   ┌──────────────┐
   │    Parent    │
   │  (state 변경) │
   └──────┬───────┘
          │ props 변경
          ▼
   ┌──────────────┐
   │    Child     │ ──> 리렌더링
   └──────────────┘

3. 부모 컴포넌트 리렌더링
   ┌──────────────┐
   │    Parent    │ ──> 리렌더링
   └──────┬───────┘
          │ 자동으로
          ▼
   ┌──────────────┐
   │    Child     │ ──> 리렌더링 (props 변경 없어도!)
   └──────────────┘

4. Context 값 변경
   ┌──────────────────┐
   │  Context Provider│
   │  (value 변경)    │
   └────────┬─────────┘
            │ context 구독
            ▼
   ┌──────────────────┐
   │  Consumer        │ ──> 리렌더링
   └──────────────────┘
```

### 리렌더링 최적화

```jsx
import { memo, useMemo, useCallback } from 'react';

// ===== 1. React.memo: 컴포넌트 메모이제이션 =====
const ExpensiveComponent = memo(function ExpensiveComponent({ data }) {
  console.log('렌더링!');
  return <div>{data}</div>;
});

function Parent() {
  const [count, setCount] = useState(0);
  const [text, setText] = useState('');

  return (
    <div>
      <button onClick={() => setCount(count + 1)}>카운트: {count}</button>
      <input value={text} onChange={e => setText(e.target.value)} />

      {/* text가 변경되어도 data가 같으면 리렌더링 안 됨 */}
      <ExpensiveComponent data="고정값" />
    </div>
  );
}

// ===== 2. useMemo: 값 메모이제이션 =====
function SearchResults({ query, items }) {
  // 비용이 큰 계산 결과를 캐싱
  const filteredItems = useMemo(() => {
    console.log('필터링 실행');
    return items.filter(item =>
      item.name.toLowerCase().includes(query.toLowerCase())
    );
  }, [query, items]);  // query나 items가 변경될 때만 재계산

  return (
    <ul>
      {filteredItems.map(item => (
        <li key={item.id}>{item.name}</li>
      ))}
    </ul>
  );
}

// ===== 3. useCallback: 함수 메모이제이션 =====
function TodoList({ todos }) {
  const [filter, setFilter] = useState('all');

  // 함수를 메모이제이션하여 참조 동일성 유지
  const handleToggle = useCallback((id) => {
    console.log('토글:', id);
    // toggle 로직
  }, []);  // 의존성이 없으므로 한 번만 생성

  return (
    <div>
      <FilterButtons onFilterChange={setFilter} />
      {todos.map(todo => (
        // handleToggle이 변경되지 않으므로 TodoItem이 불필요하게 리렌더링되지 않음
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={handleToggle}
        />
      ))}
    </div>
  );
}

const TodoItem = memo(function TodoItem({ todo, onToggle }) {
  console.log('TodoItem 렌더링:', todo.id);
  return (
    <li onClick={() => onToggle(todo.id)}>
      {todo.text}
    </li>
  );
});
```

### 리렌더링 디버깅

```jsx
// React DevTools Profiler 사용
// 또는 커스텀 훅으로 렌더링 추적

function useRenderCount(componentName) {
  const renderCount = useRef(0);

  useEffect(() => {
    renderCount.current += 1;
    console.log(`${componentName} 렌더링 횟수:`, renderCount.current);
  });

  return renderCount.current;
}

function MyComponent() {
  const renderCount = useRenderCount('MyComponent');

  return <div>렌더링 횟수: {renderCount}</div>;
}

// props 변경 감지
function useWhyDidYouUpdate(name, props) {
  const previousProps = useRef();

  useEffect(() => {
    if (previousProps.current) {
      const allKeys = Object.keys({ ...previousProps.current, ...props });
      const changedProps = {};

      allKeys.forEach(key => {
        if (previousProps.current[key] !== props[key]) {
          changedProps[key] = {
            from: previousProps.current[key],
            to: props[key]
          };
        }
      });

      if (Object.keys(changedProps).length > 0) {
        console.log('[why-did-you-update]', name, changedProps);
      }
    }

    previousProps.current = props;
  });
}
```

---

## 실무 연결

### 제어 컴포넌트로 폼 관리

```jsx
function SignupForm() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    agreeTerms: false
  });

  const [errors, setErrors] = useState({});

  // 입력값 변경 핸들러 (재사용 가능)
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));

    // 입력 시 에러 제거
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  // 실시간 유효성 검사
  const validate = () => {
    const newErrors = {};

    if (!formData.email.includes('@')) {
      newErrors.email = '유효한 이메일을 입력하세요';
    }

    if (formData.password.length < 8) {
      newErrors.password = '비밀번호는 8자 이상이어야 합니다';
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '비밀번호가 일치하지 않습니다';
    }

    if (!formData.agreeTerms) {
      newErrors.agreeTerms = '약관에 동의해주세요';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      await signup(formData);
      alert('가입 완료!');
    } catch (error) {
      setErrors({ submit: error.message });
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <input
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="이메일"
        />
        {errors.email && <span className="error">{errors.email}</span>}
      </div>

      <div>
        <input
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          placeholder="비밀번호"
        />
        {errors.password && <span className="error">{errors.password}</span>}
      </div>

      <div>
        <input
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
          placeholder="비밀번호 확인"
        />
        {errors.confirmPassword && <span className="error">{errors.confirmPassword}</span>}
      </div>

      <div>
        <label>
          <input
            type="checkbox"
            name="agreeTerms"
            checked={formData.agreeTerms}
            onChange={handleChange}
          />
          약관에 동의합니다
        </label>
        {errors.agreeTerms && <span className="error">{errors.agreeTerms}</span>}
      </div>

      {errors.submit && <div className="error">{errors.submit}</div>}

      <button type="submit">가입하기</button>
    </form>
  );
}
```

### 컴포넌트 설계 패턴

```jsx
// ===== Presentational / Container 패턴 =====

// Presentational: UI만 담당 (재사용 가능)
function UserCard({ user, onEdit, onDelete }) {
  return (
    <div className="user-card">
      <img src={user.avatar} alt={user.name} />
      <h3>{user.name}</h3>
      <p>{user.email}</p>
      <button onClick={onEdit}>수정</button>
      <button onClick={onDelete}>삭제</button>
    </div>
  );
}

// Container: 로직 담당
function UserCardContainer({ userId }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchUser(userId).then(data => {
      setUser(data);
      setIsLoading(false);
    });
  }, [userId]);

  const handleEdit = () => {
    // 수정 로직
  };

  const handleDelete = () => {
    // 삭제 로직
  };

  if (isLoading) return <LoadingSpinner />;
  if (!user) return <ErrorMessage />;

  return (
    <UserCard
      user={user}
      onEdit={handleEdit}
      onDelete={handleDelete}
    />
  );
}
```

---

## 면접 핵심 정리

**Q: JSX를 사용하는 이유는?**
> JavaScript 안에서 HTML처럼 UI를 작성할 수 있어 가독성이 좋고, React.createElement()의 syntactic sugar로 컴파일 타임에 오류를 검출할 수 있으며, XSS 공격을 자동으로 방지합니다.

**Q: Virtual DOM이 실제 DOM보다 빠른 이유는?**
> Virtual DOM 자체가 빠른 것이 아니라, Diffing 알고리즘으로 변경된 부분만 찾아 실제 DOM에 최소한으로 반영하기 때문입니다. 배치 업데이트로 여러 변경을 한 번에 처리하여 성능을 최적화합니다.

**Q: Props와 State의 가장 큰 차이는?**
> Props는 부모로부터 받는 읽기 전용 데이터이고, State는 컴포넌트가 관리하는 변경 가능한 데이터입니다. Props 변경은 부모가 하고, State 변경은 setState/useState로 합니다.

**Q: key를 사용하는 이유는?**
> React가 리스트의 어떤 항목이 변경/추가/삭제되었는지 효율적으로 식별하기 위해서입니다. key가 없으면 모든 항목을 비교해야 하지만, key가 있으면 O(n) 복잡도로 빠르게 찾을 수 있습니다.

**Q: 함수형 컴포넌트를 사용하는 이유는?**
> 코드가 간결하고, Hooks를 통해 로직 재사용이 쉬우며, this 바인딩이 필요 없어 실수가 적고, 테스트가 쉽습니다. React 공식 문서에서도 권장하는 방식입니다.

**Q: 단방향 데이터 흐름의 장점은?**
> 데이터 흐름이 명확하여 버그 추적이 쉽고, 상태 변화의 원인을 파악하기 용이하며, 애플리케이션이 복잡해져도 예측 가능한 동작을 보장합니다.

**Q: 리렌더링 최적화 방법은?**
> React.memo로 컴포넌트 메모이제이션, useMemo로 값 메모이제이션, useCallback으로 함수 메모이제이션을 사용합니다. 또한 key를 올바르게 사용하고, 상태를 적절히 분리하여 불필요한 리렌더링을 방지합니다.

**Q: Fragment를 사용하는 이유는?**
> 불필요한 DOM 노드를 추가하지 않고 여러 요소를 그룹화할 수 있어, HTML 구조가 깔끔해지고 CSS 레이아웃이 깨지지 않으며, 성능도 향상됩니다.
