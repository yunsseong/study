# React 컴포넌트 패턴 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Controlled Component와 Uncontrolled Component의 차이점을 설명하고, 각각 언제 사용하는 것이 적합한지 말씀해주세요.

> **Controlled Component (제어 컴포넌트)**
> - React state가 "single source of truth"로 작동
> - 폼 데이터를 React 컴포넌트의 state로 관리
> - 모든 입력 변경이 이벤트 핸들러를 통해 state 업데이트
>
> ```jsx
> function ControlledInput() {
>   const [value, setValue] = useState('');
>
>   return (
>     <input
>       value={value}
>       onChange={(e) => setValue(e.target.value)}
>     />
>   );
> }
> ```
>
> **Uncontrolled Component (비제어 컴포넌트)**
> - DOM 자체가 데이터의 source of truth
> - ref를 사용하여 필요할 때 DOM에서 값을 가져옴
> - React state를 거치지 않음
>
> ```jsx
> function UncontrolledInput() {
>   const inputRef = useRef(null);
>
>   const handleSubmit = () => {
>     console.log(inputRef.current.value);
>   };
>
>   return <input ref={inputRef} defaultValue="초기값" />;
> }
> ```
>
> **사용 시점**
> - **Controlled**: 실시간 유효성 검사, 조건부 활성화, 포맷팅이 필요할 때
> - **Uncontrolled**: 파일 업로드, 폼 제출 시에만 값이 필요할 때, 성능 최적화가 중요할 때

---

**Q2.** React에서 합성(Composition)과 상속(Inheritance)의 차이점은 무엇이며, React가 상속보다 합성을 권장하는 이유는 무엇인가요?

> **합성(Composition)**
> - 컴포넌트를 조합하여 새로운 컴포넌트를 만드는 방식
> - `children` prop이나 여러 prop을 통해 컴포넌트를 전달
>
> ```jsx
> // 일반적인 합성
> function Dialog({ title, children }) {
>   return (
>     <div className="dialog">
>       <h1>{title}</h1>
>       <div className="content">{children}</div>
>     </div>
>   );
> }
>
> // 특수화 (Specialization)
> function WelcomeDialog() {
>   return (
>     <Dialog title="환영합니다">
>       <p>우리 사이트에 방문해주셔서 감사합니다!</p>
>     </Dialog>
>   );
> }
> ```
>
> **상속(Inheritance)**
> - 클래스 컴포넌트에서 다른 컴포넌트를 상속받는 방식
> - React에서는 권장하지 않음
>
> **React가 합성을 권장하는 이유**
> 1. **유연성**: 런타임에 동적으로 컴포넌트 조합 가능
> 2. **재사용성**: 작은 단위의 컴포넌트를 여러 곳에서 재사용
> 3. **명확성**: 컴포넌트 간 관계가 명시적으로 드러남
> 4. **함수형 프로그래밍**: Hooks와 함수형 컴포넌트와 잘 어울림
> 5. **Props drilling 해결**: Context, children 등으로 유연하게 해결

---

**Q3.** HOC(Higher-Order Component) 패턴이 무엇인지 설명하고, 간단한 예시를 들어주세요.

> **HOC (Higher-Order Component)**
> - 컴포넌트를 인자로 받아서 새로운 컴포넌트를 반환하는 함수
> - 컴포넌트 로직을 재사용하기 위한 고급 패턴
> - "컴포넌트를 위한 고차 함수"
>
> ```jsx
> // HOC 정의
> function withLoading(Component) {
>   return function WithLoadingComponent({ isLoading, ...props }) {
>     if (isLoading) {
>       return <div>로딩 중...</div>;
>     }
>     return <Component {...props} />;
>   };
> }
>
> // 사용
> function UserList({ users }) {
>   return (
>     <ul>
>       {users.map(user => <li key={user.id}>{user.name}</li>)}
>     </ul>
>   );
> }
>
> const UserListWithLoading = withLoading(UserList);
>
> // 렌더링
> <UserListWithLoading isLoading={loading} users={users} />
> ```
>
> **실무 예시**
> ```jsx
> // 인증 체크 HOC
> function withAuth(Component) {
>   return function WithAuthComponent(props) {
>     const { isAuthenticated } = useAuth();
>
>     if (!isAuthenticated) {
>       return <Navigate to="/login" />;
>     }
>
>     return <Component {...props} />;
>   };
> }
>
> const ProtectedDashboard = withAuth(Dashboard);
> ```
>
> **특징**
> - 원본 컴포넌트를 수정하지 않음 (순수 함수)
> - 여러 HOC를 조합 가능 (compose)
> - displayName 설정 권장

---

**Q4.** Render Props 패턴에 대해 설명하고, 어떤 문제를 해결하기 위해 사용되는지 말씀해주세요.

> **Render Props 패턴**
> - 함수를 prop으로 전달하여, 무엇을 렌더링할지 컴포넌트에게 알려주는 패턴
> - "render" 또는 "children"이라는 이름의 prop으로 함수를 전달
>
> ```jsx
> // Render Props를 사용한 마우스 추적 컴포넌트
> class MouseTracker extends React.Component {
>   state = { x: 0, y: 0 };
>
>   handleMouseMove = (event) => {
>     this.setState({
>       x: event.clientX,
>       y: event.clientY
>     });
>   };
>
>   render() {
>     return (
>       <div onMouseMove={this.handleMouseMove}>
>         {this.props.render(this.state)}
>       </div>
>     );
>   }
> }
>
> // 사용
> function App() {
>   return (
>     <MouseTracker
>       render={({ x, y }) => (
>         <h1>마우스 위치: ({x}, {y})</h1>
>       )}
>     />
>   );
> }
> ```
>
> **children을 함수로 사용하는 패턴**
> ```jsx
> function DataFetcher({ url, children }) {
>   const [data, setData] = useState(null);
>   const [loading, setLoading] = useState(true);
>
>   useEffect(() => {
>     fetch(url)
>       .then(res => res.json())
>       .then(data => {
>         setData(data);
>         setLoading(false);
>       });
>   }, [url]);
>
>   return children({ data, loading });
> }
>
> // 사용
> <DataFetcher url="/api/users">
>   {({ data, loading }) => (
>     loading ? <Spinner /> : <UserList users={data} />
>   )}
> </DataFetcher>
> ```
>
> **해결하는 문제**
> - 로직 재사용 (HOC의 대안)
> - Props 이름 충돌 방지
> - 렌더링 제어권을 사용하는 쪽에 부여
> - 더 명시적인 데이터 흐름

