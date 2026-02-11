n = int(input())
for _ in range(n):
    case = int(input())
    dp = [0] * (case+1)

    dp[1] = 1
    if case >= 2:
        dp[2] = 2
    if case >= 3:
        dp[3] = 4
    
    for i in range(4, case+1):
        dp[i] = dp[i-1] + dp[i-2] + dp[i-3]
    
    print(dp[case])