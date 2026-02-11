# 배열 & 문자열

## 배열 (Array)

### 개념
- **연속된 메모리 공간**에 같은 타입의 데이터를 저장하는 자료구조
- 인덱스를 통해 O(1)로 접근 가능

### 시간복잡도

| 연산 | 시간복잡도 | 설명 |
|------|-----------|------|
| 접근 (Access) | O(1) | 인덱스로 바로 접근 |
| 탐색 (Search) | O(n) | 처음부터 순회 |
| 삽입 (Insert) | O(n) | 뒤의 요소를 밀어야 함 |
| 삭제 (Delete) | O(n) | 뒤의 요소를 당겨야 함 |
| 맨 뒤 삽입 | O(1) | 동적 배열 기준 (amortized) |

### 배열 vs 연결리스트

| 비교 | 배열 | 연결리스트 |
|------|------|-----------|
| 접근 | O(1) | O(n) |
| 삽입/삭제 (중간) | O(n) | O(1) (위치를 안다면) |
| 메모리 | 연속 할당 | 분산 할당 |
| 캐시 효율 | 좋음 (locality) | 나쁨 |

---

## 핵심 테크닉

### 1. 투 포인터 (Two Pointer)
배열의 두 지점을 동시에 이동하며 탐색하는 기법.

**언제 쓰나**: 정렬된 배열에서 쌍 찾기, 중복 제거, 부분 배열

```python
# 예시: 정렬된 배열에서 두 수의 합이 target인 쌍 찾기
def two_sum_sorted(nums, target):
    left, right = 0, len(nums) - 1
    while left < right:
        total = nums[left] + nums[right]
        if total == target:
            return [left, right]
        elif total < target:
            left += 1
        else:
            right -= 1
    return []
```

### 2. 슬라이딩 윈도우 (Sliding Window)
고정 또는 가변 크기의 윈도우를 이동하며 부분 배열을 탐색.

**언제 쓰나**: 연속 부분 배열의 최대/최소, 특정 조건을 만족하는 구간

```python
# 예시: 크기 k인 부분 배열의 최대 합
def max_subarray_sum(nums, k):
    window_sum = sum(nums[:k])
    max_sum = window_sum

    for i in range(k, len(nums)):
        window_sum += nums[i] - nums[i - k]  # 오른쪽 추가, 왼쪽 제거
        max_sum = max(max_sum, window_sum)

    return max_sum
```

### 3. 배열 뒤집기 / 회전
```python
# 배열 뒤집기 (in-place)
def reverse(nums, start, end):
    while start < end:
        nums[start], nums[end] = nums[end], nums[start]
        start += 1
        end -= 1

# 배열 k칸 오른쪽 회전
def rotate(nums, k):
    k = k % len(nums)
    reverse(nums, 0, len(nums) - 1)  # 전체 뒤집기
    reverse(nums, 0, k - 1)          # 앞부분 뒤집기
    reverse(nums, k, len(nums) - 1)  # 뒷부분 뒤집기
```

---

## 문자열 (String)

### 핵심 포인트
- 문자열은 **불변(immutable)**인 언어가 많음 (Python, Java)
- 문자열 연결 시 O(n) 비용 → StringBuilder/join 사용
- 아스키코드 활용: `ord('a')` = 97, `ord('A')` = 65

### 자주 쓰는 패턴

```python
# 1. 문자 빈도수 세기
from collections import Counter
freq = Counter("hello")  # {'l': 2, 'h': 1, 'e': 1, 'o': 1}

# 2. 애너그램 판별
def is_anagram(s, t):
    return Counter(s) == Counter(t)

# 3. 팰린드롬 판별
def is_palindrome(s):
    s = s.lower()
    left, right = 0, len(s) - 1
    while left < right:
        if s[left] != s[right]:
            return False
        left += 1
        right -= 1
    return True
```

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 10818 | 최소, 최대 | 브론즈3 | 배열 기초 |
| 2577 | 숫자의 개수 | 브론즈2 | 빈도수 카운팅 |
| 1546 | 평균 | 브론즈1 | 배열 순회 |
| 2559 | 수열 | 실버3 | 슬라이딩 윈도우 |
| 2003 | 수들의 합 2 | 실버4 | 투 포인터 |
| 1644 | 소수의 연속합 | 골드3 | 투 포인터 심화 |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| 두 수의 합 | Lv.1 | 기초 탐색 |
| 문자열 내 마음대로 정렬하기 | Lv.1 | 문자열 정렬 |
| 연속 부분 수열 합의 개수 | Lv.2 | 슬라이딩 윈도우 |

---

## 면접 예상 질문

1. **배열과 연결리스트의 차이점은?**
   - 메모리 할당, 접근 시간, 삽입/삭제 비용 비교

2. **동적 배열(ArrayList)의 크기 확장은 어떻게 동작하나요?**
   - 용량 초과 시 2배 크기의 새 배열 생성 → 복사 → amortized O(1)

3. **투 포인터와 브루트포스의 차이는?**
   - 브루트포스 O(n^2) → 투 포인터 O(n)으로 최적화
