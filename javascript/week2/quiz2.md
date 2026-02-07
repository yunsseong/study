# Quiz 2: 데이터 타입과 함수

**제한 시간**: 20분
**총 문항**: 12문제

---

## 객관식 문제 (1-6)

### 1. 다음 중 Primitive 타입이 아닌 것은?

a) String
b) Number
c) Array
d) Boolean

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 2. 다음 코드의 출력 결과는?
```javascript
const a = [1, 2, 3];
const b = a;
b.push(4);
console.log(a.length);
```

a) 3
b) 4
c) undefined
d) ReferenceError

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 3. 화살표 함수에 대한 설명으로 틀린 것은?

a) 간결한 문법을 제공한다
b) 자신만의 this 바인딩을 가진다
c) 호이스팅되지 않는다
d) arguments 객체가 없다

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 4. 다음 중 Falsy 값이 아닌 것은?

a) 0
b) ""
c) "0"
d) null

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 5. 다음 코드의 출력 결과는?
```javascript
console.log(typeof null);
```

a) "null"
b) "object"
c) "undefined"
d) "number"

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 6. Rest 파라미터에 대한 설명으로 올바른 것은?

a) 함수의 마지막 파라미터에만 사용 가능
b) 여러 개 사용 가능
c) 배열이 아닌 유사 배열 객체
d) 화살표 함수에서 사용 불가

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

## 코드 분석 문제 (7-10)

### 7. 다음 코드의 출력 결과를 예측하고 설명하세요
```javascript
const obj = { a: 1 };
const copy = { ...obj };
copy.a = 2;

console.log(obj.a);
console.log(copy.a);
```

**출력 결과**:
```
obj.a: [여기에 작성]
copy.a: [여기에 작성]
```

**설명**: [왜 그런 결과가 나오는지 설명]

---

### 8. 다음 코드를 수정하여 의도대로 작동하게 하세요
```javascript
// 문제: 1초 후 "Hello, John" 출력하려고 함
const person = {
  name: "John",
  greet: () => {
    setTimeout(() => {
      console.log(`Hello, ${this.name}`);
    }, 1000);
  }
};

person.greet();  // "Hello, undefined" 출력됨
```

**수정된 코드**:
```javascript
// 여기에 수정된 코드 작성
```

**설명**: [무엇을 수정했고 왜 그렇게 했는지 설명]

---

### 9. 다음 함수의 문제점을 찾고 개선하세요
```javascript
function getData(url, onSuccess, onError, timeout, retries) {
  // 함수 구현...
}

// 사용 예시 (파라미터가 너무 많음)
getData("https://api.com", handleSuccess, handleError, 5000, 3);
```

**개선된 코드**:
```javascript
// 여기에 개선된 코드 작성 (힌트: 객체 파라미터 사용)
```

---

### 10. 다음 고차 함수를 구현하세요
```javascript
// 함수를 n번 실행하는 repeat 함수
function repeat(n, func) {
  // 여기에 구현
}

// 사용 예시
repeat(3, () => console.log("Hello"));
// 출력:
// Hello
// Hello
// Hello
```

**구현**:
```javascript
// 여기에 코드 작성
```

---

## 실전 문제 (11-12)

### 11. 다음 요구사항을 만족하는 함수를 작성하세요

**요구사항**:
- 배열에서 특정 조건을 만족하는 요소들의 합을 구하는 함수
- 고차 함수(map, filter, reduce) 활용

```javascript
const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// 짝수들의 제곱의 합을 구하세요
// 예상 결과: 2²+4²+6²+8²+10² = 4+16+36+64+100 = 220
```

**코드**:
```javascript
// 여기에 코드 작성
```

---

### 12. 클로저를 활용한 private 변수 구현

**요구사항**:
- balance를 외부에서 직접 접근 못 하게 하기
- deposit(금액), withdraw(금액), getBalance() 메서드 제공
- 잔액 부족 시 withdraw는 false 반환

```javascript
function createAccount(initialBalance) {
  // 여기에 구현
}

// 사용 예시
const account = createAccount(1000);
console.log(account.deposit(500));    // 1500
console.log(account.withdraw(300));   // 1200
console.log(account.withdraw(2000));  // false (잔액 부족)
console.log(account.getBalance());    // 1200
console.log(account.balance);         // undefined (외부 접근 불가)
```

**코드**:
```javascript
// 여기에 코드 작성
```

---

## 채점 기준

- 객관식 (1-6): 각 8점 = 48점
- 코드 분석 (7-10): 각 10점 = 40점
- 실전 문제 (11-12): 각 6점 = 12점

**총점**: 100점
**합격 기준**: 70점 이상

---

## 정답 확인

답안을 모두 작성한 후, Claude에게 채점을 요청하세요!
