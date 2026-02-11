# 그래프 (Graph)

## 개념

- **정점(Vertex)**과 **간선(Edge)**으로 이루어진 자료구조
- 트리는 그래프의 특수한 형태 (사이클 없는 연결 그래프)

### 종류

| 종류 | 설명 |
|------|------|
| 무방향 그래프 | 간선에 방향 없음 (양방향) |
| 방향 그래프 | 간선에 방향 있음 (단방향) |
| 가중치 그래프 | 간선에 비용(가중치)이 있음 |

### 표현 방법

```python
# 1. 인접 리스트 (Adjacency List) - 메모리 효율적, 대부분 이걸 사용
graph = {
    0: [1, 2],
    1: [0, 3],
    2: [0, 3],
    3: [1, 2]
}

# defaultdict 활용
from collections import defaultdict
graph = defaultdict(list)
graph[0].append(1)
graph[1].append(0)

# 2. 인접 행렬 (Adjacency Matrix) - 간선 존재 여부 O(1) 확인
#    0  1  2  3
# 0 [0, 1, 1, 0]
# 1 [1, 0, 0, 1]
# 2 [1, 0, 0, 1]
# 3 [0, 1, 1, 0]
```

| 비교 | 인접 리스트 | 인접 행렬 |
|------|-----------|----------|
| 공간 | O(V + E) | O(V^2) |
| 간선 확인 | O(degree) | O(1) |
| 모든 이웃 순회 | O(degree) | O(V) |
| 적합한 경우 | 희소 그래프 | 밀집 그래프 |

---

## DFS (깊이 우선 탐색)

한 방향으로 끝까지 탐색 후 되돌아오는 방식.

### 재귀 구현

```python
def dfs(graph, node, visited):
    visited.add(node)
    print(node)

    for neighbor in graph[node]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited)

# 사용
visited = set()
dfs(graph, 0, visited)
```

### 스택 구현

```python
def dfs_stack(graph, start):
    visited = set()
    stack = [start]

    while stack:
        node = stack.pop()
        if node in visited:
            continue
        visited.add(node)
        print(node)

        for neighbor in graph[node]:
            if neighbor not in visited:
                stack.append(neighbor)
```

---

## BFS (너비 우선 탐색)

가까운 노드부터 레벨 단위로 탐색.

```python
from collections import deque

def bfs(graph, start):
    visited = set([start])
    queue = deque([start])

    while queue:
        node = queue.popleft()
        print(node)

        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
```

### BFS로 최단 거리 구하기

```python
def shortest_path(graph, start, end):
    visited = set([start])
    queue = deque([(start, 0)])  # (노드, 거리)

    while queue:
        node, dist = queue.popleft()
        if node == end:
            return dist
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append((neighbor, dist + 1))

    return -1  # 도달 불가
```

---

## DFS vs BFS 비교

| 비교 | DFS | BFS |
|------|-----|-----|
| 자료구조 | 스택 (재귀) | 큐 |
| 탐색 순서 | 깊이 우선 | 너비 우선 |
| 최단 경로 | 보장 안 됨 | **보장** (가중치 없을 때) |
| 메모리 | 경로 길이만큼 | 레벨 너비만큼 |
| 활용 | 경로 탐색, 사이클 검출, 백트래킹 | 최단 거리, 레벨 탐색 |

---

## 핵심 패턴

### 1. 2차원 격자 탐색 (BFS/DFS 공통)

```python
# 상하좌우 이동
dx = [-1, 1, 0, 0]
dy = [0, 0, -1, 1]

def bfs_grid(grid, start_r, start_c):
    rows, cols = len(grid), len(grid[0])
    visited = [[False] * cols for _ in range(rows)]
    queue = deque([(start_r, start_c)])
    visited[start_r][start_c] = True

    while queue:
        r, c = queue.popleft()
        for i in range(4):
            nr, nc = r + dx[i], c + dy[i]
            if 0 <= nr < rows and 0 <= nc < cols and not visited[nr][nc] and grid[nr][nc] == 1:
                visited[nr][nc] = True
                queue.append((nr, nc))
```

### 2. 연결 요소 (Connected Components) 개수

```python
# 섬의 개수 구하기
def count_islands(grid):
    if not grid:
        return 0

    rows, cols = len(grid), len(grid[0])
    count = 0

    def dfs(r, c):
        if r < 0 or r >= rows or c < 0 or c >= cols or grid[r][c] == '0':
            return
        grid[r][c] = '0'  # 방문 처리
        dfs(r-1, c)
        dfs(r+1, c)
        dfs(r, c-1)
        dfs(r, c+1)

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == '1':
                dfs(r, c)
                count += 1

    return count
```

### 3. 위상 정렬 (Topological Sort)

DAG(방향 비순환 그래프)에서 순서 결정.

```python
from collections import deque

def topological_sort(num_nodes, edges):
    graph = defaultdict(list)
    in_degree = [0] * num_nodes

    for u, v in edges:
        graph[u].append(v)
        in_degree[v] += 1

    queue = deque([i for i in range(num_nodes) if in_degree[i] == 0])
    result = []

    while queue:
        node = queue.popleft()
        result.append(node)
        for neighbor in graph[node]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)

    return result if len(result) == num_nodes else []  # 사이클 있으면 빈 배열
```

---

## 최단 경로 알고리즘 (심화)

### 다익스트라 (Dijkstra)
가중치가 양수인 그래프에서 최단 경로.

```python
import heapq

def dijkstra(graph, start):
    dist = {node: float('inf') for node in graph}
    dist[start] = 0
    heap = [(0, start)]

    while heap:
        cost, node = heapq.heappop(heap)
        if cost > dist[node]:
            continue
        for neighbor, weight in graph[node]:
            new_cost = cost + weight
            if new_cost < dist[neighbor]:
                dist[neighbor] = new_cost
                heapq.heappush(heap, (new_cost, neighbor))

    return dist
```

---

## 추천 문제

### 백준
| 번호 | 문제 | 난이도 | 핵심 |
|------|------|--------|------|
| 1260 | DFS와 BFS | 실버2 | 기본 DFS/BFS |
| 2667 | 단지번호붙이기 | 실버1 | 격자 DFS |
| 1012 | 유기농 배추 | 실버2 | 연결 요소 |
| 2178 | 미로 탐색 | 실버1 | BFS 최단거리 |
| 7576 | 토마토 | 골드5 | 다중 시작점 BFS |
| 2252 | 줄 세우기 | 골드3 | 위상 정렬 |
| 1753 | 최단경로 | 골드4 | 다익스트라 |

### 프로그래머스
| 문제 | 난이도 | 핵심 |
|------|--------|------|
| 게임 맵 최단거리 | Lv.2 | BFS 격자 |
| 네트워크 | Lv.3 | 연결 요소 |
| 가장 먼 노드 | Lv.3 | BFS |

---

## 면접 예상 질문

1. **DFS와 BFS의 차이점과 각각의 활용 사례는?**
   - DFS: 경로 탐색, 사이클 검출 / BFS: 최단 거리

2. **인접 리스트와 인접 행렬의 차이는?**
   - 메모리, 간선 확인 시간, 적합한 그래프 유형

3. **BFS가 최단 경로를 보장하는 이유는?**
   - 레벨 단위 탐색이므로 먼저 도달한 경로가 최단
