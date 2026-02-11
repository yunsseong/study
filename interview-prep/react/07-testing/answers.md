# React 테스트 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** 단위 테스트(Unit Test), 통합 테스트(Integration Test), E2E 테스트의 차이점과 각각의 역할을 설명해주세요.

> **테스트 피라미드 구조**
>
> **1. 단위 테스트 (Unit Test)**
> - **범위**: 개별 함수, 모듈, 컴포넌트를 독립적으로 테스트
> - **특징**: 빠르고 저렴하며, 가장 많은 수를 작성
> - **도구**: Jest, Vitest
> - **예시**: 유틸 함수, 단일 컴포넌트, Custom Hook
>
> ```javascript
> // 단위 테스트 예시
> describe('formatPrice', () => {
>   it('숫자를 통화 형식으로 변환한다', () => {
>     expect(formatPrice(1000)).toBe('₩1,000');
>   });
> });
> ```
>
> **2. 통합 테스트 (Integration Test)**
> - **범위**: 여러 컴포넌트/모듈이 함께 동작하는지 테스트
> - **특징**: 실제 사용자 시나리오에 가까운 테스트
> - **도구**: React Testing Library + Jest
> - **예시**: 폼 제출, 부모-자식 컴포넌트 상호작용, API 호출
>
> ```javascript
> // 통합 테스트 예시
> test('로그인 폼 제출 시 사용자 정보를 표시한다', async () => {
>   render(<LoginForm />);
>   await userEvent.type(screen.getByLabelText('이메일'), 'test@example.com');
>   await userEvent.click(screen.getByRole('button', { name: '로그인' }));
>   expect(await screen.findByText('환영합니다')).toBeInTheDocument();
> });
> ```
>
> **3. E2E 테스트 (End-to-End Test)**
> - **범위**: 실제 브라우저에서 전체 애플리케이션 플로우 테스트
> - **특징**: 가장 느리고 비용이 높지만, 실제 사용자 경험을 검증
> - **도구**: Cypress, Playwright
> - **예시**: 회원가입 → 로그인 → 상품 구매 전체 플로우
>
> ```javascript
> // E2E 테스트 예시 (Playwright)
> test('사용자가 상품을 구매할 수 있다', async ({ page }) => {
>   await page.goto('https://example.com');
>   await page.click('text=로그인');
>   await page.fill('[name="email"]', 'test@example.com');
>   await page.fill('[name="password"]', 'password123');
>   await page.click('button:has-text("로그인")');
>   await page.click('text=상품 구매');
>   await expect(page.locator('text=구매 완료')).toBeVisible();
> });
> ```
>
> **테스트 비율 권장사항 (테스트 피라미드)**
> - 단위 테스트: 70%
> - 통합 테스트: 20%
> - E2E 테스트: 10%

---

**Q2.** React Testing Library의 철학과 이것이 Enzyme과 어떻게 다른지 설명해주세요.

> **React Testing Library (RTL) 핵심 철학**
>
> **"The more your tests resemble the way your software is used, the more confidence they can give you."**
> → 테스트가 실제 사용자 경험과 유사할수록 신뢰도가 높다
>
> **주요 원칙**
>
> 1. **사용자 관점 테스트**
>    - 구현 세부사항이 아닌 사용자가 보고 경험하는 것을 테스트
>    - DOM 쿼리를 통해 요소를 찾음 (실제 사용자처럼)
>
> 2. **접근성 우선**
>    - getByRole, getByLabelText 등 접근성 기반 쿼리 권장
>    - 접근성이 좋은 코드 작성 유도
>
> 3. **구현 세부사항 테스트 지양**
>    - State, Props 직접 접근 불가
>    - Private 메서드 테스트 불가
>
> **Enzyme과의 차이점**
>
> | 구분 | React Testing Library | Enzyme |
> |------|----------------------|--------|
> | 철학 | 사용자 관점 (Black Box) | 구현 세부사항 (White Box) |
> | 쿼리 방식 | DOM 쿼리 (getByRole 등) | 컴포넌트 인스턴스 접근 |
> | State 접근 | 불가능 (의도적) | 가능 (wrapper.state()) |
> | Props 접근 | 불가능 (의도적) | 가능 (wrapper.props()) |
> | 리렌더링 | 자동 | 수동 (wrapper.update()) |
> | 유지보수 | React 팀 공식 권장 | 더 이상 권장되지 않음 |
>
> ```javascript
> // ❌ Enzyme 방식 (구현 세부사항 테스트)
> const wrapper = shallow(<Counter />);
> expect(wrapper.state('count')).toBe(0);
> wrapper.find('button').simulate('click');
> expect(wrapper.state('count')).toBe(1);
>
> // ✅ RTL 방식 (사용자 관점 테스트)
> render(<Counter />);
> expect(screen.getByText('Count: 0')).toBeInTheDocument();
> await userEvent.click(screen.getByRole('button', { name: '증가' }));
> expect(screen.getByText('Count: 1')).toBeInTheDocument();
> ```
>
> **RTL을 사용해야 하는 이유**
> - 리팩토링에 강함 (구현이 바뀌어도 테스트는 유지)
> - 접근성 향상
> - React 공식 권장
> - 실제 사용자 경험 검증

---

**Q3.** Jest란 무엇이며 주요 기능(Mocking, Assertion, Coverage 등)에 대해 설명해주세요.

> **Jest 개요**
>
> Meta(Facebook)에서 개발한 JavaScript 테스트 프레임워크로, Zero-Config 철학을 지향합니다.
>
> **주요 기능**
>
> **1. 테스트 구조화 (describe, test/it)**
>
> ```javascript
> describe('Calculator', () => {
>   describe('add', () => {
>     it('두 숫자를 더한다', () => {
>       expect(add(1, 2)).toBe(3);
>     });
>
>     it('음수도 처리한다', () => {
>       expect(add(-1, -2)).toBe(-3);
>     });
>   });
> });
> ```
>
> **2. Assertion (단언문)**
>
> ```javascript
> // 동등성 검사
> expect(value).toBe(3);                    // ===
> expect(value).toEqual({ a: 1 });          // 깊은 비교
>
> // Boolean 검사
> expect(value).toBeTruthy();
> expect(value).toBeFalsy();
> expect(value).toBeNull();
> expect(value).toBeUndefined();
>
> // 숫자 비교
> expect(value).toBeGreaterThan(3);
> expect(value).toBeLessThanOrEqual(5);
> expect(0.1 + 0.2).toBeCloseTo(0.3);      // 부동소수점
>
> // 문자열/배열 포함
> expect('hello world').toMatch(/world/);
> expect(['apple', 'banana']).toContain('apple');
>
> // 예외 처리
> expect(() => throw Error('err')).toThrow('err');
>
> // DOM (with RTL)
> expect(element).toBeInTheDocument();
> expect(element).toHaveTextContent('안녕');
> ```
>
> **3. Mocking**
>
> ```javascript
> // 함수 Mock
> const mockFn = jest.fn();
> mockFn.mockReturnValue(42);
> mockFn.mockResolvedValue('async value');
>
> // 호출 검증
> expect(mockFn).toHaveBeenCalled();
> expect(mockFn).toHaveBeenCalledWith('arg1', 'arg2');
> expect(mockFn).toHaveBeenCalledTimes(2);
>
> // 모듈 Mock
> jest.mock('./api', () => ({
>   fetchUser: jest.fn(() => Promise.resolve({ id: 1, name: 'John' }))
> }));
>
> // Spy
> const spy = jest.spyOn(object, 'method');
> ```
>
> **4. 테스트 라이프사이클**
>
> ```javascript
> beforeAll(() => {
>   // 모든 테스트 실행 전 1회
>   console.log('테스트 스위트 시작');
> });
>
> beforeEach(() => {
>   // 각 테스트 실행 전
>   database.connect();
> });
>
> afterEach(() => {
>   // 각 테스트 실행 후
>   database.clear();
> });
>
> afterAll(() => {
>   // 모든 테스트 실행 후 1회
>   database.disconnect();
> });
> ```
>
> **5. 비동기 테스트**
>
> ```javascript
> // async/await
> test('비동기 데이터 fetch', async () => {
>   const data = await fetchData();
>   expect(data).toEqual({ id: 1 });
> });
>
> // Promise
> test('Promise 반환', () => {
>   return fetchData().then(data => {
>     expect(data).toEqual({ id: 1 });
>   });
> });
> ```
>
> **6. 테스트 커버리지**
>
> ```bash
> # package.json
> {
>   "scripts": {
>     "test:coverage": "jest --coverage"
>   }
> }
> ```
>
> ```
> ------------------|---------|----------|---------|---------|
> File              | % Stmts | % Branch | % Funcs | % Lines |
> ------------------|---------|----------|---------|---------|
> All files         |   85.5  |   78.2   |   90.1  |   84.8  |
>  utils.js         |   100   |   100    |   100   |   100   |
>  component.jsx    |   75.5  |   62.5   |   83.3  |   74.2  |
> ------------------|---------|----------|---------|---------|
> ```
>
> **7. Watch Mode**
>
> ```bash
> jest --watch          # 변경된 파일만 재실행
> jest --watchAll       # 모든 테스트 재실행
> ```
>
> **Jest의 장점**
> - Zero Configuration (별도 설정 없이 바로 사용)
> - 빠른 실행 속도 (병렬 실행)
> - Snapshot 테스트 내장
> - Code Coverage 내장
> - Mocking 강력한 지원

