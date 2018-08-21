"""
Uses the EasyStatePattern module at http://code.activestate.com/recipes/576613/.
"""
import EasyStatePattern as esp
 
# The context class  (also serving as the Abstract State Class in GOF discussion)
class Parent(object):
    moodTable = esp.StateTable('mood')
 
    def __init__(self, pocketbookCash, piggybankCash):
      Parent.moodTable.initialize( self)
      self.pocketBook = pocketbookCash
      self.piggyBank = piggybankCash
 
    @esp.Transition(moodTable)
    def getPromotion(self): pass
 
    @esp.Transition(moodTable)
    def severalDaysPass(self): pass
 
    @esp.Event(moodTable)
    def askForMoney(self, amount): pass
 
    @esp.TransitionEvent(moodTable)
    def cashPaycheck(self, amount): pass
 
    @esp.TransitionEvent(moodTable)
    def getUnexpectedBill(self, billAmount ): pass
 
    def onEnter(self):
      print('Mood is now', self.mood.name())
 
# 
# Below are defined the states. Each state is a class, States may be derived from
# other states. Topmost states must have a __metaclass__ = stateclass( state_machine_class )
# declaration.
#
class Normal( Parent, metaclass=esp.stateclass( Parent ) ):
    def askForMoney(self, amount): 
        amountToReturn = min(amount, self.pocketBook )
        self.pocketBook -= amountToReturn
        if amountToReturn == 0.0:
            self.mood.nextState = Broke
        return amountToReturn
 
    def cashPaycheck(self, amount): 
        self.pocketBook += .7 * amount
        self.piggyBank += .3*amount
 
    def getUnexpectedBill(self, billAmount ): 
        amtFromPktBook = min(billAmount, self.pocketBook)
        rmngAmt = billAmount - amtFromPktBook
        self.piggyBank -= rmngAmt
        self.pocketBook -= amtFromPktBook
 
 
class Happy( Parent, metaclass=esp.stateclass( Parent ) ):
    def askForMoney(self, amount): 
        availableMoney = self.pocketBook + self.piggyBank
        amountToReturn = max(min(amount, availableMoney), 0.0)
        amountFromPktbook =  min(amountToReturn, self.pocketBook)
        self.pocketBook -= amountFromPktbook
        self.piggyBank -= (amountToReturn - amountFromPktbook)
 
        if amountToReturn == 0.0:
            self.mood.nextState = Broke
        return amountToReturn
 
    def cashPaycheck(self, amount): 
        self.pocketBook += .75 * amount
        self.piggyBank += .25*amount
 
    def getUnexpectedBill(self, billAmount ): 
        amtFromPktBook = min(billAmount, self.pocketBook)
        rmngAmt = billAmount - amtFromPktBook
        self.piggyBank -= rmngAmt
        self.pocketBook -= amtFromPktBook
 
    def onEnter(self):
      print('Yippee! Woo Hoo!', self.mood.name()*3)
 
 
class Grouchy( Parent, metaclass=esp.stateclass( Parent ) ):
    def askForMoney(self, amount): 
       return 0.0
 
    def cashPaycheck(self, amount): 
        self.pocketBook += .70 * amount
        self.piggyBank += .30*amount
 
    def getUnexpectedBill(self, billAmount ): 
        amtFromPktBook = min(billAmount, self.pocketBook)
        rmngAmt = billAmount - amtFromPktBook
        self.piggyBank -= rmngAmt
        self.pocketBook -= amtFromPktBook
 
 
class Broke( Normal ):
    """ No metaclass declaration as its as subclass of Normal. """
 
    def cashPaycheck(self, amount): 
        piggyBankAmt = min ( amount, max(-self.piggyBank, 0.0))
        rmngAmount = amount - piggyBankAmount
        self.pocketBook += .40 * rmngAmount
        self.piggyBank += (.60 * rmngAmount + piggyBankAmt)
 
    def askForMoney(self, amount): 
        amountToReturn = min(amount, self.pocketBook )
        self.pocketBook -= amountToReturn
        if amountToReturn == 0.0:
            self.mood.nextState = Broke
        return amountToReturn
 
    def onLeave(self):
        print('Glad to finally have those bills paid.')
 
 
#Setup the Transition table in the context class
#                  (getPromotion, severalDaysPass, cashPaycheck, getUnexpectedBill )
Parent.moodTable.nextStates( Normal, ( Happy, Normal, Normal, Grouchy ))
Parent.moodTable.nextStates( Happy, ( Happy, Happy, Happy, Grouchy ))
Parent.moodTable.nextStates( Grouchy, ( Happy, Normal, Grouchy, Grouchy ))
Parent.moodTable.nextStates( Broke, ( Normal, Broke, Grouchy, Broke ))
 
# This specifies the initial state. 
Parent.moodTable.initialstate = Normal
 
def Test():
    dad = Parent(50.0, 60.0)
    mom = Parent( 60.0, 100.0)
    amount = 30.0
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 30.0
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 20.0
    dad.cashPaycheck( 40.0)
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 28.0
    dad.cashPaycheck( 80.0)                           
    dad.getUnexpectedBill(50.0 )   # Grouchy
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 0.0
    dad.severalDaysPass()           # Normal
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 6.0
    dad.severalDaysPass()
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 0.0
    dad.cashPaycheck( 50.0)
    print('from Dad', amount, dad.askForMoney(amount))   # > 30.0 30.0
    dad.getPromotion()             # Yippee!
    dad.cashPaycheck( 100.0)
    amount = 200.0
    print('from Dad', amount, dad.askForMoney(amount))   # > 200.0 200.0
    print('from Mom', amount, mom.askForMoney(amount))   # > 200.0 60.0
 
Test()