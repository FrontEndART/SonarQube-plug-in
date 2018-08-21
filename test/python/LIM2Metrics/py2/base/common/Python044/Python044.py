import sys, traceback


MyError = 'hello'

def oops():
    raise MyError, 'world'



def safe(entry, *args):
    try:
        apply(entry, args)                 # catch everything else
    except:
        traceback.print_exc()
        print 'Got', sys.exc_type, sys.exc_value

if __name__ == '__main__':
    safe(oops)