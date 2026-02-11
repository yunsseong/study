# 7. React 테스트

---

## 테스트 피라미드

```
        /\
       /E2\     E2E (10%)
      / E  \    - Cypress, Playwright
     /______\   - 느리고 비용 높음
    /  통합  \   - 전체 사용자 플로우
   / Tests  \
  /   (20%)  \ 통합 테스트 (20%)
 /____________\ - React Testing Library
/    단위      \ - 컴포넌트 상호작용
/    Tests     \
/    (70%)      \ 단위 테스트 (70%)
/________________\ - Jest
                   - 함수, Hooks, 단일 컴포넌트
```

**테스트 비용과 가치**
- 단위 테스트: 빠르고 저렴, 많이 작성
- 통합 테스트: 중간 비용, 실제 사용성 검증
- E2E 테스트: 느리고 비쌈, 핵심 플로우만

---

## Jest

### 기본 구조

```javascript
// describe: 테스트 그룹화
describe('계산기', () => {
  // it/test: 개별 테스트 케이스
  it('두 수를 더한다', () => {
    // 준비 (Arrange)
    const a = 1;
    const b = 2;

    // 실행 (Act)
    const result = add(a, b);

    // 검증 (Assert)
    expect(result).toBe(3);
  });

  test('음수도 처리한다', () => {
    expect(add(-1, -2)).toBe(-3);
  });
});
```

### 주요 Matcher

```javascript
// 동등성
expect(value).toBe(3);                     // ===
expect(value).toEqual({ a: 1 });           // 깊은 비교
expect(value).not.toBe(5);                 // 부정

// Boolean
expect(value).toBeTruthy();                // !!value === true
expect(value).toBeFalsy();                 // !!value === false
expect(value).toBeNull();                  // === null
expect(value).toBeUndefined();             // === undefined
expect(value).toBeDefined();               // !== undefined

// 숫자
expect(value).toBeGreaterThan(3);          // >
expect(value).toBeGreaterThanOrEqual(3);   // >=
expect(value).toBeLessThan(5);             // <
expect(value).toBeLessThanOrEqual(5);      // <=
expect(0.1 + 0.2).toBeCloseTo(0.3);        // 부동소수점

// 문자열
expect('hello world').toMatch(/world/);    // 정규식
expect('hello').toContain('ell');          // 포함

// 배열/반복 가능
expect(['apple', 'banana']).toContain('apple');
expect([1, 2, 3]).toHaveLength(3);

// 예외
expect(() => {
  throw new Error('오류');
}).toThrow('오류');

// 비동기
await expect(asyncFn()).resolves.toBe(value);
await expect(asyncFn()).rejects.toThrow();

// DOM (with @testing-library/jest-dom)
expect(element).toBeInTheDocument();
expect(element).toBeVisible();
expect(element).toHaveTextContent('안녕');
expect(element).toHaveClass('active');
expect(input).toHaveValue('test');
expect(button).toBeDisabled();
```

### 테스트 라이프사이클

```javascript
// 전체 테스트 스위트에서 1번
beforeAll(() => {
  console.log('모든 테스트 시작 전');
  database.connect();
});

afterAll(() => {
  console.log('모든 테스트 완료 후');
  database.disconnect();
});

// 각 테스트마다
beforeEach(() => {
  console.log('각 테스트 시작 전');
  database.clear();
});

afterEach(() => {
  console.log('각 테스트 완료 후');
  jest.clearAllMocks();
});
```

### 비동기 테스트

```javascript
// async/await (권장)
test('비동기 데이터', async () => {
  const data = await fetchData();
  expect(data).toEqual({ id: 1 });
});

// Promise
test('Promise 테스트', () => {
  return fetchData().then(data => {
    expect(data).toEqual({ id: 1 });
  });
});

// callback
test('callback 테스트', done => {
  fetchData((data) => {
    expect(data).toEqual({ id: 1 });
    done();  // 필수!
  });
});
```

