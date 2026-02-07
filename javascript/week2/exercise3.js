/**
 * Exercise 3: 함수 심화
 *
 * 지시사항:
 * 1. 각 문제를 실행하기 전에 결과를 예측해보세요
 * 2. 실제로 실행해서 예측과 비교하세요
 * 3. 왜 그런 결과가 나왔는지 exercise3-answer.md에 설명하세요
 */

console.log("=== Exercise 3 시작 ===\n");

// ============================================
// 문제 1: 함수 호이스팅
// ============================================
console.log("--- 문제 1: 함수 호이스팅 ---");

try {
  console.log("함수 선언식:", declaredFunc());
} catch (e) {
  console.log("에러:", e.message);
}

try {
  console.log("함수 표현식:", expressedFunc());
} catch (e) {
  console.log("에러:", e.message);
}

try {
  console.log("화살표 함수:", arrowFunc());
} catch (e) {
  console.log("에러:", e.message);
}

function declaredFunc() {
  return "선언식";
}

const expressedFunc = function() {
  return "표현식";
};

const arrowFunc = () => "화살표";


// ============================================
// 문제 2: this 바인딩
// ============================================
console.log("\n--- 문제 2: this 바인딩 ---");

const person = {
  name: "John",

  greet: function() {
    console.log("일반 함수:", this.name);
  },

  greetArrow: () => {
    console.log("화살표 함수:", this.name);
  },

  delayedGreet: function() {
    setTimeout(function() {
      console.log("setTimeout 일반:", this.name);
    }, 100);

    setTimeout(() => {
      console.log("setTimeout 화살표:", this.name);
    }, 100);
  }
};

person.greet();
person.greetArrow();
person.delayedGreet();


// ============================================
// 문제 3: 클로저와 함수
// ============================================
console.log("\n--- 문제 3: 클로저 ---");

function createCounter(start) {
  let count = start;

  return {
    increment: function() {
      count++;
      return count;
    },
    decrement: () => {
      count--;
      return count;
    },
    getCount: () => count
  };
}

const counter = createCounter(0);
console.log(counter.increment());  // 예측: ?
console.log(counter.increment());  // 예측: ?
console.log(counter.decrement());  // 예측: ?
console.log(counter.getCount());   // 예측: ?


// ============================================
// 문제 4: 고차 함수
// ============================================
console.log("\n--- 문제 4: 고차 함수 ---");

const numbers = [1, 2, 3, 4, 5];

// map
const doubled = numbers.map(num => num * 2);
console.log("doubled:", doubled);  // 예측: ?

// filter
const evens = numbers.filter(num => num % 2 === 0);
console.log("evens:", evens);  // 예측: ?

// reduce
const sum = numbers.reduce((acc, num) => acc + num, 0);
console.log("sum:", sum);  // 예측: ?

// 체이닝
const result = numbers
  .filter(num => num > 2)
  .map(num => num * 2)
  .reduce((acc, num) => acc + num, 0);
console.log("체이닝 결과:", result);  // 예측: ?


// ============================================
// 문제 5: Rest와 Spread
// ============================================
console.log("\n--- 문제 5: Rest와 Spread ---");

// Rest 파라미터
function sum(...numbers) {
  return numbers.reduce((acc, num) => acc + num, 0);
}

console.log(sum(1, 2, 3));        // 예측: ?
console.log(sum(1, 2, 3, 4, 5));  // 예측: ?

// Spread 연산자
const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];
const combined = [...arr1, ...arr2];
console.log("combined:", combined);  // 예측: ?

const obj1 = { a: 1, b: 2 };
const obj2 = { b: 3, c: 4 };
const merged = { ...obj1, ...obj2 };
console.log("merged:", merged);  // 예측: ? (b는?)


// ============================================
// 문제 6: 기본 파라미터
// ============================================
console.log("\n--- 문제 6: 기본 파라미터 ---");

function greet(name = "Guest", greeting = "Hello") {
  return `${greeting}, ${name}!`;
}

console.log(greet());                    // 예측: ?
console.log(greet("John"));              // 예측: ?
console.log(greet("John", "Hi"));        // 예측: ?
console.log(greet(undefined, "Hey"));    // 예측: ?


// ============================================
// 문제 7: 함수 반환하기
// ============================================
console.log("\n--- 문제 7: 함수 반환 ---");

function makeMultiplier(factor) {
  return function(number) {
    return number * factor;
  };
}

const triple = makeMultiplier(3);
const quadruple = makeMultiplier(4);

console.log(triple(5));     // 예측: ?
console.log(quadruple(5));  // 예측: ?


// ============================================
// 문제 8: 커링
// ============================================
console.log("\n--- 문제 8: 커링 ---");

const curriedAdd = a => b => c => a + b + c;

console.log(curriedAdd(1)(2)(3));  // 예측: ?

const add5 = curriedAdd(5);
const add5And10 = add5(10);
console.log(add5And10(15));  // 예측: ?


// ============================================
// 보너스 문제: 함수 조합
// ============================================
console.log("\n--- 보너스: 함수 조합 ---");

const add1 = x => x + 1;
const double = x => x * 2;
const square = x => x * x;

// 수동 조합
const result1 = square(double(add1(2)));
console.log("수동 조합:", result1);  // 예측: ?

// compose 함수 구현 (오른쪽에서 왼쪽으로)
const compose = (...funcs) => {
  return (value) => {
    return funcs.reduceRight((acc, func) => func(acc), value);
  };
};

const calculate = compose(square, double, add1);
console.log("compose 조합:", calculate(2));  // 예측: ?


console.log("\n=== Exercise 3 완료 ===");
console.log("답안을 exercise3-answer.md에 작성하세요!");
