t = int(input())

for _ in range(t):
    s = input()
    r, c = map(int, input().split())

    if r <= 2 and c <= 2:
        for _ in range(r):
            input()
        print(0)
        continue

    g = []
    for _ in range(r):
        g.append(list(input()))

    cnt = 0
    for i in range(r):
        for j in range(c - 2):
            if g[i][j] == ">" and g[i][j+1] == "o" and g[i][j+2] == "<":
                cnt += 1

    for i in range(r - 2):
        for j in range(c):
            if g[i][j] == "v" and g[i+1][j] == "o" and g[i+2][j] == "^":
                cnt += 1

    print(cnt)