r= {x for x in range(2, 101) 
if not any(x % y == 0 for y in range(2, x))} 