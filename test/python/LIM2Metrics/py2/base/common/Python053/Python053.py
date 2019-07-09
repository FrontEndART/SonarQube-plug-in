class Animal:
    def reply(self):   
       self.speak()        
    def speak(self):   
       print 'spam'        

class Mammal(Animal):
    def speak(self):   
      print 'speak?'

class Cat(Mammal):
    def speak(self):   
       print 'Speak from Cat'

class Dog(Mammal):
    def speak(self):   
       print 'Speak from Dog'

class Primate(Mammal):
    def speak(self):   
       print 'Speak from primate!'

class Hacker(Primate): pass     

if __name__ == '__main__':
    spot = Cat()
    spot.reply()        
    data = Hacker()     
    data.reply()