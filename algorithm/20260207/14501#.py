n = int(input())
c = [[0, 0]]
for _ in range(n):
    c.append(list(map(int, input().split())))

dp = {}

res = 0
def rec(day):
    if day >= n+1:
       return 0

    if day in dp:
        return dp[day]

    # 오늘 상담을 안하는 경우
    res = rec(day + 1)
    # 오늘 상담을 하는 경우
    if day + c[day][0] <= n:
        res = max(rec(day + c[day][0]) + c[day][1], res)

    dp[day] = res
    return res

print(max(rec(1)))