---

**Q4.** React Testing Library의 쿼리 우선순위(getByRole, getByLabelText, getByText 등)와 그 이유를 설명해주세요.

> **쿼리 우선순위 (공식 가이드)**
>
> **1. 모든 사용자가 접근 가능한 쿼리 (최우선)**
>
> **getByRole** (가장 권장)
> ```javascript
> // 접근성 역할(role)로 요소 찾기
> screen.getByRole('button', { name: '제출' });
> screen.getByRole('heading', { level: 1 });
> screen.getByRole('textbox', { name: '이메일' });
> screen.getByRole('checkbox', { name: '약관 동의' });
> ```
> - 스크린 리더 사용자 경험과 동일
> - 접근성 준수 강제
> - ARIA role 확인
>
> **getByLabelText**
> ```javascript
> // 폼 요소 (label과 연결된 input)
> screen.getByLabelText('비밀번호');
> screen.getByLabelText('생년월일');
> ```
> - 폼 요소에 최적
> - label-input 연결 강제 (접근성)
>
> **getByPlaceholderText**
> ```javascript
> screen.getByPlaceholderText('이름을 입력하세요');
> ```
> - label이 없을 때만 사용 (권장하지 않음)
>
> **getByText**
> ```javascript
> // 텍스트 콘텐츠로 찾기
> screen.getByText('로그인');
> screen.getByText(/^Hello/);  // 정규식 가능
> ```
> - div, span, p 등 텍스트 요소
>
> **getByDisplayValue**
> ```javascript
> // input의 현재 값으로 찾기
> screen.getByDisplayValue('현재 입력된 값');
> ```
>
> **2. Semantic 쿼리**
>
> **getByAltText**
> ```javascript
> // 이미지 alt 속성
> screen.getByAltText('프로필 사진');
> ```
>
> **getByTitle**
> ```javascript
> // title 속성
> screen.getByTitle('닫기');
> ```
>
> **3. Test ID (최후의 수단)**
>
> **getByTestId**
> ```javascript
> // data-testid 속성
> screen.getByTestId('custom-element');
> ```
> - 다른 방법이 없을 때만 사용
> - 사용자가 볼 수 없는 정보
>
> **쿼리 변형 (Variants)**
>
> | Prefix | 반환값 | 사용 시점 | 예외 발생 |
> |--------|-------|---------|----------|
> | getBy | Element | 요소가 존재해야 함 | 없으면 에러 |
> | queryBy | Element \| null | 요소가 없을 수도 있음 | 에러 없음 |
> | findBy | Promise<Element> | 비동기로 나타나는 요소 | 타임아웃 시 에러 |
> | getAllBy | Element[] | 여러 개 | 없으면 에러 |
> | queryAllBy | Element[] | 여러 개 (옵션) | 빈 배열 반환 |
> | findAllBy | Promise<Element[]> | 비동기 여러 개 | 타임아웃 시 에러 |
>
> ```javascript
> // ✅ 존재하는 요소
> const button = screen.getByRole('button');
>
> // ✅ 존재하지 않을 수 있는 요소
> const error = screen.queryByText('에러 메시지');
> expect(error).not.toBeInTheDocument();
>
> // ✅ 비동기로 나타나는 요소
> const message = await screen.findByText('로딩 완료');
>
> // ✅ 여러 개
> const items = screen.getAllByRole('listitem');
> expect(items).toHaveLength(5);
> ```
>
> **실전 예시**
>
> ```javascript
> // ❌ 나쁜 예 (test-id 남용)
> render(<LoginForm />);
> const emailInput = screen.getByTestId('email-input');
> const submitButton = screen.getByTestId('submit-button');
>
> // ✅ 좋은 예 (접근성 기반)
> render(<LoginForm />);
> const emailInput = screen.getByRole('textbox', { name: '이메일' });
> const submitButton = screen.getByRole('button', { name: '로그인' });
> ```
>
> **우선순위를 따라야 하는 이유**
> 1. 접근성 향상 (스크린 리더 사용자 고려)
> 2. 유지보수 용이 (UI 변경에 강함)
> 3. 실제 사용자 경험 반영
> 4. 시맨틱 HTML 작성 유도

---

**Q5.** 테스트 커버리지(Test Coverage)란 무엇이며, 100% 커버리지가 완벽한 테스트를 의미하는지 설명해주세요.