---

**Q5.** Error Boundary가 무엇이고, 어떻게 구현하며, 어떤 에러를 잡을 수 있는지 설명해주세요.

> **Error Boundary**
> - 하위 컴포넌트 트리에서 발생한 JavaScript 에러를 캐치하고, 에러를 로깅하며, 대체 UI를 보여주는 React 컴포넌트
> - 현재는 클래스 컴포넌트로만 구현 가능
>
> ```jsx
> class ErrorBoundary extends React.Component {
>   constructor(props) {
>     super(props);
>     this.state = { hasError: false, error: null };
>   }
>
>   // 에러 발생 시 상태 업데이트
>   static getDerivedStateFromError(error) {
>     return { hasError: true };
>   }
>
>   // 에러 정보 로깅
>   componentDidCatch(error, errorInfo) {
>     console.error('Error caught:', error, errorInfo);
>     // 에러 리포팅 서비스로 전송
>     logErrorToService(error, errorInfo);
>   }
>
>   render() {
>     if (this.state.hasError) {
>       return (
>         <div>
>           <h1>문제가 발생했습니다.</h1>
>           <button onClick={() => this.setState({ hasError: false })}>
>             다시 시도
>           </button>
>         </div>
>       );
>     }
>
>     return this.props.children;
>   }
> }
>
> // 사용
> <ErrorBoundary>
>   <MyWidget />
> </ErrorBoundary>
> ```
>
> **잡을 수 있는 에러**
> - 렌더링 중 발생한 에러
> - 생명주기 메서드에서 발생한 에러
> - 하위 트리의 constructor에서 발생한 에러
>
> **잡을 수 없는 에러**
> - 이벤트 핸들러 내부의 에러 (try-catch 사용)
> - 비동기 코드 (setTimeout, Promise 등)
> - 서버 사이드 렌더링
> - Error Boundary 자체에서 발생한 에러
>
> **실무 패턴**
> ```jsx
> // 페이지별 Error Boundary
> <ErrorBoundary FallbackComponent={PageError}>
>   <Routes>
>     <Route path="/dashboard" element={<Dashboard />} />
>   </Routes>
> </ErrorBoundary>
>
> // 컴포넌트별 세밀한 Error Boundary
> <div>
>   <ErrorBoundary FallbackComponent={WidgetError}>
>     <Widget1 />
>   </ErrorBoundary>
>   <ErrorBoundary FallbackComponent={WidgetError}>
>     <Widget2 />
>   </ErrorBoundary>
> </div>
> ```

---

## 비교/구분 (6~9)

**Q6.** HOC, Render Props, Custom Hooks의 차이점과 각각의 장단점을 비교해주세요. 현재는 어떤 패턴이 주로 사용되나요?

> | 패턴 | 장점 | 단점 | 현재 사용 |
> |------|------|------|-----------|
> | **HOC** | - 여러 컴포넌트에 동일 로직 적용 용이<br>- compose로 조합 가능 | - Wrapper Hell<br>- Props 충돌<br>- ref 전달 복잡 | 감소 추세 |
> | **Render Props** | - 렌더링 제어권이 사용자에게<br>- Props 이름 충돌 없음 | - 중첩 시 Callback Hell<br>- 코드 가독성 저하 | 감소 추세 |
> | **Custom Hooks** | - 로직과 UI 완전 분리<br>- 조합 용이<br>- 코드 간결 | - 클래스 컴포넌트 사용 불가<br>- Rules of Hooks 준수 필요 | **주류** |
>
> **HOC 예시**
> ```jsx
> const EnhancedComponent = withAuth(withLoading(withData(MyComponent)));
> // Wrapper Hell 발생
> ```
>
> **Render Props 예시**
> ```jsx
> <DataProvider>
>   {data => (
>     <ThemeProvider>
>       {theme => (
>         <AuthProvider>
>           {auth => <Component data={data} theme={theme} auth={auth} />}
>         </AuthProvider>
>       )}
>     </ThemeProvider>
>   )}
> </DataProvider>
> // Callback Hell 발생
> ```
>
> **Custom Hooks 예시**
> ```jsx
> function MyComponent() {
>   const data = useData();
>   const theme = useTheme();
>   const auth = useAuth();
>
>   // 간결하고 명확
>   return <div>...</div>;
> }
> ```
>
> **현재 추세**
> - **Custom Hooks가 주류**: 함수형 컴포넌트 시대에 가장 적합
> - HOC는 라이브러리에서 여전히 사용 (react-redux의 connect 등)
> - Render Props는 특수한 경우에만 사용

---

**Q7.** Container/Presentational 패턴이 무엇이며, 이 패턴이 Hooks 도입 이후 어떻게 변화했는지 설명해주세요.

> **Container/Presentational 패턴**
> - 로직과 UI를 분리하는 디자인 패턴
>
> **Presentational Component (프레젠테이셔널)**
> - UI 렌더링에만 집중
> - props를 통해 데이터와 콜백 받음
> - 상태를 거의 가지지 않음
> - 재사용 가능
>
> ```jsx
> // Presentational
> function UserList({ users, onUserClick }) {
>   return (
>     <ul>
>       {users.map(user => (
>         <li key={user.id} onClick={() => onUserClick(user)}>
>           {user.name}
>         </li>
>       ))}
>     </ul>
>   );
> }
> ```
>
> **Container Component (컨테이너)**
> - 데이터 fetching, 상태 관리
> - Presentational 컴포넌트에 데이터 전달
> - 비즈니스 로직 처리
>
> ```jsx
> // Container (Hooks 이전)
> class UserListContainer extends React.Component {
>   state = { users: [], loading: true };
>
>   componentDidMount() {
>     fetchUsers().then(users => {
>       this.setState({ users, loading: false });
>     });
>   }
>
>   handleUserClick = (user) => {
>     // 로직 처리
>   };
>
>   render() {
>     return (
>       <UserList
>         users={this.state.users}
>         onUserClick={this.handleUserClick}
>       />
>     );
>   }
> }
> ```
>
> **Hooks 도입 후 변화**
> ```jsx
> // Custom Hook으로 로직 분리
> function useUsers() {
>   const [users, setUsers] = useState([]);
>   const [loading, setLoading] = useState(true);
>
>   useEffect(() => {
>     fetchUsers().then(users => {
>       setUsers(users);
>       setLoading(false);
>     });
>   }, []);
>
>   const handleUserClick = (user) => {
>     // 로직 처리
>   };
>
>   return { users, loading, handleUserClick };
> }
>
> // 하나의 컴포넌트에서 로직과 UI 모두 처리
> function UserListPage() {
>   const { users, loading, handleUserClick } = useUsers();
>
>   if (loading) return <Spinner />;
>
>   return (
>     <ul>
>       {users.map(user => (
>         <li key={user.id} onClick={() => handleUserClick(user)}>
>           {user.name}
>         </li>
>       ))}
>     </ul>
>   );
> }
> ```
>
> **변화 요약**
> - **이전**: Container(로직) + Presentational(UI) 분리
> - **이후**: Custom Hook(로직) + Component(Hook 사용 + UI)
> - 파일 수 감소, 코드 간결화
> - 재사용 가능한 UI는 여전히 Presentational로 분리

