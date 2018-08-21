class car:
    def __init__(self):
        self.name = None;
        self.km = 11100;
        self.fuel = 5;
        self.oil = 5;
 
class fixFuel:
    def handleCar(self, car):
        if car.fuel < 10:
            print("added fuel");
            car.fuel = 100;
 
class fixKm:
    def handleCar(self, car):
        if car.km > 10000:
            print("made a car test.");
            car.km = 0;
 
class fixOil:
    def handleCar(self, car):
        if car.oil < 10:
            print("Added oil");
            car.oil = 100;
 
class garage:
    def __init__(self):
        self.handlers = [];
 
    def addHandler(self, handler):
        self.handlers.append(handler);
 
    def handleCar(self, car):
        for handler in self.handlers:
            handler.handleCar(car);
 
if __name__ == '__main__':
    handlers = [fixKm(), fixOil(), fixFuel()];
    garag = garage();
 
    for handle in handlers:
        garag.addHandler(handle);
    garag.handleCar(car());