import os
import socket
import pyqrcode
import datetime
from os import path
from tkinter import *
from typing import final
import tkinter.ttk as ttk
from threading import Thread
from socket import socket as sk
from tkinter import simpledialog, messagebox
from tkinter.filedialog import askopenfilenames


def dialog(title, text):
    # root = Tk()
    # root.withdraw()
    user_input = simpledialog.askstring(title=title, prompt=text)
    return user_input


def forget(args):
    for i in args:
        i.place_forget()


def place(arg, x, y, **kwargs):
    kwargs['x'] = x
    kwargs['y'] = y
    arg.place(**kwargs)


def grid(arg, row, column, **kwargs):
    kwargs['row'] = row
    kwargs['column'] = column
    arg.grid(**kwargs)


def get_size(size):
    if size >= 1_000_000_000:
        return " {:.2f} GB".format(size / 10 ** 9)
    elif size >= 1_000_000:
        return " {:.2f} MB".format(size / 10 ** 6)
    elif size >= 1_000:
        return " {:.2f} KB".format(size / 10 ** 3)


def get_host():
    return socket.gethostbyname(socket.gethostname())


class Dialog(Tk):
    def __init__(self, name, size):
        super().__init__()
        self.geometry("200x200")
        master = self.master
        self.current = Frame(master)
        self.current.pack(anchor=CENTER)
        label = Label(self.current, text=f'Receiving   {name}   with size  {get_size(size)}')
        label.pack(side=TOP)
        progress_bar = self.progress_bar = ttk.Progressbar(
            master=self.current,
            orient='horizontal',
            mode='determinate',
            length=100
        )
        progress_bar.pack(anchor=CENTER)
        Label(self.current).pack(side=BOTTOM)

    def seek_to(self, to):
        self.progress_bar['value'] = to
        if to >= float(99.8):
            self.current.pack_forget()


