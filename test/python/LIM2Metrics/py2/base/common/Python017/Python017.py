X = [1, 2, 3]
L = ['a', X, 'b']            # embed references to X's object
D = {'x':X, 'y':2}

X[1] = 'surprise'            # changes all three references!

print L

print D
