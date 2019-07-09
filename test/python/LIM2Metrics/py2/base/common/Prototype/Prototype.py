import copy
 
#
# Prototype Class
#
class Cookie:
    def __init__(self, name):
        self.name = name
 
    def clone(self):
        return copy.deepcopy(self)
 
#
# Concrete Prototypes to clone
#
class CoconutCookie(Cookie):
    def __init__(self):
        Cookie.__init__(self, 'Coconut')
 
#
# Client Class
#
class CookieMachine:
    def __init__(self, cookie):
        self.cookie = cookie
 
    def make_cookie(self):
        return self.cookie.clone()
 
if __name__ == '__main__':
    prot = CoconutCookie()
    cm = CookieMachine(prot)
 
    for i in xrange(10):
        temp_cookie = cm.make_cookie()
