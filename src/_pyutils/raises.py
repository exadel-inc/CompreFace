from typing import Type, Callable


def raises(e: Type[Exception], callable_: Callable):
    try:
        callable_()
    except e:
        return True
    else:
        return False
