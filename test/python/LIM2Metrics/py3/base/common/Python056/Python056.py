#Define a __iter__() method which returns an 
#object with a next() method. If the class defines next(), then __iter__() can just 
#return self:

class Reverse:
    "Iterator for looping over a sequence backwards"
    def __init__(self, data):
        self.data = data
        self.index = len(data)
    def __iter__(self):
        return self
    def __next__(self):
        if self.index == 0:
            raise StopIteration
        self.index = self.index - 1
        return self.data[self.index]

for char in Reverse('test'):
     print(char)