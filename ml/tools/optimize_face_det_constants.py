import numpy
from scipy import optimize


def f(xy):
    x, y = xy
    return 2 - int(0.5 <= x <= 0.6) - int(0.2 <= y <= 0.3)


def bounds(x_new, **kwargs):
    return (0 <= x_new).all() and (x_new <= 1).all()


if __name__ == '__main__':
    r = optimize.basinhopping(f, numpy.ones((2,)), accept_test=bounds)
    print(r)
