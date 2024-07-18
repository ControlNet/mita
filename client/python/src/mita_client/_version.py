import os.path

with open(os.path.join(os.path.dirname(__file__), "version.txt"), "r") as file:
    version = file.read()
