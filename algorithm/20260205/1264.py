while 1:
    s = input().lower()
    if s == "#": break

    cnt = 0
    for i in s:
        if i in ["a", "e", "i", "o", "u"]:
            cnt += 1
    print(cnt)