import urllib.request
import json
from packaging import version

def get_dockerhub_version():
    url = "https://registry.hub.docker.com/v2/repositories/controlnet/mita/tags/"
    response = urllib.request.urlopen(url)
    data = json.loads(response.read().decode())
    return max([version.parse(tag["name"]) for tag in data["results"] if tag["name"] != "latest"])

print(get_dockerhub_version())