> **테스트 커버리지 개념**
>
> 코드의 얼마나 많은 부분이 테스트에 의해 실행되었는지를 측정하는 지표입니다.
>
> **커버리지 종류**
>
> **1. Statement Coverage (구문 커버리지)**
> ```javascript
> function divide(a, b) {
>   if (b === 0) {
>     return 0;  // 이 줄이 실행되었는가?
>   }
>   return a / b;  // 이 줄이 실행되었는가?
> }
>
> // 50% 구문 커버리지
> test('나누기', () => {
>   expect(divide(10, 2)).toBe(5);  // return a / b만 실행됨
> });
>
> // 100% 구문 커버리지
> test('0으로 나누기', () => {
>   expect(divide(10, 0)).toBe(0);  // 모든 구문 실행됨
> });
> ```
>
> **2. Branch Coverage (분기 커버리지)**
> ```javascript
> function getDiscount(price, isMember) {
>   if (isMember && price > 10000) {  // 4가지 경로 존재
>     return price * 0.2;
>   }
>   return 0;
> }
>
> // 100% 분기 커버리지를 위해서는 4가지 케이스 필요
> // 1. isMember=true, price>10000
> // 2. isMember=true, price<=10000
> // 3. isMember=false, price>10000
> // 4. isMember=false, price<=10000
> ```
>
> **3. Function Coverage (함수 커버리지)**
> - 모든 함수가 최소 1번 호출되었는가?
>
> **4. Line Coverage (라인 커버리지)**
> - 모든 코드 라인이 실행되었는가?
>
> **커버리지 확인 방법**
>
> ```bash
> # Jest 커버리지 실행
> npm test -- --coverage
>
> # 특정 임계값 설정 (jest.config.js)
> module.exports = {
>   coverageThreshold: {
>     global: {
>       statements: 80,
>       branches: 80,
>       functions: 80,
>       lines: 80
>     }
>   }
> };
> ```
>
> **커버리지 리포트 예시**
>
> ```
> -------------------|---------|----------|---------|---------|-------------------
> File               | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
> -------------------|---------|----------|---------|---------|-------------------
> All files          |   87.5  |   75.0   |   90.0  |   86.2  |
>  utils/            |   95.5  |   88.2   |   100   |   94.8  |
>   math.js          |   100   |   100    |   100   |   100   |
>   string.js        |   91.2  |   76.5   |   100   |   89.7  | 15-18, 42
>  components/       |   78.3  |   62.5   |   80.0  |   77.1  |
>   Button.jsx       |   85.7  |   75.0   |   100   |   84.2  | 23, 45-48
>   Form.jsx         |   71.4  |   50.0   |   60.0  |   70.5  | 12-15, 28-35, 67
> -------------------|---------|----------|---------|---------|-------------------
> ```
>
> **100% 커버리지 = 완벽한 테스트? ❌**
>
> **반례 1: Edge Case 미검증**
> ```javascript
> function getUserAge(user) {
>   return user.age;  // 100% 커버리지 달성
> }
>
> test('나이 반환', () => {
>   expect(getUserAge({ age: 25 })).toBe(25);
> });
>
> // ❌ 검증하지 못한 케이스
> // - user가 null인 경우
> // - user.age가 undefined인 경우
> // - age가 음수인 경우
> // - age가 문자열인 경우
> ```
>
> **반례 2: 비즈니스 로직 검증 부재**
> ```javascript
> function processOrder(order) {
>   const total = order.price * order.quantity;
>   const discount = order.coupon ? total * 0.1 : 0;
>   return total - discount;
> }
>
> test('주문 처리', () => {
>   // 100% 커버리지 달성
>   expect(processOrder({ price: 100, quantity: 2, coupon: true }))
>     .toBe(180);  // ❌ 잘못된 기대값인데 통과함
> });
>
> // 실제로는 180이 아니라 180이어야 하는지 비즈니스 로직 검증 필요
> ```
>
> **반례 3: 통합/E2E 부재**
> ```javascript
> // 각 함수는 100% 커버리지지만, 통합 시 문제 발생 가능
> function validateEmail(email) {
>   return email.includes('@');  // 100% 커버
> }
>
> function sendEmail(email) {
>   // API 호출 로직
>   return api.send(email);  // 100% 커버 (mock 사용)
> }
>
> // ❌ 검증하지 못한 것
> // - validateEmail 통과 후 sendEmail이 실제로 잘 동작하는가?
> // - 네트워크 오류, 타임아웃 등 실제 상황
> ```
>
> **적절한 커버리지 목표**
>
> | 프로젝트 유형 | 권장 커버리지 | 이유 |
> |-------------|-------------|-----|
> | 금융/의료 시스템 | 90-100% | 높은 신뢰성 필요 |
> | 일반 웹 앱 | 70-80% | 비용 대비 효율 균형 |
> | 프로토타입/MVP | 50-60% | 빠른 개발 우선 |
> | 유틸리티 라이브러리 | 90%+ | 재사용성, 안정성 |
>
> **커버리지보다 중요한 것**
>
> ```javascript
> // ❌ 커버리지만 높은 무의미한 테스트
> test('컴포넌트 렌더링', () => {
>   render(<ComplexForm />);
>   // 아무 검증도 하지 않음
> });
>
> // ✅ 의미 있는 테스트 (낮은 커버리지라도 가치 있음)
> test('필수 필드 미입력 시 에러 표시', async () => {
>   render(<ComplexForm />);
>   await userEvent.click(screen.getByRole('button', { name: '제출' }));
>   expect(screen.getByText('이메일을 입력하세요')).toBeInTheDocument();
> });
> ```
>
> **결론**
> - 커버리지는 "얼마나 실행되었는가"만 측정
> - 중요한 것은 "올바르게 동작하는가"
> - 핵심 비즈니스 로직, 엣지 케이스, 통합 테스트가 더 중요
> - 커버리지는 참고 지표일 뿐, 맹신 금지

---

## 비교/구분 (6~9)

**Q6.** fireEvent와 userEvent의 차이점과 언제 userEvent를 사용해야 하는지 설명해주세요.

> **fireEvent vs userEvent 비교**
>
> **fireEvent (저수준 API)**
> ```javascript
> import { fireEvent } from '@testing-library/react';
>
> // 단일 이벤트만 발생
> fireEvent.click(button);
> fireEvent.change(input, { target: { value: 'hello' } });
> ```
> - React Testing Library 기본 제공
> - 브라우저 이벤트를 직접 발생시킴
> - 단일 이벤트만 트리거
>
> **userEvent (고수준 API)**
> ```javascript
> import userEvent from '@testing-library/user-event';
>
> // 실제 사용자 상호작용 시뮬레이션
> await userEvent.click(button);
> await userEvent.type(input, 'hello');
> ```
> - 별도 패키지 설치 필요 (`@testing-library/user-event`)
> - 실제 사용자 행동을 완전히 시뮬레이션
> - 여러 이벤트를 순서대로 발생
>
> **주요 차이점**
>
> **1. Click 이벤트**
>
> ```javascript
> // fireEvent.click → 1개 이벤트
> fireEvent.click(button);
> // 발생 이벤트: click
>
> // userEvent.click → 여러 이벤트 시퀀스
> await userEvent.click(button);
> // 발생 이벤트: mouseOver → mouseMove → mouseDown → focus → mouseUp → click
> ```
>
> **2. 텍스트 입력**
>
> ```javascript
> // ❌ fireEvent - 비현실적
> fireEvent.change(input, { target: { value: 'hello' } });
> // - 한 번에 전체 값 변경
> // - keyDown, keyPress 이벤트 없음
> // - 실제 타이핑 시뮬레이션 안 됨
>
> // ✅ userEvent - 실제 타이핑처럼
> await userEvent.type(input, 'hello');
> // - 'h' → 'he' → 'hel' → 'hell' → 'hello'
> // - 각 키마다 keyDown, keyPress, keyUp, input 이벤트 발생
> // - 실제 사용자 타이핑과 동일
> ```
>
> **3. 체크박스**
>
> ```javascript
> // fireEvent
> fireEvent.click(checkbox);
>
> // userEvent - 더 많은 검증
> await userEvent.click(checkbox);
> // - 포커스 가능 여부 확인
> // - disabled 상태 확인
> // - 실제 클릭 가능 영역 확인
> ```
>
> **실전 비교 예시**
>
> ```javascript
> // 컴포넌트
> function SearchInput({ onSearch }) {
>   const [value, setValue] = useState('');
>
>   const handleChange = (e) => {
>     setValue(e.target.value);
>     if (e.target.value.length >= 3) {
>       onSearch(e.target.value);  // 3글자 이상일 때만 검색
>     }
>   };
>
>   return <input value={value} onChange={handleChange} />;
> }
>
> // ❌ fireEvent - 잘못된 동작
> test('fireEvent 사용', () => {
>   const onSearch = jest.fn();
>   render(<SearchInput onSearch={onSearch} />);
>   const input = screen.getByRole('textbox');
>
>   fireEvent.change(input, { target: { value: 'react' } });
>
>   // onSearch가 1번 호출됨
>   expect(onSearch).toHaveBeenCalledTimes(1);
> });
>
> // ✅ userEvent - 올바른 동작
> test('userEvent 사용', async () => {
>   const onSearch = jest.fn();
>   render(<SearchInput onSearch={onSearch} />);
>   const input = screen.getByRole('textbox');
>
>   await userEvent.type(input, 'react');
>
>   // 'r', 're'는 3글자 미만이므로 호출 안 됨
>   // 'rea', 'reac', 'react'에서 3번 호출됨
>   expect(onSearch).toHaveBeenCalledTimes(3);
> });
> ```
>
> **userEvent API 주요 메서드**
>
> ```javascript
> const user = userEvent.setup();  // v14+ 권장 방식
>
> // 클릭 관련
> await user.click(element);           // 왼쪽 클릭
> await user.dblClick(element);        // 더블 클릭
> await user.tripleClick(element);     // 트리플 클릭
> await user.hover(element);           // 호버
> await user.unhover(element);         // 호버 해제
>
> // 키보드 입력
> await user.type(element, 'hello');   // 타이핑
> await user.clear(element);           // 내용 지우기
> await user.keyboard('{Enter}');      // 특수 키
> await user.keyboard('{Shift>}A{/Shift}'); // 조합키
>
> // 선택
> await user.selectOptions(select, 'option1');  // 셀렉트박스
> await user.deselectOptions(select, 'option1');
>
> // 파일 업로드
> await user.upload(fileInput, file);
>
> // 복사/붙여넣기
> await user.copy();
> await user.paste();
> ```
>
> **언제 무엇을 사용할까?**
>
> | 상황 | 권장 | 이유 |
> |------|-----|------|
> | 일반적인 경우 | userEvent | 실제 사용자 행동 시뮬레이션 |
> | 텍스트 입력 | userEvent | 키보드 이벤트 정확성 |
> | 폼 제출 | userEvent | 실제 사용자 플로우 |
> | 특수한 이벤트 | fireEvent | userEvent 미지원 이벤트 |
> | 간단한 테스트 | fireEvent | 성능상 이점 (빠름) |
> | 디버깅 | fireEvent | 단순하여 문제 파악 쉬움 |
>
> **마이그레이션 가이드**
>
> ```javascript
> // Before (fireEvent)
> fireEvent.change(input, { target: { value: 'test' } });
> fireEvent.click(button);
>
> // After (userEvent)
> const user = userEvent.setup();
> await user.type(input, 'test');
> await user.click(button);
> ```
>
> **결론**
> - **userEvent 우선 사용** (더 현실적인 테스트)
> - fireEvent는 userEvent로 불가능한 경우만
> - async/await 필수 (userEvent는 모두 비동기)