---

**Q8.** `children` prop을 활용하는 방법과, 이를 통한 컴포넌트 합성 패턴을 설명해주세요.

> **children prop 기본**
> - 컴포넌트 태그 사이의 내용을 props.children으로 접근
> - 컴포넌트 합성의 핵심 도구
>
> ```jsx
> function Card({ children }) {
>   return (
>     <div className="card">
>       {children}
>     </div>
>   );
> }
>
> // 사용
> <Card>
>   <h2>제목</h2>
>   <p>내용</p>
> </Card>
> ```
>
> **여러 슬롯 패턴**
> ```jsx
> function Dialog({ title, footer, children }) {
>   return (
>     <div className="dialog">
>       <div className="dialog-header">{title}</div>
>       <div className="dialog-body">{children}</div>
>       <div className="dialog-footer">{footer}</div>
>     </div>
>   );
> }
>
> // 사용
> <Dialog
>   title={<h1>알림</h1>}
>   footer={<button>닫기</button>}
> >
>   <p>메시지 내용</p>
> </Dialog>
> ```
>
> **children 조작**
> ```jsx
> function List({ children }) {
>   // children을 배열로 변환하여 조작
>   const items = React.Children.toArray(children);
>
>   return (
>     <ul>
>       {items.map((child, index) => (
>         <li key={index}>
>           {/* 각 child에 props 추가 */}
>           {React.cloneElement(child, { index })}
>         </li>
>       ))}
>     </ul>
>   );
> }
> ```
>
> **Layout 합성 패턴**
> ```jsx
> function Layout({ sidebar, children }) {
>   return (
>     <div className="layout">
>       <aside className="sidebar">{sidebar}</aside>
>       <main className="content">{children}</main>
>     </div>
>   );
> }
>
> // 사용
> <Layout sidebar={<Navigation />}>
>   <Dashboard />
> </Layout>
> ```
>
> **Specialization (특수화)**
> ```jsx
> function Dialog({ children }) {
>   return <div className="dialog">{children}</div>;
> }
>
> function WelcomeDialog() {
>   return (
>     <Dialog>
>       <h1>환영합니다</h1>
>       <p>방문해주셔서 감사합니다!</p>
>     </Dialog>
>   );
> }
> ```
>
> **장점**
> - 유연한 컴포넌트 설계
> - Props drilling 회피
> - 재사용성 향상
> - 명확한 컴포넌트 구조

---

**Q9.** Controlled Component와 Uncontrolled Component를 각각 언제 사용해야 하는지 실무 관점에서 설명해주세요.

> **Controlled Component 사용 시점**
>
> 1. **실시간 유효성 검사**
> ```jsx
> function EmailInput() {
>   const [email, setEmail] = useState('');
>   const [error, setError] = useState('');
>
>   const handleChange = (e) => {
>     const value = e.target.value;
>     setEmail(value);
>
>     // 실시간 검증
>     if (!value.includes('@')) {
>       setError('올바른 이메일 형식이 아닙니다');
>     } else {
>       setError('');
>     }
>   };
>
>   return (
>     <div>
>       <input value={email} onChange={handleChange} />
>       {error && <span>{error}</span>}
>     </div>
>   );
> }
> ```
>
> 2. **입력 값 포맷팅**
> ```jsx
> function PhoneInput() {
>   const [phone, setPhone] = useState('');
>
>   const handleChange = (e) => {
>     const value = e.target.value.replace(/\D/g, ''); // 숫자만
>     const formatted = value.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
>     setPhone(formatted);
>   };
>
>   return <input value={phone} onChange={handleChange} />;
> }
> ```
>
> 3. **조건부 활성화**
> ```jsx
> function Form() {
>   const [agreed, setAgreed] = useState(false);
>   const [username, setUsername] = useState('');
>
>   return (
>     <>
>       <input
>         value={username}
>         onChange={(e) => setUsername(e.target.value)}
>         disabled={!agreed}  // 동의 전까지 비활성화
>       />
>       <label>
>         <input
>           type="checkbox"
>           checked={agreed}
>           onChange={(e) => setAgreed(e.target.checked)}
>         />
>         약관 동의
>       </label>
>     </>
>   );
> }
> ```
>
> **Uncontrolled Component 사용 시점**
>
> 1. **파일 업로드**
> ```jsx
> function FileUpload() {
>   const fileInputRef = useRef(null);
>
>   const handleSubmit = (e) => {
>     e.preventDefault();
>     const file = fileInputRef.current.files[0];
>     // 파일은 controlled로 만들 수 없음
>     uploadFile(file);
>   };
>
>   return (
>     <form onSubmit={handleSubmit}>
>       <input type="file" ref={fileInputRef} />
>       <button type="submit">업로드</button>
>     </form>
>   );
> }
> ```
>
> 2. **대량의 폼 필드 (성능 최적화)**
> ```jsx
> function LargeForm() {
>   const formRef = useRef(null);
>
>   const handleSubmit = (e) => {
>     e.preventDefault();
>     const formData = new FormData(formRef.current);
>     // 제출 시에만 값 수집
>     const data = Object.fromEntries(formData);
>     submitForm(data);
>   };
>
>   return (
>     <form ref={formRef} onSubmit={handleSubmit}>
>       {/* 100개의 input 필드 */}
>       <input name="field1" defaultValue="" />
>       <input name="field2" defaultValue="" />
>       {/* ... */}
>       <button type="submit">제출</button>
>     </form>
>   );
> }
> ```
>
> 3. **서드파티 라이브러리 통합**
> ```jsx
> function RichTextEditor() {
>   const editorRef = useRef(null);
>
>   useEffect(() => {
>     // Quill, TinyMCE 등 외부 라이브러리
>     const editor = new Quill(editorRef.current);
>
>     return () => editor.destroy();
>   }, []);
>
>   return <div ref={editorRef} />;
> }
> ```
>
> **의사결정 가이드**
> ```
> 실시간 검증/포맷팅 필요? → Controlled
> 조건부 렌더링/활성화 필요? → Controlled
> 파일 업로드? → Uncontrolled
> 대량 폼 (100+ 필드)? → Uncontrolled
> 제출 시에만 값 필요? → Uncontrolled
> 외부 라이브러리 통합? → Uncontrolled
> ```

