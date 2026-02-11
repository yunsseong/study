# 스택 & 큐

## 스택 (Stack)

### 개념
- **LIFO** (Last In, First Out) - 후입선출
- 접시를 쌓는 것과 같음: 마지막에 넣은 것을 먼저 꺼냄

### 시간복잡도

| 연산 | 시간복잡도 |
|------|-----------|
| push (삽입) | O(1) |
| pop (제거) | O(1) |
| peek/top (확인) | O(1) |
| 탐색 | O(n) |

### 구현

```python
# Python - 리스트로 구현
stack = []
stack.append(1)   # push
stack.append(2)
stack.pop()       # pop → 2
stack[-1]         # peek → 1

# 직접 구현
class Stack:
    def __init__(self):
        self.items = []

    def push(self, item):
        self.items.append(item)

    def pop(self):
        if self.is_empty():
            raise IndexError("Stack is empty")
        return self.items.pop()

    def peek(self):
        if self.is_empty():
            raise IndexError("Stack is empty")
        return self.items[-1]

    def is_empty(self):
        return len(self.items) == 0

    def size(self):
        return len(self.items)
```

### 활용 사례
- **괄호 매칭** - `()`, `{}`, `[]` 유효성 검사
- **후위 표기법 계산** - 계산기
- **DFS** - 깊이 우선 탐색
- **되돌리기(Undo)** - 에디터의 Ctrl+Z
- **함수 호출 스택** - 재귀 호출 관리

### 핵심 패턴: 괄호 매칭

```python
def is_valid(s):
    stack = []
    pairs = {')': '(', '}': '{', ']': '['}

    for char in s:
        if char in '({[':
            stack.append(char)
        elif char in ')}]':
            if not stack or stack[-1] != pairs[char]:
                return False
            stack.pop()

    return len(stack) == 0

# is_valid("({[]})") → True
# is_valid("({)}") → False
```

### 핵심 패턴: 단조 스택 (Monotone Stack)

스택의 요소가 항상 단조 증가 또는 감소를 유지하는 기법.

**언제 쓰나**: 다음 큰 요소(Next Greater Element), 히스토그램 최대 넓이

```python
# 다음으로 큰 요소 찾기
# [2, 1, 2, 4, 3] → [4, 2, 4, -1, -1]
def next_greater_element(nums):
    result = [-1] * len(nums)
    stack = []  # 인덱스를 저장

    for i in range(len(nums)):
        while stack and nums[i] > nums[stack[-1]]:
            idx = stack.pop()
            result[idx] = nums[i]
        stack.append(i)

    return result
```

---

## 큐 (Queue)

### 개념
- **FIFO** (First In, First Out) - 선입선출
- 줄 서기와 같음: 먼저 온 사람이 먼저 나감

### 시간복잡도

| 연산 | 시간복잡도 |
|------|-----------|
| enqueue (삽입) | O(1) |
| dequeue (제거) | O(1) |
| peek/front (확인) | O(1) |
| 탐색 | O(n) |

### 구현

```python
from collections import deque

# deque 사용 (권장)
queue = deque()
queue.append(1)      # enqueue
queue.append(2)
queue.popleft()      # dequeue → 1
queue[0]             # peek → 2

# 직접 구현
class Queue:
    def __init__(self):
        self.items = deque()

    def enqueue(self, item):
        self.items.append(item)

    def dequeue(self):
        if self.is_empty():
            raise IndexError("Queue is empty")
        return self.items.popleft()

    def peek(self):
        if self.is_empty():
            raise IndexError("Queue is empty")
        return self.items[0]

    def is_empty(self):
        return len(self.items) == 0
```

### 활용 사례
- **BFS** - 너비 우선 탐색
- **작업 스케줄링** - 프린터 대기열, 프로세스 스케줄링
- **버퍼** - 데이터 스트리밍

---

## 변형 자료구조

### 덱 (Deque - Double-Ended Queue)
양쪽에서 삽입/삭제 가능.

```python
from collections import deque
d = deque()
d.append(1)       # 오른쪽 삽입
d.appendleft(0)   # 왼쪽 삽입
d.pop()           # 오른쪽 제거
d.popleft()       # 왼쪽 제거
```

### 우선순위 큐 (Priority Queue)
우선순위가 높은 요소가 먼저 나옴. 힙(Heap)으로 구현.

```python
import heapq

# 최소 힙 (기본)
heap = []
heapq.heappush(heap, 3)
heapq.heappush(heap, 1)
heapq.heappush(heap, 2)
heapq.heappop(heap)  # → 1 (가장 작은 값)

# 최대 힙 (음수 트릭)
heapq.heappush(heap, -3)
heapq.heappush(heap, -1)
-heapq.heappop(heap)  # → 3 (가장 큰 값)
```

---

## 스택 vs 큐 비교

| 비교 | 스택 | 큐 |
|------|------|-----|
| 순서 | LIFO (후입선출) | FIFO (선입선출) |
| 삽입 | push (top) | enqueue (rear) |
| 제거 | pop (top) | dequeue (front) |
| 대표 활용 | DFS, 괄호 매칭 | BFS, 스케줄링 |
| 비유 | 접시 쌓기 | 줄 서기 |

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 10828 | 스택 | 실버4 | 스택 구현 |
| 9012 | 괄호 | 실버4 | 괄호 매칭 |
| 4949 | 균형잡힌 세상 | 실버4 | 다중 괄호 매칭 |
| 10845 | 큐 | 실버4 | 큐 구현 |
| 2164 | 카드2 | 실버4 | 큐 활용 |
| 1874 | 스택 수열 | 실버2 | 스택 응용 |
| 17298 | 오큰수 | 골드4 | 단조 스택 |
| 1966 | 프린터 큐 | 실버3 | 우선순위 큐 |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| 같은 숫자는 싫어 | Lv.1 | 스택 기초 |
| 올바른 괄호 | Lv.2 | 괄호 매칭 |
| 프린터 | Lv.2 | 큐 활용 |
| 주식가격 | Lv.2 | 단조 스택 |

---

## 면접 예상 질문

1. **스택과 큐의 차이점은?**
   - LIFO vs FIFO, 각각의 활용 사례

2. **스택 2개로 큐를 구현할 수 있나요?**
   - inbox 스택과 outbox 스택 사용, outbox가 비면 inbox를 뒤집어서 이동

3. **우선순위 큐의 내부 구현은?**
   - 힙(Heap) 기반, 삽입/삭제 O(log n)