---

## React Testing Library

### 철학

> "The more your tests resemble the way your software is used, the more confidence they can give you."

- 사용자 관점에서 테스트
- 구현 세부사항이 아닌 동작 테스트
- 접근성 우선 (a11y)

### 기본 사용법

```javascript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

test('버튼 클릭 테스트', async () => {
  // 1. 렌더링
  render(<Counter />);

  // 2. 요소 찾기
  const button = screen.getByRole('button', { name: '증가' });
  const count = screen.getByText('Count: 0');

  // 3. 검증
  expect(count).toBeInTheDocument();

  // 4. 사용자 상호작용
  await userEvent.click(button);

  // 5. 결과 검증
  expect(screen.getByText('Count: 1')).toBeInTheDocument();
});
```

### 쿼리 우선순위

**1순위: 모든 사용자가 접근 가능**

```javascript
// getByRole (최우선)
screen.getByRole('button', { name: '제출' });
screen.getByRole('heading', { level: 1 });
screen.getByRole('textbox', { name: '이메일' });
screen.getByRole('checkbox', { name: '약관 동의' });

// getByLabelText (폼 요소)
screen.getByLabelText('비밀번호');

// getByPlaceholderText (label 없을 때만)
screen.getByPlaceholderText('이름 입력');

// getByText
screen.getByText('로그인');
screen.getByText(/^Hello/);  // 정규식
```

**2순위: Semantic 쿼리**

```javascript
// getByAltText (이미지)
screen.getByAltText('프로필 사진');

// getByTitle
screen.getByTitle('닫기');
```

**3순위: Test ID (최후의 수단)**

```javascript
screen.getByTestId('custom-element');
```

### 쿼리 변형 (Variants)

| Prefix | 없을 때 | 반환 타입 | 비동기 | 사용 시점 |
|--------|--------|---------|-------|----------|
| getBy | 에러 | Element | ❌ | 반드시 있어야 함 |
| queryBy | null | Element\|null | ❌ | 없을 수도 있음 |
| findBy | 에러 | Promise<Element> | ✅ | 비동기로 나타남 |
| getAllBy | 에러 | Element[] | ❌ | 여러 개, 필수 |
| queryAllBy | [] | Element[] | ❌ | 여러 개, 선택 |
| findAllBy | 에러 | Promise<Element[]> | ✅ | 여러 개, 비동기 |

```javascript
// getBy - 즉시 존재, 필수
const button = screen.getByRole('button');

// queryBy - 즉시 존재, 선택적
const error = screen.queryByText('에러');
expect(error).not.toBeInTheDocument();

// findBy - 비동기로 나타남
const message = await screen.findByText('로딩 완료');

// getAllBy - 여러 개
const items = screen.getAllByRole('listitem');
expect(items).toHaveLength(5);
```

### 이벤트 테스트

**userEvent (권장)**

```javascript
const user = userEvent.setup();

// 클릭
await user.click(button);
await user.dblClick(button);
await user.tripleClick(element);

// 타이핑 (실제 사용자처럼 한 글자씩)
await user.type(input, 'hello');
await user.clear(input);

// 키보드
await user.keyboard('{Enter}');
await user.keyboard('{Shift>}A{/Shift}');  // Shift+A

// 선택
await user.selectOptions(select, 'option1');

// 호버
await user.hover(element);
await user.unhover(element);

// 파일 업로드
await user.upload(fileInput, file);
```

**fireEvent (저수준)**

```javascript
// 단일 이벤트만 발생
fireEvent.click(button);
fireEvent.change(input, { target: { value: 'test' } });
fireEvent.submit(form);
```

### 비동기 테스트

