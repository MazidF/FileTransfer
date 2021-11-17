import datetime
import os
from socket import socket as sk
import os.path as path
import socket


def name_maker(name_: str):
    global directory
    number = 2
    file_type = name_.split(".")[-1]
    file_name = ".".join(name_.split(".")[:-1])
    name_ = file_name
    while True:
        file_path = "\\".join([directory, f"{file_name}.{file_type}"])
        if path.exists(file_path):
            file_name = f"{name_}{number}"
            number += 1
        else:
            return file_path


def make_file(name_input, byte):
    name_input = name_maker(name_input)
    with open(name_input, "wb") as file:
        file.write(byte)
        file.flush()
    print("done")


def read_my_file():
    global client
    result = bytearray()
    try:
        while True:
            result.extend(client.recv(1024))
            if len(result):
                client.settimeout(1)
    except socket.timeout:
        try:
            file_name = result.decode()
            if file_name == "Exit":
                print("\nserver is closed.")
                exit(0)
        except Exception:
            print(result[:20], len(result))
            exit(1)
            return
        if not len(file_name.rstrip()):
            return "", ""
        result = bytearray()
        try:
            while True:
                result.extend(client.recv(10_000))
                print(len(result))
                try:
                    if result[-5:].decode() == 'END__':
                        break
                except Exception:
                    pass
                client.settimeout(1)
        except socket.timeout:
            print("we have a problem")
            print(file_name)
            print(len(result))
            exit(2)
        except Exception as e:
            print(e)
            print(len(result))
        return file_name, result[:-5]


main_dir = r"C:\Users\vcc\Downloads\FILES"
now = datetime.datetime.now()
directory = f"{now.year}_{now.month}_{now.day}"
directory = "\\".join([main_dir, directory])
if not path.exists(directory):
    os.makedirs(directory)
server = sk(socket.AF_INET, socket.SOCK_STREAM)
server.bind((socket.gethostbyname(socket.gethostname()), 9999))
print(socket.gethostbyname(socket.gethostname()))
server.listen(10)
client, address = server.accept()
print(f"{address} connected")
print(client.recv(4))
while True:
    name, data = read_my_file()
    if len(name) != 0 and len(data) != 0:
        print("\n", name, '   size: ', len(data), sep="")
        make_file(name, data)
    else:
        print("0", end="\t")

