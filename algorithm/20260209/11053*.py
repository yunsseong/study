n = int(input())
nums = [0]
nums.extend(list(map(int, input().split())))

dp = [1] * (n+1)

for i in range(1, n+1):
    for j in range(1, i):
        if nums[j] < nums[i]:
            dp[i] = max(dp[i], dp[j] + 1)

print(max(dp))
