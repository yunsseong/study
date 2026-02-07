n, k = map(int, input().split())
nums = list(map(int, input().split()))
maxn = float('-inf')
        
prefix = [0 for _ in range(n + 1)]

for i in range(1, n + 1):
    prefix[i] = prefix[i-1] + nums[i-1]

for i in range(n - k + 1):
    maxn = max(maxn, prefix[i + k] - prefix[i])

print(maxn)