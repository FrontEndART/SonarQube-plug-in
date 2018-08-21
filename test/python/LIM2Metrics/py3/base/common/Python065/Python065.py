def f1():
    x = 88
    def f2():
        print(x)
    f2()

f1()   


def f1():
    x = 88
    def f2(x=x):
        print(x)
    f2()

f1()  

def func():
    x = 4
    action = (lambda n: x ** n)          # x in enclosing def
    return action

x = func()
print(x(2)) # prints 16
