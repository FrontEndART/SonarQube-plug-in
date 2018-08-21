"""
Supports implementation of state pattern.

State Machine is defined a class containing one or more StateTable objeects, 
and using decorators to define Events and Transitions that are handled by the 
state machine.  Individual states are Subclasses of the state machine, with 
a __metaclass__ specifier.

Author: Rodney Drenth
Date:   January, 2009
Version: Whatever

Copyright 2009, Rodney Drenth

Permission to use granted under the terms and conditions of the Python Software
Foundation License
 (http://www.python.org/download/releases/2.4.2/license/)

"""
#! /usr/bin/env Python
#

import types

class _StateVariable( object ):
   """Used as attribute of a class to maintain state.

      State Variable objects are not directly instantiated in the user's state machine class
      but are instantiated by the StateTable class attribute defined within the state machine.
   """

   def __init__(self, stateTable):
      """ Constructs state variable and sets it to the initial state
      """
      self._current_state = stateTable.initialstate
      self._next_state = stateTable.initialstate
      self.sTable = stateTable

   def _toNextState(self, context):
      """Sets state of to the next state, if new state is different. 
      calls onLeave and onEnter methods
      """
      if self._next_state is not self._current_state:
         if hasattr(self._current_state, 'onLeave'):
            self._current_state.onLeave(context)
         self._current_state = self._next_state
         if hasattr(self._current_state, 'onEnter'):
            self._current_state.onEnter(context)

   def name(self):
      """Returns ame of current state."""
      return self._current_state.__name__

   def setXition(self, func):
      """ Sets the state to transitionto upon seeing Transition event"""
#      next_state = self.sTable.nextStates[self._current_state][func.__name__]
      next_state = self._current_state.nextStates[func.__name__]
      if next_state is not None:
         self._next_state = next_state

   def getFunc(self, func):
      """ Gets event handler function based on current State"""
      funky = getattr( self._current_state, func.__name__)
#      print 'current State:', self._current_state.__name__
      if funky is None:
         raise NotImplementedError('%s.%s'%(self.name(),func.__name__))

      # when funky.name is objCall, it means that we've recursed all the
      # way back to the context class and need to call func as a default
      return funky if funky.__name__ != 'objCall' else func


class StateTable( object ):
   """Defines a state table for a state machine class

   A state table for a class is associated with the state variable in the instances
   of the class. The name of the state variable is given in the constructor to the 
   StateTable object.  StateTable objects are attributes of state machine classes, 
   not intances of the state machine class.   A state machine class can have more
   than one StateTable.
   """

   def __init__(self, stateVarblName):
      """State Table initializer

      stateVarblName is the name of the associated state variable, which will
      be instantiated in each instance of the state machine class
      """
      self.inst_state_name = stateVarblName
      self.eventList = []
      self.initialstate = None
      nextStates = {}

   def initialize(self, obj ):
      """Initialization of StateTable and state varible

      obj is the instance of the state machine being initialized. This method
      must be called in the __init__ method of the user's state machine.
      """
      obj.__dict__[self.inst_state_name] = _StateVariable( self )

   def _addEventHandler(self, funcName):
      """Notifies State table of a method that's handle's an transition.

      This is called by @Transition and @TransitionEvent decorators,
      whose definitions are below.
      """
      self.eventList.append(funcName)

   def nextStates(self, subState, nslList):
      """Sets up transitions from the state specified by subState

      subState is a state class, subclassed from user's state machine class
      nslList is a list of states that will be transitioned to upon Transitions.
      This functions maps each @Transition decorated method in the state machine
      class to a to the corresponding state in this list.  None can be specified
      as a state to indicate the state should not change.
      """
      if len(nslList) != len(self.eventList):
         raise RuntimeError( "Wrong number of states in transition list.")
      subState.nextStates = dict(list(zip(self.eventList, nslList)))
#      self.nextStates[subState] = dict(zip( self.eventList, nslList))

def Event( state_table ):
   """Decorator for indicating state dependant method

   Decorator is applied to methods of state machine class to indicate that
   invoking the method will call state dependant method.   States are implemented
   as subclasses of the state machine class with a metaclass qualification.
   """
   stateVarName = state_table.inst_state_name

   def wrapper(func):
      # no adding of event handler to statetable...
      def objCall( self, *args, **kwargs):
         state_var = getattr(self, stateVarName )
         retn = state_var.getFunc(func)(self, *args, **kwargs)
         return retn

      return objCall
   return wrapper


def Transition( state_table ):
   """Decorator for indicating the method causes a state transition.

   Decorator is applied to methods of the state machine class. Invokeing
   the method can cause a transition to another state.  Transitions are defined
   using the nextStates method of the StateTable class
   """
   stateVarName = state_table.inst_state_name

   def wrapper(func):
      state_table._addEventHandler( func.__name__)

      def objCall( self, *args, **kwargs):
         state_var = getattr(self, stateVarName )
         state_var.setXition(func)
         rtn = func(self, *args, **kwargs)
         state_var._toNextState(self)
         return  rtn

      return objCall
   return wrapper


def TransitionEvent( state_table ):
   """Decorator for defining a method that both triggers a transition and
   invokes a state dependant method.

   This is equivalent to, but more efficient than using
   @Transition(stateTable)
   @Event(stateTable)
   """
   stateVarName = state_table.inst_state_name

   def wrapper(func):
      state_table._addEventHandler( func.__name__)

      def objCall( self, *args, **kwargs):
         state_var = getattr(self, stateVarName )
#         if not issubclass( state_var._current_state, self.__class__):
#            raise TypeError('expecting instance to be derived from %s'% baseClasss.__name__)
         state_var.setXition(func)
         retn = state_var.getFunc(func)(self, *args, **kwargs)
         state_var._toNextState(self)
         return retn

      return objCall
   return wrapper


class _stateclass(type):
   """ A stateclass metaclass 
   
   Modifies class so its subclass's methods so can be called as methods
   of a base class. Is used for implementing states
   """

   def __init__(self, name, bases, cl_dict):
      self.__baseClass__ = _bcType              # global - set by stateclass(), which follows
      if not issubclass(self, _bcType):
         raise TypeError( 'Class must derive from %s'%_bcType.__name__ )
      type.__init__(self, name, bases, cl_dict)

   def __getattribute__(self, name):
      if name.startswith('__'):
            return type.__getattribute__(self, name)
      try:
         atr = self.__dict__[name]
         if type(atr) == types.FunctionType:
            atr = types.MethodType(atr, None, self.__baseClass__)
      except KeyError as ke:
         for bclas in self.__bases__:
            try:
               atr = getattr(bclas, name)
               break
            except AttributeError as ae:
               pass
         else:
            raise AttributeError( "'%s' has no attribute '%s'" % (self.__name__, name) )
      return atr


def stateclass( statemachineclass ):
   """A method that returns a metaclass for constructing states of the
   users state machine class
   """
   global _bcType
   _bcType = statemachineclass
   return _stateclass

#  vim : set ai sw=3 et ts=6 :
