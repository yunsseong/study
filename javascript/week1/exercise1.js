/**
 * Exercise 1: 변수 선언과 호이스팅
 *
 * 지시사항:
 * 1. 각 문제를 실행하기 전에 결과를 예측해보세요
 * 2. 실제로 실행해서 예측과 비교하세요
 * 3. 왜 그런 결과가 나왔는지 exercise1-answer.md에 설명하세요
 */

console.log("=== Exercise 1 시작 ===\n");

// ============================================
// 문제 1: 호이스팅 동작 이해
// ============================================
console.log("--- 문제 1 ---");
function test1() {
  console.log("a:", a);  // 예측: ?
  console.log("b:", b);  // 예측: ?
  var a = 1;
  let b = 2;
}

//test1(); // 주석 해제하고 실행해보세요


// ============================================
// 문제 2: 함수 스코프와 변수 섀도잉
// ============================================
console.log("\n--- 문제 2 ---");
var x = 1;
function test2() {
  console.log("첫 번째 x:", x);  // 예측: ?
  var x = 2;
  console.log("두 번째 x:", x);  // 예측: ?
}

test2(); // 주석 해제하고 실행해보세요


// ============================================
// 문제 3: 클로저와 var의 함정
// ============================================
console.log("\n--- 문제 3 ---");
console.log("3초 후 결과를 확인하세요:");

for (var i = 0; i < 3; i++) {
  setTimeout(function() {
    console.log("var i:", i);  // 예측: ?
  }, 1000);
}

// 비교: let 사용
for (let j = 0; j < 3; j++) {
  setTimeout(function() {
    console.log("let j:", j);  // 예측: ?
  }, 1000);
}


// ============================================
// 문제 4: const의 특성
// ============================================
console.log("\n--- 문제 4 ---");
const obj = { name: "John" };

// 각 줄이 가능한지 예측하고 실행해보세요
try {
  obj.name = "Jane";  // 가능한가?
  console.log("obj.name 변경:", obj.name);
} catch (e) {
  console.log("에러:", e.message);
}

try {
  obj.age = 30;  // 가능한가?
  console.log("obj.age 추가:", obj.age);
} catch (e) {
  console.log("에러:", e.message);
}

try {
  obj = { name: "Bob" };  // 가능한가?
  console.log("obj 재할당:", obj);
} catch (e) {
  console.log("에러:", e.message);
}


// ============================================
// 문제 5: 블록 스코프 이해
// ============================================
console.log("\n--- 문제 5 ---");
function blockScopeTest() {
  let x = 1;
  const y = 2;

  if (true) {
    let x = 3;      // 새로운 x
    const y = 4;    // 새로운 y
    var z = 5;      // 함수 스코프

    console.log("블록 안 x:", x);  // 예측: ?
    console.log("블록 안 y:", y);  // 예측: ?
  }

  console.log("블록 밖 x:", x);  // 예측: ?
  console.log("블록 밖 y:", y);  // 예측: ?
  console.log("블록 밖 z:", z);  // 예측: ?
}

blockScopeTest(); // 주석 해제하고 실행해보세요


console.log("\n=== Exercise 1 완료 ===");
console.log("답안을 exercise1-answer.md에 작성하세요!");
