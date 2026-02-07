# Lesson 2: 데이터 타입과 타입 변환

## 📖 학습 목표
- Primitive 타입과 Reference 타입의 차이 이해
- 타입 변환 메커니즘 파악
- typeof 연산자와 타입 체크 방법 학습
- == vs === 차이 이해

---

## 1. 자바스크립트의 데이터 타입

### 📊 타입 분류

```javascript
// Primitive 타입 (원시 타입) - 7가지
let num = 42;              // Number
let str = "Hello";         // String
let bool = true;           // Boolean
let nothing = null;        // null
let notDefined;            // undefined
let sym = Symbol("id");    // Symbol (ES6)
let bigNum = 9007199254740991n;  // BigInt (ES11)

// Reference 타입 (참조 타입)
let obj = { name: "John" };     // Object
let arr = [1, 2, 3];            // Array
let func = function() { };      // Function
```

---

## 2. Primitive vs Reference

### Primitive 타입 (값 복사)

```javascript
// 값 자체가 복사됨
let a = 10;
let b = a;  // a의 값(10)을 복사

a = 20;

console.log(a);  // 20
console.log(b);  // 10 (영향 없음)
```

**메모리 구조**:
```
[메모리]
a → [20]
b → [10]  (별도의 메모리 공간)
```

### Reference 타입 (참조 복사)

```javascript
// 참조(주소)가 복사됨
let obj1 = { value: 10 };
let obj2 = obj1;  // obj1의 주소를 복사

obj1.value = 20;

console.log(obj1.value);  // 20
console.log(obj2.value);  // 20 (같은 객체를 참조)
```

**메모리 구조**:
```
[메모리]
obj1 → [주소: 0x001] → { value: 20 }
obj2 → [주소: 0x001] → (같은 객체)
```

---

## 3. 타입별 상세 설명

### Number

```javascript
// 정수와 실수 구분 없음
let integer = 42;
let float = 3.14;
let negative = -100;

// 특수 값
let infinity = Infinity;
let negInfinity = -Infinity;
let notANumber = NaN;  // Not a Number

// 연산
console.log(0.1 + 0.2);  // 0.30000000000000004 (부동소수점 오차)
console.log(10 / 0);     // Infinity
console.log("abc" * 3);  // NaN
```

**주의사항**:
```javascript
console.log(NaN === NaN);  // false
console.log(isNaN(NaN));   // true (NaN 체크 방법)
console.log(Number.isNaN(NaN));  // true (더 안전)
```

### String

```javascript
// 문자열 생성
let single = 'Hello';
let double = "World";
let template = `Hello ${name}`;  // 템플릿 리터럴

// 문자열은 불변(immutable)
let str = "Hello";
str[0] = "h";  // 동작하지 않음
console.log(str);  // "Hello"

// 새로운 문자열 생성
str = str.toLowerCase();  // "hello"
```

### Boolean

```javascript
let isTrue = true;
let isFalse = false;

// Falsy 값들 (false로 변환되는 값)
Boolean(false);      // false
Boolean(0);          // false
Boolean(-0);         // false
Boolean(0n);         // false
Boolean("");         // false
Boolean(null);       // false
Boolean(undefined);  // false
Boolean(NaN);        // false

// Truthy 값 (나머지 모두)
Boolean(1);          // true
Boolean("0");        // true
Boolean("false");    // true
Boolean([]);         // true
Boolean({});         // true
```

### null vs undefined

```javascript
// undefined: 값이 할당되지 않음
let x;
console.log(x);  // undefined

function noReturn() { }
console.log(noReturn());  // undefined

// null: 의도적으로 빈 값
let y = null;
console.log(y);  // null

// 차이점
console.log(typeof undefined);  // "undefined"
console.log(typeof null);       // "object" (언어 설계 오류)

console.log(undefined == null);   // true
console.log(undefined === null);  // false
```

---

## 4. 타입 변환

### 명시적 타입 변환 (개발자가 직접)

```javascript
// String 변환
String(123);        // "123"
String(true);       // "true"
(123).toString();   // "123"

// Number 변환
Number("123");      // 123
Number("123abc");   // NaN
Number(true);       // 1
Number(false);      // 0
parseInt("123px");  // 123
parseFloat("3.14"); // 3.14

// Boolean 변환
Boolean(1);         // true
Boolean(0);         // false
Boolean("hello");   // true
Boolean("");        // false
```

### 암묵적 타입 변환 (자동 변환)

```javascript
// 문자열 + 숫자 = 문자열
console.log("5" + 3);      // "53"
console.log("Hello" + 1);  // "Hello1"

// 문자열 - 숫자 = 숫자
console.log("5" - 3);      // 2
console.log("10" * "2");   // 20
console.log("10" / "2");   // 5

// Boolean 컨텍스트
if ("hello") {  // "hello" → true
  console.log("실행됨");
}

// 주의: + 연산자의 특수성
console.log(1 + 2 + "3");    // "33" (1+2=3, 3+"3"="33")
console.log("1" + 2 + 3);    // "123" ("1"+2="12", "12"+3="123")
```

