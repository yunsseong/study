n = int(input())
nums = [[0]]
nums.extend(list(map(int, input().split())) for _ in range(n))
nums.sort(key = lambda x : x[0])

dp = [1] * (n+1)

for i in range(1, n+1):
    for j in range(1, i):
        if nums[i][1] > nums[j][1]:
            dp[i] = max(dp[i], dp[j] + 1)

print(n - max(dp))
