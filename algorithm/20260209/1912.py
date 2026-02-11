n = int(input())
nums = [0]
nums.extend(list(map(int, input().split())))

dp = [float('-inf')] * (n+1)
dp[1] = nums[1]

for i in range(2, n+1):
    dp[i] = max(dp[i-1] + nums[i], nums[i])

print(max(dp))