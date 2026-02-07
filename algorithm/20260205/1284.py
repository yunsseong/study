while 1:
    num = input()
    if num == "0": break

    cnt = len(num) + 1
    for i in num:
        if i == "1": cnt += 2
        elif i == "0": cnt += 4
        else: cnt += 3

    print(cnt)    

