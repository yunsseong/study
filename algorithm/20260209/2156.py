n = int(input())
l = [0]
l.extend([int(input()) for _ in range(n)])

dp = [0] * (n+1)

dp[1] = l[1]

if n >= 2:
    dp[2] = l[1] + l[2]

if n >= 3:
    dp[3] = max(l[1] + l[2], l[1] + l[3], l[2] + l[3])  

for i in range(4, n+1):
    dp[i] = max(max(dp[i-2], dp[i-3] + l[i-1]) + l[i], dp[i-1])

print(dp[n])


