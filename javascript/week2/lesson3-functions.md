# Lesson 3: 함수 심화

## 📖 학습 목표
- 함수 선언 방식의 차이 이해 (선언식, 표현식, 화살표 함수)
- 일급 객체로서의 함수 특징 파악
- 고차 함수 개념 학습
- 함수 파라미터 다루기 (기본값, Rest, Spread)

---

## 1. 함수 선언 방식

### 함수 선언식 (Function Declaration)

```javascript
function add(a, b) {
  return a + b;
}

console.log(add(2, 3));  // 5
```

**특징**:
- 호이스팅됨 (함수 전체가 끌어올려짐)
- 함수 이름 필수

```javascript
// 선언 전에 호출 가능!
console.log(subtract(5, 3));  // 2

function subtract(a, b) {
  return a - b;
}
```

---

### 함수 표현식 (Function Expression)

```javascript
const add = function(a, b) {
  return a + b;
};

console.log(add(2, 3));  // 5
```

**특징**:
- 호이스팅 안 됨 (변수 선언만 호이스팅)
- 익명 함수 가능

```javascript
// 선언 전에 호출 불가!
console.log(multiply(2, 3));  // ReferenceError

const multiply = function(a, b) {
  return a * b;
};
```

---

### 화살표 함수 (Arrow Function)

```javascript
// 기본 형태
const add = (a, b) => {
  return a + b;
};

// 간결한 형태 (return 생략)
const add = (a, b) => a + b;

// 파라미터 하나일 때 괄호 생략
const square = x => x * x;

// 파라미터 없을 때
const greet = () => console.log("Hello");
```

**특징**:
- 간결한 문법
- `this` 바인딩 없음 (중요!)
- `arguments` 객체 없음

---

## 2. 함수 선언 방식 비교

### 예제: 호이스팅 차이

```javascript
// 선언식: 가능
sayHello();  // "Hello"
function sayHello() {
  console.log("Hello");
}

// 표현식: 불가능
sayBye();  // ReferenceError
const sayBye = function() {
  console.log("Bye");
};

// 화살표: 불가능
greet();  // ReferenceError
const greet = () => console.log("Hi");
```

---

### 예제: this 바인딩 차이

```javascript
const person = {
  name: "John",

  // 일반 함수: this는 person
  sayHello: function() {
    console.log(`Hello, ${this.name}`);
  },

  // 화살표 함수: this는 상위 스코프 (person이 아님!)
  sayBye: () => {
    console.log(`Bye, ${this.name}`);  // undefined
  },

  // 메서드 안의 화살표 함수
  greet: function() {
    setTimeout(() => {
      console.log(`Hi, ${this.name}`);  // "John" (상위의 this)
    }, 1000);
  }
};

person.sayHello();  // "Hello, John"
person.sayBye();    // "Bye, undefined"
person.greet();     // "Hi, John" (1초 후)
```

---

## 3. 일급 객체 (First-Class Object)

자바스크립트에서 함수는 **일급 객체**입니다.

### 특징 1: 변수에 할당 가능

```javascript
const greet = function(name) {
  return `Hello, ${name}`;
};

console.log(greet("John"));  // "Hello, John"
```

### 특징 2: 함수의 인자로 전달 가능

```javascript
function execute(func, value) {
  return func(value);
}

const double = x => x * 2;
console.log(execute(double, 5));  // 10
```

### 특징 3: 함수에서 반환 가능

```javascript
function makeMultiplier(factor) {
  return function(number) {
    return number * factor;
  };
}

const triple = makeMultiplier(3);
console.log(triple(5));  // 15
```

### 특징 4: 객체의 프로퍼티로 저장 가능

```javascript
const calculator = {
  add: (a, b) => a + b,
  subtract: (a, b) => a - b,
  multiply: (a, b) => a * b
};

console.log(calculator.add(2, 3));  // 5
```

---

## 4. 고차 함수 (Higher-Order Function)

> 함수를 인자로 받거나 함수를 반환하는 함수

### 예제 1: 함수를 인자로 받기

```javascript
// 배열 메서드들은 고차 함수
const numbers = [1, 2, 3, 4, 5];

// map: 각 요소를 변환
const doubled = numbers.map(num => num * 2);
console.log(doubled);  // [2, 4, 6, 8, 10]

// filter: 조건에 맞는 요소만
const evens = numbers.filter(num => num % 2 === 0);
console.log(evens);  // [2, 4]

// reduce: 누적 연산
const sum = numbers.reduce((acc, num) => acc + num, 0);
console.log(sum);  // 15
```

### 예제 2: 함수를 반환하기

```javascript
function makeAdder(x) {
  return function(y) {
    return x + y;
  };
}

const add5 = makeAdder(5);
console.log(add5(3));  // 8
console.log(add5(10)); // 15
```

### 예제 3: 실용적인 고차 함수

```javascript
// 로깅 데코레이터
function withLogging(func) {
  return function(...args) {
    console.log(`실행: ${func.name}(${args})`);
    const result = func(...args);
    console.log(`결과: ${result}`);
    return result;
  };
}

const add = (a, b) => a + b;
const loggedAdd = withLogging(add);

loggedAdd(2, 3);
// 실행: add(2,3)
// 결과: 5
```

---

## 5. 함수 파라미터

### 기본 파라미터 (Default Parameters)