```javascript
// findBy - 간단한 경우
const element = await screen.findByText('완료');

// waitFor - 복잡한 조건
import { waitFor } from '@testing-library/react';

await waitFor(() => {
  expect(screen.getByText('데이터 로드 완료')).toBeInTheDocument();
});

// 타임아웃 설정
await waitFor(
  () => expect(screen.getByText('완료')).toBeInTheDocument(),
  { timeout: 3000 }
);

// act - 수동 상태 업데이트
import { act } from '@testing-library/react';

act(() => {
  jest.advanceTimersByTime(1000);
});
```

---

## 컴포넌트 테스트 전략

### 1. Props 렌더링

```javascript
test('props에 따라 올바르게 렌더링', () => {
  render(<Greeting name="홍길동" />);
  expect(screen.getByText('안녕하세요, 홍길동님')).toBeInTheDocument();
});
```

### 2. 사용자 상호작용

```javascript
test('버튼 클릭 시 함수 호출', async () => {
  const handleClick = jest.fn();
  render(<Button onClick={handleClick}>클릭</Button>);

  await userEvent.click(screen.getByRole('button'));

  expect(handleClick).toHaveBeenCalledTimes(1);
});
```

### 3. 조건부 렌더링

```javascript
test('로그인 상태에 따른 렌더링', () => {
  const { rerender } = render(<Header isLoggedIn={false} />);

  expect(screen.getByText('로그인')).toBeInTheDocument();
  expect(screen.queryByText('로그아웃')).not.toBeInTheDocument();

  rerender(<Header isLoggedIn={true} />);

  expect(screen.queryByText('로그인')).not.toBeInTheDocument();
  expect(screen.getByText('로그아웃')).toBeInTheDocument();
});
```

### 4. API 호출 (MSW)

```javascript
// setupTests.js
import { setupServer } from 'msw/node';
import { rest } from 'msw';

const server = setupServer(
  rest.get('/api/users/:id', (req, res, ctx) => {
    return res(ctx.json({ id: 1, name: '홍길동' }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// test
test('사용자 데이터 로드', async () => {
  render(<UserProfile userId="1" />);

  expect(await screen.findByText('홍길동')).toBeInTheDocument();
});
```

### 5. 폼 검증

```javascript
test('빈 이메일 제출 시 에러 표시', async () => {
  render(<LoginForm />);

  const submitButton = screen.getByRole('button', { name: '로그인' });
  await userEvent.click(submitButton);

  expect(await screen.findByText('이메일을 입력하세요')).toBeInTheDocument();
});
```

---

## Custom Hook 테스트

```javascript
import { renderHook, act } from '@testing-library/react';

// Hook
function useCounter(initialValue = 0) {
  const [count, setCount] = useState(initialValue);

  const increment = () => setCount(c => c + 1);
  const decrement = () => setCount(c => c - 1);

  return { count, increment, decrement };
}

// 테스트
test('useCounter', () => {
  const { result } = renderHook(() => useCounter());

  // 초기값 확인
  expect(result.current.count).toBe(0);

  // 증가
  act(() => {
    result.current.increment();
  });
  expect(result.current.count).toBe(1);

  // 감소
  act(() => {
    result.current.decrement();
  });
  expect(result.current.count).toBe(0);
});

// 인자와 함께
test('초기값 설정', () => {
  const { result } = renderHook(() => useCounter(10));
  expect(result.current.count).toBe(10);
});

// 재렌더링
test('props 변경', () => {
  const { result, rerender } = renderHook(
    ({ initialValue }) => useCounter(initialValue),
    { initialProps: { initialValue: 0 } }
  );

  expect(result.current.count).toBe(0);

  rerender({ initialValue: 10 });
  expect(result.current.count).toBe(10);
});
```

---

## Mocking

### jest.fn()

```javascript
// Mock 함수 생성
const mockFn = jest.fn();

// 반환값 설정
mockFn.mockReturnValue(42);
mockFn.mockResolvedValue('async');
mockFn.mockRejectedValue(new Error('fail'));

// 호출마다 다른 값
mockFn
  .mockReturnValueOnce('first')
  .mockReturnValueOnce('second')
  .mockReturnValue('default');

// 구현 제공
mockFn.mockImplementation((a, b) => a + b);

// 검증
expect(mockFn).toHaveBeenCalled();
expect(mockFn).toHaveBeenCalledTimes(2);
expect(mockFn).toHaveBeenCalledWith('arg1', 'arg2');
```

