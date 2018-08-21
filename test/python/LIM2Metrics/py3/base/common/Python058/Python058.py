i = 0
while i < 256:
    print(chr(i), end=' ')
    if i != 0 and i % 8 == 0 :
        print()
    i = i + 1