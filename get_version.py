import re

with open("build.sbt", "r", encoding="UTF-8") as f:
    version = re.search(r'version := "(.*)"', f.read()).group(1)

print(version)
