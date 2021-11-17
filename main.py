from App import App

app = App()
app.mainloop()
# from tkinter import *
#
# import pyqrcode
#
# url = pyqrcode.create('192.168.43.207:9999')
# # print(url.terminal(quiet_zone=1))
# root = Tk()
# img = BitmapImage(data=url.xbm(scale=8))
# Label(root, image=img).pack()
# print(img)
# root.mainloop()
#
#
# def make_qr(master, data='192.168.43.207:9999'):
#     url = pyqrcode.create(data)
#     img = BitmapImage(data=url.xbm(scale=8))
#     Label(root, image=img).pack()