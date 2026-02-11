# 정렬 & 이진 탐색

## 정렬 알고리즘 비교

| 알고리즘 | 평균 | 최악 | 공간 | 안정성 | 특징 |
|---------|------|------|------|--------|------|
| 버블 정렬 | O(n^2) | O(n^2) | O(1) | 안정 | 교육용, 실무 X |
| 선택 정렬 | O(n^2) | O(n^2) | O(1) | 불안정 | 교환 횟수 최소 |
| 삽입 정렬 | O(n^2) | O(n^2) | O(1) | 안정 | 거의 정렬된 데이터에 빠름 |
| **병합 정렬** | O(n log n) | O(n log n) | O(n) | 안정 | 안정적 성능 보장 |
| **퀵 정렬** | O(n log n) | O(n^2) | O(log n) | 불안정 | 실무에서 가장 빠름 |
| 힙 정렬 | O(n log n) | O(n log n) | O(1) | 불안정 | 추가 메모리 불필요 |
| 계수 정렬 | O(n + k) | O(n + k) | O(k) | 안정 | 범위가 작을 때 |

> **안정 정렬**: 같은 값의 원래 순서가 유지됨

---

## 주요 정렬 구현

### 병합 정렬 (Merge Sort)

분할 → 정복 → 병합

```python
def merge_sort(arr):
    if len(arr) <= 1:
        return arr

    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])

    return merge(left, right)

def merge(left, right):
    result = []
    i = j = 0

    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1

    result.extend(left[i:])
    result.extend(right[j:])
    return result
```

### 퀵 정렬 (Quick Sort)

피벗 선택 → 분할 → 재귀

```python
def quick_sort(arr):
    if len(arr) <= 1:
        return arr

    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]

    return quick_sort(left) + middle + quick_sort(right)
```

### Python 내장 정렬

```python
# sorted(): 새 리스트 반환
sorted([3, 1, 2])                     # [1, 2, 3]
sorted([3, 1, 2], reverse=True)       # [3, 2, 1]

# sort(): 원본 리스트 변경 (in-place)
arr = [3, 1, 2]
arr.sort()

# key 함수 활용
students = [("Alice", 90), ("Bob", 80), ("Charlie", 85)]
sorted(students, key=lambda x: x[1])              # 점수 오름차순
sorted(students, key=lambda x: -x[1])             # 점수 내림차순
sorted(students, key=lambda x: (x[1], x[0]))      # 점수 → 이름 순
```

---

## 이진 탐색 (Binary Search)

**전제 조건**: 배열이 **정렬**되어 있어야 함.

### 기본 구현

```python
def binary_search(arr, target):
    left, right = 0, len(arr) - 1

    while left <= right:
        mid = (left + right) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1

    return -1  # 찾지 못함
```

### Lower Bound / Upper Bound

```python
# Lower Bound: target 이상인 첫 번째 위치
def lower_bound(arr, target):
    left, right = 0, len(arr)
    while left < right:
        mid = (left + right) // 2
        if arr[mid] < target:
            left = mid + 1
        else:
            right = mid
    return left

# Upper Bound: target 초과인 첫 번째 위치
def upper_bound(arr, target):
    left, right = 0, len(arr)
    while left < right:
        mid = (left + right) // 2
        if arr[mid] <= target:
            left = mid + 1
        else:
            right = mid
    return left

# target의 개수 = upper_bound - lower_bound
```

### bisect 모듈 (Python)

```python
import bisect

arr = [1, 3, 3, 3, 5, 7]
bisect.bisect_left(arr, 3)   # 1 (lower bound)
bisect.bisect_right(arr, 3)  # 4 (upper bound)
bisect.insort(arr, 4)        # 정렬 유지하며 삽입
```

---

## 핵심 패턴

### 1. 매개변수 탐색 (Parametric Search)

"조건을 만족하는 최소/최대값을 찾아라" → 이진 탐색 적용

```python
# 예시: 나무 자르기 (백준 2805)
# 높이 H로 잘랐을 때 필요한 양 이상 얻을 수 있는 최대 H
def max_cut_height(trees, need):
    left, right = 0, max(trees)

    while left <= right:
        mid = (left + right) // 2
        total = sum(max(0, t - mid) for t in trees)

        if total >= need:
            left = mid + 1   # 더 높이 잘라도 될 수 있음
        else:
            right = mid - 1  # 너무 높이 잘랐음

    return right
```

### 2. 정렬 + 이진 탐색 조합

```python
# 두 배열에서 합이 target인 쌍 찾기
def find_pair(arr1, arr2, target):
    arr2.sort()
    for a in arr1:
        complement = target - a
        idx = binary_search(arr2, complement)
        if idx != -1:
            return (a, complement)
    return None
```

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 2750 | 수 정렬하기 | 브론즈2 | 기본 정렬 |
| 1427 | 소트인사이드 | 실버5 | 내림차순 정렬 |
| 10989 | 수 정렬하기 3 | 브론즈1 | 계수 정렬 |
| 1920 | 수 찾기 | 실버4 | 이진 탐색 기본 |
| 10816 | 숫자 카드 2 | 실버4 | lower/upper bound |
| 2805 | 나무 자르기 | 실버2 | 매개변수 탐색 |
| 1654 | 랜선 자르기 | 실버2 | 매개변수 탐색 |
| 2110 | 공유기 설치 | 골드4 | 매개변수 탐색 심화 |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| K번째수 | Lv.1 | 정렬 기초 |
| 가장 큰 수 | Lv.2 | 커스텀 정렬 |
| H-Index | Lv.2 | 정렬 + 탐색 |
| 입국심사 | Lv.3 | 이진 탐색 |

---

## 면접 예상 질문

1. **퀵 정렬의 시간복잡도가 O(n^2)이 되는 경우는?**
   - 이미 정렬된 배열에서 첫/마지막 원소를 피벗으로 선택 시

2. **안정 정렬이란?**
   - 같은 값의 원소가 원래 순서를 유지하는 정렬 (병합 정렬, 삽입 정렬)

3. **이진 탐색의 시간복잡도와 전제 조건은?**
   - O(log n), 배열이 정렬되어 있어야 함
