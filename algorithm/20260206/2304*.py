n = int(input())
l = sorted([list(map(int, input().split())) for _ in range(n)], key = lambda x: x[0])
m = max(l, key = lambda x: x[1])
max_idx = l.index(m)

maxh = l[0][1]
acc = m[1]
for i in range(1, max_idx + 1):
    acc += maxh * (l[i][0] - l[i-1][0])
    maxh = max(l[i][1], maxh)

maxh = l[-1][1]
for i in range(len(l)-2, max_idx-1, -1):
    acc += maxh * (l[i+1][0] - l[i][0])
    maxh = max(l[i][1], maxh)
    
print(acc)

    
