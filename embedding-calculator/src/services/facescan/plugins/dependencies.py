#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

from typing import Tuple

from src.constants import ENV
from src.services.utils.pyutils import get_env


def get_tensorflow(version='2.1.4') -> Tuple[str, ...]:
    libs = [f'tensorflow=={version}']
    cuda_version = get_env('CUDA', '').replace('.', '')
    if ENV.GPU_IDX > -1 and cuda_version:
        libs.append(f'tensorflow-gpu=={version}')
    return tuple(libs)


def get_mxnet() -> Tuple[str, ...]:
    cuda_version = get_env('CUDA', '').replace('.', '')

    mxnet_lib = 'mxnet-'
    if ENV.GPU_IDX > -1 and cuda_version:
        mxnet_lib += f"cu{cuda_version}"
    if ENV.INTEL_OPTIMIZATION:
        mxnet_lib += 'mkl'
    mxnet_lib = mxnet_lib.rstrip('-')
    return (f'{mxnet_lib}<1.7',)
