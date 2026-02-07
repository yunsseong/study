n, k = map(int, input().split())
l = [int(input()) for _ in range(n)]

start = 1
end = max(l)

while start <= end:
    mid = (start + end) // 2
    cnt = sum(i // mid for i in l)
    if cnt >= k:
        start = mid + 1
    else:
        end = mid - 1

print(end)