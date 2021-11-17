class MyFile:
    def __init__(self, name: str, file_type: str, file_bytes: bytearray):
        self.name = name
        self.file_type = file_type
        self.file_bytes = file_bytes

    def __hash__(self):
        pass
