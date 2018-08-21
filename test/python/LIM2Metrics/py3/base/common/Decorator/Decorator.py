def set_cost_decorator(func):
    def wrapper1(instance, cost):
        func(instance, cost)
    return wrapper1
 
def get_cost_decorator(additional_cost):
    def wrapper1(func):
        def wrapper2(instance):
            return func(instance) + additional_cost
        return wrapper2
    return wrapper1
 
class Coffee(object):
    @set_cost_decorator
    def set_cost(self, cost):
        self.cost = cost
 
    @get_cost_decorator(0.5)
    @get_cost_decorator(0.7)
    @get_cost_decorator(0.2)
    def get_cost(self):
        return self.cost
 
coffee = Coffee()
coffee.set_cost(1.0)
print(coffee.get_cost()) # 2.4
