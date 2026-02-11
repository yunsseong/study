from collections import deque
import sys

n, m = map(int, input().split())
g = [list(map(int, list(input()))) for _ in range(n)]
point = []
for i in range(m):
    if g[0][i] == 0:
        point.append([0, i])

for pr, pc in point:
    q = deque()
    q.append([pr, pc])
    visited = [[False] * m for _ in range(n)]
    visited[pr][pc] = True
    while q:
        r, c = q.popleft()

        if r == n-1:
            print("YES")
            sys.exit()

        for dr, dc in [[0, 1], [1, 0], [-1, 0], [0, -1]]:
            nr, nc = r + dr, c + dc

            if 0 <= nr < n and 0 <= nc < m and g[nr][nc] == 0 and not visited[nr][nc]:
                q.append([nr, nc])
                visited[nr][nc] = True

print("NO")