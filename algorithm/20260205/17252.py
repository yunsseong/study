import sys

num = int(input())

if num == 0:
    print("NO")
    sys.exit()

cnt = 0
while 1:
        if num >= 3 ** cnt:
            cnt += 1
        else:
            cnt -= 1
            break

for i in range(cnt, -1, -1):
    if num >= 3 ** i:
        num -= 3 ** i

if num == 0:
    print("YES")
else:
    print("NO")