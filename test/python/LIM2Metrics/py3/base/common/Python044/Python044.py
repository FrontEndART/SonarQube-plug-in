import sys, traceback


MyError = 'hello'

def oops():
    raise MyError('world')



def safe(entry, *args):
    try:
        entry(*args)                 # catch everything else
    except:
        traceback.print_exc()
        print('Got', sys.exc_info()[0], sys.exc_info()[1])

if __name__ == '__main__':
    safe(oops)