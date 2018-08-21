counters = [1, 2, 3, 4]

updated = []
for x in counters:
     updated.append(x + 10)              # add 10 to each item

print(updated)

def inc(x): return x + 10               # function to be run

print(list(map(inc, counters)))                      # collect results


print(list(map((lambda x: x + 3), counters)))        # function expression