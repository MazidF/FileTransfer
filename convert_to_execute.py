import os
import shutil


def exe(path, file_name):
    os.system(f"pyinstaller --onefile {path}\\{file_name}.py")
    os.rename(f"{path}\\dist\\{file_name}.exe", f"C:\\Users\\vcc\\Downloads\\EXE Files\\{file_name}.exe")
    os.removedirs(f"{path}\\dist")
    shutil.rmtree(f"{path}\\build")
    shutil.rmtree(f"{path}\\__pycache__")
    os.remove(f"{path}\\{file_name}.spec")


exe(r"C:\Users\vcc\PycharmProjects\Graphical_Interface", "server")
