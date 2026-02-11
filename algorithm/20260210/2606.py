from collections import deque

n = int(input())
p = int(input())
g = [[] for _ in range(n+1)]

for _ in range(p):
    a, b = map(int, input().split())
    g[a].append(b)
    g[b].append(a)

visited = [0 for _ in range(n+1)]

q = deque()
q.append(1)
visited[1] = 1

while q:
    c = q.popleft()

    for nc in g[c]:
        if not visited[nc]:
            q.append(nc)
            visited[nc] = 1

print(sum(visited) - 1)