# DP & 그리디

## 다이나믹 프로그래밍 (DP)

### 개념
- **큰 문제를 작은 부분 문제로 나누어** 풀고, 결과를 저장하여 재사용
- 핵심 조건:
  - **최적 부분 구조**: 부분 문제의 최적해로 전체 최적해 구성 가능
  - **중복 부분 문제**: 같은 부분 문제가 반복적으로 등장

### 접근 방식

| 방식 | 설명 | 방향 |
|------|------|------|
| Top-Down (메모이제이션) | 재귀 + 캐싱 | 큰 문제 → 작은 문제 |
| Bottom-Up (타뷸레이션) | 반복문으로 테이블 채우기 | 작은 문제 → 큰 문제 |

### DP 풀이 4단계

```
1. 상태 정의    : dp[i]가 무엇을 의미하는지 정의
2. 점화식 도출  : dp[i]를 이전 값들로 표현
3. 초기값 설정  : 기저 조건 (base case)
4. 계산 순서    : 어떤 순서로 채울지 결정
```

---

### 기본 예제: 피보나치

```python
# Top-Down (메모이제이션)
def fib_top_down(n, memo={}):
    if n <= 1:
        return n
    if n in memo:
        return memo[n]
    memo[n] = fib_top_down(n-1, memo) + fib_top_down(n-2, memo)
    return memo[n]

# Bottom-Up (타뷸레이션)
def fib_bottom_up(n):
    if n <= 1:
        return n
    dp = [0] * (n + 1)
    dp[1] = 1
    for i in range(2, n + 1):
        dp[i] = dp[i-1] + dp[i-2]
    return dp[n]

# 공간 최적화 O(1)
def fib_optimized(n):
    if n <= 1:
        return n
    prev, curr = 0, 1
    for _ in range(2, n + 1):
        prev, curr = curr, prev + curr
    return curr
```

---

### 대표 유형

#### 1. 계단 오르기 (1칸 또는 2칸)

```python
# dp[i] = i번째 계단에 도달하는 방법의 수
# 점화식: dp[i] = dp[i-1] + dp[i-2]
def climb_stairs(n):
    if n <= 2:
        return n
    dp = [0] * (n + 1)
    dp[1] = 1
    dp[2] = 2
    for i in range(3, n + 1):
        dp[i] = dp[i-1] + dp[i-2]
    return dp[n]
```

#### 2. 배낭 문제 (0/1 Knapsack)

```python
# 무게 제한 W, 각 물건의 (무게, 가치)
# dp[i][w] = i번째 물건까지 고려, 무게 w일 때 최대 가치
def knapsack(W, weights, values):
    n = len(weights)
    dp = [[0] * (W + 1) for _ in range(n + 1)]

    for i in range(1, n + 1):
        for w in range(W + 1):
            if weights[i-1] <= w:
                dp[i][w] = max(
                    dp[i-1][w],                          # 안 넣기
                    dp[i-1][w - weights[i-1]] + values[i-1]  # 넣기
                )
            else:
                dp[i][w] = dp[i-1][w]

    return dp[n][W]
```

#### 3. 최장 증가 부분 수열 (LIS)

```python
# dp[i] = arr[i]로 끝나는 LIS의 길이
# 점화식: dp[i] = max(dp[j] + 1) (j < i, arr[j] < arr[i])
def lis(arr):
    n = len(arr)
    dp = [1] * n

    for i in range(1, n):
        for j in range(i):
            if arr[j] < arr[i]:
                dp[i] = max(dp[i], dp[j] + 1)

    return max(dp)

# [10, 20, 10, 30, 20, 50] → LIS = [10, 20, 30, 50] → 길이 4
```

#### 4. 최장 공통 부분 수열 (LCS)

```python
# dp[i][j] = s1[:i]와 s2[:j]의 LCS 길이
def lcs(s1, s2):
    m, n = len(s1), len(s2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]

    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if s1[i-1] == s2[j-1]:
                dp[i][j] = dp[i-1][j-1] + 1
            else:
                dp[i][j] = max(dp[i-1][j], dp[i][j-1])

    return dp[m][n]
```

