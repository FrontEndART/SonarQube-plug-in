class Multiton(object):
    def __init__(self):
        self.instances = {}
 
    def __call__(self, key, instance):
        self.instances[key] = instance
 
    def get_instance(self, key):
        return self.instances[key]
 
class A:
    def __init__(self, *args, **kw):
        pass
 
m = Multiton()
a0 = m('a0', A())
a1 = m('a1', A())
print m.get_instance('a0')
print m.get_instance('a1')
