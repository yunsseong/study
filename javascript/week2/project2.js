/**
 * 미니 프로젝트 2: 다기능 계산기
 *
 * 목표: 데이터 타입과 함수를 활용한 계산기 구현
 *
 * 난이도: ⭐⭐⭐
 * 예상 시간: 2-3시간
 */

console.log("=== 미니 프로젝트 2: 다기능 계산기 ===\n");

// ============================================
// 과제 1: 기본 계산기 (일급 객체 활용)
// ============================================

/**
 * 기본 연산을 수행하는 계산기
 *
 * 요구사항:
 * 1. add, subtract, multiply, divide 함수 구현
 * 2. 각 함수는 두 숫자를 받아 결과 반환
 * 3. divide는 0으로 나눌 때 에러 처리
 * 4. calculate 함수는 연산자 문자열과 두 숫자를 받아 결과 반환
 */

// TODO: 여기에 구현
function add(a, b) {
  // 구현
}

function subtract(a, b) {
  // 구현
}

function multiply(a, b) {
  // 구현
}

function divide(a, b) {
  // 구현 (0으로 나누기 체크)
}

function calculate(operator, a, b) {
  // 구현 (operator에 따라 적절한 함수 호출)
  // 힌트: 객체를 사용하여 연산자와 함수를 매핑
}

// 테스트 (주석 해제하고 실행)
// console.log(calculate("+", 5, 3));   // 8
// console.log(calculate("-", 5, 3));   // 2
// console.log(calculate("*", 5, 3));   // 15
// console.log(calculate("/", 6, 2));   // 3
// console.log(calculate("/", 5, 0));   // "Error: Division by zero"


// ============================================
// 과제 2: 체이닝 계산기 (클로저 활용)
// ============================================

/**
 * 메서드 체이닝이 가능한 계산기
 *
 * 요구사항:
 * 1. 초기값으로 시작
 * 2. add, subtract, multiply, divide 메서드로 연산
 * 3. getValue()로 최종 결과 반환
 * 4. 메서드 체이닝 가능
 *
 * 예시:
 * createCalculator(10)
 *   .add(5)
 *   .multiply(2)
 *   .subtract(10)
 *   .getValue()  // 20
 */

function createCalculator(initialValue) {
  // TODO: 여기에 구현
  // 힌트: 클로저로 현재 값을 저장하고, 각 메서드는 this를 반환
}

// 테스트 (주석 해제하고 실행)
// const calc = createCalculator(10);
// console.log(
//   calc
//     .add(5)       // 15
//     .multiply(2)  // 30
//     .subtract(10) // 20
//     .divide(2)    // 10
//     .getValue()   // 10
// );


// ============================================
// 과제 3: 고급 계산기 (고차 함수 활용)
// ============================================

/**
 * 배열 연산을 지원하는 계산기
 *
 * 요구사항:
 * 1. 숫자 배열을 받아 연산 수행
 * 2. sumAll: 모든 숫자의 합
 * 3. multiplyAll: 모든 숫자의 곱
 * 4. filterAndSum: 조건을 만족하는 숫자들의 합
 * 5. transform: 변환 함수를 적용한 후 합계
 */

const arrayCalculator = {
  // TODO: sumAll 구현 (reduce 사용)
  sumAll: function(numbers) {
    // 구현
  },

  // TODO: multiplyAll 구현
  multiplyAll: function(numbers) {
    // 구현
  },

  // TODO: filterAndSum 구현
  // 예: filterAndSum([1, 2, 3, 4, 5], x => x > 2) → 12
  filterAndSum: function(numbers, condition) {
    // 구현 (filter + reduce)
  },

  // TODO: transform 구현
  // 예: transform([1, 2, 3], x => x * 2) → 12 (2+4+6)
  transform: function(numbers, transformFunc) {
    // 구현 (map + reduce)
  }
};

// 테스트 (주석 해제하고 실행)
// const numbers = [1, 2, 3, 4, 5];
// console.log(arrayCalculator.sumAll(numbers));           // 15
// console.log(arrayCalculator.multiplyAll(numbers));      // 120
// console.log(arrayCalculator.filterAndSum(numbers, x => x > 2));  // 12
// console.log(arrayCalculator.transform(numbers, x => x * 2));     // 30


// ============================================
// 과제 4: 통계 계산기 (함수 조합)
// ============================================

/**
 * 통계 연산을 지원하는 계산기
 *
 * 요구사항:
 * 1. average: 평균
 * 2. median: 중앙값
 * 3. mode: 최빈값
 * 4. range: 범위 (최댓값 - 최솟값)
 */

