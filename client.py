import socket
from socket import socket as sk

client = sk(socket.AF_INET, socket.SOCK_STREAM)
client.connect(("192.168.169.143", 80))