---

**Q7.** getBy, queryBy, findBy 쿼리의 차이점과 각각의 사용 시점을 설명해주세요.

> **쿼리 변형(Query Variants) 완벽 가이드**
>
> **핵심 차이점**
>
> | Query | 요소 없을 때 | 반환 타입 | 사용 시점 | 비동기 |
> |-------|-----------|---------|----------|-------|
> | getBy | 에러 발생 | Element | 요소가 반드시 있어야 함 | ❌ |
> | queryBy | null 반환 | Element \| null | 요소가 없을 수도 있음 | ❌ |
> | findBy | 에러 발생 | Promise<Element> | 비동기로 나타나는 요소 | ✅ |
>
> **1. getBy - 동기, 반드시 존재**
>
> ```javascript
> // ✅ 요소가 존재하는 경우
> test('로그인 버튼이 있다', () => {
>   render(<LoginForm />);
>   const button = screen.getByRole('button', { name: '로그인' });
>   expect(button).toBeInTheDocument();
> });
>
> // ❌ 요소가 없으면 테스트 실패
> test('에러 발생', () => {
>   render(<LoginForm />);
>   // TestingLibraryElementError: Unable to find an element...
>   const error = screen.getByText('에러 메시지');
> });
> ```
>
> **사용 시점**
> - 페이지 로드 시 즉시 존재하는 요소
> - 반드시 있어야 하는 필수 요소
> - 요소 부재 시 테스트 실패가 맞는 경우
>
> **2. queryBy - 동기, 선택적 존재**
>
> ```javascript
> // ✅ 요소 부재를 검증할 때
> test('초기에는 에러 메시지가 없다', () => {
>   render(<Form />);
>   const error = screen.queryByText('에러 메시지');
>   expect(error).not.toBeInTheDocument();  // null 검증
> });
>
> // ✅ 조건부 렌더링 검증
> test('로그인 상태에 따라 버튼 표시', () => {
>   const { rerender } = render(<Header isLoggedIn={false} />);
>
>   expect(screen.queryByText('로그아웃')).not.toBeInTheDocument();
>   expect(screen.getByText('로그인')).toBeInTheDocument();
>
>   rerender(<Header isLoggedIn={true} />);
>
>   expect(screen.queryByText('로그인')).not.toBeInTheDocument();
>   expect(screen.getByText('로그아웃')).toBeInTheDocument();
> });
>
> // ✅ 요소 사라짐 검증
> test('삭제 버튼 클릭 시 항목 제거', async () => {
>   render(<TodoList />);
>   const item = screen.getByText('할 일 1');
>   const deleteBtn = screen.getByRole('button', { name: '삭제' });
>
>   await userEvent.click(deleteBtn);
>
>   expect(screen.queryByText('할 일 1')).not.toBeInTheDocument();
> });
> ```
>
> **사용 시점**
> - 요소가 없음을 검증할 때 (not.toBeInTheDocument)
> - 조건부 렌더링 검증
> - 요소가 사라졌는지 확인
>
> **3. findBy - 비동기, 반드시 존재**
>
> ```javascript
> // ✅ API 호출 후 나타나는 요소
> test('사용자 데이터 로드', async () => {
>   render(<UserProfile userId="123" />);
>
>   // 로딩 중...
>   expect(screen.getByText('로딩 중...')).toBeInTheDocument();
>
>   // 비동기로 나타나는 요소 기다림 (기본 1000ms 타임아웃)
>   const userName = await screen.findByText('홍길동');
>   expect(userName).toBeInTheDocument();
> });
>
> // ✅ 타임아웃 커스터마이징
> test('긴 API 호출', async () => {
>   render(<SlowComponent />);
>
>   const element = await screen.findByText(
>     '데이터 로드 완료',
>     {},
>     { timeout: 3000 }  // 3초까지 기다림
>   );
> });
>
> // ❌ 타임아웃 내에 요소가 안 나타나면 에러
> test('타임아웃 에러', async () => {
>   render(<Component />);
>
>   // 1초 안에 나타나지 않으면 실패
>   await screen.findByText('절대 안 나타남');
>   // Error: Unable to find an element with the text: 절대 안 나타남
> });
> ```
>
> **사용 시점**
> - API 호출 후 렌더링되는 요소
> - setTimeout/setInterval 후 나타나는 요소
> - 애니메이션 후 나타나는 요소
> - 비동기 상태 업데이트 후 렌더링
>
> **findBy vs waitFor 비교**
>
> ```javascript
> // ✅ findBy - 간결한 방법 (권장)
> const element = await screen.findByText('완료');
>
> // ✅ waitFor + getBy - 동일한 결과
> await waitFor(() => {
>   expect(screen.getByText('완료')).toBeInTheDocument();
> });
>
> // waitFor이 필요한 경우: 복잡한 조건
> await waitFor(() => {
>   const items = screen.getAllByRole('listitem');
>   expect(items).toHaveLength(5);
>   expect(items[0]).toHaveTextContent('첫 번째');
> });
> ```
>
> **복수 요소 쿼리 (All 변형)**
>
> ```javascript
> // getAllBy - 동기, 반드시 1개 이상
> const items = screen.getAllByRole('listitem');
> expect(items).toHaveLength(3);
>
> // queryAllBy - 동기, 0개 이상
> const errors = screen.queryAllByRole('alert');
> expect(errors).toHaveLength(0);  // 빈 배열 []
>
> // findAllBy - 비동기, 반드시 1개 이상
> const loadedItems = await screen.findAllByRole('listitem');
> expect(loadedItems).toHaveLength(5);
> ```
>
> **실전 의사결정 플로우**
>
> ```
> 요소를 찾아야 하는가?
> │
> ├─ 즉시 존재하는가?
> │  ├─ YES → 반드시 있어야 하는가?
> │  │         ├─ YES → getBy
> │  │         └─ NO → queryBy (+ not.toBeInTheDocument)
> │  │
> │  └─ NO (비동기) → findBy (await 필수)
> │
> └─ 여러 개인가?
>    └─ getAllBy / queryAllBy / findAllBy
> ```
>
> **실전 예시 모음**
>
> ```javascript
> // 시나리오: 검색 기능 테스트
> test('검색 플로우', async () => {
>   render(<SearchPage />);
>
>   // 1. 초기 렌더링 - 검색 폼 존재 확인
>   const searchInput = screen.getByRole('textbox', { name: '검색' });
>   const searchButton = screen.getByRole('button', { name: '검색' });
>
>   // 2. 초기에는 결과 없음
>   expect(screen.queryByText('검색 결과')).not.toBeInTheDocument();
>
>   // 3. 검색어 입력 및 검색
>   await userEvent.type(searchInput, 'React');
>   await userEvent.click(searchButton);
>
>   // 4. 로딩 표시 확인
>   expect(screen.getByText('검색 중...')).toBeInTheDocument();
>
>   // 5. 비동기 결과 기다림
>   const results = await screen.findAllByRole('article');
>   expect(results).toHaveLength(10);
>
>   // 6. 로딩 표시 사라짐 확인
>   expect(screen.queryByText('검색 중...')).not.toBeInTheDocument();
> });
> ```
>
> **정리**
>
> | 상황 | 쿼리 | 예시 |
> |------|-----|------|
> | 필수 요소 | getBy | 제목, 필수 버튼 |
> | 없음 검증 | queryBy | 에러 메시지 없음 |
> | API 후 표시 | findBy | 로드된 데이터 |
> | 조건부 렌더링 | queryBy | 로그인/로그아웃 버튼 |
> | 여러 개 즉시 | getAllBy | 리스트 아이템 |
> | 여러 개 비동기 | findAllBy | 비동기 로드된 리스트 |

