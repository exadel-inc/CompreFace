import attr

from src._pyutils.convertible_to_dict import ConvertibleToDict


@attr.s(auto_attribs=True, frozen=True)
class BoundingBox(ConvertibleToDict):
    x_min: int = attr.ib(converter=int)
    y_min: int = attr.ib(converter=int)
    x_max: int = attr.ib(converter=int)
    y_max: int = attr.ib(converter=int)
    probability: float
