import attr

from src.app import JSONEncodable


# noinspection PyUnresolvedReferences
@attr.s(auto_attribs=True, frozen=True)
class BoundingBox(JSONEncodable):
    """
    >>> BoundingBox(x_min=10, x_max=0, y_min=100, y_max=200, probability=0.5)
    Traceback (most recent call last):
    ...
    ValueError: 'x_min' must be smaller than 'x_max'
    """
    x_min: int = attr.ib(converter=int)
    y_min: int = attr.ib(converter=int)
    x_max: int = attr.ib(converter=int)
    y_max: int = attr.ib(converter=int)
    probability: float = attr.ib(converter=float)

    @x_min.validator
    def check(self, attribute, value):
        if value > self.x_max:
            raise ValueError("'x_min' must be smaller than 'x_max'")

    @y_min.validator
    def check(self, attribute, value):
        if value > self.y_max:
            raise ValueError("'y_min' must be smaller than 'y_max'")

    @probability.validator
    def check(self, attribute, value):
        if not (0 <= value <= 1):
            raise ValueError("'probability' must be between 0 and 1")
