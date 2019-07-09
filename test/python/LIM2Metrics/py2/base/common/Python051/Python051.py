basket = ['apple', 'orange', 'apple', 'pear', 'orange', 'banana']
fruit = set(basket)                     # create a set without duplicates
print fruit

print 'orange' in fruit                 # fast membership testing

print 'crabgrass' in fruit


queue = ["A", "B", "C"]
queue.append("D")          
queue.append("E")          
print queue.pop(0)

print queue.pop(0)

print queue

L = []
L.append(1)                    # push onto stack
L.append(2)
print L

L.pop()                        # pop off stack
print L