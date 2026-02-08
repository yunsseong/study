n = int(input())
duration = [0]
pay = [0]

for i in range(n):
    d, p = map(int, input().split())
    duration.append(d)
    pay.append(p)

dp = [None] * (n+2)

for i in range(n, 0, -1):
    dp[day] = dp[day + 1]
    if day + duration[day] <= n + 1:
        dp[day] = max(dp[day], dp[day + duration[day]] + pay[day])

print(dp[1])