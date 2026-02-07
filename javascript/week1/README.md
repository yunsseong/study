# Week 1: 자바스크립트 코어 기초

## 📚 학습 주제
- 변수 선언 (var, let, const)
- 스코프 (함수 스코프 vs 블록 스코프)
- 호이스팅 (Hoisting)
- TDZ (Temporal Dead Zone)

## 📁 파일 구조

```
week1/
├── README.md                      # 주차 개요 (이 파일)
├── lesson1-variables-hoisting.md  # 이론 학습 자료
├── exercise1.js                   # 실습 과제
├── exercise1-answer.md            # 답안 제출 파일
├── quiz1.md                       # 퀴즈
└── project1.js                    # 미니 프로젝트
```

## 🎯 학습 순서

### 1단계: 이론 학습 (1-2시간)
📖 **lesson1-variables-hoisting.md** 읽기
- 개념 이해하기
- 예제 코드 직접 실행해보기
- 이해 안 되는 부분 메모하기

### 2단계: 실습 과제 (1-2시간)
💻 **exercise1.js** 실행
```bash
cd javascript/week1
node exercise1.js
```
- 각 문제의 결과 예측하기
- 실제 실행해서 확인하기
- **exercise1-answer.md**에 답안 작성하기

### 3단계: 퀴즈 (30분)
📝 **quiz1.md** 풀기
- 객관식, 코드 분석, 참/거짓 문제
- 답안 작성 후 채점 요청

### 4단계: 미니 프로젝트 (1-2시간)
🚀 **project1.js** 완성
```bash
node project1.js
```
- 버그가 있는 코드 분석
- let/const로 수정
- 보너스 과제 도전

### 5단계: 코드 리뷰
✅ 완성한 코드를 Claude에게 제출
- 피드백 받기
- 개선점 적용하기
- 다음 학습으로 이동

## ✅ 체크리스트

- [ ] lesson1-variables-hoisting.md 읽기
- [ ] exercise1.js 실행 및 답안 작성
- [ ] quiz1.md 풀기
- [ ] project1.js 완성
- [ ] 코드 리뷰 받기
- [ ] 학습 내용 복습

## 💡 핵심 포인트

1. **const 우선 사용**: 재할당이 필요없으면 항상 const
2. **재할당 필요시 let**: 값이 바뀌어야 할 때만 let
3. **var는 피하기**: 레거시가 아니면 사용하지 않기
4. **블록 스코프 이해**: let/const는 {} 단위로 스코프 생성
5. **호이스팅 주의**: 선언 전 사용 시 var는 undefined, let/const는 에러

## 🤔 자주 하는 실수

1. ❌ const 객체의 재할당 시도
   ```javascript
   const obj = { a: 1 };
   obj = { b: 2 };  // TypeError
   ```

2. ❌ 루프에서 var 사용
   ```javascript
   for (var i = 0; i < 3; i++) {
     setTimeout(() => console.log(i), 100);  // 3, 3, 3
   }
   ```

3. ❌ TDZ 무시
   ```javascript
   console.log(x);  // ReferenceError
   let x = 1;
   ```

## 📚 추가 학습 자료

- [MDN: var](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/var)
- [MDN: let](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/let)
- [MDN: const](https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/const)
- [JavaScript.info: 변수와 상수](https://ko.javascript.info/variables)

## 🎓 다음 학습

Week 1을 완료하면 **Week 2: 데이터 타입과 함수**로 이동합니다.

---

**예상 학습 시간**: 6-8시간
**난이도**: ⭐⭐ (5점 만점)
