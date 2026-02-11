# 트리 (Tree)

## 개념

- **계층적 구조**의 비선형 자료구조
- 하나의 루트 노드에서 시작, 자식 노드들이 연결
- 사이클이 없는 연결 그래프

### 용어 정리

```
        1          ← 루트 (Root)
       / \
      2   3        ← 내부 노드 (Internal)
     / \   \
    4   5   6      ← 리프 (Leaf: 자식이 없는 노드)
```

| 용어 | 설명 |
|------|------|
| 깊이 (Depth) | 루트에서 해당 노드까지의 간선 수 |
| 높이 (Height) | 해당 노드에서 가장 먼 리프까지의 간선 수 |
| 차수 (Degree) | 자식 노드의 수 |
| 레벨 (Level) | 깊이 + 1 (루트 = 레벨 1) |

---

## 이진 트리 (Binary Tree)

각 노드가 **최대 2개의 자식**을 가지는 트리.

### 종류

| 종류 | 특징 |
|------|------|
| 완전 이진 트리 | 마지막 레벨 제외 모두 채움, 왼쪽부터 채움 |
| 포화 이진 트리 | 모든 레벨이 완전히 채워짐 |
| 균형 이진 트리 | 좌우 높이 차이 ≤ 1 |

### 노드 구현

```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right
```

---

## 트리 순회 (Traversal)

### 깊이 우선 순회 (DFS)

```python
# 전위 순회 (Pre-order): 루트 → 왼쪽 → 오른쪽
def preorder(node):
    if not node:
        return
    print(node.val)         # 루트 처리
    preorder(node.left)
    preorder(node.right)

# 중위 순회 (In-order): 왼쪽 → 루트 → 오른쪽
def inorder(node):
    if not node:
        return
    inorder(node.left)
    print(node.val)         # 루트 처리
    inorder(node.right)

# 후위 순회 (Post-order): 왼쪽 → 오른쪽 → 루트
def postorder(node):
    if not node:
        return
    postorder(node.left)
    postorder(node.right)
    print(node.val)         # 루트 처리
```

### 너비 우선 순회 (BFS) - 레벨 순회

```python
from collections import deque

def level_order(root):
    if not root:
        return []
    result = []
    queue = deque([root])

    while queue:
        level = []
        for _ in range(len(queue)):
            node = queue.popleft()
            level.append(node.val)
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        result.append(level)

    return result
# [[1], [2, 3], [4, 5, 6]]
```

---

## 이진 탐색 트리 (BST)

**규칙**: 왼쪽 < 부모 < 오른쪽

```
        8
       / \
      3   10
     / \    \
    1   6    14
```

### 시간복잡도

| 연산 | 평균 | 최악 (편향) |
|------|------|------------|
| 탐색 | O(log n) | O(n) |
| 삽입 | O(log n) | O(n) |
| 삭제 | O(log n) | O(n) |

### 구현

```python
# 탐색
def search(node, target):
    if not node:
        return None
    if target == node.val:
        return node
    elif target < node.val:
        return search(node.left, target)
    else:
        return search(node.right, target)

# 삽입
def insert(node, val):
    if not node:
        return TreeNode(val)
    if val < node.val:
        node.left = insert(node.left, val)
    else:
        node.right = insert(node.right, val)
    return node
```

> **핵심**: BST의 중위 순회 결과는 항상 **오름차순 정렬**

---

## 핵심 패턴

### 1. 트리 높이 구하기

```python
def height(node):
    if not node:
        return 0
    return 1 + max(height(node.left), height(node.right))
```

### 2. 균형 트리 판별

```python
def is_balanced(node):
    if not node:
        return True
    left_h = height(node.left)
    right_h = height(node.right)
    return (abs(left_h - right_h) <= 1
            and is_balanced(node.left)
            and is_balanced(node.right))
```

### 3. 최소 공통 조상 (LCA)

```python
# BST에서 LCA
def lca_bst(root, p, q):
    if p.val < root.val and q.val < root.val:
        return lca_bst(root.left, p, q)
    if p.val > root.val and q.val > root.val:
        return lca_bst(root.right, p, q)
    return root  # 갈라지는 지점 = LCA
```

---

## 힙 (Heap)

**완전 이진 트리** 기반의 우선순위 큐 구현체.

| 종류 | 규칙 |
|------|------|
| 최소 힙 | 부모 ≤ 자식 (루트가 최소) |
| 최대 힙 | 부모 ≥ 자식 (루트가 최대) |

```python
import heapq

# 최소 힙
nums = [3, 1, 4, 1, 5]
heapq.heapify(nums)        # O(n)으로 힙 변환
heapq.heappush(nums, 2)    # 삽입 O(log n)
heapq.heappop(nums)        # 최소값 제거 O(log n) → 1

# Top K 요소 찾기
def top_k(nums, k):
    return heapq.nlargest(k, nums)
```

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 1991 | 트리 순회 | 실버1 | 전위/중위/후위 |
| 5639 | 이진 검색 트리 | 골드5 | BST 구성 |
| 11279 | 최대 힙 | 실버2 | 힙 구현 |
| 1927 | 최소 힙 | 실버2 | 힙 구현 |
| 11725 | 트리의 부모 찾기 | 실버2 | BFS/DFS |
| 1167 | 트리의 지름 | 골드2 | 트리 DFS |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| 더 맵게 | Lv.2 | 힙 활용 |
| 디스크 컨트롤러 | Lv.3 | 힙 + 스케줄링 |
| 이중우선순위큐 | Lv.3 | 힙 응용 |

---

## 면접 예상 질문

1. **이진 탐색 트리와 이진 트리의 차이는?**
   - BST는 왼쪽 < 부모 < 오른쪽 규칙이 있음

2. **BST의 최악 시간복잡도는? 해결법은?**
   - O(n) 편향 트리 → AVL, 레드-블랙 트리로 균형 유지

3. **힙과 BST의 차이는?**
   - 힙: 부모-자식 관계만 보장, 최대/최소값 추출에 특화
   - BST: 전체 정렬 순서 보장, 탐색에 특화
