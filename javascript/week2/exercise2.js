/**
 * Exercise 2: 데이터 타입과 타입 변환
 *
 * 지시사항:
 * 1. 각 문제를 실행하기 전에 결과를 예측해보세요
 * 2. 실제로 실행해서 예측과 비교하세요
 * 3. 왜 그런 결과가 나왔는지 exercise2-answer.md에 설명하세요
 */

console.log("=== Exercise 2 시작 ===\n");

// ============================================
// 문제 1: Primitive vs Reference
// ============================================
console.log("--- 문제 1: Primitive vs Reference ---");

let a = 10;
let b = a;
a = 20;

console.log("a:", a);  // 예측: ?
console.log("b:", b);  // 예측: ?

let obj1 = { value: 10 };
let obj2 = obj1;
obj1.value = 20;

console.log("obj1.value:", obj1.value);  // 예측: ?
console.log("obj2.value:", obj2.value);  // 예측: ?


// ============================================
// 문제 2: 타입 변환
// ============================================
console.log("\n--- 문제 2: 타입 변환 ---");

console.log("5" + 3);     // 예측: ?
console.log("5" - 3);     // 예측: ?
console.log("5" * "2");   // 예측: ?
console.log("abc" - 3);   // 예측: ?


// ============================================
// 문제 3: Falsy 값
// ============================================
console.log("\n--- 문제 3: Falsy 값 ---");

const values = [false, 0, "", null, undefined, NaN, "0", [], {}];

console.log("어떤 값들이 false로 변환될까?");
values.forEach((value, index) => {
  console.log(`${index}: ${value} →`, Boolean(value));
});


// ============================================
// 문제 4: == vs ===
// ============================================
console.log("\n--- 문제 4: == vs === ---");

console.log("5 == '5':", 5 == '5');        // 예측: ?
console.log("5 === '5':", 5 === '5');      // 예측: ?
console.log("0 == false:", 0 == false);    // 예측: ?
console.log("0 === false:", 0 === false);  // 예측: ?
console.log("null == undefined:", null == undefined);    // 예측: ?
console.log("null === undefined:", null === undefined);  // 예측: ?


// ============================================
// 문제 5: 객체 비교
// ============================================
console.log("\n--- 문제 5: 객체 비교 ---");

const arr1 = [1, 2, 3];
const arr2 = [1, 2, 3];
const arr3 = arr1;

console.log("arr1 === arr2:", arr1 === arr2);  // 예측: ?
console.log("arr1 === arr3:", arr1 === arr3);  // 예측: ?

const obj3 = { a: 1 };
const obj4 = { a: 1 };

console.log("obj3 === obj4:", obj3 === obj4);  // 예측: ?
console.log("JSON 비교:",
  JSON.stringify(obj3) === JSON.stringify(obj4));  // 예측: ?


// ============================================
// 문제 6: typeof 연산자
// ============================================
console.log("\n--- 문제 6: typeof 연산자 ---");

console.log("typeof 42:", typeof 42);              // 예측: ?
console.log("typeof 'hello':", typeof 'hello');    // 예측: ?
console.log("typeof true:", typeof true);          // 예측: ?
console.log("typeof undefined:", typeof undefined); // 예측: ?
console.log("typeof null:", typeof null);          // 예측: ? (함정!)
console.log("typeof {}:", typeof {});              // 예측: ?
console.log("typeof []:", typeof []);              // 예측: ? (함정!)
console.log("typeof function(){}:", typeof function(){});  // 예측: ?


// ============================================
// 문제 7: 배열 복사
// ============================================
console.log("\n--- 문제 7: 배열 복사 ---");

const original = [1, 2, { a: 3 }];

// 얕은 복사
const shallowCopy = [...original];
shallowCopy[0] = 10;
shallowCopy[2].a = 30;

console.log("original[0]:", original[0]);    // 예측: ?
console.log("original[2].a:", original[2].a);  // 예측: ?


// ============================================
// 문제 8: 암묵적 타입 변환 퀴즈
// ============================================
console.log("\n--- 문제 8: 암묵적 타입 변환 ---");

console.log("[] + []:", [] + []);              // 예측: ?
console.log("[] + {}:", [] + {});              // 예측: ?
console.log("{} + []:", {} + []);              // 예측: ? (브라우저 콘솔과 다를 수 있음)
console.log("true + true:", true + true);      // 예측: ?
console.log("'5' - '2':", '5' - '2');          // 예측: ?
console.log("'5' + - '2':", '5' + - '2');      // 예측: ?


// ============================================
// 보너스 문제: 복잡한 시나리오
// ============================================
console.log("\n--- 보너스 문제 ---");

function mystery(value) {
  if (value) {
    return "Truthy";
  } else {
    return "Falsy";
  }
}

console.log("mystery([]):", mystery([]));          // 예측: ?
console.log("mystery(''):", mystery(''));          // 예측: ?
console.log("mystery('0'):", mystery('0'));        // 예측: ?
console.log("mystery(0):", mystery(0));            // 예측: ?


console.log("\n=== Exercise 2 완료 ===");
console.log("답안을 exercise2-answer.md에 작성하세요!");