---

## 심화/실무 (10~12)

**Q10.** Compound Components 패턴이 무엇인지 설명하고, Context API를 활용한 구현 예시를 들어주세요.

> **Compound Components 패턴**
> - 여러 컴포넌트가 함께 작동하여 하나의 기능을 구성하는 패턴
> - 각 컴포넌트는 독립적으로는 의미가 없지만, 함께 사용될 때 강력함
> - HTML의 `<select>`와 `<option>` 관계와 유사
>
> **기본 구조**
> ```jsx
> // Context로 상태 공유
> const TabContext = React.createContext();
>
> function Tabs({ children, defaultTab }) {
>   const [activeTab, setActiveTab] = useState(defaultTab);
>
>   return (
>     <TabContext.Provider value={{ activeTab, setActiveTab }}>
>       <div className="tabs">{children}</div>
>     </TabContext.Provider>
>   );
> }
>
> function TabList({ children }) {
>   return <div className="tab-list">{children}</div>;
> }
>
> function Tab({ id, children }) {
>   const { activeTab, setActiveTab } = useContext(TabContext);
>   const isActive = activeTab === id;
>
>   return (
>     <button
>       className={isActive ? 'tab active' : 'tab'}
>       onClick={() => setActiveTab(id)}
>     >
>       {children}
>     </button>
>   );
> }
>
> function TabPanel({ id, children }) {
>   const { activeTab } = useContext(TabContext);
>
>   if (activeTab !== id) return null;
>
>   return <div className="tab-panel">{children}</div>;
> }
>
> // 컴포넌트를 Tabs의 속성으로 연결
> Tabs.List = TabList;
> Tabs.Tab = Tab;
> Tabs.Panel = TabPanel;
>
> export default Tabs;
> ```
>
> **사용 예시**
> ```jsx
> function App() {
>   return (
>     <Tabs defaultTab="profile">
>       <Tabs.List>
>         <Tabs.Tab id="profile">프로필</Tabs.Tab>
>         <Tabs.Tab id="settings">설정</Tabs.Tab>
>         <Tabs.Tab id="notifications">알림</Tabs.Tab>
>       </Tabs.List>
>
>       <Tabs.Panel id="profile">
>         <h2>프로필</h2>
>         <p>프로필 내용...</p>
>       </Tabs.Panel>
>
>       <Tabs.Panel id="settings">
>         <h2>설정</h2>
>         <p>설정 내용...</p>
>       </Tabs.Panel>
>
>       <Tabs.Panel id="notifications">
>         <h2>알림</h2>
>         <p>알림 내용...</p>
>       </Tabs.Panel>
>     </Tabs>
>   );
> }
> ```
>
> **실무 예시: Accordion**
> ```jsx
> const AccordionContext = React.createContext();
>
> function Accordion({ children, allowMultiple = false }) {
>   const [openItems, setOpenItems] = useState([]);
>
>   const toggleItem = (id) => {
>     if (allowMultiple) {
>       setOpenItems(prev =>
>         prev.includes(id)
>           ? prev.filter(item => item !== id)
>           : [...prev, id]
>       );
>     } else {
>       setOpenItems(prev => prev.includes(id) ? [] : [id]);
>     }
>   };
>
>   return (
>     <AccordionContext.Provider value={{ openItems, toggleItem }}>
>       <div className="accordion">{children}</div>
>     </AccordionContext.Provider>
>   );
> }
>
> function AccordionItem({ id, children }) {
>   return <div className="accordion-item">{children}</div>;
> }
>
> function AccordionHeader({ id, children }) {
>   const { openItems, toggleItem } = useContext(AccordionContext);
>   const isOpen = openItems.includes(id);
>
>   return (
>     <button onClick={() => toggleItem(id)}>
>       {children}
>       <span>{isOpen ? '▲' : '▼'}</span>
>     </button>
>   );
> }
>
> function AccordionPanel({ id, children }) {
>   const { openItems } = useContext(AccordionContext);
>
>   if (!openItems.includes(id)) return null;
>
>   return <div className="accordion-panel">{children}</div>;
> }
>
> Accordion.Item = AccordionItem;
> Accordion.Header = AccordionHeader;
> Accordion.Panel = AccordionPanel;
> ```
>
> **장점**
> - API가 직관적이고 유연함
> - 상태 공유가 암묵적 (사용자가 props 전달 불필요)
> - 컴포넌트 조합의 자유도 높음
> - 확장성 좋음

---

**Q11.** React Portal이 무엇이고, 어떤 상황에서 사용하며, 이벤트 버블링은 어떻게 동작하는지 설명해주세요.

