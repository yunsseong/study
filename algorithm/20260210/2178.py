from collections import deque

n, m = map(int, input().split())
g = [list(map(int, list(input()))) for _ in range(n)]
q = deque()
q.append([0, 0])

while q:
    r, c = q.popleft()

    for dr, dc in [[0, 1], [1, 0], [-1, 0], [0, -1]]:
        nr, nc = r + dr, c + dc
        
        if 0 <= nr < n and 0 <= nc < m and g[nr][nc] == 1:
            q.append([nr, nc])
            g[nr][nc] = g[r][c] + 1

print(g[n-1][m-1])
        
