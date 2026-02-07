# 슬라이딩 윈도우

n, k = map(int, input().split())
nums = list(map(int, input().split()))
window = sum(nums[:k])
maxn = window

for i in range(k, n):
    window = window - nums[i-k] + nums[i]
    maxn = max(maxn, window)

print(maxn)