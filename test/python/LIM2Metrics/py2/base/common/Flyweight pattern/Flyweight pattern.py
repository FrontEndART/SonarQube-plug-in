import weakref
 
class Card(object):
    card_pool = weakref.WeakValueDictionary()
 
    def __new__(cls, value, suit):
        obj = cls.card_pool.get(value + suit, None)
        if not obj:
            obj = object.__new__(cls)
            cls.card_pool[value + suit] = obj
 
        return obj
 
    def __init__(self, value, suit):
        self.value = value
        self.suit = suit
 
c1 = Card(0, 1)
c2 = Card(1, 0)
print id(c1), id(c2)