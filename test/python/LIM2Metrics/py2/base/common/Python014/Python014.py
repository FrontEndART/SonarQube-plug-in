max= 0
a = 2
b = 3
if a < b: max= b
if b < a: max= a
assert (max == a or max == b) and max >= a and max >= b
