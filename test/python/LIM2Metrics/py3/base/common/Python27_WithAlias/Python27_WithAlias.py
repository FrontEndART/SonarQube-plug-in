from contextlib import ContextDecorator

class mycontext(ContextDecorator):
    def __enter__(self):
        return self

    def __exit__(self, *exc):
        return False



class b(object):
	var = []
	
K, b.var = [0, 1, 2], 4
		
with mycontext() as a:
    print("hello")
	
with mycontext() as L[2]:
    print("hello")
	
with mycontext() as b.var:
    print("hello")
	
with mycontext() as [a, b]:
    print("hello")
	
# del a, b, c

g = (1, 2, 3)

for x, y in g[:]:
    if x < 0: g.remove(x)
	
assign = 4
augassign += 4

del a, b
del assign, b.var