---

**Q8.** jest.fn(), jest.mock(), jest.spyOn()의 차이점과 사용 사례를 설명해주세요.

> **Mocking 3총사 완벽 가이드**
>
> **1. jest.fn() - Mock 함수 생성**
>
> **개념**: 가짜 함수를 만들어 호출 여부, 인자, 반환값을 제어하고 추적
>
> ```javascript
> // 기본 사용법
> const mockFn = jest.fn();
>
> mockFn('hello', 123);
> mockFn('world');
>
> // 호출 검증
> expect(mockFn).toHaveBeenCalled();
> expect(mockFn).toHaveBeenCalledTimes(2);
> expect(mockFn).toHaveBeenCalledWith('hello', 123);
> expect(mockFn).toHaveBeenNthCalledWith(2, 'world');
>
> // 마지막 호출 확인
> expect(mockFn).toHaveBeenLastCalledWith('world');
> ```
>
> **반환값 제어**
>
> ```javascript
> // 고정 값 반환
> const mockFn = jest.fn().mockReturnValue(42);
> expect(mockFn()).toBe(42);
>
> // 호출마다 다른 값 반환
> const mockFn = jest.fn()
>   .mockReturnValueOnce('first')
>   .mockReturnValueOnce('second')
>   .mockReturnValue('default');
>
> expect(mockFn()).toBe('first');
> expect(mockFn()).toBe('second');
> expect(mockFn()).toBe('default');
> expect(mockFn()).toBe('default');
>
> // Promise 반환
> const mockFn = jest.fn().mockResolvedValue({ id: 1, name: 'John' });
> const result = await mockFn();
> expect(result).toEqual({ id: 1, name: 'John' });
>
> // Promise 거부
> const mockFn = jest.fn().mockRejectedValue(new Error('Failed'));
> await expect(mockFn()).rejects.toThrow('Failed');
> ```
>
> **구현 제공**
>
> ```javascript
> // 커스텀 구현
> const mockFn = jest.fn((a, b) => a + b);
> expect(mockFn(1, 2)).toBe(3);
>
> // mockImplementation
> const mockFn = jest.fn().mockImplementation((name) => `Hello, ${name}`);
> expect(mockFn('Alice')).toBe('Hello, Alice');
>
> // 호출마다 다른 구현
> const mockFn = jest.fn()
>   .mockImplementationOnce(() => 'first')
>   .mockImplementationOnce(() => 'second')
>   .mockImplementation(() => 'default');
> ```
>
> **사용 사례**
>
> ```javascript
> // 1. 콜백 함수 테스트
> test('버튼 클릭 시 핸들러 호출', async () => {
>   const handleClick = jest.fn();
>   render(<Button onClick={handleClick}>클릭</Button>);
>
>   await userEvent.click(screen.getByRole('button'));
>
>   expect(handleClick).toHaveBeenCalledTimes(1);
> });
>
> // 2. API 함수 Mock
> test('사용자 데이터 로드', async () => {
>   const mockFetchUser = jest.fn().mockResolvedValue({
>     id: 1,
>     name: '홍길동',
>     email: 'hong@example.com'
>   });
>
>   render(<UserProfile fetchUser={mockFetchUser} userId="1" />);
>
>   expect(mockFetchUser).toHaveBeenCalledWith('1');
>   expect(await screen.findByText('홍길동')).toBeInTheDocument();
> });
> ```
>
> **2. jest.mock() - 모듈 전체 Mock**
>
> **개념**: 외부 모듈(라이브러리, 유틸 파일)을 가짜로 대체
>
> ```javascript
> // API 모듈 Mock
> jest.mock('./api');
>
> import { fetchUser, createUser } from './api';
>
> // Mock 함수로 자동 변환됨
> fetchUser.mockResolvedValue({ id: 1, name: 'John' });
> createUser.mockResolvedValue({ id: 2, name: 'Jane' });
>
> test('사용자 생성', async () => {
>   const user = await createUser({ name: 'Jane' });
>   expect(createUser).toHaveBeenCalledWith({ name: 'Jane' });
>   expect(user).toEqual({ id: 2, name: 'Jane' });
> });
> ```
>
> **부분 Mock (일부만 Mock)**
>
> ```javascript
> // utils.js
> export const add = (a, b) => a + b;
> export const subtract = (a, b) => a - b;
> export const multiply = (a, b) => a * b;
>
> // test.js
> jest.mock('./utils', () => ({
>   ...jest.requireActual('./utils'),  // 실제 구현 유지
>   multiply: jest.fn(() => 999)       // multiply만 Mock
> }));
>
> import { add, subtract, multiply } from './utils';
>
> test('부분 Mock', () => {
>   expect(add(1, 2)).toBe(3);           // 실제 함수
>   expect(subtract(5, 3)).toBe(2);      // 실제 함수
>   expect(multiply(2, 3)).toBe(999);    // Mock 함수
> });
> ```
>
> **자동 Mock (Auto Mock)**
>
> ```javascript
> jest.mock('axios');  // axios 전체를 Mock으로
>
> import axios from 'axios';
>
> axios.get.mockResolvedValue({ data: { id: 1 } });
>
> test('API 호출', async () => {
>   const response = await axios.get('/users/1');
>   expect(response.data).toEqual({ id: 1 });
> });
> ```
>
> **수동 Mock (__mocks__ 디렉토리)**
>
> ```javascript
> // __mocks__/axios.js
> export default {
>   get: jest.fn(() => Promise.resolve({ data: {} })),
>   post: jest.fn(() => Promise.resolve({ data: {} }))
> };
>
> // test.js
> jest.mock('axios');  // __mocks__/axios.js 자동 사용
> ```
>
> **사용 사례**
>
> ```javascript
> // 1. 외부 라이브러리 Mock (localStorage)
> const localStorageMock = {
>   getItem: jest.fn(),
>   setItem: jest.fn(),
>   clear: jest.fn()
> };
> global.localStorage = localStorageMock;
>
> // 2. 날짜 Mock
> jest.mock('./utils/date', () => ({
>   getCurrentDate: jest.fn(() => new Date('2024-01-01'))
> }));
>
> // 3. React Router Mock
> jest.mock('react-router-dom', () => ({
>   ...jest.requireActual('react-router-dom'),
>   useNavigate: () => jest.fn()
> }));
> ```
>
> **3. jest.spyOn() - 실제 객체 메서드 감시**
>
> **개념**: 실제 객체의 메서드를 감시하면서 원본 동작 유지 또는 대체
>
> ```javascript
> // 객체 메서드 감시
> const user = {
>   getName: () => 'John',
>   getAge: () => 30
> };
>
> const spy = jest.spyOn(user, 'getName');
>
> // 원본 함수는 그대로 실행됨
> expect(user.getName()).toBe('John');
> expect(spy).toHaveBeenCalled();
>
> // 복원
> spy.mockRestore();
> ```
>
> **구현 대체**
>
> ```javascript
> const spy = jest.spyOn(user, 'getName').mockReturnValue('Jane');
>
> expect(user.getName()).toBe('Jane');  // Mock 값 반환
> expect(spy).toHaveBeenCalled();
>
> spy.mockRestore();
> expect(user.getName()).toBe('John');  // 원본 복원
> ```
>
> **실전 사용 사례**
>
> ```javascript
> // 1. console 감시
> test('에러 로그 확인', () => {
>   const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
>
>   render(<BuggyComponent />);
>
>   expect(consoleSpy).toHaveBeenCalledWith(
>     expect.stringContaining('Warning')
>   );
>
>   consoleSpy.mockRestore();
> });
>
> // 2. API 함수 감시
> import * as api from './api';
>
> test('API 호출 감시', async () => {
>   const spy = jest.spyOn(api, 'fetchUser').mockResolvedValue({
>     id: 1,
>     name: 'Test'
>   });
>
>   render(<UserProfile userId="1" />);
>
>   expect(spy).toHaveBeenCalledWith('1');
>
>   spy.mockRestore();
> });
>
> // 3. Date 감시
> test('현재 시간 Mock', () => {
>   const mockDate = new Date('2024-01-01');
>   const spy = jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
>
>   const component = render(<Clock />);
>   expect(component.getByText('2024-01-01')).toBeInTheDocument();
>
>   spy.mockRestore();
> });
> ```
>
> **3가지 비교표**
>
> | 기능 | jest.fn() | jest.mock() | jest.spyOn() |
> |------|-----------|-------------|--------------|
> | 목적 | 새 Mock 함수 생성 | 모듈 전체 Mock | 기존 메서드 감시 |
> | 대상 | 함수 | 모듈/파일 | 객체 메서드 |
> | 원본 유지 | ❌ (원본 없음) | ❌ (대체됨) | ✅ (선택 가능) |
> | 복원 | 불필요 | mockClear | mockRestore |
> | 사용 시점 | 콜백, Props | 외부 라이브러리 | console, API 감시 |
>
> **정리 및 선택 가이드**
>
> ```javascript
> // jest.fn() - 단순 함수 Mock
> const onClick = jest.fn();
> render(<Button onClick={onClick} />);
>
> // jest.mock() - 외부 모듈 전체 Mock
> jest.mock('axios');
> import axios from 'axios';
> axios.get.mockResolvedValue({ data: {} });
>
> // jest.spyOn() - 기존 메서드 감시하며 Mock
> const spy = jest.spyOn(console, 'log');
> console.log('test');
> expect(spy).toHaveBeenCalledWith('test');
> spy.mockRestore();
> ```

