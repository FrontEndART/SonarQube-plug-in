# Product
class Pizza:
    def __init__(self):
        self.dough = None
        self.sauce = None
        self.topping = None
 
    def set_dough(self, dough):
        self.dough = dough
 
    def set_sauce(self, sauce):
        self.sauce = sauce
 
    def set_topping(self, topping):
        self.topping = topping
 
# Abstract Builder
class PizzaBuilder:
    def __init__(self):
        self.pizza = None
 
    def get_pizza(self):
        return self.pizza
 
    def create_new_pizza_product(self):
        self.pizza = Pizza()
 
# ConcreteBuilder
class HawaiianPizzaBuilder(PizzaBuilder):
    def build_dough(self):
        self.pizza.set_dough("cross")
 
    def build_sauce(self):
        self.pizza.set_sauce("mild")
 
    def build_topping(self):
        self.pizza.set_topping("ham+pineapple")
 
# Director
class Cook:
    def __init__(self):
        self.pizza_builder = None
 
    def set_pizza_builder(self, pb):
        self.pizza_builder = pb
 
    def get_pizza(self):
        return self.pizza_builder.get_pizza()
 
    def construct_pizza(self):
        self.pizza_builder.create_new_pizza_product()
        self.pizza_builder.build_dough()
        self.pizza_builder.build_sauce()
        self.pizza_builder.build_topping()
 
# A given type of pizza being constructed.
if __name__ == '__main__':
    cook = Cook()
    cook.set_pizza_builder(HawaiianPizzaBuilder())
    cook.construct_pizza()
    pizza = cook.get_pizza()
