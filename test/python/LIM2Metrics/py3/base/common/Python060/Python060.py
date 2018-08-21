from tkinter import *

root = Tk()
root.title('Canvas')
canvas = Canvas(root, width =400, height=400)

xy = 10, 105, 100, 200
canvas.create_arc(xy, start=0, extent=270, fill='gray60')

canvas.pack()
root.mainloop()