class App(Tk):
    def __init__(self):
        super().__init__()
        self.send_clicked_item = None
        self.geometry('400x500')
        self.minsize(width=400, height=500)
        # self.resizable(width=False, height=False)
        self.main_dir: final = r"C:\Users\vcc\Downloads\FILES"
        self.directory = self.make_directory()

        self.qr_code_image = None

        self.is_started = False
        self.send_dic = {}
        self.receive_dic = {}
        self.current_receive = None
        self.progress_bar = None
        self.current_size = None
        self.current_name = None

        self.port_entry = None
        self.server = None
        self.client = None
        self.host: final = get_host()
        self.port = 9999
        self.client_number = 1

        self.controller = ttk.Notebook(self.master)
        self.send_tab = ttk.Frame(self.controller)
        self.send_list_box = None
        self.send_list_box_size = None
        self.receive_tab = ttk.Frame(self.controller)
        self.receive_list_box = None
        self.receive_list_box_size = None
        self.settings_tab = ttk.Frame(self.controller)
        self.settings_button = None
        self.controller.add(self.settings_tab, text="  Settings  ")
        self.controller.add(self.receive_tab, text="  Receiving  ", state=DISABLED)
        self.controller.add(self.send_tab, text="  Sending  ", state=DISABLED)
        self.controller.pack(expand=1, fill="both")

        self.confirm_information()
        self.receive_tab_init()
        self.send_tab_init()

    def make_directory(self):
        now = datetime.datetime.now()
        directory = f"{now.year}_{now.month}_{now.day}"
        directory = "\\".join([self.main_dir, directory])
        if not path.exists(directory):
            os.makedirs(directory)
        return directory

    def server_init(self):
        self.server = sk(socket.AF_INET, socket.SOCK_STREAM)

    def disconnect(self):
        self.server.close()
        self.client = None
        self.current_name = None
        self.current_size = None
        self.qr_code_image.pack_forget()
        self.is_started = False
        self.button_change()

    def read_server_address(self):
        self.port = int(self.port_entry.get())

    def add_receiving(self, name: str):
        name, size = name.split("SIZE:")
        size = self.current_size = int(size)
        self.current_name = name
        self.current_receive = Dialog(name, size)
        return name

    def read_my_file(self):
        result = bytearray()
        client = self.client
        try:
            while True:
                result.extend(client.recv(1024))
                if len(result):
                    client.settimeout(1)
        except socket.timeout:
            try:
                file_name = result.decode()
                if file_name == "Exit":
                    answer = messagebox.askyesno(title="Client close app", message="Restart the server?")
                    self.settings_button.invoke()
                    if answer == "yes":
                        self.settings_button.invoke()
            except Exception as e:
                print(result[:20], len(result))
                raise e
            if not len(file_name.rstrip()):
                return "", ""
            result = bytearray()
            file_name = self.add_receiving(file_name)
            try:
                while True:
                    asd = client.recv(1_000_000)
                    print(len(asd))
                    result.extend(asd)
                    # result.extend(client.recv(1_000_000))
                    self.current_receive.seek_to((len(result) / self.current_size) * 100)
                    try:
                        if result[-5:].decode() == 'END__':
                            break
                    except Exception:
                        pass
                    client.settimeout(1)
            except socket.timeout:
                messagebox.showerror(title="Socket timeout", message="we have a problem")
                print(file_name)
                print(len(result))
                self.disconnect()
                exit(2)
            except Exception as e:
                print(len(result))
                print(self.current_name)
                print(self.current_size)
                self.disconnect()
                raise e
            return file_name, result[:-5]

    def name_maker(self):
        name = self.current_name
        number = 2
        file_type = name.split(".")[-1]
        file_name = ".".join(name.split(".")[:-1])
        name_ = file_name
        while True:
            file_path = "\\".join([self.directory, f"{file_name}.{file_type}"])
            if path.exists(file_path):
                file_name = f"{name_}{number}"
                number += 1
            else:
                return file_name, file_path

    def make_file(self, byte):
        file_name, file_path = self.name_maker()
        with open(file_path, "wb") as file:
            file.write(byte)
            file.flush()
        self.receive_dic[file_name] = file_path
        self.receive_list_box.insert(END, f" {file_name}")
        self.set_size(self.current_size, self.receive_list_box_size)

    def thread_loop(self):
        def loop():
            client, address = self.server.accept()
            self.client = client
            client.recv(4)
            self.qr_code_image.pack_forget()
            self.controller.tab(1, state=NORMAL)
            self.controller.tab(2, state=NORMAL)
            while True:
                name, data = self.read_my_file()
                if len(name) != 0 and len(data) != 0:
                    self.make_file(data)

        Thread(target=loop, daemon=False).start()

    def make_qr(self, master, data):
        self.url = pyqrcode.create(data)
        self.img = BitmapImage(data=self.url.xbm(scale=8))
        self.qr_code_image = Label(master, image=self.img)
        self.qr_code_image.pack(anchor=CENTER)

    def connect(self):
        self.read_server_address()
        self.server_init()
        self.server.bind((self.host, self.port))
        self.server.listen(self.client_number)
        self.is_started = True
        self.button_change()
        self.thread_loop()

    def button_change(self):
        if self.is_started:
            self.close_button(self.settings_button)
        else:
            self.start_button(self.settings_button)
        self.update()

    def choose_file(self):
        files = askopenfilenames()
        for file in files:
            file_path = str(file)
            file_name = file_path.split("/")[-1]
            if file_name not in self.send_dic.keys():
                self.send_dic[file_name] = file_path
                self.send_list_box.insert(END, f" {file_name}")
                size = path.getsize(file_path)
                self.set_size(size)

    def set_size(self, size, listbox=None, index=END):
        if listbox is None:
            listbox = self.send_list_box_size
        listbox.insert(index, get_size(size))

    def open_file(self, event, is_send=True):
        selection = event.widget.curselection()
        if selection:
            index = selection[0]
            if is_send:
                file_name = str(self.send_list_box.get(index))
                file_path = self.send_dic[file_name[1:]]
            else:
                file_name = str(self.receive_list_box.get(index))
                file_path = self.receive_dic[file_name[1:]]
            os.startfile(file_path)

    def open_file_location(self, event, is_send=True):
        selection = event.widget.curselection()
        if selection:
            index = selection[0]
            if is_send:
                file_name = str(self.send_list_box.get(index))
                file_path = str(self.send_dic[file_name[1:]]).split("/")[:-1]
            else:
                file_name = str(self.receive_list_box.get(index))
                file_path = str(self.receive_dic[file_name[1:]]).split("\\")[:-1]
                print(self.receive_dic[file_name[1:]])
            file_path = "/".join(file_path)
            os.system(f"start {file_path}")

    def make_menu(self, master, event, is_send=True):
        menu = Menu(master, tearoff=0)
        menu.add_command(label="Open file", command=lambda: [self.open_file(event, is_send)])
        menu.add_command(label="Open file location", command=lambda: [self.open_file_location(event, is_send)])
        if is_send:
            menu.add_separator()
            menu.add_command(label="Delete", command=lambda: [self.delete_item(event)])
            menu.add_command(label="Rename", command=lambda: [self.rename_item(event)])

        return menu

    def do_popup(self, master, event, is_send=True):
        selection = event.widget.curselection()
        if selection:
            menu = self.make_menu(master, event, is_send)
            try:
                menu.tk_popup(event.x_root, event.y_root)
            finally:
                menu.grab_release()

    def delete_item(self, event):
        selection = event.widget.curselection()
        if selection:
            index = selection[0]
            file_name = str(self.send_list_box.get(index))
            answer = messagebox.askquestion(title="Delete Item", message=f"Continue Deleting {file_name}")

            if answer == 'yes':
                self.send_list_box.delete(selection)
                self.send_list_box_size.delete(selection)
                self.send_dic.pop(file_name[1:])

    def rename_item(self, event):
        selection = event.widget.curselection()
        if selection:
            index = selection[0]
            file_name = str(self.send_list_box.get(index))
            value = self.send_dic[file_name[1:]]
            new_value = dialog("Rename", "Change file name:")
            if new_value is not None and new_value.strip() != "":
                self.send_list_box.delete(selection)
                self.send_list_box.insert(index, f" {new_value.strip()}")
                self.send_dic.pop(file_name[1:])
                self.send_dic[new_value] = value

    def send(self):
        pass

    def send_tab_init(self):
        master = self.send_tab
        frame_top = Frame(master)
        frame_top.pack()
        frame_scroller = Frame(master)
        frame_scroller.pack(expand=True, fill=BOTH)
        frame_bottom = Frame(master)
        frame_bottom.pack()

        chooser = Button(frame_top, text="Browse", command=lambda: [self.choose_file()])
        chooser.pack(anchor=CENTER, pady=10)

        frame = Frame(frame_scroller)
        frame.pack(side=LEFT, expand=True, fill=BOTH)

        self.send_list_box = Listbox(frame)
        self.send_list_box_size = Listbox(frame, width=12)
        scrollbar = Scrollbar(frame_scroller)
        self.send_list_box.pack(side=LEFT, fill=BOTH, expand=True, padx=(5, 5), pady=10)
        self.send_list_box_size.pack(side=RIGHT, fill=BOTH, pady=10)
        scrollbar.pack(side=RIGHT, fill=BOTH, pady=10)

        self.send_list_box.config(yscrollcommand=scrollbar.set)
        self.send_list_box_size.config(yscrollcommand=scrollbar.set)
        scrollbar.config(command=lambda *args: [self.send_list_box.yview(*args),
                                                self.send_list_box_size.yview(*args)])
        self.send_list_box.bind('<Button-3>', lambda event: self.do_popup(self.send_list_box, event))
        self.send_list_box.bind("<Double-Button-1>", self.rename_item)

        send_button = Button(frame_bottom, text="Send All", command=self.send)
        send_button.pack(anchor=CENTER, pady=10)

    def receive_tab_init(self):
        master = self.receive_tab

        frame = Frame(master)
        frame.pack(side=LEFT, expand=True, fill=BOTH)

        self.receive_list_box = Listbox(frame)
        self.receive_list_box_size = Listbox(frame, width=12)
        scrollbar = Scrollbar(master)
        self.receive_list_box.pack(side=LEFT, fill=BOTH, expand=True, padx=(5, 5), pady=10)
        self.receive_list_box_size.pack(side=RIGHT, fill=BOTH, pady=10)
        scrollbar.pack(side=RIGHT, fill=BOTH, pady=10)

        self.receive_list_box.config(yscrollcommand=scrollbar.set)
        self.receive_list_box_size.config(yscrollcommand=scrollbar.set)
        scrollbar.config(command=lambda *args: [self.receive_list_box.yview(*args),
                                                self.receive_list_box_size.yview(*args)])
        self.receive_list_box.bind('<Button-3>',
                                   lambda event: self.do_popup(self.receive_list_box, event, is_send=False))

    def set_host(self):
        self.host = get_host()

    def reset_host(self, text_host):
        if not self.is_started:
            self.set_host(),
            text_host.configure(state=NORMAL),
            text_host.delete(0, END),
            text_host.insert(1, get_host()),
            text_host.configure(state=DISABLED),
            self.update()
            print("host reset is done")

    def confirm_information(self):
        master = Frame(self.settings_tab)
        master.pack(expand=True)
        frame_host = Frame(master)
        frame_host.pack(pady=(10, 2))
        frame_port = Frame(master)
        frame_port.pack()
        label_host = Label(frame_host, text="Host")
        label_host.pack(side=LEFT)

        text_host = Entry(frame_host)
        text_host.insert(0, self.host)
        text_host.pack(side=RIGHT, padx=5)
        text_host.configure(state=DISABLED)

        label_port = Label(frame_port, text="Port")
        label_port.pack(side=LEFT)

        text_port = Entry(frame_port)
        self.port_entry = text_port
        text_port.insert(0, self.port)
        text_port.pack(side=RIGHT, padx=5)

        self.close_button = lambda button: button.configure(text="close server", command=lambda: [
            text_port.configure(state=NORMAL),
            self.controller.tab(1, state=DISABLED),
            self.controller.tab(2, state=DISABLED),
            self.disconnect()
        ])

        self.start_button = lambda button: button.configure(text="start server", command=lambda: [
            text_port.configure(state=DISABLED),
            self.connect(),
            self.make_qr(master, f'{self.host}:{self.port}'),
            self.update()
        ])

        self.settings_button = Button(master)
        self.settings_button.bind('<Button-3>', lambda event: self.reset_host(text_host))
        self.button_change()
        self.settings_button.pack(anchor=CENTER, pady=20)
