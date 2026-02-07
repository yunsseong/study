import sys

n, k, a, b = map(int, input().split())
p = [k] * n
cnt = 1

while 1:
    minn = min(p)
    min_index = p.index(minn)

    for i in range(a):
        p[min_index + i] += b
    
    for i in range(n):
        p[i] -= 1

        if p[i] == 0:
            print(cnt)
            sys.exit()

    cnt += 1