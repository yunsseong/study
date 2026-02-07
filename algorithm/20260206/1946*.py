import sys
input = sys.stdin.readline

t = int(input())

for _ in range(t):
    n = int(input())
    case = sorted([list(map(int, input().split())) for _ in range(n)], key = lambda x:x[0])

    cnt = 0
    nth =  100001
    for i in range(n):
        if case[i][1] < nth:
            nth = case[i][1]
            cnt += 1

    print(cnt)

    