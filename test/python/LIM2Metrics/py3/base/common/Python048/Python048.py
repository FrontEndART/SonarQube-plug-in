
from tkinter import *

def showAllEvent(event):
    print(event)
    for attr in dir(event): 
        print(attr, '=>', getattr(event, attr)) 


def showPosEvent(event):
    print('Widget=%s X=%s Y=%s' % (event.widget, event.x, event.y))


def onMiddleClick(event):
    print('Got middle mouse button click:', end=' ') 
    showPosEvent(event)
    showAllEvent(event)


tkroot = Tk()
labelfont = ('courier', 20, 'bold')               
widget = Label(tkroot, text='Hello bind world')
widget.config(bg='red', font=labelfont)        
widget.config(height=5, width=20)              
widget.pack(expand=YES, fill=BOTH)

widget.bind('<Button-2>',  onMiddleClick)       


widget.focus()                                
tkroot.title('Click Me')
tkroot.mainloop()