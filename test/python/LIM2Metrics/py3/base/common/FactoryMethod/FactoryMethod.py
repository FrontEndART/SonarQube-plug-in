#
# Pizza
#
class Pizza:
    def __init__(self):
        self.price = None
 
    def get_price(self):
        return self.price
 
class HamAndMushroomPizza(Pizza):
    def __init__(self):
        self.price = 8.5
 
class DeluxePizza(Pizza):
    def __init__(self):
        self.price = 10.5
 
class HawaiianPizza(Pizza):
    def __init__(self):
        self.price = 11.5
 
#
# PizzaFactory
#
class PizzaFactory:
    @staticmethod
    def create_pizza(pizza_type):
        if pizza_type == 'HamMushroom':
            return HamAndMushroomPizza()
        elif pizza_type == 'Deluxe':
            return DeluxePizza()
        elif pizza_type == 'Hawaiian':
            return HawaiianPizza()
 
if __name__ == '__main__':
    for pizza_type in ('HamMushroom', 'Deluxe', 'Hawaiian'):
        print('Price of {0} is {1}'.format(pizza_type, PizzaFactory.create_pizza(pizza_type).get_price()))
