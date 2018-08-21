def action(x):
     return (lambda y: x + y)

act = action(99)
print(act)

print(act(2))