---

**Q9.** Cypress와 Playwright의 차이점과 각각의 장단점을 설명해주세요.

> **Cypress vs Playwright 비교 분석**
>
> **핵심 차이점**
>
> | 구분 | Cypress | Playwright |
> |------|---------|------------|
> | 제작사 | Cypress.io | Microsoft |
> | 출시연도 | 2017 | 2020 |
> | 브라우저 지원 | Chrome, Edge, Firefox, Electron | Chrome, Edge, Firefox, Safari, Webkit |
> | 언어 지원 | JavaScript/TypeScript | JavaScript, Python, Java, C# |
> | 실행 방식 | 브라우저 내부에서 실행 | 브라우저 외부에서 제어 (CDP) |
> | 멀티 탭 | ❌ 제한적 | ✅ 완전 지원 |
> | iframe | ❌ 제한적 | ✅ 완전 지원 |
> | 병렬 실행 | 유료 플랜 | 무료 지원 |
> | 학습 곡선 | 쉬움 | 중간 |
>
> **1. Cypress**
>
> **아키텍처 특징**
> - 브라우저 내부에서 테스트 코드 실행
> - Node.js 프로세스와 브라우저가 통신
> - DOM에 직접 접근 가능
>
> **장점**
>
> ```javascript
> // 1. 직관적이고 간결한 API
> describe('로그인 테스트', () => {
>   it('사용자가 로그인할 수 있다', () => {
>     cy.visit('/login');
>     cy.get('[name="email"]').type('test@example.com');
>     cy.get('[name="password"]').type('password123');
>     cy.get('button').contains('로그인').click();
>     cy.url().should('include', '/dashboard');
>     cy.contains('환영합니다').should('be.visible');
>   });
> });
>
> // 2. 자동 대기 (Automatic Waiting)
> cy.get('.loading').should('not.exist');  // 사라질 때까지 자동 대기
> cy.get('.data').should('be.visible');    // 나타날 때까지 자동 대기
>
> // 3. 시간 여행 (Time Travel)
> // Cypress Test Runner에서 각 단계를 시각적으로 확인 가능
>
> // 4. 네트워크 요청 Stub
> cy.intercept('GET', '/api/users', { fixture: 'users.json' }).as('getUsers');
> cy.visit('/users');
> cy.wait('@getUsers');
> cy.get('.user-list').should('have.length', 5);
>
> // 5. 실시간 리로드
> // 테스트 코드 변경 시 자동으로 재실행
> ```
>
> **단점**
>
> ```javascript
> // 1. 멀티 탭 지원 안 됨
> // ❌ 불가능: 새 탭에서 OAuth 인증 후 원래 탭으로 돌아오기
>
> // 2. iframe 제한
> // ❌ 복잡한 iframe 시나리오 어려움
>
> // 3. 동일 Origin 제한
> // ❌ 불가능: 두 개의 다른 도메인 오가기
> cy.visit('https://site1.com');
> cy.visit('https://site2.com');  // 에러 발생
>
> // 4. 브라우저 제한
> // Safari, IE 지원 안 됨 (Webkit 실험적 지원)
> ```
>
> **2. Playwright**
>
> **아키텍처 특징**
> - Chrome DevTools Protocol (CDP) 사용
> - 브라우저를 외부에서 제어
> - 모든 최신 브라우저 엔진 지원
>
> **장점**
>
> ```javascript
> // 1. 진정한 크로스 브라우저 테스트
> import { test, expect } from '@playwright/test';
>
> test.describe('크로스 브라우저', () => {
>   test('Chromium에서 실행', async ({ page }) => {
>     await page.goto('/');
>   });
>
>   // Safari (Webkit)에서도 동일하게 실행 가능
> });
>
> // 2. 멀티 탭/컨텍스트 완벽 지원
> test('멀티 탭 시나리오', async ({ browser }) => {
>   const context = await browser.newContext();
>   const page1 = await context.newPage();
>   const page2 = await context.newPage();
>
>   await page1.goto('/admin');
>   await page2.goto('/user');
>
>   // 두 탭 간 상호작용 테스트 가능
> });
>
> // 3. Auto-waiting (자동 대기)
> await page.click('button');  // 클릭 가능할 때까지 자동 대기
> await page.fill('input', 'text');  // 입력 가능할 때까지 대기
>
> // 4. 강력한 선택자
> await page.getByRole('button', { name: '제출' }).click();
> await page.getByLabel('이메일').fill('test@example.com');
> await page.getByText('환영합니다').waitFor();
>
> // 5. 네트워크 완벽 제어
> await page.route('/api/users', route => {
>   route.fulfill({
>     status: 200,
>     body: JSON.stringify([{ id: 1, name: 'John' }])
>   });
> });
>
> // 6. 병렬 실행 (무료)
> // playwright.config.js
> export default {
>   workers: 4  // 4개 워커로 병렬 실행
> };
>
> // 7. 다중 도메인 지원
> await page.goto('https://site1.com');
> await page.click('a[href="https://site2.com"]');
> await page.waitForURL('https://site2.com');
> ```
>
> **단점**
>
> ```javascript
> // 1. 학습 곡선이 다소 높음
> // Cypress보다 API가 많고 복잡할 수 있음
>
> // 2. Time Travel 기능 없음
> // Cypress처럼 시각적 디버깅 도구가 약함
>
> // 3. 커뮤니티 및 플러그인 생태계
> // Cypress보다 작음 (하지만 빠르게 성장 중)
> ```
>
> **실전 비교 예시**
>
> **시나리오: 로그인 후 프로필 확인**
>
> ```javascript
> // Cypress
> describe('프로필 테스트', () => {
>   beforeEach(() => {
>     cy.visit('/login');
>     cy.get('[name="email"]').type('test@example.com');
>     cy.get('[name="password"]').type('password123');
>     cy.get('button').contains('로그인').click();
>     cy.url().should('include', '/dashboard');
>   });
>
>   it('프로필 정보 표시', () => {
>     cy.get('[data-testid="username"]').should('have.text', '홍길동');
>   });
> });
>
> // Playwright
> import { test, expect } from '@playwright/test';
>
> test.describe('프로필 테스트', () => {
>   test.beforeEach(async ({ page }) => {
>     await page.goto('/login');
>     await page.getByLabel('이메일').fill('test@example.com');
>     await page.getByLabel('비밀번호').fill('password123');
>     await page.getByRole('button', { name: '로그인' }).click();
>     await expect(page).toHaveURL(/.*dashboard/);
>   });
>
>   test('프로필 정보 표시', async ({ page }) => {
>     const username = page.getByTestId('username');
>     await expect(username).toHaveText('홍길동');
>   });
> });
> ```
>
> **선택 가이드**
>
> **Cypress를 선택해야 하는 경우**
> - 팀이 E2E 테스트 경험이 적음
> - 단일 도메인 SPA 애플리케이션
> - Chrome/Firefox만 지원하면 충분
> - 시각적 디버깅이 중요
> - 빠르게 시작하고 싶음
>
> **Playwright를 선택해야 하는 경우**
> - Safari/Webkit 지원 필수
> - 멀티 탭/iframe 시나리오 많음
> - 여러 도메인 간 이동 필요
> - 병렬 실행으로 속도 향상 원함
> - 모바일 브라우저 테스트 필요
> - CI/CD에서 대규모 테스트 실행
>
> **성능 비교**
>
> ```bash
> # 동일한 100개 테스트 실행 시간
> Cypress (순차 실행): ~15분
> Cypress Cloud (병렬): ~3분 (유료)
> Playwright (병렬 4 workers): ~2분 (무료)
> ```
>
> **최신 동향 (2024 기준)**
> - Playwright 채택률 급증
> - Cypress는 Component Testing 강화
> - 두 도구 모두 활발히 개발 중
>
> **결론**
> - **간단한 프로젝트**: Cypress (빠른 시작)
> - **복잡한 시나리오**: Playwright (강력한 기능)
> - **크로스 브라우저 필수**: Playwright
> - **시각적 피드백 중요**: Cypress

