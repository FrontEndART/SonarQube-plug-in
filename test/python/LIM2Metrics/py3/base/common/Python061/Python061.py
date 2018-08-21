def func(x, y, z): return x + y + z

print(func(*(2, 3, 4)))

f = lambda x, y, z: x + y + z

print(f(*(2, 3, 4)))