> **React Portal**
> - 부모 컴포넌트의 DOM 외부에 있는 DOM 노드로 자식을 렌더링하는 방법
> - `ReactDOM.createPortal(child, container)`
>
> **기본 사용법**
> ```jsx
> import { createPortal } from 'react-dom';
>
> function Modal({ children, isOpen }) {
>   if (!isOpen) return null;
>
>   return createPortal(
>     <div className="modal-overlay">
>       <div className="modal-content">
>         {children}
>       </div>
>     </div>,
>     document.getElementById('modal-root') // DOM의 다른 위치
>   );
> }
> ```
>
> **HTML 구조**
> ```html
> <body>
>   <div id="root">
>     <!-- React 앱의 메인 컨텐츠 -->
>   </div>
>   <div id="modal-root">
>     <!-- Portal로 렌더링될 모달 -->
>   </div>
> </body>
> ```
>
> **사용 시점**
>
> 1. **Modal/Dialog**
> ```jsx
> function App() {
>   const [isOpen, setIsOpen] = useState(false);
>
>   return (
>     <div style={{ position: 'relative', overflow: 'hidden' }}>
>       {/* overflow: hidden인 부모 안에서도 모달은 화면 전체에 표시됨 */}
>       <button onClick={() => setIsOpen(true)}>모달 열기</button>
>
>       <Modal isOpen={isOpen}>
>         <h2>모달 제목</h2>
>         <button onClick={() => setIsOpen(false)}>닫기</button>
>       </Modal>
>     </div>
>   );
> }
> ```
>
> 2. **Tooltip/Popover**
> ```jsx
> function Tooltip({ children, content, targetRef }) {
>   const [position, setPosition] = useState({ top: 0, left: 0 });
>
>   useEffect(() => {
>     if (targetRef.current) {
>       const rect = targetRef.current.getBoundingClientRect();
>       setPosition({
>         top: rect.bottom + window.scrollY,
>         left: rect.left + window.scrollX
>       });
>     }
>   }, [targetRef]);
>
>   return createPortal(
>     <div
>       className="tooltip"
>       style={{
>         position: 'absolute',
>         top: position.top,
>         left: position.left
>       }}
>     >
>       {content}
>     </div>,
>     document.body
>   );
> }
> ```
>
> 3. **Toast/Notification**
> ```jsx
> function Toast({ message }) {
>   return createPortal(
>     <div className="toast">
>       {message}
>     </div>,
>     document.getElementById('toast-root')
>   );
> }
> ```
>
> **이벤트 버블링의 특이점**
>
> Portal로 렌더링된 요소는 DOM 트리상 다른 위치에 있지만, React 트리에서는 여전히 부모의 자식입니다.
>
> ```jsx
> function Parent() {
>   const handleClick = (e) => {
>     console.log('Parent clicked!'); // Portal 내부 클릭해도 실행됨
>   };
>
>   return (
>     <div onClick={handleClick}>
>       <h1>Parent Component</h1>
>       <PortalModal>
>         <button>Portal 내부 버튼</button>
>       </PortalModal>
>     </div>
>   );
> }
>
> function PortalModal({ children }) {
>   return createPortal(
>     <div className="modal">
>       {children}
>       {/* 이 버튼을 클릭하면 Parent의 handleClick도 실행됨 */}
>     </div>,
>     document.body
>   );
> }
> ```
>
> **이벤트 버블링 동작 원리**
> ```
> DOM 트리:
> <body>
>   <div id="root">
>     <div onClick={handleClick}>  ← 이벤트 핸들러
>       <h1>Parent</h1>
>     </div>
>   </div>
>   <div id="modal-root">
>     <div class="modal">
>       <button>버튼</button>  ← 클릭 발생
>     </div>
>   </div>
> </body>
>
> React 트리:
> <Parent onClick={handleClick}>
>   <h1>Parent</h1>
>   <Portal>
>     <button>버튼</button>  ← React 트리에서는 자식
>   </Portal>
> </Parent>
>
> → 버튼 클릭 시 React 트리를 따라 이벤트 버블링됨
> → handleClick 실행됨
> ```
>
> **장점**
> - z-index 스택 문제 해결
> - CSS overflow 제약 회피
> - 전역 UI 요소를 논리적으로 로컬 컴포넌트 내에서 관리

---

**Q12.** `React.forwardRef`와 `useImperativeHandle`의 사용 목적과 실무 활용 사례를 설명해주세요.

> **React.forwardRef**
> - 함수형 컴포넌트에서 ref를 받아 자식 요소로 전달하기 위한 HOC
> - 일반적으로 함수형 컴포넌트는 ref를 prop으로 받을 수 없음
>
> ```jsx
> // forwardRef 없이는 불가능
> function Input(props) {
>   return <input {...props} />;
> }
>
> // 사용 시
> const inputRef = useRef();
> <Input ref={inputRef} /> // 경고 발생, ref 전달 안됨
> ```
>
> ```jsx
> // forwardRef 사용
> const Input = React.forwardRef((props, ref) => {
>   return <input ref={ref} {...props} />;
> });
>
> // 사용
> function Form() {
>   const inputRef = useRef();
>
>   useEffect(() => {
>     inputRef.current.focus(); // DOM 접근 가능
>   }, []);
>
>   return <Input ref={inputRef} />;
> }
> ```
>
> **useImperativeHandle**
> - 부모 컴포넌트에게 노출할 ref의 인터페이스를 커스터마이징
> - DOM 요소를 직접 노출하지 않고, 특정 메서드만 노출
>
> ```jsx
> const FancyInput = React.forwardRef((props, ref) => {
>   const inputRef = useRef();
>
>   useImperativeHandle(ref, () => ({
>     // 부모에게 노출할 메서드만 정의
>     focus: () => {
>       inputRef.current.focus();
>     },
>     clear: () => {
>       inputRef.current.value = '';
>     },
>     getValue: () => {
>       return inputRef.current.value;
>     }
>   }));
>
>   return <input ref={inputRef} {...props} />;
> });
>
> // 사용
> function Form() {
>   const fancyInputRef = useRef();
>
>   const handleSubmit = () => {
>     const value = fancyInputRef.current.getValue(); // 정의한 메서드 호출
>     console.log(value);
>     fancyInputRef.current.clear();
>   };
>
>   const handleFocus = () => {
>     fancyInputRef.current.focus();
>   };
>
>   return (
>     <>
>       <FancyInput ref={fancyInputRef} />
>       <button onClick={handleFocus}>포커스</button>
>       <button onClick={handleSubmit}>제출</button>
>     </>
>   );
> }
> ```
>
> **실무 활용 사례**
>
> 1. **재사용 가능한 Input 컴포넌트**
> ```jsx
> const TextInput = React.forwardRef(({ label, error, ...props }, ref) => {
>   return (
>     <div className="input-group">
>       <label>{label}</label>
>       <input ref={ref} {...props} />
>       {error && <span className="error">{error}</span>}
>     </div>
>   );
> });
>
> // 폼에서 첫 번째 에러 필드에 포커스
> function LoginForm() {
>   const emailRef = useRef();
>   const passwordRef = useRef();
>
>   const handleSubmit = (e) => {
>     e.preventDefault();
>     if (!isValidEmail(emailRef.current.value)) {
>       emailRef.current.focus(); // 에러 필드에 포커스
>       return;
>     }
>   };
>
>   return (
>     <form onSubmit={handleSubmit}>
>       <TextInput ref={emailRef} label="이메일" />
>       <TextInput ref={passwordRef} label="비밀번호" type="password" />
>     </form>
>   );
> }
> ```
>
> 2. **비디오 플레이어 제어**
> ```jsx
> const VideoPlayer = React.forwardRef((props, ref) => {
>   const videoRef = useRef();
>
>   useImperativeHandle(ref, () => ({
>     play: () => videoRef.current.play(),
>     pause: () => videoRef.current.pause(),
>     seekTo: (time) => {
>       videoRef.current.currentTime = time;
>     },
>     getCurrentTime: () => videoRef.current.currentTime
>   }));
>
>   return <video ref={videoRef} {...props} />;
> });
>
> function VideoControls() {
>   const playerRef = useRef();
>
>   return (
>     <>
>       <VideoPlayer ref={playerRef} src="video.mp4" />
>       <button onClick={() => playerRef.current.play()}>재생</button>
>       <button onClick={() => playerRef.current.pause()}>정지</button>
>       <button onClick={() => playerRef.current.seekTo(30)}>30초로</button>
>     </>
>   );
> }
> ```
>
> 3. **Modal 컴포넌트 제어**
> ```jsx
> const Modal = React.forwardRef(({ children }, ref) => {
>   const [isOpen, setIsOpen] = useState(false);
>
>   useImperativeHandle(ref, () => ({
>     open: () => setIsOpen(true),
>     close: () => setIsOpen(false),
>     toggle: () => setIsOpen(prev => !prev)
>   }));
>
>   if (!isOpen) return null;
>
>   return createPortal(
>     <div className="modal-overlay">
>       <div className="modal-content">{children}</div>
>     </div>,
>     document.body
>   );
> });
>
> function App() {
>   const modalRef = useRef();
>
>   return (
>     <>
>       <button onClick={() => modalRef.current.open()}>모달 열기</button>
>       <Modal ref={modalRef}>
>         <h2>모달 제목</h2>
>         <button onClick={() => modalRef.current.close()}>닫기</button>
>       </Modal>
>     </>
>   );
> }
> ```
>
> **주의사항**
> - ref는 선언적이 아닌 명령적 코드이므로 최소한으로 사용
> - 가능하면 props로 제어하는 것이 React 철학에 부합
> - 애니메이션, 포커스, 텍스트 선택 등 DOM에 직접 접근이 필요한 경우에만 사용

