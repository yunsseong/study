n = int(input())
duration = [0]
pay = [0]

for i in range(n):
    d, p = map(int, input().split())
    duration.append(d)
    pay.append(p)

dp = [None] * (n+1)

def rec(day):
    if day == n + 1:
        return 0

    if dp[day] is not None:
        return dp[day]

    # 오늘 상담을 안하는 경우
    res = rec(day + 1)

    # 상담을 할 수 있는 기간인가
    if day + duration[day] <= n + 1:
        res = max(rec(day + duration[day]) + pay[day], res)
    dp[day] = res

    return res

print(rec(1))