---

## 5. typeof 연산자

```javascript
// Primitive 타입
console.log(typeof 42);          // "number"
console.log(typeof "hello");     // "string"
console.log(typeof true);        // "boolean"
console.log(typeof undefined);   // "undefined"
console.log(typeof Symbol());    // "symbol"
console.log(typeof 123n);        // "bigint"

// Reference 타입
console.log(typeof {});          // "object"
console.log(typeof []);          // "object" (배열도!)
console.log(typeof function(){}); // "function"
console.log(typeof null);        // "object" (버그)

// 배열 체크
Array.isArray([]);   // true
Array.isArray({});   // false
```

---

## 6. == vs ===

### === (엄격한 동등)

```javascript
// 타입과 값이 모두 같아야 함
console.log(5 === 5);        // true
console.log(5 === "5");      // false (타입 다름)
console.log(true === 1);     // false
console.log(null === undefined);  // false
```

### == (느슨한 동등)

```javascript
// 타입 변환 후 비교
console.log(5 == "5");       // true ("5" → 5)
console.log(true == 1);      // true (true → 1)
console.log(false == 0);     // true (false → 0)
console.log(null == undefined);  // true (특별 규칙)

// 예상 못한 결과
console.log("" == 0);        // true
console.log("0" == 0);       // true
console.log("" == "0");      // false
```

**권장사항**: 항상 `===` 사용!

---

## 7. 객체와 배열의 비교

```javascript
// 참조 비교
const obj1 = { a: 1 };
const obj2 = { a: 1 };
const obj3 = obj1;

console.log(obj1 === obj2);  // false (다른 객체)
console.log(obj1 === obj3);  // true (같은 객체)

// 배열도 마찬가지
const arr1 = [1, 2, 3];
const arr2 = [1, 2, 3];

console.log(arr1 === arr2);  // false (다른 배열)
```

**객체 내용 비교**:
```javascript
// 직접 비교
function compareObjects(obj1, obj2) {
  const keys1 = Object.keys(obj1);
  const keys2 = Object.keys(obj2);

  if (keys1.length !== keys2.length) return false;

  for (let key of keys1) {
    if (obj1[key] !== obj2[key]) return false;
  }

  return true;
}

console.log(compareObjects({ a: 1 }, { a: 1 }));  // true

// JSON 사용 (간단하지만 제한적)
JSON.stringify({ a: 1 }) === JSON.stringify({ a: 1 });  // true
```

---

## 8. 실전 예제

### 예제 1: 값 복사 vs 참조 복사

```javascript
// Primitive: 값 복사
function modifyPrimitive(value) {
  value = value + 10;
  return value;
}

let num = 5;
console.log(modifyPrimitive(num));  // 15
console.log(num);  // 5 (원본 변경 없음)

// Reference: 참조 복사
function modifyObject(obj) {
  obj.value = obj.value + 10;
  return obj;
}

let myObj = { value: 5 };
console.log(modifyObject(myObj));  // { value: 15 }
console.log(myObj);  // { value: 15 } (원본 변경됨!)
```

### 예제 2: 배열/객체 복사

```javascript
// 얕은 복사 (Shallow Copy)
const original = { a: 1, b: { c: 2 } };

// 방법 1: Spread 연산자
const copy1 = { ...original };

// 방법 2: Object.assign
const copy2 = Object.assign({}, original);

copy1.a = 10;
console.log(original.a);  // 1 (영향 없음)

// 하지만 중첩 객체는...
copy1.b.c = 20;
console.log(original.b.c);  // 20 (영향 있음!)

// 깊은 복사 (Deep Copy)
const deepCopy = JSON.parse(JSON.stringify(original));
deepCopy.b.c = 30;
console.log(original.b.c);  // 20 (영향 없음)
```

---

## 💡 핵심 정리

1. **Primitive 타입**: 값 자체가 복사됨 (독립적)
2. **Reference 타입**: 참조(주소)가 복사됨 (공유됨)
3. **타입 변환**: 명시적으로 하는 게 안전
4. **비교**: 항상 `===` 사용
5. **typeof**: null은 "object"로 나옴 (버그 주의)
6. **Falsy 값**: false, 0, "", null, undefined, NaN
7. **객체 비교**: 참조를 비교하므로 내용이 같아도 false

---

## 🤔 생각해보기

1. `[] == false`의 결과는? 왜 그럴까요?
2. `{} === {}`가 false인 이유는?
3. 깊은 복사를 JSON 방식 말고 다른 방법으로 구현하려면?

---

## 📚 참고 자료

- [MDN: 데이터 타입](https://developer.mozilla.org/ko/docs/Web/JavaScript/Data_structures)
- [MDN: 타입 변환](https://developer.mozilla.org/ko/docs/Glossary/Type_coercion)
- [MDN: 동등 비교](https://developer.mozilla.org/ko/docs/Web/JavaScript/Equality_comparisons_and_sameness)
