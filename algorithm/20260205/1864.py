while 1:
    s = input()
    if s == "#": break
    num = 0
    length = len(s)

    for x, i in enumerate(s):
        cur = 0
        if i == "\\":
            cur = 1
        elif i == "(":
            cur = 2
        elif i == "@":
            cur = 3
        elif i == "?":
            cur = 4
        elif i == ">":
            cur = 5
        elif i == "&":
            cur = 6
        elif i == "%":
            cur = 7
        elif i == "/":
            cur = -1
        num +=  cur * 8 ** (length - x - 1)

    print(num)