# Lesson 1: 변수 선언과 호이스팅

## 📖 학습 목표
- var, let, const의 차이점 이해
- 스코프(함수 스코프 vs 블록 스코프) 개념 파악
- 호이스팅 동작 원리 이해
- TDZ(Temporal Dead Zone) 개념 학습

---

## 1. 변수 선언 방식의 차이

자바스크립트에는 3가지 변수 선언 방식이 있습니다:

```javascript
var name = "John";      // ES5 방식 (피해야 할 방식)
let age = 25;           // ES6+ 재할당 가능
const PI = 3.14;        // ES6+ 재할당 불가능
```

### 핵심 차이점

| 특성 | var | let | const |
|------|-----|-----|-------|
| 스코프 | 함수 스코프 | 블록 스코프 | 블록 스코프 |
| 재선언 | 가능 ❌ | 불가능 ✅ | 불가능 ✅ |
| 재할당 | 가능 | 가능 | 불가능 |
| 호이스팅 | undefined | TDZ 에러 | TDZ 에러 |

---

## 2. 스코프 (Scope)

### 함수 스코프 vs 블록 스코프

```javascript
// var: 함수 스코프 (블록 무시)
function varTest() {
  var x = 1;
  if (true) {
    var x = 2;  // 같은 변수!
    console.log(x);  // 2
  }
  console.log(x);  // 2 (덮어씌워짐)
}

// let: 블록 스코프
function letTest() {
  let x = 1;
  if (true) {
    let x = 2;  // 다른 변수!
    console.log(x);  // 2
  }
  console.log(x);  // 1 (영향 없음)
}
```

### 왜 중요한가?

```javascript
// 클래식 var 버그
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100);
}
// 출력: 3, 3, 3 (예상: 0, 1, 2)

// let으로 해결
for (let i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100);
}
// 출력: 0, 1, 2 ✅
```

---

## 3. 호이스팅 (Hoisting)

**호이스팅이란?** 변수와 함수 선언이 코드 실행 전에 해당 스코프 최상단으로 "끌어올려지는" 현상

### var 호이스팅

```javascript
console.log(name);  // undefined (에러 아님!)
var name = "John";

// 실제 동작 (자바스크립트 엔진이 해석하는 방식)
var name;           // 선언이 끌어올려짐
console.log(name);  // undefined
name = "John";      // 할당은 원래 위치
```

### let/const 호이스팅

```javascript
console.log(age);  // ReferenceError: Cannot access 'age' before initialization
let age = 25;

// let도 호이스팅되지만, TDZ(Temporal Dead Zone)에 걸림
```

### Temporal Dead Zone (TDZ)

```javascript
{
  // TDZ 시작
  console.log(name);  // ❌ ReferenceError

  let name = "John";  // TDZ 종료
  console.log(name);  // ✅ "John"
}
```

---

## 4. 실전 예제

```javascript
// ❌ 나쁜 예: var 사용
function calculateTotal() {
  var total = 0;

  for (var i = 0; i < 5; i++) {
    var total = total + i;  // 실수로 재선언 (에러 없음)
  }

  console.log(i);  // 5 (루프 밖에서도 접근 가능 - 버그 가능성)
  return total;
}

// ✅ 좋은 예: const/let 사용
function calculateTotal() {
  let total = 0;  // 재할당 필요하므로 let

  for (let i = 0; i < 5; i++) {
    total = total + i;  // 정상 작동
  }

  // console.log(i);  // ❌ ReferenceError (안전!)
  return total;
}

// ✅ 더 좋은 예: const 우선
function calculateTotal() {
  const numbers = [1, 2, 3, 4, 5];
  const total = numbers.reduce((sum, num) => sum + num, 0);
  return total;
}
```

---

## 💡 핵심 정리

1. **const 우선 사용**: 재할당이 필요없으면 const
2. **재할당 필요시 let**: 값이 바뀌어야 하면 let
3. **var는 피하기**: 레거시 코드가 아니면 사용하지 않기
4. **호이스팅 이해**: 선언이 끌어올려진다는 점 기억
5. **블록 스코프 활용**: let/const로 안전한 스코프 관리

---

## 📚 참고 자료
- [MDN: var](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/var)
- [MDN: let](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/let)
- [MDN: const](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/const)
- [MDN: 호이스팅](https://developer.mozilla.org/ko/docs/Glossary/Hoisting)
