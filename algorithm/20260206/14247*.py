n = int(input())
h = list(map(int, input().split()))
a = list(map(int, input().split()))

l = sorted([[i, j] for i, j in zip(h, a)], key = lambda x: x[1])

tree = 0
for i in range(n):
    tree += l[i][0] + l[i][1] * i
print(tree)

