import functools


def run_once(func):
    """ Runs the function only once (caches the return value for subsequent runs) """

    @functools.wraps(func)
    def decorator(*args, **kwargs):
        if decorator.has_run:
            return decorator.result
        decorator.has_run = True
        decorator.result = func(*args, **kwargs)
        return decorator.result

    decorator.has_run = False
    decorator.result = None
    return decorator


def run_first(preceding_func):
    """ Runs some function before running decorated function """

    def decorator(func):
        @functools.wraps(func)
        def wrapper_decorator(*args, **kwargs):
            preceding_func()
            returned_value = func(*args, **kwargs)
            return returned_value

        return wrapper_decorator

    return decorator