---

## 꼬리질문 대비 (13~15)

**Q13.** TypeScript 환경에서 제네릭 컴포넌트를 어떻게 작성하며, Props 타입을 안전하게 정의하는 방법은 무엇인가요?

> **기본 Props 타입 정의**
> ```typescript
> interface ButtonProps {
>   label: string;
>   onClick: () => void;
>   disabled?: boolean;  // optional
> }
>
> function Button({ label, onClick, disabled = false }: ButtonProps) {
>   return (
>     <button onClick={onClick} disabled={disabled}>
>       {label}
>     </button>
>   );
> }
> ```
>
> **React.FC vs 일반 함수**
> ```typescript
> // React.FC (권장하지 않음 - 암묵적 children 등 문제)
> const Button: React.FC<ButtonProps> = ({ label, onClick }) => {
>   return <button onClick={onClick}>{label}</button>;
> };
>
> // 일반 함수 (권장)
> function Button({ label, onClick }: ButtonProps) {
>   return <button onClick={onClick}>{label}</button>;
> }
> ```
>
> **제네릭 컴포넌트 - 기본**
> ```typescript
> interface ListProps<T> {
>   items: T[];
>   renderItem: (item: T) => React.ReactNode;
> }
>
> function List<T>({ items, renderItem }: ListProps<T>) {
>   return (
>     <ul>
>       {items.map((item, index) => (
>         <li key={index}>{renderItem(item)}</li>
>       ))}
>     </ul>
>   );
> }
>
> // 사용
> interface User {
>   id: number;
>   name: string;
> }
>
> const users: User[] = [{ id: 1, name: '홍길동' }];
>
> <List
>   items={users}
>   renderItem={(user) => <span>{user.name}</span>}  // user는 User 타입으로 추론
> />
> ```
>
> **제네릭 컴포넌트 - Select**
> ```typescript
> interface Option<T> {
>   value: T;
>   label: string;
> }
>
> interface SelectProps<T> {
>   options: Option<T>[];
>   value: T;
>   onChange: (value: T) => void;
> }
>
> function Select<T extends string | number>({
>   options,
>   value,
>   onChange
> }: SelectProps<T>) {
>   return (
>     <select
>       value={value as any}
>       onChange={(e) => {
>         const selectedValue = options.find(
>           opt => String(opt.value) === e.target.value
>         )?.value;
>         if (selectedValue !== undefined) {
>           onChange(selectedValue);
>         }
>       }}
>     >
>       {options.map((opt) => (
>         <option key={String(opt.value)} value={String(opt.value)}>
>           {opt.label}
>         </option>
>       ))}
>     </select>
>   );
> }
>
> // 사용
> type Status = 'active' | 'inactive' | 'pending';
>
> <Select<Status>
>   options={[
>     { value: 'active', label: '활성' },
>     { value: 'inactive', label: '비활성' }
>   ]}
>   value="active"
>   onChange={(status) => console.log(status)} // status는 Status 타입
> />
> ```
>
> **이벤트 핸들러 타입**
> ```typescript
> interface FormProps {
>   onSubmit: (data: FormData) => void;
> }
>
> function Form({ onSubmit }: FormProps) {
>   // 이벤트 타입 명시
>   const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
>     e.preventDefault();
>     // ...
>   };
>
>   const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
>     console.log(e.target.value);
>   };
>
>   const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
>     console.log('clicked');
>   };
>
>   return (
>     <form onSubmit={handleSubmit}>
>       <input onChange={handleChange} />
>       <button onClick={handleClick}>제출</button>
>     </form>
>   );
> }
> ```
>
> **Children 타입**
> ```typescript
> interface CardProps {
>   children: React.ReactNode;  // 가장 일반적
>   title?: React.ReactElement;  // 특정 타입만
>   footer?: JSX.Element;
> }
>
> function Card({ children, title, footer }: CardProps) {
>   return (
>     <div>
>       {title}
>       <div>{children}</div>
>       {footer}
>     </div>
>   );
> }
> ```
>
> **Ref 타입**
> ```typescript
> const Input = React.forwardRef<
>   HTMLInputElement,  // ref 타입
>   { placeholder?: string }  // props 타입
> >((props, ref) => {
>   return <input ref={ref} {...props} />;
> });
>
> // 사용
> function Form() {
>   const inputRef = useRef<HTMLInputElement>(null);
>
>   useEffect(() => {
>     inputRef.current?.focus();  // null 체크
>   }, []);
>
>   return <Input ref={inputRef} />;
> }
> ```
>
> **유틸리티 타입 활용**
> ```typescript
> interface BaseProps {
>   className?: string;
>   style?: React.CSSProperties;
> }
>
> // HTML 속성 상속
> interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
>   variant?: 'primary' | 'secondary';
> }
>
> function Button({ variant = 'primary', ...props }: ButtonProps) {
>   return <button {...props} className={`btn-${variant}`} />;
> }
>
> // Omit으로 특정 prop 제외
> interface InputProps extends Omit<
>   React.InputHTMLAttributes<HTMLInputElement>,
>   'size'  // size prop 제외
> > {
>   size?: 'small' | 'medium' | 'large';  // 커스텀 size
> }
> ```