---

## 그리디 (Greedy)

### 개념
- **매 순간 최선의 선택**을 하는 알고리즘
- 지역 최적해 → 전역 최적해 (항상 성립하지는 않음)

### 그리디가 적용 가능한 조건
1. **탐욕 선택 속성**: 현재의 최선 선택이 이후 선택에 영향을 주지 않음
2. **최적 부분 구조**: 부분 문제의 최적해가 전체 최적해를 구성

### 대표 유형

#### 1. 활동 선택 문제 (회의실 배정)

```python
# 가장 많은 회의를 배정하려면?
# 전략: 끝나는 시간이 빠른 순으로 선택
def max_meetings(meetings):
    meetings.sort(key=lambda x: x[1])  # 종료 시간 기준 정렬

    count = 0
    last_end = 0

    for start, end in meetings:
        if start >= last_end:
            count += 1
            last_end = end

    return count
```

#### 2. 동전 문제 (거스름돈)

```python
# 최소 동전 개수로 거스름돈 만들기
# 전략: 큰 동전부터 사용 (동전이 배수 관계일 때만 가능)
def min_coins_greedy(coins, amount):
    coins.sort(reverse=True)
    count = 0

    for coin in coins:
        count += amount // coin
        amount %= coin

    return count if amount == 0 else -1
```

#### 3. 분할 가능 배낭 (Fractional Knapsack)

```python
# 물건을 쪼갤 수 있는 배낭 문제
# 전략: 무게 대비 가치(가성비)가 높은 순서로 담기
def fractional_knapsack(W, items):
    items.sort(key=lambda x: x[1] / x[0], reverse=True)  # 가성비 순

    total_value = 0
    for weight, value in items:
        if W >= weight:
            total_value += value
            W -= weight
        else:
            total_value += value * (W / weight)  # 일부만 담기
            break

    return total_value
```

---

## DP vs 그리디 비교

| 비교 | DP | 그리디 |
|------|-----|-------|
| 접근 | 모든 경우 고려 | 현재 최선만 선택 |
| 최적해 보장 | 항상 보장 | 조건 충족 시에만 |
| 시간복잡도 | 보통 더 느림 | 보통 더 빠름 |
| 공간 | 테이블 필요 | 적음 |
| 적합한 문제 | 배낭, LIS, LCS | 활동 선택, 정렬 기반 |

**판단 기준**: "이전 선택이 다음 선택에 영향을 주는가?"
- Yes → DP
- No → 그리디 가능성 검토

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 유형 |
|------|------|--------|------|
| 1003 | 피보나치 함수 | 실버3 | DP 기초 |
| 1463 | 1로 만들기 | 실버3 | DP 기초 |
| 9095 | 1, 2, 3 더하기 | 실버3 | DP 기초 |
| 12865 | 평범한 배낭 | 골드5 | 배낭 문제 |
| 11053 | 가장 긴 증가하는 부분 수열 | 실버2 | LIS |
| 9251 | LCS | 골드5 | LCS |
| 11047 | 동전 0 | 실버4 | 그리디 |
| 1931 | 회의실 배정 | 실버1 | 그리디 |
| 1541 | 잃어버린 괄호 | 실버2 | 그리디 |

### 프로그래머스
| 문제 | 난이도 | 유형 |
|------|--------|------|
| N으로 표현 | Lv.3 | DP |
| 정수 삼각형 | Lv.3 | DP |
| 등굣길 | Lv.3 | DP |
| 체육복 | Lv.1 | 그리디 |
| 구명보트 | Lv.2 | 그리디 |
| 큰 수 만들기 | Lv.2 | 그리디 |

---

## 면접 예상 질문

1. **DP와 그리디의 차이점은?**
   - DP는 모든 경우를 고려, 그리디는 매 순간 최선만 선택

2. **메모이제이션과 타뷸레이션의 차이는?**
   - Top-Down(재귀+캐싱) vs Bottom-Up(반복문)

3. **DP 문제를 어떻게 판별하나요?**
   - 최적 부분 구조 + 중복 부분 문제가 있는지 확인
