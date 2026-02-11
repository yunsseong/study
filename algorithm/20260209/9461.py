t = int(input())
for _ in range(t):
    n = int(input())
    dp = [0] * (n+1)
    
    dp[1] = 1

    if n >= 2:
        dp[2] = 1
    
    if n >= 3:
        dp[3] = 1
    
    if n >= 4:
        dp[4] = 2

    if n >= 5:
        dp[5] = 2

    for i in range(6, n+1):
        dp[i] = dp[i-1] + dp[i-5]

    print(dp[n])