const statisticsCalculator = {
  // TODO: average 구현
  average: function(numbers) {
    // 구현
  },

  // TODO: median 구현
  // 힌트: 정렬 후 중간값 (배열 길이가 짝수면 중간 두 값의 평균)
  median: function(numbers) {
    // 구현
  },

  // TODO: mode 구현 (가장 많이 나온 숫자)
  // 힌트: 객체로 빈도수 세기
  mode: function(numbers) {
    // 구현
  },

  // TODO: range 구현
  range: function(numbers) {
    // 구현
  }
};

// 테스트 (주석 해제하고 실행)
// const data = [1, 2, 2, 3, 4, 5];
// console.log(statisticsCalculator.average(data));   // 2.833...
// console.log(statisticsCalculator.median(data));    // 2.5
// console.log(statisticsCalculator.mode(data));      // 2
// console.log(statisticsCalculator.range(data));     // 4


// ============================================
// 보너스 과제: 수식 파서 (문자열 처리)
// ============================================

/**
 * 문자열 수식을 계산하는 함수
 *
 * 요구사항:
 * 1. "5 + 3" 형태의 문자열을 받아 계산
 * 2. 공백 처리
 * 3. 에러 처리 (잘못된 수식)
 *
 * 예시:
 * evaluateExpression("10 + 5")  // 15
 * evaluateExpression("20 - 8")  // 12
 * evaluateExpression("4 * 3")   // 12
 * evaluateExpression("15 / 3")  // 5
 */

function evaluateExpression(expression) {
  // TODO: 여기에 구현
  // 힌트:
  // 1. 문자열을 공백으로 split
  // 2. 첫 번째: 숫자, 두 번째: 연산자, 세 번째: 숫자
  // 3. Number()로 문자열을 숫자로 변환
  // 4. 위에서 만든 calculate 함수 재사용
}

// 테스트 (주석 해제하고 실행)
// console.log(evaluateExpression("10 + 5"));   // 15
// console.log(evaluateExpression("20 - 8"));   // 12
// console.log(evaluateExpression("4 * 3"));    // 12
// console.log(evaluateExpression("15 / 3"));   // 5


// ============================================
// 추가 챌린지: 함수형 계산기
// ============================================

/**
 * 함수 조합을 활용한 계산기
 *
 * 요구사항:
 * 1. 여러 연산을 조합하여 새로운 계산 함수 생성
 * 2. pipe 함수 구현 (왼쪽에서 오른쪽으로 실행)
 * 3. compose 함수 구현 (오른쪽에서 왼쪽으로 실행)
 *
 * 예시:
 * const addThenDouble = pipe(add5, double);
 * addThenDouble(10)  // (10 + 5) * 2 = 30
 */

// 기본 연산 함수들
const add5 = x => x + 5;
const double = x => x * 2;
const square = x => x * x;
const subtract3 = x => x - 3;

// TODO: pipe 구현
function pipe(...funcs) {
  // 구현 (reduce 사용)
  // 왼쪽에서 오른쪽으로 실행
}

// TODO: compose 구현
function compose(...funcs) {
  // 구현 (reduceRight 사용)
  // 오른쪽에서 왼쪽으로 실행
}

// 테스트 (주석 해제하고 실행)
// const calculate1 = pipe(add5, double, subtract3);
// console.log(calculate1(10));  // ((10 + 5) * 2) - 3 = 27
//
// const calculate2 = compose(subtract3, double, add5);
// console.log(calculate2(10));  // (10 + 5) * 2 - 3 = 27


console.log("\n=== 프로젝트 완료 후 ===");
console.log("1. 모든 테스트 코드 주석을 해제하고 실행하세요");
console.log("2. 각 함수가 어떻게 작동하는지 주석으로 설명하세요");
console.log("3. 코드를 Claude에게 제출하여 리뷰 받으세요!");


// ============================================
// 학습 포인트 정리
// ============================================

/**
 * 이 프로젝트에서 배울 수 있는 것:
 *
 * 1. 일급 객체로서의 함수
 *    - 함수를 변수에 저장
 *    - 함수를 객체의 값으로 사용
 *    - 함수를 동적으로 선택하여 실행
 *
 * 2. 클로저 활용
 *    - 상태를 클로저 안에 캡슐화
 *    - 메서드 체이닝 구현
 *    - private 변수 패턴
 *
 * 3. 고차 함수
 *    - map, filter, reduce 실전 활용
 *    - 함수를 인자로 받기
 *    - 함수를 반환하기
 *
 * 4. 함수 조합
 *    - 작은 함수들을 조합하여 복잡한 기능 구현
 *    - pipe와 compose 패턴
 *    - 재사용 가능한 함수 설계
 *
 * 5. 데이터 타입 처리
 *    - 타입 변환 (문자열 → 숫자)
 *    - 타입 체크와 에러 처리
 *    - 배열과 객체 활용
 */