### jest.mock()

```javascript
// 모듈 전체 Mock
jest.mock('./api');

import { fetchUser } from './api';

fetchUser.mockResolvedValue({ id: 1, name: 'John' });

// 부분 Mock
jest.mock('./utils', () => ({
  ...jest.requireActual('./utils'),
  multiply: jest.fn(() => 999)
}));
```

### jest.spyOn()

```javascript
// 객체 메서드 감시
const spy = jest.spyOn(console, 'log').mockImplementation();

console.log('test');
expect(spy).toHaveBeenCalledWith('test');

spy.mockRestore();  // 원본 복원
```

---

## MSW (Mock Service Worker)

### 설정

```javascript
// src/mocks/handlers.js
import { rest } from 'msw';

export const handlers = [
  rest.get('/api/users/:id', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ id: req.params.id, name: '홍길동' })
    );
  }),

  rest.post('/api/login', (req, res, ctx) => {
    const { email, password } = req.body;

    if (email === 'test@example.com' && password === 'password') {
      return res(
        ctx.status(200),
        ctx.json({ token: 'fake-token' })
      );
    }

    return res(
      ctx.status(401),
      ctx.json({ message: '인증 실패' })
    );
  })
];
```

```javascript
// src/setupTests.js
import { setupServer } from 'msw/node';
import { handlers } from './mocks/handlers';

const server = setupServer(...handlers);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

### 테스트

```javascript
test('사용자 데이터 로드', async () => {
  render(<UserProfile userId="1" />);

  expect(await screen.findByText('홍길동')).toBeInTheDocument();
});

test('런타임에 핸들러 오버라이드', async () => {
  server.use(
    rest.get('/api/users/:id', (req, res, ctx) => {
      return res(ctx.status(500));
    })
  );

  render(<UserProfile userId="1" />);

  expect(await screen.findByText('로드 실패')).toBeInTheDocument();
});
```

---

## 스냅샷 테스트

### 기본 사용

```javascript
test('컴포넌트 스냅샷', () => {
  const { container } = render(<Button>클릭</Button>);
  expect(container).toMatchSnapshot();
});

// __snapshots__/Button.test.js.snap 파일 생성
// exports[`컴포넌트 스냅샷 1`] = `
// <div>
//   <button>
//     클릭
//   </button>
// </div>
// `;
```

### 인라인 스냅샷

```javascript
test('인라인 스냅샷', () => {
  const data = { name: 'John', age: 30 };
  expect(data).toMatchInlineSnapshot(`
    {
      "age": 30,
      "name": "John",
    }
  `);
});
```

### 장단점

**장점**
- 빠르게 회귀 테스트 작성
- UI 변경 감지

**단점**
- 큰 스냅샷은 리뷰 어려움
- 무분별한 업데이트 위험
- 의미 있는 검증 부족 가능

**사용 시점**
- 복잡한 JSON 응답 검증
- 에러 메시지 일관성 체크
- 정적 콘텐츠 확인

---

## E2E 테스트

### Playwright

```javascript
import { test, expect } from '@playwright/test';

test('로그인 플로우', async ({ page }) => {
  // 페이지 이동
  await page.goto('http://localhost:3000');

  // 로그인 폼 작성
  await page.getByLabel('이메일').fill('test@example.com');
  await page.getByLabel('비밀번호').fill('password123');
  await page.getByRole('button', { name: '로그인' }).click();

  // 리다이렉트 확인
  await expect(page).toHaveURL(/.*dashboard/);

  // 요소 표시 확인
  await expect(page.getByText('환영합니다')).toBeVisible();

  // 스크린샷
  await page.screenshot({ path: 'dashboard.png' });
});

