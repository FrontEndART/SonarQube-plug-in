class Component(object):
    def __init__(self, *args, **kw):
        pass
 
class Leaf(Component):
    def __init__(self, *args, **kw):
        Component.__init__(self, *args, **kw)
 
class Composite(Component):
    def __init__(self, *args, **kw):
        Component.__init__(self, *args, **kw)
        self.children = []
 
    def append_child(self, child):
        self.children.append(child)
 
    def remove_child(self, child):
        self.children.remove(child)
 
c = Composite()
l = Leaf()
c.append_child(l)
