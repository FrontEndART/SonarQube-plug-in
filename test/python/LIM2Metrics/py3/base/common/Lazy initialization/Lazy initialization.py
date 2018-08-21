class Fruit:
    def __init__(self, type):
        self.type = type
 
class Fruits:
    def __init__(self):
        self.types = {}
 
    def get_fruit(self, type):
        if type not in self.types:
            self.types[type] = Fruit(type)
 
        return self.types[type]
 
if __name__ == '__main__':
    fruits = Fruits()
    print(fruits.get_fruit('Apple'))
    print(fruits.get_fruit('Lime'))
