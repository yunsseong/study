# 해시맵 (HashMap)

## 개념

- **Key-Value** 쌍으로 데이터를 저장하는 자료구조
- 해시 함수를 사용하여 키를 인덱스로 변환
- 평균 O(1)로 삽입, 삭제, 탐색 가능

### 동작 원리

```
Key → 해시 함수 → 해시값 → 인덱스 → 버킷에 저장

예시:
"apple" → hash("apple") → 394829 → 394829 % 16 = 5 → bucket[5]에 저장
```

### 시간복잡도

| 연산 | 평균 | 최악 |
|------|------|------|
| 삽입 | O(1) | O(n) |
| 삭제 | O(1) | O(n) |
| 탐색 | O(1) | O(n) |

> 최악: 모든 키가 같은 버킷에 몰리는 경우 (해시 충돌)

---

## 해시 충돌 (Hash Collision)

서로 다른 키가 같은 인덱스에 매핑되는 현상.

### 해결법 1: 체이닝 (Chaining)
같은 인덱스에 연결리스트로 연결.

```
bucket[5] → ("apple", 3) → ("banana", 7) → null
```

- 장점: 구현 간단, 삭제 쉬움
- 단점: 메모리 추가 사용, 캐시 효율 낮음

### 해결법 2: 오픈 어드레싱 (Open Addressing)
충돌 시 다른 빈 버킷을 찾아 저장.

- **선형 탐사**: 다음 칸으로 이동 (i+1, i+2, ...)
- **이차 탐사**: 제곱만큼 이동 (i+1, i+4, i+9, ...)
- **이중 해싱**: 두 번째 해시 함수 사용

### Java HashMap 동작 (면접 빈출)

```
1. 초기 버킷 크기: 16
2. 로드 팩터: 0.75 (75% 차면 2배로 확장)
3. 충돌 해결: 체이닝 (연결리스트)
4. 리스트 길이 ≥ 8: 레드-블랙 트리로 변환 (O(log n))
5. 트리 노드 ≤ 6: 다시 연결리스트로 변환
```

---

## Python 활용

```python
# 딕셔너리 (Python의 해시맵)
d = {}
d["name"] = "John"    # 삽입
d["name"]             # 조회 → "John"
del d["name"]         # 삭제
"name" in d           # 존재 확인 → False

# defaultdict - 기본값 자동 생성
from collections import defaultdict
counter = defaultdict(int)
counter["a"] += 1     # 키가 없어도 0에서 시작

graph = defaultdict(list)
graph["a"].append("b")  # 키가 없어도 빈 리스트에서 시작

# Counter - 빈도수 세기
from collections import Counter
freq = Counter([1, 2, 2, 3, 3, 3])
# Counter({3: 3, 2: 2, 1: 1})
freq.most_common(2)  # [(3, 3), (2, 2)]
```

---

## 핵심 패턴

### 1. 빈도수 카운팅

```python
# 문자열에서 가장 많이 등장하는 문자
def most_frequent(s):
    freq = {}
    for char in s:
        freq[char] = freq.get(char, 0) + 1
    return max(freq, key=freq.get)
```

### 2. Two Sum (해시맵 활용의 정석)

```python
# 배열에서 두 수의 합이 target인 인덱스 쌍 찾기
# 브루트포스 O(n^2) → 해시맵 O(n)
def two_sum(nums, target):
    seen = {}  # {값: 인덱스}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []
```

### 3. 중복 검사

```python
# 배열에 중복 요소가 있는지
def has_duplicate(nums):
    return len(nums) != len(set(nums))

# 또는
def has_duplicate(nums):
    seen = set()
    for num in nums:
        if num in seen:
            return True
        seen.add(num)
    return False
```

### 4. 그룹핑 (애너그램 그룹)

```python
# ["eat","tea","tan","ate","nat","bat"]
# → [["eat","tea","ate"], ["tan","nat"], ["bat"]]
def group_anagrams(strs):
    groups = defaultdict(list)
    for s in strs:
        key = tuple(sorted(s))  # 정렬된 문자열을 키로
        groups[key].append(s)
    return list(groups.values())
```

---

## Set (집합)

해시맵에서 Value 없이 Key만 저장하는 자료구조.

```python
s = set()
s.add(1)        # 삽입 O(1)
s.remove(1)     # 삭제 O(1)
1 in s           # 탐색 O(1)

# 집합 연산
a = {1, 2, 3}
b = {2, 3, 4}
a | b    # 합집합: {1, 2, 3, 4}
a & b    # 교집합: {2, 3}
a - b    # 차집합: {1}
a ^ b    # 대칭차집합: {1, 4}
```

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 10816 | 숫자 카드 2 | 실버4 | 빈도수 카운팅 |
| 1764 | 듣보잡 | 실버4 | 교집합 |
| 7785 | 회사에 있는 사람 | 실버5 | 해시셋 |
| 1620 | 나는야 포켓몬 마스터 | 실버4 | 양방향 매핑 |
| 9375 | 패션왕 신해빈 | 실버3 | 해시맵 + 조합 |
| 17219 | 비밀번호 찾기 | 실버4 | 기본 해시맵 |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| 폰켓몬 | Lv.1 | Set 활용 |
| 완주하지 못한 선수 | Lv.1 | Counter 활용 |
| 전화번호 목록 | Lv.2 | 해시 탐색 |
| 위장 | Lv.2 | 해시맵 + 수학 |
| 베스트앨범 | Lv.3 | 복합 해시맵 |

---

## 면접 예상 질문

1. **해시맵의 동작 원리를 설명해주세요**
   - 해시 함수 → 인덱스 변환 → 버킷 저장 → 충돌 처리

2. **해시 충돌이란? 해결 방법은?**
   - 체이닝(연결리스트), 오픈 어드레싱(선형/이차 탐사, 이중 해싱)

3. **해시맵의 시간복잡도가 O(n)이 될 수 있는 경우는?**
   - 모든 키가 같은 버킷에 충돌, 로드 팩터가 높을 때

4. **좋은 해시 함수의 조건은?**
   - 균일 분포, 빠른 계산, 결정적(같은 입력 = 같은 출력)
