a, b = map(int, input().split())

ans = []
for i in range(-1001, 1001):
    if i ** 2 + 2 * a * i + b == 0:
        ans.append(i)
print(*sorted(list(set(ans))))
        