---

**Q14.** Suspense와 ErrorBoundary를 조합하여 사용하는 패턴에 대해 설명하고, 실무에서 어떻게 활용할 수 있는지 말씀해주세요.

> **Suspense 기본**
> - 비동기 컴포넌트가 로딩될 때까지 fallback UI를 보여줌
> - React 18부터 데이터 페칭에도 공식 지원
>
> ```jsx
> import { Suspense } from 'react';
>
> function App() {
>   return (
>     <Suspense fallback={<Spinner />}>
>       <UserProfile />  {/* 비동기 데이터 로딩 */}
>     </Suspense>
>   );
> }
> ```
>
> **Suspense + ErrorBoundary 조합**
> ```jsx
> function App() {
>   return (
>     <ErrorBoundary FallbackComponent={ErrorFallback}>
>       <Suspense fallback={<Spinner />}>
>         <UserProfile />
>       </Suspense>
>     </ErrorBoundary>
>   );
> }
>
> // 흐름:
> // 1. 로딩 중 → Suspense의 fallback 표시
> // 2. 에러 발생 → ErrorBoundary가 캐치하여 ErrorFallback 표시
> // 3. 성공 → UserProfile 렌더링
> ```
>
> **재사용 가능한 Wrapper 컴포넌트**
> ```jsx
> function AsyncBoundary({
>   children,
>   loadingFallback = <Spinner />,
>   errorFallback = <ErrorMessage />
> }) {
>   return (
>     <ErrorBoundary FallbackComponent={errorFallback}>
>       <Suspense fallback={loadingFallback}>
>         {children}
>       </Suspense>
>     </ErrorBoundary>
>   );
> }
>
> // 사용
> <AsyncBoundary>
>   <UserProfile />
> </AsyncBoundary>
> ```
>
> **실무 패턴 1: 페이지 레벨 적용**
> ```jsx
> function App() {
>   return (
>     <Router>
>       <Routes>
>         <Route
>           path="/dashboard"
>           element={
>             <AsyncBoundary
>               loadingFallback={<PageSkeleton />}
>               errorFallback={<PageError />}
>             >
>               <Dashboard />
>             </AsyncBoundary>
>           }
>         />
>       </Routes>
>     </Router>
>   );
> }
> ```
>
> **실무 패턴 2: 세밀한 제어 (여러 레벨)**
> ```jsx
> function Dashboard() {
>   return (
>     <div>
>       <h1>대시보드</h1>
>
>       {/* 각 섹션별로 독립적인 로딩/에러 처리 */}
>       <AsyncBoundary loadingFallback={<WidgetSkeleton />}>
>         <SalesWidget />
>       </AsyncBoundary>
>
>       <AsyncBoundary loadingFallback={<WidgetSkeleton />}>
>         <AnalyticsWidget />
>       </AsyncBoundary>
>
>       <AsyncBoundary loadingFallback={<WidgetSkeleton />}>
>         <RecentOrders />
>       </AsyncBoundary>
>     </div>
>   );
> }
> ```
>
> **실무 패턴 3: React Query와 조합**
> ```jsx
> import { QueryErrorResetBoundary } from '@tanstack/react-query';
>
> function App() {
>   return (
>     <QueryErrorResetBoundary>
>       {({ reset }) => (
>         <ErrorBoundary
>           onReset={reset}  // 에러 리셋 시 쿼리도 리셋
>           FallbackComponent={({ resetErrorBoundary }) => (
>             <div>
>               <p>에러가 발생했습니다</p>
>               <button onClick={resetErrorBoundary}>다시 시도</button>
>             </div>
>           )}
>         >
>           <Suspense fallback={<Spinner />}>
>             <UserList />
>           </Suspense>
>         </ErrorBoundary>
>       )}
>     </QueryErrorResetBoundary>
>   );
> }
>
> function UserList() {
>   // suspense: true 옵션으로 Suspense 통합
>   const { data } = useQuery({
>     queryKey: ['users'],
>     queryFn: fetchUsers,
>     suspense: true
>   });
>
>   return (
>     <ul>
>       {data.map(user => <li key={user.id}>{user.name}</li>)}
>     </ul>
>   );
> }
> ```
>
> **실무 패턴 4: 중첩 Suspense (병렬 로딩)**
> ```jsx
> function ProductPage() {
>   return (
>     <div>
>       {/* 상품 정보는 빨리 로드되면 먼저 표시 */}
>       <Suspense fallback={<ProductInfoSkeleton />}>
>         <ProductInfo />
>       </Suspense>
>
>       {/* 리뷰는 나중에 로드되어도 OK */}
>       <Suspense fallback={<ReviewsSkeleton />}>
>         <Reviews />
>       </Suspense>
>
>       {/* 추천 상품도 독립적으로 로드 */}
>       <Suspense fallback={<RecommendationsSkeleton />}>
>         <Recommendations />
>       </Suspense>
>     </div>
>   );
> }
> ```
>
> **실무 패턴 5: Retry 로직 포함**
> ```jsx
> class ErrorBoundaryWithRetry extends React.Component {
>   state = { hasError: false, retryCount: 0 };
>
>   static getDerivedStateFromError(error) {
>     return { hasError: true };
>   }
>
>   componentDidCatch(error, errorInfo) {
>     logErrorToService(error, errorInfo);
>   }
>
>   handleRetry = () => {
>     this.setState(prev => ({
>       hasError: false,
>       retryCount: prev.retryCount + 1
>     }));
>   };
>
>   render() {
>     if (this.state.hasError) {
>       return (
>         <div>
>           <p>에러가 발생했습니다</p>
>           {this.state.retryCount < 3 ? (
>             <button onClick={this.handleRetry}>
>               다시 시도 ({this.state.retryCount}/3)
>             </button>
>           ) : (
>             <p>여러 번 시도했지만 실패했습니다. 고객센터에 문의하세요.</p>
>           )}
>         </div>
>       );
>     }
>
>     return this.props.children;
>   }
> }
> ```
>
> **장점**
> - 선언적 로딩/에러 처리
> - 코드 간결화 (try-catch, loading state 제거)
> - 사용자 경험 향상 (세밀한 로딩 제어)
> - 관심사 분리 (비즈니스 로직과 UI 상태 분리)

