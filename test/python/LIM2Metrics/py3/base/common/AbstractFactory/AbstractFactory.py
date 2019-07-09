#!/usr/bin/python
#-*- coding: utf-8 -*-
 
class button:
    """Root class of GUI button."""
    def paint(self):
        pass
 
class button_win (button):
    def paint(self):
        print('A Windows button!')
 
class button_x (button):
    def paint(self):
        print('An X button!')
 
class gui_factory:
    def create_button(self):
        pass
 
class gui_factory_win (gui_factory):
    def create_button(self):
        return button_win()
 
class gui_factory_x (gui_factory):
    def create_button(self):
        return button_x()
 
class client:
 
    def __init__(self, os_type):
        if 'windows' == os_type:
            self.gui_factory = gui_factory_win()
        elif 'linux' == os_type:
            self.gui_factory = gui_factory_x()
        else:
            print('Unknown OS')
            exit()
 
    def new_button(self):
        return self.gui_factory.create_button()
 
if __name__ == '__main__':
    app = client(input('OS is: '))
    app.new_button().paint()
