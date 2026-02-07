# Quiz 1: 변수 선언과 호이스팅

**제한 시간**: 15분
**총 문항**: 10문제

---

## 객관식 문제 (1-5)

### 1. 다음 중 올바른 설명은?
```javascript
const arr = [1, 2, 3];
arr.push(4);
```

a) 에러가 발생한다 (const는 변경 불가)
b) 정상 작동한다 (배열 메서드는 사용 가능)
c) undefined가 반환된다
d) arr이 null이 된다

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 2. 다음 코드의 출력 결과는?
```javascript
console.log(typeof x);
var x = 10;
```

a) ReferenceError
b) undefined
c) number
d) null

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 3. let과 var의 가장 중요한 차이점은?

a) let은 재할당이 불가능하다
b) let은 블록 스코프, var는 함수 스코프
c) var는 ES6에서 사용할 수 없다
d) let은 호이스팅되지 않는다

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 4. 다음 코드의 출력 결과는?
```javascript
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0);
}
```

a) 0, 1, 2
b) 3, 3, 3
c) undefined, undefined, undefined
d) ReferenceError

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

### 5. TDZ(Temporal Dead Zone)는 언제 발생하는가?

a) var 변수 선언 전에 접근할 때
b) let/const 변수 선언 전에 접근할 때
c) 함수가 호출되지 않았을 때
d) 변수에 null을 할당했을 때

**답**: [여기에 답 작성]
**이유**: [선택한 이유 설명]

---

## 코드 분석 문제 (6-8)

### 6. 다음 코드의 문제점을 찾고 수정하세요
```javascript
function createMultipliers() {
  var multipliers = [];

  for (var i = 1; i <= 3; i++) {
    multipliers.push(function(x) {
      return x * i;
    });
  }

  return multipliers;
}

const funcs = createMultipliers();
console.log(funcs[0](2));  // 기대값: 2
console.log(funcs[1](2));  // 기대값: 4
console.log(funcs[2](2));  // 기대값: 6
```

**문제점**: [여기에 작성]
**수정된 코드**:
```javascript
// 여기에 수정된 코드 작성
```

---

### 7. 다음 코드의 출력 결과를 예측하고 설명하세요
```javascript
let x = 1;
{
  console.log(x);
  let x = 2;
}
```

**출력 결과**: [여기에 작성]
**설명**: [왜 그런 결과가 나오는지 설명]

---

### 8. 다음 코드를 const를 최대한 활용하도록 리팩토링하세요
```javascript
function calculateStats(numbers) {
  let sum = 0;
  let count = numbers.length;

  for (let i = 0; i < count; i++) {
    sum += numbers[i];
  }

  let average = sum / count;
  return average;
}
```

**리팩토링된 코드**:
```javascript
// 여기에 작성
```

---

## 참/거짓 문제 (9-10)

### 9. 다음 설명이 참인지 거짓인지 답하고 이유를 설명하세요

**설명**: "const로 선언한 객체는 프로퍼티를 추가하거나 수정할 수 없다"

**답**: [참/거짓]
**이유**: [설명]

---

### 10. 다음 설명이 참인지 거짓인지 답하고 이유를 설명하세요

**설명**: "호이스팅은 var에만 발생하고 let과 const에는 발생하지 않는다"

**답**: [참/거짓]
**이유**: [설명]

---

## 채점 기준

- 객관식 (1-5): 각 10점 = 50점
- 코드 분석 (6-8): 각 15점 = 45점
- 참/거짓 (9-10): 각 2.5점 = 5점

**총점**: 100점
**합격 기준**: 70점 이상

---

## 정답 확인

답안을 모두 작성한 후, Claude에게 채점을 요청하세요!
