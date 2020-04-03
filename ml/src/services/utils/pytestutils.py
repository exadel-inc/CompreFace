from typing import Type, Callable


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


def raises(e: Type[Exception], callable_: Callable):
    try:
        callable_()
    except e:
        return True
    else:
        return False


class Expando:
    pass