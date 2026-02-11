from collections import deque
import sys

n, m = map(int, input().split())
g = []
tomato = deque()
for i in range(m):
    tmp = list(map(int, input().split()))
    g.append(tmp)
    for j in range(n):
        if tmp[j] == 1:
            tomato.append([i, j])

while tomato:
    r, c = tomato.popleft()

    for dr, dc in [[0, 1], [1, 0], [-1, 0], [0, -1]]:
        nr, nc = r + dr, c + dc
        
        if 0 <= nr < m and 0 <= nc < n and g[nr][nc] == 0:
            tomato.append([nr, nc])
            g[nr][nc] = g[r][c] + 1

for i in range(m):
    for j in range(n):
        if g[i][j] == 0:
            print(-1)
            sys.exit()

print(max(max(row) for row in g)-1)