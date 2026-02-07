n = int(input())
l = sorted([list(map(int, input().split())) for _ in range(n)], key = lambda x: x[0])
mn = max(l, key = lambda x : x[1])
max_index = l.index(mn)
acc = mn[1]
maxh = l[0][1]

for i in range(1, max_index + 1):
    acc += (l[i][0] - l[i-1][0]) * maxh
    maxh = max(maxh, l[i][1])

maxh = l[-1][1]

for i in range(n - 2, max_index - 1, -1):
    acc += (l[i+1][0] - l[i][0]) * maxh
    maxh = max(maxh, l[i][1])

print(acc)