---

**Q15.** HOC 패턴에서 발생할 수 있는 문제점(props 충돌, displayName, ref 전달 등)과 해결 방법을 설명해주세요.

> **문제 1: Props 이름 충돌**
> ```jsx
> // 문제 상황
> function withUser(Component) {
>   return function WithUser(props) {
>     const user = useAuth();
>     return <Component {...props} user={user} />;  // user prop 추가
>   };
> }
>
> function withData(Component) {
>   return function WithData(props) {
>     const user = useUserData();  // 같은 이름의 prop!
>     return <Component {...props} user={user} />;
>   };
> }
>
> // user prop이 충돌
> const MyComponent = withUser(withData(BaseComponent));
> ```
>
> **해결 방법**
> ```jsx
> // 1. 명확한 이름 사용
> function withAuth(Component) {
>   return function WithAuth(props) {
>     const authUser = useAuth();  // 명확한 이름
>     return <Component {...props} authUser={authUser} />;
>   };
> }
>
> // 2. 네임스페이스 사용
> function withUser(Component) {
>   return function WithUser(props) {
>     const user = useAuth();
>     return <Component {...props} auth={{ user }} />;
>   };
> }
> ```
>
> **문제 2: displayName 누락**
> ```jsx
> function withLoading(Component) {
>   return function(props) {  // displayName 없음
>     return props.loading ? <Spinner /> : <Component {...props} />;
>   };
> }
>
> // React DevTools에서 <Unknown> 또는 함수명으로만 표시됨
> ```
>
> **해결 방법**
> ```jsx
> function withLoading(Component) {
>   const WithLoading = function(props) {
>     return props.loading ? <Spinner /> : <Component {...props} />;
>   };
>
>   // displayName 설정
>   WithLoading.displayName = `WithLoading(${
>     Component.displayName || Component.name || 'Component'
>   })`;
>
>   return WithLoading;
> }
>
> // React DevTools에서 "WithLoading(MyComponent)"로 표시
> ```
>
> **문제 3: Ref 전달 불가**
> ```jsx
> function withLogger(Component) {
>   return function WithLogger(props) {
>     useEffect(() => {
>       console.log('Component rendered');
>     });
>     return <Component {...props} />;  // ref가 전달되지 않음
>   };
> }
>
> const EnhancedInput = withLogger(Input);
>
> // ref를 전달해도 HOC에서 막힘
> const ref = useRef();
> <EnhancedInput ref={ref} />  // 작동 안 함
> ```
>
> **해결 방법**
> ```jsx
> function withLogger(Component) {
>   const WithLogger = React.forwardRef((props, ref) => {
>     useEffect(() => {
>       console.log('Component rendered');
>     });
>     return <Component {...props} ref={ref} />;  // ref 전달
>   });
>
>   WithLogger.displayName = `WithLogger(${
>     Component.displayName || Component.name || 'Component'
>   })`;
>
>   return WithLogger;
> }
>
> // 이제 ref가 정상 작동
> const ref = useRef();
> <EnhancedInput ref={ref} />
> ```
>
> **문제 4: Static 메서드 복사 누락**
> ```jsx
> class MyComponent extends React.Component {
>   static someStaticMethod() {
>     return 'static method';
>   }
>   render() {
>     return <div>Component</div>;
>   }
> }
>
> function withLoading(Component) {
>   return function WithLoading(props) {
>     return props.loading ? <Spinner /> : <Component {...props} />;
>   };
> }
>
> const Enhanced = withLoading(MyComponent);
> Enhanced.someStaticMethod();  // undefined! static 메서드가 사라짐
> ```
>
> **해결 방법**
> ```jsx
> import hoistNonReactStatics from 'hoist-non-react-statics';
>
> function withLoading(Component) {
>   const WithLoading = function(props) {
>     return props.loading ? <Spinner /> : <Component {...props} />;
>   };
>
>   // static 메서드 복사
>   hoistNonReactStatics(WithLoading, Component);
>
>   return WithLoading;
> }
>
> const Enhanced = withLoading(MyComponent);
> Enhanced.someStaticMethod();  // 'static method' - 정상 작동
> ```
>
> **문제 5: HOC 중첩 시 성능 문제**
> ```jsx
> // 매 렌더링마다 새로운 컴포넌트 생성 - 안티패턴!
> function Parent() {
>   const EnhancedComponent = withAuth(withData(MyComponent));
>   return <EnhancedComponent />;
> }
> ```
>
> **해결 방법**
> ```jsx
> // 컴포넌트 외부에서 한 번만 생성
> const EnhancedComponent = withAuth(withData(MyComponent));
>
> function Parent() {
>   return <EnhancedComponent />;
> }
>
> // 또는 useMemo 사용 (필요시)
> function Parent() {
>   const EnhancedComponent = useMemo(
>     () => withAuth(withData(MyComponent)),
>     []
>   );
>   return <EnhancedComponent />;
> }
> ```
>
> **완전한 HOC 템플릿**
> ```jsx
> import hoistNonReactStatics from 'hoist-non-react-statics';
>
> function withEnhancement(Component) {
>   const WithEnhancement = React.forwardRef((props, ref) => {
>     // HOC 로직
>     const enhancedProps = useEnhancement();
>
>     return <Component {...props} {...enhancedProps} ref={ref} />;
>   });
>
>   // displayName 설정
>   WithEnhancement.displayName = `WithEnhancement(${
>     Component.displayName || Component.name || 'Component'
>   })`;
>
>   // static 메서드 복사
>   hoistNonReactStatics(WithEnhancement, Component);
>
>   return WithEnhancement;
> }
> ```
>
> **현대적 대안: Custom Hooks**
> ```jsx
> // HOC 대신 Custom Hook 사용 (권장)
> function useEnhancement() {
>   // 로직
>   return enhancedProps;
> }
>
> function MyComponent(props) {
>   const enhancedProps = useEnhancement();
>
>   // props 충돌, ref, displayName 문제 모두 해결
>   return <div>{/* ... */}</div>;
> }
> ```
