def raiseExceptionDoNotCatch():
   try:
      print "In raiseExceptionDoNotCatch"
      raise Exception
   finally:
      print "Finally executed in raiseExceptionDoNotCatch"

   print "Will never reach this point"

print "\nCalling raiseExceptionDoNotCatch"

try:
   raiseExceptionDoNotCatch()
except Exception:
   print "Caught exception from raiseExceptionDoNotCatch in main program."