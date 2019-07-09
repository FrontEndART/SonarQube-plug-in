# Implementor
class drawing_api:
    def draw_circle(self, x, y, radius):
        pass
 
# ConcreteImplementor 1/2
class drawing_api1(drawing_api):
    def draw_circle(self, x, y, radius):
        print('API1.circle at %f:%f radius %f' % (x, y, radius))
 
# ConcreteImplementor 2/2
class drawing_api2(drawing_api):
    def draw_circle(self, x, y, radius):
        print('API2.circle at %f:%f radius %f' % (x, y, radius))
 
# Abstraction
class Shape:
    def draw(self):
        pass
 
    def resize_by_percentage(self, pct):
        pass
 
# Refined Abstraction
class CircleShape(Shape):
    def __init__(self, x, y, radius, drawing_api):
       self.x = x
       self.y = y
       self.radius = radius
       self.drawing_api = drawing_api
 
    def draw(self):
        self.drawing_api.draw_circle(self.x, self.y, self.radius)
 
    def resize_by_percentage(self, pct):
        self.radius *= pct
 
# Client
if __name__ == '__main__':
    shapes = [
        CircleShape(1, 2, 3, drawing_api1()),
        CircleShape(5, 7, 11, drawing_api2())
    ]
 
    for shape in shapes:
        shape.resize_by_percentage(2.5)
        shape.draw()
