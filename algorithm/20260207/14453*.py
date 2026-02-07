n = int(input())
l = [0]

for _ in range(n):
    l.append(input())

h, p, s = [0] * (n+1), [0] * (n+1), [0] * (n+1)

for i in range(1, n+1):
    if l[i] == "H":
        h[i] += 1
    elif l[i] == "P":
        p[i] += 1
    else: 
        s[i] += 1

    h[i], p[i], s[i] = h[i] + h[i-1], p[i] + p[i-1], s[i] + s[i-1]

ans = 0
for i in range(n):
    ans = max(ans, max(h[i], p[i], s[i]) + max(h[-1] - h[i], p[-1] - p[i], s[-1] - s[i]))

print(ans)