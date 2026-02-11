n = int(input())
dp = [0] * (n+1)

for i in range(1, n+1):
    k = 1
    while k * k <= i:
        dp[i] = min(dp[i], dp[i-k*k] + 1)
        k+=1
print(dp[n])