// 멀티 브라우저
test('크로스 브라우저 테스트', async ({ browserName, page }) => {
  console.log(`Testing on ${browserName}`);
  // Chromium, Firefox, Webkit에서 모두 실행
});
```

### Cypress

```javascript
describe('로그인 플로우', () => {
  it('사용자가 로그인할 수 있다', () => {
    cy.visit('/login');

    cy.get('[name="email"]').type('test@example.com');
    cy.get('[name="password"]').type('password123');
    cy.get('button').contains('로그인').click();

    cy.url().should('include', '/dashboard');
    cy.contains('환영합니다').should('be.visible');
  });

  // API Mocking
  it('API Stub', () => {
    cy.intercept('GET', '/api/users', {
      fixture: 'users.json'
    }).as('getUsers');

    cy.visit('/users');
    cy.wait('@getUsers');

    cy.get('.user-list').should('have.length', 5);
  });
});
```

---

## Storybook

### 설정

```bash
npx storybook@latest init
```

### 스토리 작성

```javascript
// Button.stories.jsx
import Button from './Button';

export default {
  title: 'Components/Button',
  component: Button,
  argTypes: {
    variant: {
      control: 'select',
      options: ['primary', 'secondary', 'danger']
    }
  }
};

// 기본 스토리
export const Primary = {
  args: {
    variant: 'primary',
    children: '클릭하세요'
  }
};

export const Secondary = {
  args: {
    variant: 'secondary',
    children: '취소'
  }
};

export const Disabled = {
  args: {
    disabled: true,
    children: '비활성화'
  }
};

// 인터랙션 테스트
import { userEvent, within } from '@storybook/testing-library';
import { expect } from '@storybook/jest';

export const ClickTest = {
  args: {
    children: '클릭 테스트'
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button');

    await userEvent.click(button);

    await expect(button).toHaveClass('clicked');
  }
};
```

---

## 테스트 전략 가이드

| 테스트 종류 | 도구 | 테스트 대상 | 실행 속도 | 작성 비용 | 신뢰도 |
|------------|------|-----------|---------|---------|--------|
| 단위 테스트 | Jest | 유틸 함수, Hooks | 매우 빠름 | 낮음 | 중간 |
| 컴포넌트 테스트 | RTL + Jest | 개별 컴포넌트 | 빠름 | 중간 | 높음 |
| 통합 테스트 | RTL + MSW | 컴포넌트 상호작용 | 중간 | 중간 | 높음 |
| E2E 테스트 | Playwright | 전체 사용자 플로우 | 느림 | 높음 | 매우 높음 |
| 시각적 테스트 | Storybook | UI 컴포넌트 | 빠름 | 낮음 | 중간 |

---

## TDD 프로세스

```
1. Red (실패하는 테스트 작성)
   ↓
2. Green (테스트를 통과하는 최소 코드)
   ↓
3. Refactor (코드 개선)
   ↓
   반복
```

### 예시

```javascript
// 1. Red - 테스트 먼저 작성
test('할 일을 추가할 수 있다', () => {
  render(<TodoList />);

  const input = screen.getByRole('textbox');
  const button = screen.getByRole('button', { name: '추가' });

  userEvent.type(input, '우유 사기');
  userEvent.click(button);

  expect(screen.getByText('우유 사기')).toBeInTheDocument();
});

// 2. Green - 최소 구현
function TodoList() {
  const [todos, setTodos] = useState([]);
  const [input, setInput] = useState('');

  const handleAdd = () => {
    setTodos([...todos, input]);
    setInput('');
  };

  return (
    <div>
      <input value={input} onChange={e => setInput(e.target.value)} />
      <button onClick={handleAdd}>추가</button>
      {todos.map((todo, i) => <div key={i}>{todo}</div>)}
    </div>
  );
}

