import setuptools

with open("../../README.md", "r", encoding="UTF-8") as file:
    long_description = file.read()

requirements = []
with open("requirements.txt", "r", encoding="UTF-8") as file:
    for line in file:
        requirements.append(line.strip())


def read_version() -> str:
    with open("version.txt", "r") as f:
        ver = f.read()
    return ver


def write_version(ver: str) -> None:
    with open("src/mita/version.txt", "w") as f:
        f.write(ver)


def init_version() -> None:
    ver = read_version()
    write_version(ver)


version = read_version()
write_version(version)

setuptools.setup(
    name="mita",
    version=version,
    author="ControlNet",
    author_email="smczx@hotmail.com",
    description="Mita client for Python.",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/ControlNet/mita",
    project_urls={
        "Bug Tracker": "https://github.com/ControlNet/mita/issues",
        "Source Code": "https://github.com/ControlNet/mita",
    },
    keywords=["web"],
    package_dir={"": "src"},
    packages=setuptools.find_packages(where="src", include=["mita", "mita.*"]),
    package_data={
        "mita": [
            "version.txt"
        ]
    },
    python_requires=">=3.6",
    install_requires=requirements,
    classifiers=[
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Intended Audience :: Developers",
        "Topic :: System :: Monitoring",
        "Topic :: System :: Logging",
    ],
)
