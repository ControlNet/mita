import json

with open("package.json") as f:
    package = json.load(f)

print(package["version"])
