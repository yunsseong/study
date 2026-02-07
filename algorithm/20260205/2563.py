n = int(input())
point = [list(map(int, input().split())) for _ in range(n)]
grid = [[0] * 100 for _ in range(100)]

for r, c in point:
    for i in range(10):
        for j in range(10):
            grid[r + i][c + j] = 1

print(sum(list(map(sum, grid))))
