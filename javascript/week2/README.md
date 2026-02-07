# Week 2: 데이터 타입과 함수

## 📚 학습 주제
- 데이터 타입 (Primitive vs Reference)
- 타입 변환 (명시적/암묵적)
- 함수 선언 방식 (선언식, 표현식, 화살표 함수)
- 함수의 특징 (일급 객체, 고차 함수)
- 함수 파라미터 (기본값, Rest, Spread)

## 📁 파일 구조

```
week2/
├── README.md                   # 주차 개요 (이 파일)
├── lesson2-data-types.md       # 데이터 타입과 타입 변환
├── lesson3-functions.md        # 함수 심화
├── exercise2.js                # 데이터 타입 실습
├── exercise2-answer.md         # 답안 제출
├── exercise3.js                # 함수 실습
├── exercise3-answer.md         # 답안 제출
├── quiz2.md                    # 퀴즈
└── project2.js                 # 미니 프로젝트: 다기능 계산기
```

## 🎯 학습 순서

### 1단계: 데이터 타입 (2-3시간)
📖 **lesson2-data-types.md** 읽기
- Primitive vs Reference 타입
- 타입 변환 메커니즘
- typeof, === vs ==

💻 **exercise2.js** 실행
```bash
cd javascript/week2
node exercise2.js
```

### 2단계: 함수 (2-3시간)
📖 **lesson3-functions.md** 읽기
- 함수 선언 방식의 차이
- 화살표 함수의 특징
- 일급 객체와 고차 함수

💻 **exercise3.js** 실행
```bash
node exercise3.js
```

### 3단계: 퀴즈 (30분)
📝 **quiz2.md** 풀기

### 4단계: 미니 프로젝트 (2-3시간)
🚀 **project2.js** 완성
- 다기능 계산기 구현
- 함수 조합 활용

### 5단계: 코드 리뷰
✅ 완성한 코드를 Claude에게 제출

## ✅ 체크리스트

- [ ] lesson2-data-types.md 읽기
- [ ] exercise2.js 실행 및 답안 작성
- [ ] lesson3-functions.md 읽기
- [ ] exercise3.js 실행 및 답안 작성
- [ ] quiz2.md 풀기
- [ ] project2.js 완성
- [ ] 코드 리뷰 받기

## 💡 핵심 포인트

### 데이터 타입
1. **Primitive**: 값 자체가 복사됨 (Number, String, Boolean, null, undefined, Symbol, BigInt)
2. **Reference**: 참조(주소)가 복사됨 (Object, Array, Function)
3. **타입 변환**: 명시적(개발자 의도) vs 암묵적(자동 변환)

### 함수
1. **선언식**: 호이스팅됨, 어디서든 호출 가능
2. **표현식**: 호이스팅 안 됨, 변수 스코프 따름
3. **화살표 함수**: this 바인딩 없음, 간결한 문법
4. **일급 객체**: 함수를 변수에 할당, 인자로 전달, 반환 가능

## 🤔 자주 하는 실수

### 데이터 타입
1. ❌ 객체 비교 시 === 사용
   ```javascript
   const obj1 = { a: 1 };
   const obj2 = { a: 1 };
   console.log(obj1 === obj2);  // false (다른 참조)
   ```

2. ❌ 암묵적 타입 변환 무시
   ```javascript
   console.log("5" + 3);   // "53" (문자열)
   console.log("5" - 3);   // 2 (숫자)
   ```

### 함수
1. ❌ 화살표 함수의 this
   ```javascript
   const obj = {
     value: 10,
     arrow: () => console.log(this.value)  // undefined
   };
   ```

2. ❌ 함수 선언 전 호출 (표현식)
   ```javascript
   myFunc();  // ReferenceError
   const myFunc = function() { };
   ```

## 📚 추가 학습 자료

- [MDN: 데이터 타입](https://developer.mozilla.org/ko/docs/Web/JavaScript/Data_structures)
- [MDN: 함수](https://developer.mozilla.org/ko/docs/Web/JavaScript/Guide/Functions)
- [MDN: 화살표 함수](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Functions/Arrow_functions)
- [JavaScript.info: 타입 변환](https://ko.javascript.info/type-conversions)

## 🎓 다음 학습

Week 2를 완료하면 **Week 3: 비동기 프로그래밍**으로 이동합니다.

---

**예상 학습 시간**: 8-10시간
**난이도**: ⭐⭐⭐ (5점 만점)
