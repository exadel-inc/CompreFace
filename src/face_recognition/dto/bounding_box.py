import attr

from src.pyutils.convertible_to_dict import ConvertibleToDict


@attr.s(auto_attribs=True, frozen=True)
class BoundingBox(ConvertibleToDict):
    xmin: int = attr.ib(converter=int)
    ymin: int = attr.ib(converter=int)
    xmax: int = attr.ib(converter=int)
    ymax: int = attr.ib(converter=int)
