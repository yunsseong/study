n = int(input())
duration, pay = [0], [0]
for _ in range(n):
    t, p = map(int, input().split())
    duration.append(t)
    pay.append(p)

dp = [0] * (n+2)

# dp[day] : day로부터 마지막날까지 벌 수 있는 최대 금액
for day in range(n, 0, -1):
    dp[day] = dp[day+1]
    if day + duration[day] <= n+1:
        dp[day] = max(dp[day+1], dp[day + duration[day]] + pay[day])
        
print(dp[1])
    