// 3. Refactor - 개선
// - 상태 관리 개선
// - 접근성 추가 (label, role)
// - 스타일링
```

---

## 테스트 커버리지

### 실행

```bash
npm test -- --coverage
```

### 출력

```
------------------|---------|----------|---------|---------|-------------------
File              | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
------------------|---------|----------|---------|---------|-------------------
All files         |   87.5  |   75.0   |   90.0  |   86.2  |
 components/      |   78.3  |   62.5   |   80.0  |   77.1  |
  Button.jsx      |   85.7  |   75.0   |   100   |   84.2  | 23, 45-48
  Form.jsx        |   71.4  |   50.0   |   60.0  |   70.5  | 12-15, 28-35
 utils/           |   95.5  |   88.2   |   100   |   94.8  |
  math.js         |   100   |   100    |   100   |   100   |
------------------|---------|----------|---------|---------|-------------------
```

### 목표 설정

```javascript
// jest.config.js
module.exports = {
  coverageThreshold: {
    global: {
      statements: 80,
      branches: 80,
      functions: 80,
      lines: 80
    }
  }
};
```

**주의사항**
- 100% 커버리지 ≠ 완벽한 테스트
- Edge Case 검증이 더 중요
- 의미 있는 테스트에 집중

---

## 면접 핵심 정리

### 1. 테스트 철학
- **사용자 관점**: 구현이 아닌 사용자 경험 테스트
- **접근성**: getByRole, getByLabelText 우선 사용
- **통합 테스트**: 독립된 단위보다 통합적 동작 검증

### 2. 주요 API
```javascript
// 쿼리 선택
getBy    // 즉시 존재, 필수
queryBy  // 즉시 존재, 선택적
findBy   // 비동기, 필수

// 상호작용
userEvent.click()  // 실제 사용자처럼
userEvent.type()   // 한 글자씩 입력

// 비동기
await screen.findByText()
await waitFor(() => expect())
```

### 3. Mocking 전략
```javascript
jest.fn()       // Mock 함수
jest.mock()     // 모듈 Mock
jest.spyOn()    // 메서드 감시
MSW             // API Mock (권장)
```

### 4. 테스트 우선순위
1. 핵심 비즈니스 로직
2. 사용자 주요 플로우
3. 에러 처리
4. 엣지 케이스

### 5. 실무 팁
- **통합 테스트 중심**: 단위 테스트보다 통합 테스트에 집중
- **MSW 활용**: API Mocking은 MSW 사용
- **접근성 개선**: 테스트가 접근성 향상 유도
- **CI/CD 통합**: 자동화된 테스트 실행 환경 구축

---

## 자주 하는 실수

### ❌ 구현 세부사항 테스트

```javascript
// 나쁜 예
expect(wrapper.state('count')).toBe(1);

// 좋은 예
expect(screen.getByText('Count: 1')).toBeInTheDocument();
```

### ❌ 과도한 test-id 사용

```javascript
// 나쁜 예
screen.getByTestId('submit-button');

// 좋은 예
screen.getByRole('button', { name: '제출' });
```

### ❌ 비동기 처리 누락

```javascript
// 나쁜 예
test('데이터 로드', () => {
  render(<Component />);
  expect(screen.getByText('데이터')).toBeInTheDocument(); // 에러!
});

// 좋은 예
test('데이터 로드', async () => {
  render(<Component />);
  expect(await screen.findByText('데이터')).toBeInTheDocument();
});
```

### ❌ 스냅샷 남용

```javascript
// 나쁜 예
expect(container).toMatchSnapshot(); // 너무 큼

// 좋은 예
expect(data).toMatchInlineSnapshot(`
  {
    "name": "John",
    "age": 30
  }
`);
```

---

## 추가 리소스

- [React Testing Library 공식 문서](https://testing-library.com/react)
- [Jest 공식 문서](https://jestjs.io/)
- [Testing Library Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [MSW 공식 문서](https://mswjs.io/)
- [Playwright 공식 문서](https://playwright.dev/)
