from collections import deque
import sys

n, m = map(int, input().split())
water = deque()
q = deque()
g = []
water_visited = [[False] * m for _ in range(n)]
biber_visited = [[False] * m for _ in range(n)]
dist = [[0] * m for _ in range(n)]


for i in range(n):
    tmp = list(input())
    for j in range(m):
        if tmp[j] == "*":
            water.append([i, j])
            water_visited[i][j] = True
        elif tmp[j] == "S":
            q.append([i, j])
            biber_visited[i][j] = True
    g.append(tmp)

while q:
    next_water = []
    while water:
        wr, wc = water.popleft()
        for dr, dc in [[0, 1], [1, 0], [-1, 0], [0, -1]]:
            nwr, nwc = wr + dr, wc + dc

            if 0 <= nwr < n and 0 <= nwc < m and g[nwr][nwc] != "D" and g[nwr][nwc] != "X" and not water_visited[nwr][nwc]:
                g[nwr][nwc] = "*"
                next_water.append([nwr, nwc])
                water_visited[nwr][nwc] = True
    water.extend(next_water)

    next_biber = []
    while q:
        r, c = q.popleft()

        if g[r][c] == "D":
            print(dist[r][c])
            sys.exit()

        for dr, dc in [[0, 1], [1, 0], [-1, 0], [0, -1]]:
            nr, nc = r + dr, c + dc
            if 0 <= nr < n and 0 <= nc < m and g[nr][nc] != "*" and g[nr][nc] != "X" and not biber_visited[nr][nc]:
                dist[nr][nc] = dist[r][c] + 1
                next_biber.append([nr, nc])
                biber_visited[nr][nc] = True
    q.extend(next_biber)

print("KAKTUS")

