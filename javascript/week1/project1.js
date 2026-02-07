/**
 * 미니 프로젝트 1: 변수 스코프 디버거
 *
 * 목표: var의 함수 스코프 문제를 이해하고 let/const로 수정하기
 *
 * 난이도: ⭐⭐
 * 예상 시간: 20-30분
 */

console.log("=== 미니 프로젝트 1: 변수 스코프 디버거 ===\n");

// ============================================
// 버그가 있는 코드
// ============================================

/**
 * 문제: 이 함수는 3개의 카운터 버튼을 생성하려고 합니다.
 * 각 버튼은 독립적인 카운터를 가져야 하지만, 현재는 제대로 작동하지 않습니다.
 */
function createCountersBuggy() {
  var count = 0;
  var buttons = [];

  for (var i = 0; i < 3; i++) {
    buttons.push(function() {
      console.log("Button " + i + " clicked");
      count++;
      return count;
    });
  }

  return buttons;
}

console.log("--- 버그가 있는 버전 ---");
const buggyCounters = createCountersBuggy();

console.log("기대: Button 0 clicked, 1");
console.log("실제:", buggyCounters[0]());

console.log("\n기대: Button 1 clicked, 2");
console.log("실제:", buggyCounters[1]());

console.log("\n기대: Button 2 clicked, 3");
console.log("실제:", buggyCounters[2]());


// ============================================
// TODO: 여기에 수정된 코드를 작성하세요
// ============================================

/**
 * 과제 1: createCountersFixed() 함수를 구현하세요
 *
 * 요구사항:
 * 1. var 대신 let/const를 사용하세요
 * 2. 각 버튼이 올바른 번호를 출력해야 합니다
 * 3. count가 전역적으로 증가해야 합니다 (1, 2, 3...)
 */
function createCountersFixed() {
  const buttons = [];
  let count = 0;

  for(let i = 0; i < 3; i++) {
    buttons.push(() => {
      console.log("Button" + i + "clicked");
      count ++;
      return count;
    })
  }
  return buttons;
}

// 테스트 코드 (주석 해제하고 실행)
console.log("\n\n--- 수정된 버전 ---");
const fixedCounters = createCountersFixed();

console.log("기대: Button 0 clicked, 1");
console.log("실제:", fixedCounters[0]());

console.log("\n기대: Button 1 clicked, 2");
console.log("실제:", fixedCounters[1]());

console.log("\n기대: Button 2 clicked, 3");
console.log("실제:", fixedCounters[2]());


// ============================================
// 보너스 과제: 독립적인 카운터
// ============================================

/**
 * 과제 2: createIndependentCounters() 함수를 구현하세요
 *
 * 요구사항:
 * 1. 각 버튼이 독립적인 카운터를 가져야 합니다
 * 2. 버튼 0을 여러 번 클릭하면 1, 2, 3... 증가
 * 3. 버튼 1을 클릭하면 다시 1부터 시작
 * 4. 클로저를 활용하세요
 */

function createIndependentCounters() {
  const buttons = [];

  for (let i = 0; i < 3; i++) {
    let count = 0;
    buttons.push(() => {
      console.log("Buttons " + i + " clicked");
      count++;
      return count;
    })
  }
  return buttons;
}

// 테스트 코드 (주석 해제하고 실행)
console.log("\n\n--- 보너스: 독립적인 카운터 ---");
const independentCounters = createIndependentCounters();

console.log("버튼 0 첫 클릭:", independentCounters[0]());  // Button 0, count: 1
console.log("버튼 0 두번째:", independentCounters[0]());   // Button 0, count: 2
console.log("버튼 1 첫 클릭:", independentCounters[1]());  // Button 1, count: 1
console.log("버튼 0 세번째:", independentCounters[0]());   // Button 0, count: 3


// ============================================
// 추가 챌린지: 카운터 리셋 기능
// ============================================

/**
 * 과제 3: createCountersWithReset() 함수를 구현하세요
 *
 * 요구사항:
 * 1. 각 카운터 객체는 increment()와 reset() 메서드를 가져야 합니다
 * 2. increment()는 카운트를 증가시킵니다
 * 3. reset()은 카운트를 0으로 초기화합니다
 * 4. 객체 리터럴과 클로저를 활용하세요
 *
 * 예시:
 * const counter = createCounter(0);
 * counter.increment(); // 1
 * counter.increment(); // 2
 * counter.reset();     // 0
 * counter.increment(); // 1
 */
function createCounter(id) {
  // 힌트: 객체를 반환하고, 메서드에서 클로저를 활용
  let count = 0;

  return {
    increment() {
      count++;
      return count;
    },
    reset() {
      count = 0;
      return count;
    }
  }
}

// 테스트 코드 (주석 해제하고 실행)
console.log("\n\n--- 챌린지: 리셋 기능 ---");
const counter1 = createCounter(0);
const counter2 = createCounter(1);

console.log("Counter 0:", counter1.increment());  // 1
console.log("Counter 0:", counter1.increment());  // 2
console.log("Counter 1:", counter2.increment());  // 1
console.log("Counter 0 reset:", counter1.reset());  // 0
console.log("Counter 0:", counter1.increment());  // 1


console.log("\n\n=== 프로젝트 완료 후 ===");
console.log("1. 모든 테스트 코드 주석을 해제하고 실행하세요");
console.log("2. 각 함수가 어떻게 작동하는지 주석으로 설명하세요");
console.log("3. 코드를 Claude에게 제출하여 리뷰 받으세요!");


// ============================================
// 학습 포인트 정리
// ============================================

/**
 * 이 프로젝트에서 배울 수 있는 것:
 *
 * 1. var vs let/const의 스코프 차이
 *    - var: 함수 스코프 → 루프 변수가 공유됨
 *    - let: 블록 스코프 → 각 반복마다 새로운 변수
 *
 * 2. 클로저 (Closure)
 *    - 함수가 선언될 때의 환경을 기억
 *    - 내부 함수가 외부 함수의 변수에 접근
 *
 * 3. 함수형 프로그래밍 패턴
 *    - 함수를 반환하는 함수
 *    - 상태를 클로저 안에 캡슐화
 *
 * 4. 실전 디버깅 스킬
 *    - 예상과 실제 결과 비교
 *    - 문제의 원인 파악
 *    - 체계적인 해결 방법 적용
 */