---

## 심화/실무 (10~12)

**Q10.** 비동기 작업(API 호출, setTimeout 등)을 포함한 컴포넌트를 테스트하는 방법과 waitFor, findBy, act의 역할을 설명해주세요.

> **비동기 테스트 완벽 가이드**
>
> **1. findBy - 가장 간단한 방법 (권장)**
>
> ```javascript
> // 컴포넌트
> function UserProfile({ userId }) {
>   const [user, setUser] = useState(null);
>   const [loading, setLoading] = useState(true);
>
>   useEffect(() => {
>     fetchUser(userId).then(data => {
>       setUser(data);
>       setLoading(false);
>     });
>   }, [userId]);
>
>   if (loading) return <div>로딩 중...</div>;
>   return <div>{user.name}</div>;
> }
>
> // ✅ 테스트
> test('사용자 데이터 로드', async () => {
>   render(<UserProfile userId="123" />);
>
>   // findBy는 요소가 나타날 때까지 자동으로 대기 (최대 1000ms)
>   const userName = await screen.findByText('홍길동');
>   expect(userName).toBeInTheDocument();
> });
> ```
>
> **2. waitFor - 복잡한 조건**
>
> ```javascript
> import { waitFor } from '@testing-library/react';
>
> // ✅ 여러 조건을 동시에 확인
> test('복잡한 비동기 조건', async () => {
>   render(<Dashboard />);
>
>   await waitFor(() => {
>     expect(screen.getByText('총 사용자: 100')).toBeInTheDocument();
>     expect(screen.getByText('활성 사용자: 75')).toBeInTheDocument();
>     expect(screen.queryByText('로딩 중')).not.toBeInTheDocument();
>   });
> });
>
> // ✅ 배열 길이 확인
> test('리스트 아이템 개수', async () => {
>   render(<UserList />);
>
>   await waitFor(() => {
>     const items = screen.getAllByRole('listitem');
>     expect(items).toHaveLength(10);
>   });
> });
>
> // ✅ 타임아웃 커스터마이징
> await waitFor(
>   () => {
>     expect(screen.getByText('완료')).toBeInTheDocument();
>   },
>   { timeout: 3000 }  // 3초까지 대기
> );
>
> // ✅ Interval 설정
> await waitFor(
>   () => {
>     expect(screen.getByText('완료')).toBeInTheDocument();
>   },
>   {
>     timeout: 3000,
>     interval: 100  // 100ms마다 확인 (기본값 50ms)
>   }
> );
> ```
>
> **3. act() - React 상태 업데이트 경고 해결**
>
> **act()가 필요한 이유**
>
> React 18부터는 대부분 자동 처리되지만, 일부 경우 명시적 사용 필요:
>
> ```javascript
> // ❌ act() 없이 - 경고 발생 가능
> test('수동 상태 업데이트', () => {
>   const { result } = renderHook(() => useState(0));
>
>   result.current[1](1);  // Warning: An update to TestComponent...
> });
>
> // ✅ act() 사용
> import { act } from '@testing-library/react';
>
> test('수동 상태 업데이트', () => {
>   const { result } = renderHook(() => useState(0));
>
>   act(() => {
>     result.current[1](1);
>   });
>
>   expect(result.current[0]).toBe(1);
> });
> ```
>
> **act() 사용 사례**
>
> ```javascript
> // 1. 타이머 테스트
> test('타이머 후 상태 변경', () => {
>   jest.useFakeTimers();
>   render(<CountdownTimer initialSeconds={10} />);
>
>   expect(screen.getByText('10초 남음')).toBeInTheDocument();
>
>   act(() => {
>     jest.advanceTimersByTime(1000);
>   });
>
>   expect(screen.getByText('9초 남음')).toBeInTheDocument();
>
>   jest.useRealTimers();
> });
>
> // 2. setInterval 테스트
> test('자동 갱신', () => {
>   jest.useFakeTimers();
>   render(<AutoRefreshComponent />);
>
>   act(() => {
>     jest.advanceTimersByTime(5000);
>   });
>
>   expect(screen.getByText('5초 경과')).toBeInTheDocument();
>
>   jest.useRealTimers();
> });
>
> // 3. 수동 이벤트 디스패치
> test('커스텀 이벤트', () => {
>   render(<Component />);
>
>   act(() => {
>     window.dispatchEvent(new Event('resize'));
>   });
>
>   expect(screen.getByText('화면 크기 변경됨')).toBeInTheDocument();
> });
> ```
>
> **언제 act()를 사용하지 않아도 되는가?**
>
> ```javascript
> // ✅ userEvent - 내부적으로 act() 처리됨
> await userEvent.click(button);
>
> // ✅ fireEvent - 내부적으로 act() 처리됨
> fireEvent.click(button);
>
> // ✅ waitFor - 내부적으로 act() 처리됨
> await waitFor(() => expect(...));
>
> // ✅ findBy - 내부적으로 act() 처리됨
> await screen.findByText('완료');
> ```
>
> **4. API 호출 테스트 패턴**
>
> **패턴 1: Mock 함수 사용**
>
> ```javascript
> // api.js
> export const fetchUser = async (id) => {
>   const response = await fetch(`/api/users/${id}`);
>   return response.json();
> };
>
> // Component.test.js
> jest.mock('./api');
> import { fetchUser } from './api';
>
> test('API 호출 후 데이터 표시', async () => {
>   fetchUser.mockResolvedValue({ id: 1, name: '홍길동' });
>
>   render(<UserProfile userId="1" />);
>
>   expect(screen.getByText('로딩 중...')).toBeInTheDocument();
>
>   const userName = await screen.findByText('홍길동');
>   expect(userName).toBeInTheDocument();
>   expect(fetchUser).toHaveBeenCalledWith('1');
> });
>
> // 에러 처리 테스트
> test('API 에러 처리', async () => {
>   fetchUser.mockRejectedValue(new Error('Network error'));
>
>   render(<UserProfile userId="1" />);
>
>   const errorMsg = await screen.findByText('사용자 로드 실패');
>   expect(errorMsg).toBeInTheDocument();
> });
> ```
>
> **패턴 2: MSW (Mock Service Worker) 사용**
>
> ```javascript
> // mocks/handlers.js
> import { rest } from 'msw';
>
> export const handlers = [
>   rest.get('/api/users/:id', (req, res, ctx) => {
>     return res(
>       ctx.status(200),
>       ctx.json({ id: req.params.id, name: '홍길동' })
>     );
>   })
> ];
>
> // setupTests.js
> import { setupServer } from 'msw/node';
> import { handlers } from './mocks/handlers';
>
> const server = setupServer(...handlers);
>
> beforeAll(() => server.listen());
> afterEach(() => server.resetHandlers());
> afterAll(() => server.close());
>
> // test.js
> test('MSW로 API Mock', async () => {
>   render(<UserProfile userId="1" />);
>
>   const userName = await screen.findByText('홍길동');
>   expect(userName).toBeInTheDocument();
> });
>
> // 런타임 핸들러 오버라이드
> test('에러 응답', async () => {
>   server.use(
>     rest.get('/api/users/:id', (req, res, ctx) => {
>       return res(ctx.status(500));
>     })
>   );
>
>   render(<UserProfile userId="1" />);
>
>   const error = await screen.findByText('사용자 로드 실패');
>   expect(error).toBeInTheDocument();
> });
> ```
>
> **5. 실전 패턴 모음**
>
> **setTimeout 테스트**
>
> ```javascript
> function DelayedMessage() {
>   const [show, setShow] = useState(false);
>
>   useEffect(() => {
>     const timer = setTimeout(() => setShow(true), 2000);
>     return () => clearTimeout(timer);
>   }, []);
>
>   return show ? <div>메시지 표시</div> : null;
> }
>
> // ✅ 실제 타이머 사용
> test('2초 후 메시지 표시', async () => {
>   render(<DelayedMessage />);
>
>   expect(screen.queryByText('메시지 표시')).not.toBeInTheDocument();
>
>   const message = await screen.findByText(
>     '메시지 표시',
>     {},
>     { timeout: 3000 }
>   );
>   expect(message).toBeInTheDocument();
> });
>
> // ✅ Fake 타이머 사용 (빠름)
> test('2초 후 메시지 표시 (Fake Timer)', () => {
>   jest.useFakeTimers();
>   render(<DelayedMessage />);
>
>   expect(screen.queryByText('메시지 표시')).not.toBeInTheDocument();
>
>   act(() => {
>     jest.advanceTimersByTime(2000);
>   });
>
>   expect(screen.getByText('메시지 표시')).toBeInTheDocument();
>
>   jest.useRealTimers();
> });
> ```
>
> **debounce 테스트**
>
> ```javascript
> function SearchInput({ onSearch }) {
>   const [value, setValue] = useState('');
>
>   useEffect(() => {
>     const timer = setTimeout(() => {
>       if (value) onSearch(value);
>     }, 300);
>     return () => clearTimeout(timer);
>   }, [value, onSearch]);
>
>   return <input value={value} onChange={e => setValue(e.target.value)} />;
> }
>
> test('300ms debounce 검색', async () => {
>   jest.useFakeTimers();
>   const onSearch = jest.fn();
>   render(<SearchInput onSearch={onSearch} />);
>
>   const input = screen.getByRole('textbox');
>
>   await userEvent.type(input, 'react');
>
>   // 아직 호출되지 않음
>   expect(onSearch).not.toHaveBeenCalled();
>
>   act(() => {
>     jest.advanceTimersByTime(300);
>   });
>
>   // 1번만 호출됨 (마지막 값으로)
>   expect(onSearch).toHaveBeenCalledTimes(1);
>   expect(onSearch).toHaveBeenCalledWith('react');
>
>   jest.useRealTimers();
> });
> ```
>
> **결론**
>
> | 상황 | 권장 방법 | 이유 |
> |------|----------|------|
> | 단일 요소 대기 | findBy | 간결하고 명확 |
> | 복잡한 조건 | waitFor | 유연성 |
> | 타이머 테스트 | act() + fake timers | 빠르고 정확 |
> | API 호출 | MSW 또는 Mock | 실제 네트워크 호출 방지 |
> | 수동 상태 변경 | act() | React 경고 방지 |

---

(답변 계속...)
