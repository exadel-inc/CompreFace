# MIT License
#
# Copyright (c) 2021 CDL Digidow <https://digidow.eu>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name='mtcnn_tflite',
    version='0.0.4',
    author="Philipp Hofer",
    author_email="philipp.hofer@ins.jku.at",
    description="MTCNN face detection implementation in Tensorflow Lite.",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/mobilesec/mtcnn-tflite",
    packages=setuptools.find_packages(exclude=["tests.*", "tests"]),
    install_requires=[
          "tensorflow>=2.0.0",
          "numpy",
          "opencv-python~=4.4.0",
          "fcache>=0.4.0"
      ],
    tests_require=['pytest'],  
    license="MIT License",
    classifiers=[
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Operating System :: OS Independent"
    ],
    include_package_data=True
)
