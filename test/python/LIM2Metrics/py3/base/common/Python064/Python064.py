def saver(x=None):
     if x is None:         # no argument passed?
         x = []            # run code to make a new list
     x.append(1)           # changes new list object
     print(x)

saver([2])

saver()                   # doesn't grow here

saver()

def f(x, y=None):
    if y is None: y = []
    y.append(x)
    return y

print(f(23))  

def birthday1(name, age):
    print("Happy birthday,", name, "!", " I hear you're", age, "today.\n")

birthday1("Joe", 1)
birthday1(1, "Joe")
birthday1(name = "Joe", age = 1)
birthday1(age = 1, name = "Joe")