```javascript
// ES5 방식
function greet(name) {
  name = name || "Guest";
  console.log(`Hello, ${name}`);
}

// ES6 방식
function greet(name = "Guest") {
  console.log(`Hello, ${name}`);
}

greet();         // "Hello, Guest"
greet("John");   // "Hello, John"
```

### Rest 파라미터 (나머지 파라미터)

```javascript
// 가변 인자 함수
function sum(...numbers) {
  return numbers.reduce((acc, num) => acc + num, 0);
}

console.log(sum(1, 2, 3));        // 6
console.log(sum(1, 2, 3, 4, 5));  // 15

// 일부 + 나머지
function greetAll(greeting, ...names) {
  names.forEach(name => {
    console.log(`${greeting}, ${name}`);
  });
}

greetAll("Hello", "John", "Jane", "Bob");
// Hello, John
// Hello, Jane
// Hello, Bob
```

### Spread 연산자 (전개 연산자)

```javascript
// 배열 전개
const numbers = [1, 2, 3];
console.log(Math.max(...numbers));  // 3

// 배열 합치기
const arr1 = [1, 2];
const arr2 = [3, 4];
const combined = [...arr1, ...arr2];
console.log(combined);  // [1, 2, 3, 4]

// 객체 전개
const person = { name: "John", age: 30 };
const employee = { ...person, job: "Developer" };
console.log(employee);  // { name: "John", age: 30, job: "Developer" }
```

---

## 6. arguments 객체 vs Rest 파라미터

### arguments (구식)

```javascript
function sum() {
  let total = 0;
  for (let i = 0; i < arguments.length; i++) {
    total += arguments[i];
  }
  return total;
}

console.log(sum(1, 2, 3));  // 6
```

**문제점**:
- 배열처럼 보이지만 배열이 아님
- 화살표 함수에서 사용 불가

### Rest 파라미터 (최신)

```javascript
const sum = (...numbers) => {
  return numbers.reduce((acc, num) => acc + num, 0);
};

console.log(sum(1, 2, 3));  // 6
```

**장점**:
- 진짜 배열
- 화살표 함수에서도 사용 가능
- 명시적이고 가독성 좋음

---

## 7. 함수 조합 (Function Composition)

```javascript
// 간단한 함수들
const double = x => x * 2;
const square = x => x * x;
const addOne = x => x + 1;

// 수동 조합
const result1 = addOne(square(double(3)));
console.log(result1);  // 37 (3 → 6 → 36 → 37)

// 조합 함수 만들기
const compose = (...funcs) => {
  return (value) => {
    return funcs.reduceRight((acc, func) => func(acc), value);
  };
};

const calculate = compose(addOne, square, double);
console.log(calculate(3));  // 37
```

---

## 8. 실전 예제

### 예제 1: 콜백 함수

```javascript
function fetchData(url, callback) {
  console.log(`Fetching ${url}...`);

  setTimeout(() => {
    const data = { id: 1, name: "John" };
    callback(data);
  }, 1000);
}

fetchData("https://api.example.com/user", (data) => {
  console.log("데이터:", data);
});
```

### 예제 2: 커링 (Currying)

```javascript
// 일반 함수
const add = (a, b, c) => a + b + c;
console.log(add(1, 2, 3));  // 6

// 커링된 함수
const curriedAdd = a => b => c => a + b + c;
console.log(curriedAdd(1)(2)(3));  // 6

// 부분 적용
const add1 = curriedAdd(1);
const add1And2 = add1(2);
console.log(add1And2(3));  // 6
```

### 예제 3: 메모이제이션 (Memoization)

```javascript
function memoize(func) {
  const cache = {};

  return function(...args) {
    const key = JSON.stringify(args);

    if (cache[key]) {
      console.log("캐시에서 반환");
      return cache[key];
    }

    console.log("계산 수행");
    const result = func(...args);
    cache[key] = result;
    return result;
  };
}

const slowFibonacci = (n) => {
  if (n <= 1) return n;
  return slowFibonacci(n - 1) + slowFibonacci(n - 2);
};

const fastFibonacci = memoize(slowFibonacci);

console.log(fastFibonacci(10));  // 계산 수행
console.log(fastFibonacci(10));  // 캐시에서 반환
```

---

## 💡 핵심 정리

### 함수 선언 방식
1. **선언식**: 호이스팅됨, 전통적
2. **표현식**: 호이스팅 안 됨, 변수처럼 다룸
3. **화살표**: 간결, this 없음, 최신

### 언제 무엇을 사용할까?

| 상황 | 추천 |
|------|------|
| 일반 함수 | 화살표 함수 |
| 메서드 | function 키워드 |
| 콜백 | 화살표 함수 |
| this 필요 | function 키워드 |
| 호이스팅 필요 | 함수 선언식 |

### 함수의 특징
1. **일급 객체**: 변수처럼 다룰 수 있음
2. **고차 함수**: 함수를 인자로, 반환값으로
3. **클로저**: 외부 변수 기억
4. **조합 가능**: 작은 함수들을 조합

---

## 🤔 생각해보기

1. 화살표 함수를 메서드로 사용하면 안 되는 이유는?
2. Rest 파라미터와 Spread 연산자의 차이는?
3. 함수 조합이 유용한 이유는?

---

## 📚 참고 자료

- [MDN: 함수](https://developer.mozilla.org/ko/docs/Web/JavaScript/Guide/Functions)
- [MDN: 화살표 함수](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Functions/Arrow_functions)
- [MDN: Rest 파라미터](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Functions/rest_parameters)
- [JavaScript.info: 함수](https://ko.javascript.info/function-basics)
