from collections import defaultdict
 
class Observable (defaultdict):
 
    def __init__ (self):
        defaultdict.__init__(self, object)
 
    def emit (self, *args):
        '''Pass parameters to all observers and update states.'''
        for subscriber in self:
            response = subscriber(*args)
            self[subscriber] = response
 
    def subscribe (self, subscriber):
        '''Add a new subscriber to self.'''
        self[subscriber]
 
    def stat (self):
        '''Return a tuple containing the state of each observer.'''
        return tuple(self.values())