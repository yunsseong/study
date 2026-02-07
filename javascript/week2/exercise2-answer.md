# Exercise 2 답안 제출

**이름**: [여기에 이름 작성]
**날짜**: [제출 날짜]

---

## 문제 1: Primitive vs Reference

### 예측
```
a: 
b: [예측 작성]
obj1.value: [예측 작성]
obj2.value: [예측 작성]
```

### 실제 결과
```
a:
b:
obj1.value:
obj2.value:
```

### 설명
[Primitive 타입과 Reference 타입의 차이를 설명해주세요]

---

## 문제 2: 타입 변환

### 예측
```
"5" + 3: [예측 작성]
"5" - 3: [예측 작성]
"5" * "2": [예측 작성]
"abc" - 3: [예측 작성]
```

### 실제 결과
```
"5" + 3:
"5" - 3:
"5" * "2":
"abc" - 3:
```

### 설명
[+ 연산자와 다른 연산자의 타입 변환 차이를 설명해주세요]

---

## 문제 3: Falsy 값

### 실제 결과
```
false: [true/false]
0: [true/false]
"": [true/false]
null: [true/false]
undefined: [true/false]
NaN: [true/false]
"0": [true/false]
[]: [true/false]
{}: [true/false]
```

### 설명
[어떤 값들이 Falsy이고, 왜 그런지 설명해주세요]

---

## 문제 4: == vs ===

### 예측
```
5 == '5': [예측]
5 === '5': [예측]
0 == false: [예측]
0 === false: [예측]
null == undefined: [예측]
null === undefined: [예측]
```

### 실제 결과
```
5 == '5':
5 === '5':
0 == false:
0 === false:
null == undefined:
null === undefined:
```

### 설명
[== 과 === 의 차이를 설명해주세요]

---

## 문제 5: 객체 비교

### 예측
```
arr1 === arr2: [예측]
arr1 === arr3: [예측]
obj3 === obj4: [예측]
JSON 비교: [예측]
```

### 실제 결과
```
arr1 === arr2:
arr1 === arr3:
obj3 === obj4:
JSON 비교:
```

### 설명
[객체/배열 비교가 어떻게 동작하는지 설명해주세요]

---

## 문제 6: typeof 연산자

### 예측
```
typeof 42: [예측]
typeof 'hello': [예측]
typeof true: [예측]
typeof undefined: [예측]
typeof null: [예측]
typeof {}: [예측]
typeof []: [예측]
typeof function(){}: [예측]
```

### 실제 결과
```
typeof 42:
typeof 'hello':
typeof true:
typeof undefined:
typeof null:
typeof {}:
typeof []:
typeof function(){}:
```

### 설명
[typeof의 특이한 동작(null, 배열)을 설명해주세요]

---

## 문제 7: 배열 복사

### 예측
```
original[0]: [예측]
original[2].a: [예측]
```

### 실제 결과
```
original[0]:
original[2].a:
```

### 설명
[얕은 복사와 깊은 복사의 차이를 설명해주세요]

---

## 문제 8: 암묵적 타입 변환

### 예측
```
[] + []: [예측]
[] + {}: [예측]
{} + []: [예측]
true + true: [예측]
'5' - '2': [예측]
'5' + - '2': [예측]
```

### 실제 결과
```
[] + []:
[] + {}:
{} + []:
true + true:
'5' - '2':
'5' + - '2':
```

### 설명
[자바스크립트의 암묵적 타입 변환 규칙을 설명해주세요]

---

## 보너스 문제

### 예측
```
mystery([]): [예측]
mystery(''): [예측]
mystery('0'): [예측]
mystery(0): [예측]
```

### 실제 결과
```
mystery([]):
mystery(''):
mystery('0'):
mystery(0):
```

### 설명
[Truthy와 Falsy 값의 차이를 설명해주세요]

---

## 학습 소감

[이번 학습에서 가장 어려웠던 점이나 새로 알게 된 점을 자유롭게 작성해주세요]
