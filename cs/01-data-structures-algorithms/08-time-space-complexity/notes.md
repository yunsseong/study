# 시간/공간 복잡도

## Big-O 표기법

알고리즘의 **최악의 경우** 성능을 나타내는 표기법.

### 복잡도 순서 (느린 순)

```
O(1) < O(log n) < O(n) < O(n log n) < O(n^2) < O(2^n) < O(n!)
```

### 직관적 이해 (n = 1,000,000 기준)

| 복잡도 | 이름 | 연산 수 | 1초 이내? |
|--------|------|---------|----------|
| O(1) | 상수 | 1 | O |
| O(log n) | 로그 | ~20 | O |
| O(n) | 선형 | 1,000,000 | O |
| O(n log n) | 선형로그 | ~20,000,000 | O |
| O(n^2) | 이차 | 1,000,000,000,000 | X |
| O(2^n) | 지수 | 천문학적 | X |

> 일반적으로 **1초에 약 1억 번** 연산 가능하다고 가정

---

## 시간복잡도 판별법

### 반복문 기준

```python
# O(1) - 상수
x = arr[0]

# O(n) - 단일 반복
for i in range(n):
    print(i)

# O(n^2) - 이중 반복
for i in range(n):
    for j in range(n):
        print(i, j)

# O(n^3) - 삼중 반복
for i in range(n):
    for j in range(n):
        for k in range(n):
            print(i, j, k)

# O(log n) - 반씩 줄어드는 반복
while n > 0:
    n //= 2

# O(n log n) - 반복 안에 로그
for i in range(n):         # O(n)
    j = 1
    while j < n:           # O(log n)
        j *= 2
```

### 재귀 기준

```python
# O(2^n) - 이진 재귀 (피보나치)
def fib(n):
    if n <= 1: return n
    return fib(n-1) + fib(n-2)

# O(n) - 선형 재귀
def factorial(n):
    if n <= 1: return 1
    return n * factorial(n-1)

# O(log n) - 반씩 줄이는 재귀 (이진 탐색)
def binary_search(arr, target, left, right):
    if left > right: return -1
    mid = (left + right) // 2
    if arr[mid] == target: return mid
    elif arr[mid] < target:
        return binary_search(arr, target, mid+1, right)
    else:
        return binary_search(arr, target, left, mid-1)
```

---

## 주요 연산별 복잡도

### Python 자료구조

| 자료구조 | 연산 | 시간복잡도 |
|---------|------|-----------|
| **list** | 인덱스 접근 | O(1) |
| | append | O(1) amortized |
| | insert(i, x) | O(n) |
| | pop() | O(1) |
| | pop(i) | O(n) |
| | in (탐색) | O(n) |
| | sort | O(n log n) |
| **dict** | 삽입/삭제/탐색 | O(1) 평균 |
| **set** | add/remove/in | O(1) 평균 |
| **deque** | append/popleft | O(1) |
| **heapq** | push/pop | O(log n) |
| | heapify | O(n) |

### 알고리즘별

| 알고리즘 | 시간복잡도 | 비고 |
|---------|-----------|------|
| 이진 탐색 | O(log n) | 정렬 필요 |
| DFS/BFS | O(V + E) | 그래프 탐색 |
| 다익스트라 | O(E log V) | 힙 사용 시 |
| 위상 정렬 | O(V + E) | |
| 병합 정렬 | O(n log n) | 안정적 |
| 퀵 정렬 | O(n log n) 평균 | 최악 O(n^2) |

---

## 공간복잡도

알고리즘이 사용하는 **추가 메모리**.

```python
# O(1) - 변수 몇 개만 사용
def sum_array(arr):
    total = 0
    for x in arr:
        total += x
    return total

# O(n) - 입력 크기만큼 추가 배열
def duplicate(arr):
    return arr[:]

# O(n^2) - 2차원 배열
def create_matrix(n):
    return [[0] * n for _ in range(n)]
```

### 시간 vs 공간 트레이드오프

| 전략 | 시간 | 공간 | 예시 |
|------|------|------|------|
| 메모이제이션 | 줄어듦 | 늘어남 | DP 캐싱 |
| 해시맵 탐색 | O(1) | O(n) | Two Sum |
| 정렬 후 이진탐색 | O(log n) | O(1) | 정렬된 배열 탐색 |
| 브루트포스 | O(n^2) | O(1) | 추가 메모리 불필요 |

---

## 문제 풀 때 복잡도 전략

### 입력 크기별 허용 복잡도

| n의 범위 | 허용 복잡도 | 적합한 알고리즘 |
|---------|-----------|---------------|
| n ≤ 10 | O(n!) | 완전 탐색 |
| n ≤ 20 | O(2^n) | 비트마스크, 백트래킹 |
| n ≤ 500 | O(n^3) | 플로이드 워셜 |
| n ≤ 5,000 | O(n^2) | 이중 반복 DP |
| n ≤ 100,000 | O(n log n) | 정렬, 이진 탐색 |
| n ≤ 10,000,000 | O(n) | 투 포인터, 해시맵 |
| n > 10,000,000 | O(log n), O(1) | 이진 탐색, 수학 |

> **팁**: 문제의 n 범위를 보고 어떤 복잡도의 알고리즘을 써야 하는지 역추정

---

## 면접 예상 질문

1. **Big-O, Big-Omega, Big-Theta의 차이는?**
   - O: 상한 (최악), Omega: 하한 (최선), Theta: 정확한 차수 (평균)

2. **O(n)과 O(2n)의 차이는?**
   - 같음. Big-O에서 상수는 무시 (O(2n) = O(n))

3. **공간복잡도를 줄이려면 어떻게 하나요?**
   - in-place 알고리즘, 변수 재활용, DP 공간 최적화 (1차원으로 줄이기)

4. **amortized O(1)이란?**
   - 대부분 O(1)이지만 가끔 O(n) (예: 동적 배열 확장). 평균하면 O(1)
