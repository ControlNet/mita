import re
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--input', required=True)
parser.add_argument("--input2", required=True)

# replace 'placeholder="Password"' to 'placeholder="Password is 123"'
def process_placeholder(content):
    return re.sub(r'placeholder="Password"', 'placeholder="Password is 123"', content)

def process_package(content):
    content = content.replace(
        '"test": "react-scripts test --env=jsdom",',
        '"deploy": "gh-pages -d ../public",'
    )
    return content

if __name__ == '__main__':
    args = parser.parse_args()
    with open(args.input, 'r', encoding="UTF-8") as f:
        content = f.read()
        content = process_placeholder(content)
    with open(args.input, 'w', encoding="UTF-8") as f:
        f.write(content)


    with open(args.input2, 'r', encoding="UTF-8") as f:
        content = f.read()
        content = process_package(content)
    with open(args.input2, 'w', encoding="UTF-8") as f:
        f.write(content)
